package com.crimemanagement;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class DesktopLauncher {
    private static final Path LOG_PATH = Paths.get(System.getProperty("user.home"), "Desktop", "KERALA_POLICE_ERP_LOG.txt");

    private static void log(String message) {
        try {
            String timestamp = new Date().toString();
            String fullMessage = "[" + timestamp + "] " + message + "\n";
            Files.write(LOG_PATH, fullMessage.getBytes(), 
                Files.exists(LOG_PATH) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (Exception e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // GLOBAL SAFETY NET: Capture anything that slips through the threads
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log("FATAL THREAD ERROR (" + t.getName() + "): " + e.toString());
            e.printStackTrace();
        });

        try {
            // JVM ALARM: Show a physical popup to prove the CPU is executing the code
            try {
                JOptionPane.showMessageDialog(null, "Kerala Police ERP: Booting Engine...", "System Alert", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ignored) {}

            // Reset log
            if (Files.exists(LOG_PATH)) Files.delete(LOG_PATH);
            log("STEP 1: Application Started (Version 6.6.1-JUSTICE)");

            // 0. GUI DOMINANCE: Ensure AWT is NOT headless on macOS
            System.setProperty("java.awt.headless", "false");
            System.setProperty("apple.awt.application.name", "Kerala Police ERP");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            
            // 1. THE LOCK-BREAKER
            log("STEP 2: Cleaning Database Locks...");
            try {
                File dbBaseDir = new File(System.getProperty("user.home"), ".kerala-erp-justice-graduation");
                File dbDir = new File(dbBaseDir, "database");
                if (dbDir.exists()) {
                    File[] files = dbDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().contains("lock") || f.getName().contains("trace")) f.delete();
                        }
                    }
                }
            } catch (Exception e) {
                log("WARNING: Lock cleanup error: " + e.getMessage());
            }

            // 2. Start the Database Bootstrap
            log("STEP 3: Initializing Database Content...");
            try {
                // Ensure synchronization between different DB URLs
                String dbUrl = "jdbc:h2:file:" + System.getProperty("user.home") + "/.kerala-erp-justice-graduation/database/crime_management_erp_v660;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
                System.setProperty("db.url", dbUrl);
                System.setProperty("spring.datasource.url", dbUrl);
                
                com.crimemanagement.util.GraduationDataBootstrap.main(args);
                log("STEP 4: Database Content Ready.");
            } catch (Exception e) {
                log("ERROR: Database Bootstrap failed: " + e.getMessage());
                e.printStackTrace();
            }

            // 3. Setup UI Look and Feel
            log("STEP 5: Setting up Look and Feel...");
            try {
                FlatLightLaf.setup();
            } catch (Exception e) {
                log("WARNING: Look and Feel setup error: " + e.getMessage());
            }

            // 4. Start the Login Flow
            log("STEP 6: Launching Login Dialog...");
            startApp(args);

            // 5. Staggered Backend: Spring Boot starts after GUI
            new Thread(() -> {
                try {
                    Thread.sleep(3000); 
                    log("STEP 7: Initializing Spring Boot Backend...");
                    WebApplication.main(args);
                    log("STEP 8: Backend Services Ready.");
                } catch (Exception e) {
                    log("ERROR: Backend startup failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();

        } catch (Throwable t) {
            log("CRITICAL ERROR in Main: " + t.toString());
            t.printStackTrace();
        }
    }

    public static void startApp(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                log("STEP 6.1: Invoking Swing Login Dialog...");
                com.crimemanagement.ui.LoginDialog login = new com.crimemanagement.ui.LoginDialog(null);
                
                log("STEP 6.2: Displaying Login Dialog...");
                login.setVisible(true);
                
                if (!login.isSuccess()) {
                    log("STEP 6.3: Login Cancelled or Failed.");
                    return; 
                }
                
                log("STEP 6.4: Login Successful. Launching MainWindow...");
                Thread.sleep(200); 

                try {
                    com.crimemanagement.ui.MainWindow window = new com.crimemanagement.ui.MainWindow(
                        login.getRole(), login.getUsername(), login.getOfficerId(), 
                        login.getJailId(), login.getCourtId(), login.getScientistId()
                    );
                    window.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    window.setVisible(true);
                    window.toFront();
                    window.requestFocus();
                    log("STEP 6.5: MainWindow Displayed Successfully.");
                } catch (Exception mainEx) {
                    log("ERROR: MainWindow launch failed: " + mainEx.getMessage());
                    mainEx.printStackTrace();
                }
            } catch (Exception ex) {
                log("ERROR: Login flow error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
