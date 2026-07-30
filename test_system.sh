#!/bin/bash

# Crime Management System Test Script
echo "🚔 Crime Management System - Test Script"
echo "========================================"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if MySQL is running
if ! pgrep -x "mysqld" > /dev/null; then
    echo "⚠️  MySQL is not running. Please start MySQL first."
    echo "   On macOS: brew services start mysql"
    echo "   On Ubuntu: sudo systemctl start mysql"
    echo "   On Windows: net start mysql"
    exit 1
fi

echo "✅ Prerequisites check passed!"

# Compile the project
echo "🔨 Compiling the project..."
mvn -q -DskipTests clean package

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed. Please check the error messages above."
    exit 1
fi

echo "✅ Compilation successful!"

# Optional database setup (non-interactive). Provide MYSQL_USER and MYSQL_PASSWORD to enable.
if [ -n "$MYSQL_USER" ] && [ -n "$MYSQL_PASSWORD" ]; then
    echo "🗄️  Setting up database as $MYSQL_USER ..."
    # If MYSQL_DB is not set, default to running the full setup script (includes CREATE DATABASE)
    if [ -n "$MYSQL_DB" ]; then
        mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DB" < setup_database_complete.sql
    else
        mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" < setup_database_complete.sql
    fi
    if [ $? -ne 0 ]; then
        echo "❌ Database setup encountered an error. Skipping app run."
        exit 1
    fi
    echo "✅ Database setup completed!"
else
    echo "ℹ️  Skipping database setup (set MYSQL_USER and MYSQL_PASSWORD to enable)."
fi

# Optional run step. Set RUN_APP=true to run the desktop app after build.
if [ "$RUN_APP" = "true" ]; then
    echo "🚀 Starting Crime Management System (Desktop)..."
    JAR_PATH=$(ls -1 target/*-with-dependencies.jar 2>/dev/null | head -n 1)
    if [ -z "$JAR_PATH" ]; then
        # try shaded jar name
        JAR_PATH=$(ls -1 target/*-shaded.jar 2>/dev/null | head -n 1)
    fi
    if [ -z "$JAR_PATH" ]; then
        # fallback to standard jar
        JAR_PATH=$(ls -1 target/*.jar 2>/dev/null | head -n 1)
    fi
    if [ -n "$JAR_PATH" ]; then
        echo "Running $JAR_PATH"
        java -jar "$JAR_PATH"
    else
        echo "❌ Could not find built jar in target/"
        exit 1
    fi
else
    echo "✅ Test script finished (build only). Set RUN_APP=true to run the desktop app."
fi
