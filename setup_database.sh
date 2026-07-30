#!/bin/bash

# Set up MySQL database for Crime Management System
echo "Setting up Crime Management System database..."

# MySQL credentials
MYSQL_USER="root"
MYSQL_PASSWORD="root"  # Using the password from database.properties

# Create database and tables
mysql -u$MYSQL_USER -p$MYSQL_PASSWORD < setup_database.sql

echo "Database setup completed!"