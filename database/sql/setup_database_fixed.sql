-- Crime Record Management System Database Setup Script
-- This script creates the database and all required tables

-- Create database
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS crime_management;
CREATE DATABASE crime_management;
USE crime_management;

-- Officers table
CREATE TABLE officers (
    officer_id INT PRIMARY KEY AUTO_INCREMENT,
    badge_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    `rank` VARCHAR(30) NOT NULL,
    department VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Criminals table
CREATE TABLE criminals (
    criminal_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    alias VARCHAR(50),
    date_of_birth DATE,
    gender ENUM('M', 'F', 'Other') NOT NULL,
    address TEXT,
    phone_number VARCHAR(15),
    criminal_record TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- FIRs (First Information Reports) table
CREATE TABLE firs (
    fir_id INT PRIMARY KEY AUTO_INCREMENT,
    fir_number VARCHAR(20) UNIQUE NOT NULL,
    crime_type VARCHAR(100) NOT NULL,
    crime_description TEXT NOT NULL,
    location VARCHAR(200) NOT NULL,
    date_of_incident DATETIME NOT NULL,
    date_reported DATETIME NOT NULL,
    complainant_name VARCHAR(100) NOT NULL,
    complainant_phone VARCHAR(15),
    complainant_address TEXT,
    status ENUM('Open', 'Under Investigation', 'Closed', 'Solved') DEFAULT 'Open',
    assigned_officer_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_officer_id) REFERENCES officers(officer_id) ON DELETE SET NULL
);

-- Cases table
CREATE TABLE cases (
    case_id INT PRIMARY KEY AUTO_INCREMENT,
    case_number VARCHAR(20) UNIQUE NOT NULL,
    fir_id INT NOT NULL,
    case_title VARCHAR(200) NOT NULL,
    case_description TEXT,
    priority ENUM('Low', 'Medium', 'High', 'Critical') DEFAULT 'Medium',
    status ENUM('Open', 'Under Investigation', 'In Court', 'Closed', 'Solved') DEFAULT 'Open',
    assigned_officer_id INT,
    court_number VARCHAR(50),
    court_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (fir_id) REFERENCES firs(fir_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_officer_id) REFERENCES officers(officer_id) ON DELETE SET NULL
);

-- Evidence table
CREATE TABLE evidence (
    evidence_id INT PRIMARY KEY AUTO_INCREMENT,
    case_id INT NOT NULL,
    evidence_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    location_found VARCHAR(200),
    collected_by INT,
    collected_date DATETIME NOT NULL,
    storage_location VARCHAR(200),
    status ENUM('Collected', 'Analyzed', 'In Court', 'Returned', 'Destroyed') DEFAULT 'Collected',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (collected_by) REFERENCES officers(officer_id) ON DELETE SET NULL
);

-- Jails table (Kerala Prison Registry)
CREATE TABLE jails (
    jail_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type ENUM('CENTRAL', 'DISTRICT', 'SUB_JAIL', 'SPECIAL') NOT NULL,
    location VARCHAR(100) NOT NULL,
    capacity INT DEFAULT 500,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inmate Tracking table
CREATE TABLE jail_inmates (
    inmate_id INT PRIMARY KEY AUTO_INCREMENT,
    criminal_id INT NOT NULL,
    jail_id INT NOT NULL,
    case_id INT NOT NULL,
    entry_date DATE NOT NULL,
    sentence_duration VARCHAR(50),
    release_date DATE,
    status ENUM('ACTIVE', 'RELEASED', 'PAROLE', 'TRANSFERRED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (criminal_id) REFERENCES criminals(criminal_id) ON DELETE CASCADE,
    FOREIGN KEY (jail_id) REFERENCES jails(jail_id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE
);

-- Suspects table (many-to-many relationship between criminals and cases)
CREATE TABLE suspects (
    suspect_id INT PRIMARY KEY AUTO_INCREMENT,
    case_id INT NOT NULL,
    criminal_id INT NOT NULL,
    role ENUM('Primary Suspect', 'Secondary Suspect', 'Accomplice', 'Witness') NOT NULL,
    arrest_date DATE,
    bail_amount DECIMAL(10,2),
    bail_status ENUM('Not Set', 'Set', 'Paid', 'Forfeited') DEFAULT 'Not Set',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (criminal_id) REFERENCES criminals(criminal_id) ON DELETE CASCADE,
    UNIQUE KEY unique_case_criminal (case_id, criminal_id)
);

-- Case updates table for tracking case progress
CREATE TABLE case_updates (
    update_id INT PRIMARY KEY AUTO_INCREMENT,
    case_id INT NOT NULL,
    officer_id INT NOT NULL,
    update_type ENUM('Status Change', 'Evidence Added', 'Suspect Added', 'Court Update', 'General Update') NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (officer_id) REFERENCES officers(officer_id) ON DELETE CASCADE
);
DROP TABLE IF EXISTS users;
CREATE TABLE users (
   user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
   username VARCHAR(50) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   role ENUM('OFFICER','ADMIN','INVESTIGATOR','COURT','FORENSIC_EXAMINER','PROSECUTOR','JAILER','PRO') NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- Create indexes for better performance
CREATE INDEX idx_fir_crime_type ON firs(crime_type);
CREATE INDEX idx_fir_date ON firs(date_of_incident);
CREATE INDEX idx_fir_status ON firs(status);
CREATE INDEX idx_case_status ON cases(status);
CREATE INDEX idx_case_priority ON cases(priority);
CREATE INDEX idx_criminal_name ON criminals(first_name, last_name);
CREATE INDEX idx_evidence_type ON evidence(evidence_type);
CREATE INDEX idx_suspect_role ON suspects(role);

-- Insert sample officers
INSERT INTO officers (badge_number, first_name, last_name, `rank`, department, phone_number, email) VALUES
('OFF001', 'John', 'Smith', 'Detective', 'Homicide Division', '555-0101', 'john.smith@police.gov'),
('OFF002', 'Sarah', 'Johnson', 'Sergeant', 'Robbery Division', '555-0102', 'sarah.johnson@police.gov');
-- Insert Kerala Jails
INSERT INTO jails (name, type, location, capacity) VALUES
('Central Prison & Correctional Home, Poojappura', 'CENTRAL', 'Thiruvananthapuram', 1500),
('Central Prison & Correctional Home, Viyyur', 'CENTRAL', 'Thrissur', 1200),
('Central Prison & Correctional Home, Kannur', 'CENTRAL', 'Kannur', 1400),
('Central Prison & Correctional Home, Tavanur', 'CENTRAL', 'Malappuram', 1000),
('District Jail, Kollam', 'DISTRICT', 'Kollam', 500),
('District Jail, Ernakulam', 'DISTRICT', 'Ernakulam', 600),
('District Jail, Kozhikode', 'DISTRICT', 'Kozhikode', 700);

-- Insert sample criminals
INSERT INTO criminals (first_name, last_name, alias, date_of_birth, gender, address, phone_number, criminal_record) VALUES
('James', 'Rodriguez', 'Jimmy', '1985-03-15', 'M', '123 Oak Street, Downtown', '555-1001', 'Previous convictions: Theft (2019), Assault (2021)'),
('Maria', 'Garcia', 'Mari', '1990-07-22', 'F', '456 Pine Avenue, Uptown', '555-1002', 'Previous convictions: Fraud (2020)'),
('Robert', 'Taylor', 'Bobby', '1982-11-08', 'M', '789 Elm Street, Midtown', '555-1003', 'Previous convictions: Burglary (2018), Drug Possession (2020)'),
('Lisa', 'Anderson', 'Liz', '1988-05-12', 'F', '321 Maple Drive, Eastside', '555-1004', 'Previous convictions: Shoplifting (2019)'),
('Carlos', 'Martinez', 'Carl', '1992-09-30', 'M', '654 Cedar Lane, Westside', '555-1005', 'No previous convictions');

-- Insert sample FIRs
INSERT INTO firs (fir_number, crime_type, crime_description, location, date_of_incident, date_reported, complainant_name, complainant_phone, complainant_address, status, assigned_officer_id) VALUES
('FIR-2024-001', 'Robbery', 'Armed robbery at convenience store, suspect brandished weapon and stole cash register', '123 Main Street, Downtown', '2024-01-15 14:30:00', '2024-01-15 15:00:00', 'John Doe', '555-2001', '123 Main Street, Downtown', 'Under Investigation', 1),
('FIR-2024-002', 'Burglary', 'Residential break-in, electronics and jewelry stolen', '456 Oak Avenue, Uptown', '2024-01-20 02:15:00', '2024-01-20 08:30:00', 'Jane Smith', '555-2002', '456 Oak Avenue, Uptown', 'Open', 2),
('FIR-2024-003', 'Assault', 'Physical altercation resulting in injury', '789 Pine Street, Midtown', '2024-01-25 19:45:00', '2024-01-25 20:15:00', 'Mike Johnson', '555-2003', '789 Pine Street, Midtown', 'Solved', 1),
('FIR-2024-004', 'Fraud', 'Credit card fraud, unauthorized transactions', '321 Elm Drive, Eastside', '2024-02-01 10:20:00', '2024-02-01 11:00:00', 'Sarah Wilson', '555-2004', '321 Elm Drive, Eastside', 'Under Investigation', 4),
('FIR-2024-005', 'Drug Possession', 'Found in possession of controlled substances', '654 Cedar Lane, Westside', '2024-02-05 16:30:00', '2024-02-05 17:00:00', 'Officer Patrol', '555-2005', '654 Cedar Lane, Westside', 'Open', 3);

-- Insert sample cases
INSERT INTO cases (case_number, fir_id, case_title, case_description, priority, status, assigned_officer_id, court_number, court_date) VALUES
('CASE-2024-001', 1, 'Convenience Store Armed Robbery', 'Investigation of armed robbery at Main Street convenience store', 'High', 'Under Investigation', 1, 'Court A-101', '2024-03-15'),
('CASE-2024-002', 2, 'Residential Burglary Investigation', 'Break-in and theft at Oak Avenue residence', 'Medium', 'Open', 2, 'Court B-202', '2024-03-20'),
('CASE-2024-003', 3, 'Assault Case Resolution', 'Physical altercation case with suspect identified and arrested', 'Medium', 'Solved', 1, 'Court A-102', '2024-02-28'),
('CASE-2024-004', 4, 'Credit Card Fraud Investigation', 'Investigation of unauthorized credit card transactions', 'High', 'Under Investigation', 4, 'Court C-303', '2024-04-01'),
('CASE-2024-005', 5, 'Drug Possession Case', 'Drug possession and distribution investigation', 'Critical', 'Open', 3, 'Court B-201', '2024-03-10');

-- Insert sample evidence
INSERT INTO evidence (case_id, evidence_type, description, location_found, collected_by, collected_date, storage_location, status) VALUES
(1, 'Weapon', 'Handgun used in robbery', 'Convenience store floor', 1, '2024-01-15 16:00:00', 'Evidence Room A-001', 'Analyzed'),
(1, 'Fingerprints', 'Fingerprints on cash register', 'Convenience store counter', 1, '2024-01-15 16:30:00', 'Evidence Room A-002', 'Analyzed'),
(2, 'Tools', 'Crowbar used to break window', 'Backyard of residence', 2, '2024-01-20 09:00:00', 'Evidence Room B-001', 'Collected'),
(3, 'Clothing', 'Blood-stained shirt', 'Alley behind Pine Street', 1, '2024-01-25 21:00:00', 'Evidence Room A-003', 'In Court'),
(4, 'Documents', 'Fraudulent credit card receipts', 'Suspects residence', 4, '2024-02-01 12:00:00', 'Evidence Room C-001', 'Analyzed'),
(5, 'Substances', 'Controlled substances in plastic bags', 'Suspects vehicle', 3, '2024-02-05 17:30:00', 'Evidence Room B-002', 'Collected');

-- Insert sample suspects
INSERT INTO suspects (case_id, criminal_id, role, arrest_date, bail_amount, bail_status) VALUES
(1, 1, 'Primary Suspect', '2024-01-16', 50000.00, 'Set'),
(2, 2, 'Primary Suspect', '2024-01-21', 25000.00, 'Paid'),
(3, 3, 'Primary Suspect', '2024-01-26', 30000.00, 'Paid'),
(4, 4, 'Primary Suspect', '2024-02-02', 40000.00, 'Set'),
(5, 5, 'Primary Suspect', '2024-02-06', 100000.00, 'Not Set'),
(1, 2, 'Accomplice', '2024-01-17', 20000.00, 'Set');

-- Insert sample case updates
INSERT INTO case_updates (case_id, officer_id, update_type, description) VALUES
(1, 1, 'Status Change', 'Case assigned to Detective Smith for investigation'),
(1, 1, 'Evidence Added', 'Weapon and fingerprints collected from crime scene'),
(1, 1, 'Suspect Added', 'Primary suspect James Rodriguez arrested'),
(2, 2, 'Status Change', 'Case opened and assigned to Sergeant Johnson'),
(2, 2, 'Evidence Added', 'Crowbar used in break-in collected as evidence'),
(3, 1, 'Status Change', 'Case solved - suspect arrested and charged'),
(3, 1, 'Court Update', 'Suspect convicted and sentenced to 2 years'),
(4, 4, 'Status Change', 'Fraud investigation assigned to Detective Davis'),
(4, 4, 'Evidence Added', 'Fraudulent documents seized from suspect'),
(5, 3, 'Status Change', 'Drug case assigned to Lieutenant Brown'),
(5, 3, 'Suspect Added', 'Suspect Carlos Martinez arrested for drug possession');

INSERT INTO users (username, password_hash, role) VALUES
('admin', '$2a$10$vs9dNV/DtXrEugQsN.CSK.XJ2Ky0xLQqfehgk95k2/Sap858Gs1r6', 'ADMIN');

-- Display success message
SELECT 'Database setup completed successfully!' as Status;
SET FOREIGN_KEY_CHECKS = 1;