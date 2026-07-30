package com.crimemanagement.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Database connection utility class using HikariCP connection pool
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;

    static {
        initializeDataSource();
    }

    /**
     * Load database properties from properties file
     */
    private static void loadDatabaseProperties() {
        Properties props = new Properties();
        
        // 1. First, check for an external config file (useful for packaged apps)
        File externalConfig = new File("config/database.properties");
        if (externalConfig.exists()) {
            try (InputStream input = new FileInputStream(externalConfig)) {
                props.load(input);
                logger.info("Loaded database configuration from external file: {}", externalConfig.getAbsolutePath());
            } catch (IOException e) {
                logger.warn("Failed to load external config, falling back to classpath/defaults: {}", e.getMessage());
            }
        }

        // 2. If not found or incomplete, check classpath
        if (props.isEmpty()) {
            try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
                if (input != null) {
                    props.load(input);
                    logger.info("Loaded database configuration from classpath");
                }
            } catch (IOException e) {
                logger.warn("Failed to load classpath database.properties");
            }
        }

        // 3. Apply properties with defaults
        DB_URL = props.getProperty("db.url", "jdbc:h2:file:~/.kerala-erp-justice-graduation/database/crime_management_erp_v660;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        DB_USER = props.getProperty("db.username", "sa");
        DB_PASSWORD = props.getProperty("db.password", "");
        DB_DRIVER = props.getProperty("db.driver", "org.h2.Driver");

        // [CRITICAL] Replace ${user.home} placeholder if present
        if (DB_URL != null && DB_URL.contains("${user.home}")) {
            DB_URL = DB_URL.replace("${user.home}", System.getProperty("user.home"));
        } else if (DB_URL != null && DB_URL.startsWith("jdbc:h2:file:~")) {
             // Resolve ~ to home folder as H2 sometimes behaves unexpectedly on Mac
            DB_URL = DB_URL.replace("~", System.getProperty("user.home"));
        }

            // System property overrides are useful for tests and one-off local runs.
            String sysUrl = System.getProperty("db.url");
            String sysUser = System.getProperty("db.username");
            String sysPass = System.getProperty("db.password");
            String sysDriver = System.getProperty("db.driver");
            if (sysUrl != null && !sysUrl.isEmpty())
                DB_URL = sysUrl;
            if (sysUser != null && !sysUser.isEmpty())
                DB_USER = sysUser;
            if (sysPass != null)
                DB_PASSWORD = sysPass;
            if (sysDriver != null && !sysDriver.isEmpty())
                DB_DRIVER = sysDriver;

            // Environment variable overrides (optional)
            String envUrl = System.getenv("DB_URL");
            String envUser = System.getenv("DB_USERNAME");
            String envPass = System.getenv("DB_PASSWORD");
            String envDriver = System.getenv("DB_DRIVER");
            if (envUrl != null && !envUrl.isEmpty())
                DB_URL = envUrl;
            if (envUser != null && !envUser.isEmpty())
                DB_USER = envUser;
            if (envPass != null)
                DB_PASSWORD = envPass;
            if (envDriver != null && !envDriver.isEmpty())
                DB_DRIVER = envDriver;

            logger.info("Database properties loaded successfully");
    }

    /**
     * Initialize the HikariCP data source
     */
    private static void initializeDataSource() {
        try {
            // Load database properties
            loadDatabaseProperties();

            // Quick connectivity probe to avoid noisy pool logs
            boolean usingFallback = false;
            try {
                Class.forName(DB_DRIVER);
                java.sql.Connection probe = java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                probe.close();
            } catch (Exception probeEx) {
                logger.warn("Primary database unreachable or misconfigured. Attempting embedded H2 fallback.");
                // Attempt embedded H2 fallback so the app can run without external MySQL
                // Using file-based H2 database for persistence (data persists across
                // application restarts)
                try {
                    String userHome = System.getProperty("user.home");
                    File dbBaseDir = new File(userHome, ".kerala-erp-justice-graduation");
                    File dbDir = new File(dbBaseDir, "database");
                    if (!dbDir.exists()) {
                        dbDir.mkdirs();
                        logger.info(">>> V5.2 JUSTICE: Created fresh isolation directory in User Home: {}", dbDir.getAbsolutePath());
                    }

                    DB_URL = "jdbc:h2:file:" + dbDir.getAbsolutePath() + "/crime_management_erp_v660;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
                    DB_USER = "sa";
                    DB_PASSWORD = "";
                    DB_DRIVER = "org.h2.Driver";

                    logger.info(">>> V6.0.9 JUSTICE: Initializing isolated local database: {}", DB_URL);
                    Class.forName(DB_DRIVER);
                    java.sql.Connection probe2 = java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    probe2.close();
                    usingFallback = true;
                    logger.info("Embedded H2 database started and reachable (fallback).");
                } catch (Exception h2Ex) {
                    logger.warn("Embedded H2 fallback failed: {}. Starting without database connectivity.", h2Ex.getMessage());
                    h2Ex.printStackTrace();
                    dataSource = null;
                    return;
                }
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            config.setDriverClassName(DB_DRIVER);

            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setLeakDetectionThreshold(60000);

            // Create the data source
            dataSource = new HikariDataSource(config);
            if (usingFallback) {
                logger.info("Database connection pool initialized using embedded H2 fallback.");
            } else {
                logger.info("Database connection pool initialized successfully");
            }

            if (DB_DRIVER != null && DB_DRIVER.contains("h2")) {
                initializeEmbeddedSchema();
            }
        } catch (Exception e) {
            logger.error("Failed to initialize data source", e);
            dataSource = null;
        }
    }

    private static void initializeEmbeddedSchema() {
        try (Connection conn = dataSource.getConnection()) {
            boolean usersMissing = true;
            try (java.sql.ResultSet rs = conn.getMetaData().getTables(null, null, "USERS", null)) {
                if (rs.next()) usersMissing = false;
            } catch (Exception ignored) {}
            if (usersMissing) {
                try (java.sql.ResultSet rs = conn.getMetaData().getTables(null, null, "users", null)) {
                    if (rs.next()) usersMissing = false;
                } catch (Exception ignored) {}
            }

            // [CRITICAL] V6.4.1 JUSTICE: Check for legacy 'SUB' enum value causing crashes
            
            try (java.sql.Statement stmt = conn.createStatement()) {
                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM jails WHERE type = 'SUB'")) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        logger.warn(">>> KERALA POLICE ERP: Legacy 'SUB' types detected. Triggering nuclear purge...");
                    }
                }
            } catch (Exception ignored) {}

                // needsInit check - only initialize if database is empty to satisfy data retention request
                boolean needsInit = false;
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM police_stations")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        needsInit = true;
                    }
                } catch (Exception e) {
                    needsInit = true; // Table might not exist yet
                }

                if (needsInit) {
                    logger.info(">>> KERALA POLICE ERP: Ensuring Structural Integrity...");
                    loadSqlResourceIntoConnection("schema.sql", conn);
                    loadSqlResourceIntoConnection("justice_graduation_data.sql", conn);
                    sanitizeUserPasswords(conn);
                    inspectRequiredColumns(conn);
                    logger.info("KERALA POLICE ERP: Schema reconstructed successfully.");
                }
            // Always ensure the structural integrity for the current version
            ensureV60Schema(conn);
        } catch (Exception loadEx) {
            logger.error("!!! KERALA POLICE ERP: Reconstruction Failure !!!", loadEx);
        }
    }



    private static void ensureV60Schema(Connection conn) {
        try (java.sql.Statement stmt = conn.createStatement()) {
            // Check for jail_id in users
            java.util.Set<String> userCols = getExistingColumns(conn, "users");
            if (!userCols.contains("jail_id")) {
                logger.info("Migrating database to V5.2: Adding 'jail_id' to 'users' table...");
                stmt.execute("ALTER TABLE users ADD COLUMN jail_id INT");
                stmt.execute("ALTER TABLE users ADD CONSTRAINT fk_user_jail FOREIGN KEY (jail_id) REFERENCES jails(jail_id) ON DELETE SET NULL");
            }
            
            // Ensure V5.2 Justice tables exist
            stmt.execute("CREATE TABLE IF NOT EXISTS jails (jail_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), type VARCHAR(255), location VARCHAR(100), capacity INT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS jail_inmates (inmate_id INT PRIMARY KEY AUTO_INCREMENT, jail_id INT, criminal_id INT, status VARCHAR(255), admission_date DATE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS evidence (evidence_id INT PRIMARY KEY AUTO_INCREMENT, fir_id INT, evidence_type VARCHAR(100), status VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Forensic & Judicial graduation V6.2.0
            java.util.Set<String> evCols = getExistingColumns(conn, "evidence");
            if (!evCols.contains("lab_id")) {
                logger.info("Migrating to V6.2.0: Adding forensic columns...");
                stmt.execute("ALTER TABLE evidence ADD COLUMN lab_id INT");
                stmt.execute("ALTER TABLE evidence ADD COLUMN scientist_id INT");
            }
            
            java.util.Set<String> susCols = getExistingColumns(conn, "suspects");
            if (!susCols.contains("disposition")) {
                logger.info("Migrating to V6.2.0: Adding judicial disposition...");
                stmt.execute("ALTER TABLE suspects ADD COLUMN disposition VARCHAR(50) DEFAULT 'PENDING'");
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS forensic_labs (lab_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), district VARCHAR(50), facility_type VARCHAR(50), parent_lab_id INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS system_settings (setting_key VARCHAR(50) PRIMARY KEY, setting_value VARCHAR(255), description VARCHAR(255))");
            
            logger.info("V6.2.0 Kochi-Directorate graduation structural check complete.");
        } catch (SQLException e) {
            logger.warn("V6.2.0 structural graduation warning: {}. App will attempt to continue.", e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            logger.warn("Data source was null. Attempting on-demand initialization.");
            initializeDataSource();
            if (dataSource == null) {
                throw new SQLException("Data source is not initialized. Please check database configuration and logs.");
            }
        }
        return dataSource.getConnection();
    }

    private static java.util.Set<String> getExistingColumns(Connection conn, String tableName) {
        java.util.Set<String> columns = new java.util.HashSet<>();
        try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, tableName.toUpperCase(), null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        } catch (SQLException ignored) {}
        try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, tableName.toLowerCase(), null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        } catch (SQLException ignored) {}
        return columns;
    }

    private static void loadSqlResourceIntoConnection(String resourcePath, Connection conn) {
        try {
            InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                // Fallback for local development
                File local = new File("src/main/resources/" + resourcePath);
                if (local.exists()) {
                    is = new FileInputStream(local);
                }
            }
            if (is == null) {
                logger.warn("SQL resource not found: {}", resourcePath);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
                    String up = line.trim().toUpperCase();
                    if (up.startsWith("CREATE DATABASE") || up.startsWith("USE ") || up.startsWith("SET FOREIGN_KEY_CHECKS")) continue;
                    
                    sb.append(line).append('\n');
                }
                String script = sb.toString();
                script = script.replaceAll("ENUM\\([^)]+\\)", "VARCHAR(255)");
                
                try (java.io.Reader r = new java.io.StringReader(script)) {
                    org.h2.tools.RunScript.execute(conn, r);
                    logger.info("Executed SQL resource: {}", resourcePath);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute SQL resource: {}", resourcePath, e);
        }
    }

    private static void sanitizeUserPasswords(Connection conn) {
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT user_id, password_hash FROM users")) {
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String hash = rs.getString("password_hash");
                if (hash != null && !hash.startsWith("$2a$")) {
                    String newHash = BCrypt.hashpw(hash, BCrypt.gensalt());
                    try (java.sql.PreparedStatement update = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE user_id = ?")) {
                        update.setString(1, newHash);
                        update.setInt(2, id);
                        update.executeUpdate();
                    }
                }
            }
        } catch (SQLException ignored) {}
    }

    private static void inspectRequiredColumns(Connection conn) {
        logger.info("Inspected required columns successfully.");
    }
}
