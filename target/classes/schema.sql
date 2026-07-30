-- Crime Record Management System database schema (H2/MySQL Compatible)
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS police_stations (
    station_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    max_inspectors INT DEFAULT 1,
    max_total_strength INT DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courts (
    court_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    jurisdiction_level VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS jails (
    jail_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    capacity INT DEFAULT 100,
    current_occupancy INT DEFAULT 0,
    security_level VARCHAR(50) DEFAULT 'MEDIUM',
    block_count INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS forensic_labs (
    lab_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- SFSL, RFSL, DFSL
    facility_type VARCHAR(50) DEFAULT 'GENERAL', -- CYBER, DNA, BALLISTICS, GENERAL
    location VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    parent_lab_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_lab_id) REFERENCES forensic_labs(lab_id)
);

CREATE TABLE IF NOT EXISTS legal_sections (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS system_settings (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(50) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS citizens (
    citizen_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    access_key VARCHAR(16) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS officers (
    officer_id INT PRIMARY KEY AUTO_INCREMENT,
    badge_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    `rank` VARCHAR(50) NOT NULL,
    salary DECIMAL(10,2) DEFAULT 0.00,
    leave_balance INT DEFAULT 30,
    station_id INT,
    jurisdiction_level VARCHAR(50) DEFAULT 'STATION_OFFICER',
    phone_number VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_on_duty BOOLEAN DEFAULT FALSE,
    specialization_wing VARCHAR(100) DEFAULT 'GENERAL',
    gender VARCHAR(10),
    date_of_joining DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (station_id) REFERENCES police_stations(station_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id INT PRIMARY KEY AUTO_INCREMENT,
    officer_id INT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    approved_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (officer_id) REFERENCES officers(officer_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES officers(officer_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS forensic_scientists (
    scientist_id INT PRIMARY KEY AUTO_INCREMENT,
    lab_id INT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    `rank` VARCHAR(50) NOT NULL, -- Director, Joint Director, Assistant Director, SO, JSO
    contact_number VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lab_id) REFERENCES forensic_labs(lab_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS criminals (
    criminal_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    alias VARCHAR(50),
    date_of_birth DATE,
    gender VARCHAR(10) NOT NULL,
    address TEXT,
    phone_number VARCHAR(15),
    criminal_record TEXT,
    height_cm INT,
    weight_kg INT,
    id_marks TEXT,
    native_place VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    officer_id INT UNIQUE,
    citizen_id INT UNIQUE,
    jail_id INT,
    court_id INT,
    scientist_id INT UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (officer_id) REFERENCES officers(officer_id) ON DELETE SET NULL,
    FOREIGN KEY (citizen_id) REFERENCES citizens(citizen_id) ON DELETE CASCADE,
    FOREIGN KEY (jail_id) REFERENCES jails(jail_id) ON DELETE SET NULL,
    FOREIGN KEY (court_id) REFERENCES courts(court_id) ON DELETE SET NULL,
    FOREIGN KEY (scientist_id) REFERENCES forensic_scientists(scientist_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS intranet_messages (
    msg_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT,
    body TEXT NOT NULL,
    is_bolo BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'PENDING',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS complaints (
    complaint_id INT PRIMARY KEY AUTO_INCREMENT,
    citizen_id INT,
    crime_type VARCHAR(100),
    description TEXT NOT NULL,
    location VARCHAR(200) NOT NULL,
    tracking_id VARCHAR(50) UNIQUE,
    matched_station_id INT,
    assigned_officer_id INT,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (citizen_id) REFERENCES citizens(citizen_id) ON DELETE SET NULL,
    FOREIGN KEY (matched_station_id) REFERENCES police_stations(station_id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_officer_id) REFERENCES officers(officer_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS firs (
    fir_id INT PRIMARY KEY AUTO_INCREMENT,
    fir_number VARCHAR(20) UNIQUE NOT NULL,
    crime_type VARCHAR(100) NOT NULL,
    crime_description TEXT NOT NULL,
    location VARCHAR(200) NOT NULL,
    date_of_incident DATETIME NOT NULL,
    date_reported DATETIME NOT NULL,
    citizen_id INT,
    status VARCHAR(50) DEFAULT 'Open',
    assigned_officer_id INT,
    station_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (citizen_id) REFERENCES citizens(citizen_id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_officer_id) REFERENCES officers(officer_id) ON DELETE SET NULL,
    FOREIGN KEY (station_id) REFERENCES police_stations(station_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS cases (
    case_id INT PRIMARY KEY AUTO_INCREMENT,
    case_number VARCHAR(20) UNIQUE NOT NULL,
    fir_id INT NOT NULL,
    case_title VARCHAR(200) NOT NULL,
    case_description TEXT,
    priority VARCHAR(50) DEFAULT 'Medium',
    status VARCHAR(50) DEFAULT 'Open',
    assigned_officer_id INT,
    court_id INT,
    target_date DATE,
    delay_explanation TEXT,
    next_trial_date DATE,
    witness_summons INT DEFAULT 0,
    is_public BOOLEAN DEFAULT FALSE,
    verdict_text TEXT,
    draft_verdict TEXT,
    article_text TEXT,
    is_verified_by_court BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fir_id) REFERENCES firs(fir_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_officer_id) REFERENCES officers(officer_id) ON DELETE SET NULL,
    FOREIGN KEY (court_id) REFERENCES courts(court_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS suspects (
    suspect_id INT PRIMARY KEY AUTO_INCREMENT,
    case_id INT NOT NULL,
    criminal_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    charge VARCHAR(500),
    arrest_date DATE,
    bail_amount DECIMAL(15,2) DEFAULT 0.00,
    bail_status VARCHAR(50) DEFAULT 'IN_CUSTODY',
    disposition VARCHAR(50) DEFAULT 'PENDING', -- GUILTY, ACQUITTED, RELEASED, PENDING
    gender VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (criminal_id) REFERENCES criminals(criminal_id) ON DELETE CASCADE,
    UNIQUE (case_id, criminal_id)
);

CREATE TABLE IF NOT EXISTS jail_inmates (
    inmate_id INT PRIMARY KEY AUTO_INCREMENT,
    jail_id INT NOT NULL,
    criminal_id INT NOT NULL,
    case_id INT,
    admission_date DATE NOT NULL,
    sentence_period_months INT,
    release_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jail_id) REFERENCES jails(jail_id),
    FOREIGN KEY (criminal_id) REFERENCES criminals(criminal_id),
    FOREIGN KEY (case_id) REFERENCES cases(case_id)
);

CREATE TABLE IF NOT EXISTS evidence (
    evidence_id INT PRIMARY KEY AUTO_INCREMENT,
    fir_id INT NOT NULL,
    evidence_type VARCHAR(100) NOT NULL,
    description TEXT,
    location_found VARCHAR(200),
    collected_by INT,
    collected_date DATETIME NOT NULL,
    storage_location VARCHAR(100),
    lab_id INT,
    scientist_id INT,
    status VARCHAR(50) DEFAULT 'COLLECTED', -- COLLECTED, LAB_PROCESS, SFSL_ESCALATION, COURT_SUBMITTED
    media_type VARCHAR(50),
    media_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fir_id) REFERENCES firs(fir_id) ON DELETE CASCADE,
    FOREIGN KEY (collected_by) REFERENCES officers(officer_id) ON DELETE SET NULL,
    FOREIGN KEY (lab_id) REFERENCES forensic_labs(lab_id) ON DELETE SET NULL,
    FOREIGN KEY (scientist_id) REFERENCES forensic_scientists(scientist_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS case_updates (
    update_id INT PRIMARY KEY AUTO_INCREMENT,
    case_id INT NOT NULL,
    officer_id INT NOT NULL,
    update_text TEXT NOT NULL,
    status_at_time VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (officer_id) REFERENCES officers(officer_id)
);

CREATE TABLE IF NOT EXISTS forensic_reports (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    evidence_id INT NOT NULL,
    scientist_id INT NOT NULL,
    report_text TEXT NOT NULL,
    conclusion VARCHAR(200),
    findings_summary TEXT,
    is_final BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id) ON DELETE CASCADE,
    FOREIGN KEY (scientist_id) REFERENCES forensic_scientists(scientist_id)
);

SET FOREIGN_KEY_CHECKS = 1;
