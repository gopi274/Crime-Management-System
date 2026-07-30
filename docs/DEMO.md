# Crime Management System Demo 🚔

## Quick Start Guide

### 1. Prerequisites
- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### 2. Setup
```bash
# Clone and navigate to the project
cd crime-management-system

# Make the test script executable
chmod +x test_system.sh

# Run the test script (this will set up everything)
./test_system.sh
```

### 3. Manual Setup (Alternative)
```bash
# 1. Start MySQL
brew services start mysql  # macOS
# or
sudo systemctl start mysql  # Ubuntu

# 2. Create database and tables
mysql -u root -p < setup_database.sql

# 3. Update database credentials in src/main/resources/database.properties

# 4. Compile and run
mvn clean compile
mvn exec:java
```

## System Features Demo

### Main Menu Options
1. **👮 Manage Officers** - Add, view, search, update, delete police officers
2. **🦹 Manage Criminals** - Manage criminal records with detailed information
3. **📄 Manage FIRs** - Create and manage First Information Reports
4. **🔍 Search Records** - Advanced search across all entities
5. **📊 View Statistics** - System overview and analytics

### Sample Data Included
The system comes with pre-loaded sample data:
- 5 police officers across different departments
- 5 criminal records with varying backgrounds
- 5 FIRs with different crime types and statuses
- 5 cases with evidence and suspect relationships
- Sample evidence and case updates

### Search Capabilities
- **Keyword Search**: Search across all records with terms like "robbery"
- **Crime Type Search**: Find specific crimes like "robbery in 2023"
- **Status Search**: Filter by case/FIR status (Open, Under Investigation, etc.)
- **Officer Search**: Find all records assigned to specific officers
- **Priority Search**: Filter cases by priority level

### Example Searches
1. Search for "robbery" - finds all robbery-related records
2. Search for "theft in 2024" - finds theft cases in 2024
3. Search by officer ID - shows all assigned cases
4. Search by status "Open" - shows all open cases

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

## Sample Workflow

1. **Add an Officer**:
   - Go to "Manage Officers" → "Add Officer"
   - Enter badge number, name, rank, department
   - System validates and creates the officer

2. **Add a Criminal**:
   - Go to "Manage Criminals" → "Add Criminal"
   - Enter personal details and criminal record
   - System validates and creates the criminal

3. **Create a FIR**:
   - Go to "Manage FIRs" → "Add FIR"
   - Enter crime details, location, complainant info
   - Assign to an officer
   - System creates the FIR with unique number

4. **Search Records**:
   - Go to "Search Records" → "Search All Records"
   - Enter keyword like "robbery"
   - System searches across all entities and displays results

5. **View Statistics**:
   - Go to "View Statistics"
   - See system overview, FIR status breakdown, criminal demographics

## Technical Features

- **Connection Pooling**: Uses HikariCP for efficient database connections
- **Logging**: Comprehensive logging with SLF4J and Logback
- **Error Handling**: Robust error handling and validation
- **Search**: Advanced search with keyword matching across all entities
- **Statistics**: Real-time system statistics and analytics
- **Menu-driven Interface**: User-friendly console interface

## Troubleshooting

### Common Issues
1. **Database Connection Failed**: Check MySQL is running and credentials are correct
2. **Compilation Errors**: Ensure Java 11+ is installed
3. **Permission Denied**: Make sure test_system.sh is executable

### Logs
- Application logs are written to `logs/crime-management.log`
- Console output shows real-time application status

## Next Steps

1. **Customize**: Modify database credentials in `database.properties`
2. **Extend**: Add new entity types or search capabilities
3. **Deploy**: Package as JAR for production deployment
4. **Integrate**: Add REST API endpoints for web integration

---

**Note**: This is a demonstration system. For production use, additional security measures, validation, and error handling should be implemented.
