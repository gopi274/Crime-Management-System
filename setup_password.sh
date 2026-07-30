#!/bin/bash

# Crime Management System - Password Setup Script
echo "🔐 Crime Management System - Password Setup"
echo "==========================================="

# Check if MySQL is running
if ! pgrep -x "mysqld" > /dev/null; then
    echo "❌ MySQL is not running. Please start MySQL first."
    echo "   On macOS: brew services start mysql"
    echo "   On Ubuntu: sudo systemctl start mysql"
    exit 1
fi

echo "✅ MySQL is running!"

# Test connection with empty password
echo "🔍 Testing connection with empty password..."
mysql -u root -e "SELECT 1;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Connection successful with empty password!"
    echo "   The system is ready to run with default settings."
    echo ""
    echo "🚀 You can now run the application with:"
    echo "   mvn exec:java"
    exit 0
fi

# If empty password doesn't work, prompt for password
echo "❌ Empty password didn't work."
echo ""
echo "Please enter your MySQL root password:"
read -s mysql_password

# Test connection with provided password
echo "🔍 Testing connection with provided password..."
mysql -u root -p"$mysql_password" -e "SELECT 1;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Connection successful!"
    
    # Update database.properties
    sed -i.bak "s/db.password=.*/db.password=$mysql_password/" src/main/resources/database.properties
    
    # Update DatabaseConnection.java
    sed -i.bak "s/private static final String DB_PASSWORD = \".*\";/private static final String DB_PASSWORD = \"$mysql_password\";/" src/main/java/com/crimemanagement/util/DatabaseConnection.java
    
    echo "✅ Configuration updated successfully!"
    echo ""
    echo "🚀 You can now run the application with:"
    echo "   mvn exec:java"
else
    echo "❌ Connection failed. Please check your MySQL password and try again."
    exit 1
fi
