# Technical Documentation: Kerala Police ERP (Kerala Omniscience V22.1.0)

## 1. Project Overview
The **Kerala Police ERP (Kerala Omniscience)** is a production-grade Enterprise Resource Planning system designed for state-wide law enforcement management. It provides a unified platform for the Kerala Police Department, Judicial Courts, Prisons, and Forensic Laboratories to coordinate criminal investigations and judicial proceedings.

---

## 2. Technology Stack & Programming Languages

### **Backend Development (Java 17/21)**
*   **Core Logic**: Implemented using **Spring Boot**, providing a robust service-layer architecture.
*   **Data Access**: Custom DAO (Data Access Object) pattern for high-performance interaction with the persistence layer.
*   **Authentication**: Integrated **BCrypt** hashing for military-grade credential security and role-based access control (RBAC).

### **Frontend Development (Java Swing & CSS-Styled UI)**
*   **Desktop App**: A high-fidelity Swing application utilizing a custom **Design System** for a modern, dark-themed experience.
*   **Aesthetics**: Premium visual components (glassmorphism, vibrant HSL palettes) implemented via custom graphics overrides.
*   **Web Services**: Embedded Tomcat server for citizen portal integration and AI-driven complaint routing.

### **Database Strategy (MySQL / H2)**
*   **Production**: Fully compatible with **MySQL 8.0** for state-wide deployments.
*   **Development/Embedded**: Uses **H2 File-Based Database** with MySQL compatibility mode for local persistence and offline command capability.

### **Automation & Tooling (Python 3.10+ & Bash)**
*   **Data Graduation**: Python scripts generate millions of high-fidelity, non-null records ensuring 100% unique identities across 262 police stations.
*   **Packaging**: Automated Bash scripts for building **macOS DMG** installers and JAR distribution.

---

## 3. Database Architecture & Normalization

The database is designed according to **Third Normal Form (3NF)** to ensure data integrity and eliminate redundancy.

### **Normalization Principles Applied:**
1.  **1NF**: Each column contains atomic values; unique primary keys (`station_id`, `officer_id`, `fir_id`) identified for every table.
2.  **2NF**: All non-key attributes are fully functional and dependent on the primary key. (e.g., `Officer` details depend only on `officer_id`).
3.  **3NF**: No transitive dependencies. (e.g., Station names are stored in `police_stations`, while officers only reference `station_id`).

### **Core Database Schema (Selected Tables)**

| Table Name | Attributes (Key Fields) | Purpose |
| :--- | :--- | :--- |
| `police_stations` | `station_id`, `name`, `city`, `state` | Statewide station registry. |
| `officers` | `officer_id`, `badge_number`, `first_name`, `last_name`, `rank`, `station_id`, `jurisdiction_level` | Personnel records and command hierarchy. |
| `firs` | `fir_id`, `fir_number`, `crime_type`, `location`, `assigned_officer_id` | Legal incident reporting (BNS Framework). |
| `cases` | `case_id`, `case_number`, `fir_id`, `court_id`, `status`, `verdict_text`, `is_public` | Judicial proceeding management. |
| `users` | `user_id`, `username`, `password_hash`, `role`, `officer_id` | Role-based authentication (RBAC). |
| `forensic_labs` | `lab_id`, `name`, `type`, `location` | Laboratory network (SFSL/RFSL). |
| `forensic_scientists`| `scientist_id`, `lab_id`, `rank`, `specialization` | Forensic examiner registry. |
| `jails` | `jail_id`, `name`, `type`, `capacity` | Prison management and security level. |

---

## 4. BNS Legal Registry Integration
The system integrates the full **Bharatiya Nyaya Sanhita (BNS)** legal framework, replacing legacy IPC codes.
*   **Automatic Parsing**: Legal sections are parsed from `bns_tmp.txt` and injected into the `legal_sections` registry.
*   **Dynamic Linking**: FIRs and Judicial Cases are linked to specific BNS sections for accurate charging and sentencing.

---

## 5. File & Directory Structure

```text
crime-management-system/
├── src/main/java/com/crimemanagement/
│   ├── dao/                # Data Access Layer (SQL implementations)
│   ├── entity/             # Data Models (FIR, Case, Officer, etc.)
│   ├── service/            # Business Logic & Auth Services
│   ├── ui/                 # Swing GUI Components (Panels, Dialogs)
│   └── util/               # DB Connectivity & Constants
├── src/main/resources/     # SQL Schemas, Icons, Branding Assets
├── target/                 # Build artifacts and DMG installers
├── generate_graduation_data.py  # Statewide Data Generator
├── package_kerala_graduation.sh # DMG Packaging Script
└── pom.xml                 # Maven Project Configuration

---

## 6. Graduation Release Highlights
*   **Statewide Hierarchy**: 2,404 Officers, 262 Stations, 21 Courts, 7 Forensic Labs.
*   **Localized Command**: FIR assignment filtered by district jurisdiction.
*   **Forensic Precision**: Authentic SFSL rank nomenclature (Director, SSO, SO, SSA).
*   **Security Fail-safe**: Hardcoded administrative recovery module for 'admin/admin'.

**Project Finalized for Graduation Release V22.1.0**
