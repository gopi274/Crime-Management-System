# Crime Record Management System 🚔

A comprehensive Java-based system for managing crime records, FIRs, criminals, cases, and evidence with MySQL database backend.

## Features

- **Officer Management**: Add, update, delete, and search police officers
- **Criminal Management**: Maintain criminal records with detailed information
- **FIR Management**: Create and manage First Information Reports
- **Case Management**: Track cases with status updates and assignments
- **Evidence Management**: Record and track evidence collection
- **Advanced Search**: Search across all records with keyword matching
- **Statistics Dashboard**: View system statistics and reports

## Technology Stack

- **Backend**: Java 11
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Connection Pooling**: HikariCP
- **Logging**: SLF4J with Logback

## Prerequisites

- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd crime-management-system
```

### 2. Database Setup

1. **Start MySQL Server**
   ```bash
   # On macOS with Homebrew
   brew services start mysql
   
   # On Ubuntu/Debian
   sudo systemctl start mysql
   
   # On Windows
   net start mysql
   ```

2. **Create Database and Tables**
   ```bash
   mysql -u root -p < setup_database_complete.sql
   ```

3. **Update Database Configuration**
   Edit `src/main/resources/database.properties` and update:
   ```properties
   db.url=jdbc:mysql://localhost:3306/crime_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   db.username=root
   db.password=your_mysql_password
   ```

### 3. Build and Run

1. **Compile the Project**
   ```bash
   mvn clean compile
   ```

2. **Run the Application**
   ```bash
   mvn exec:java
   ```

   Or run the JAR file:
   ```bash
   mvn clean package
   java -jar target/crime-management-system-1.0.0.jar
   ```

## Usage

### Main Menu Options

1. **👮 Manage Officers**
   - Add new police officers
   - View all officers
   - Search officers by badge number, name, or department
   - Update officer information
   - Delete officers

2. **🦹 Manage Criminals**
   - Add criminal records
   - View all criminals
   - Search criminals by name, gender, or alias
   - Update criminal information
   - Delete criminal records

3. **📄 Manage FIRs**
   - Create new First Information Reports
   - View all FIRs
   - Search FIRs by keyword, crime type, status, or officer
   - Update FIR information
   - Delete FIRs

4. **🔍 Search Records**
   - Comprehensive search across all entities
   - Search by crime type (e.g., "robbery in 2023")
   - Search by status, priority, or officer
   - Advanced keyword matching

5. **📊 View Statistics**
   - System overview statistics
   - FIR status breakdown
   - Criminal demographics
   - Officer department distribution

### Search Examples

- **Keyword Search**: "robbery" - finds all records containing "robbery"
- **Crime Type Search**: "theft" with year "2023" - finds all theft cases in 2023
- **Status Search**: "Open" - finds all open cases and FIRs
- **Officer Search**: Enter officer ID to find all assigned cases

## Database Schema

### Core Tables

- **officers**: Police officer information
- **criminals**: Criminal records and personal details
- **firs**: First Information Reports
- **cases**: Case management and tracking
- **evidence**: Evidence collection and storage
- **suspects**: Suspect-case relationships
- **case_updates**: Case progress tracking

### Relationships

- Officers can be assigned to multiple FIRs and cases
- Criminals can be suspects in multiple cases
- Cases are linked to FIRs
- Evidence is collected for specific cases
- Case updates track progress over time

## Configuration

### Database Connection
Update `src/main/resources/database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/crime_management
db.username=your_username
db.password=your_password
```

### Connection Pool Settings
```properties
db.pool.maximum=10
db.pool.minimum=5
db.pool.connection.timeout=30000
```

## API Reference

### Service Classes

- **OfficerService**: Officer management operations
- **CriminalService**: Criminal record management
- **FIRService**: FIR creation and management
- **SearchService**: Advanced search functionality

### DAO Classes

- **OfficerDAO**: Database operations for officers
- **CriminalDAO**: Database operations for criminals
- **FIRDAO**: Database operations for FIRs
- **CaseDAO**: Database operations for cases
- **EvidenceDAO**: Database operations for evidence

## Sample Data

The system comes with sample data including:
- 5 police officers across different departments
- 5 criminal records with varying backgrounds
- 5 FIRs with different crime types and statuses
- 5 cases with evidence and suspect relationships
- Sample evidence and case updates

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running
   - Check database credentials in `database.properties`
   - Ensure database exists and is accessible

2. **Compilation Errors**
   - Ensure Java 11+ is installed
   - Run `mvn clean compile` to rebuild

3. **Runtime Errors**
   - Check database connection settings
   - Verify all required tables exist
   - Check logs for detailed error messages

### Logs

Application logs are written to the console and can be configured in `logback.xml`.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository.

---

**Note**: This is a demonstration system. For production use, additional security measures, validation, and error handling should be implemented.
