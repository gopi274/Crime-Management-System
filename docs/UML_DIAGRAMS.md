# Crime Management System - UML & Use Case Diagrams

**Version:** 1.0.0  
**Generated:** 2025

---

## 1. Use Case Diagram

### Actors
- **ADMIN**: System administrator with full access
- **OFFICER**: Police officer with data management access  
- **COURT**: Court user with case management access

### Use Case Diagram Structure

```
+================================================================================+
|                    CRIME MANAGEMENT SYSTEM                                     |
|                         USE CASE DIAGRAM                                       |
+================================================================================+


+================================================================================+
|                             ADMIN ACTOR                                        |
+================================================================================+
         |
         |
         +----------------------------------------------------------------------+
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage User Accounts                                        |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Officers                                            |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Criminals                                           |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage FIRs                                                |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Cases                                               |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Suspects                                            |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Dashboard Statistics                                  |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Search All Records                                         |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         +----------------------------------------------------------------------+


+================================================================================+
|                            OFFICER ACTOR                                       |
+================================================================================+
         |
         |
         +----------------------------------------------------------------------+
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Officers                                            |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Criminals                                           |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage FIRs                                                |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Cases                                                 |   |
         |  |  (Read Only)                                                 |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Suspects                                            |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Dashboard Statistics                                  |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Search All Records                                         |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Own Assigned Cases                                    |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         +----------------------------------------------------------------------+


+================================================================================+
|                             COURT ACTOR                                        |
+================================================================================+
         |
         |
         +----------------------------------------------------------------------+
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Manage Cases                                               |   |
         |  |  (Create, Read, Update, Delete)                              |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Criminals                                             |   |
         |  |  (Read Only)                                                 |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View FIRs                                                  |   |
         |  |  (Read Only)                                                 |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Suspects                                              |   |
         |  |  (Read Only)                                                 |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Dashboard Statistics                                  |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Search Cases and Related Records                           |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         +----------------------------------------------------------------------+


+================================================================================+
|                    COMMON USE CASES (All Roles)                               |
+================================================================================+
         |
         |
         +----------------------------------------------------------------------+
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Login to System                                            |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  View Dashboard                                             |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Search Records                                            |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         |  +--------------------------------------------------------------+   |
         |  |  Logout                                                     |   |
         |  +--------------------------------------------------------------+   |
         |                                                                      |
         +----------------------------------------------------------------------+
```

---

## 2. Class Diagram - Entity Relationships

```
+================================================================================+
|                    CLASS DIAGRAM - ENTITIES                                    |
+================================================================================+

+---------------------+
|    UserAccount      |
+---------------------+
| - userId: Integer   |
| - username: String  |
| - passwordHash: Str |
| - role: String      |
+----------+----------+
           | (1) has
           |
           v
    Authentication
           |
           +----> ADMIN
           +----> OFFICER
           +----> COURT

+---------------------+         +---------------------+
|      Officer        |         |      Criminal      |
+---------------------+         +---------------------+
| - officerId: int    |         | - criminalId: int  |
| - badgeNumber: Str  |         | - firstName: Str   |
| - firstName: Str    |         | - lastName: Str    |
| - lastName: Str     |         | - alias: String   |
| - rank: String      |         | - dateOfBirth: Date|
| - department: Str   |         | - gender: String   |
| - phoneNumber: Str  |         | - address: String  |
| - email: String     |         | - phoneNumber: Str |
+---------------------+         | - criminalRecord:  |
         |                       |   String           |
         | (1)                   +---------------------+
         | assigns/collects              |
         |                               | (N)
         | (N)                           |
         v                               v
+---------------------+         +---------------------+
|        FIR          |         |      Suspect        |
+---------------------+         +---------------------+
| - firId: int        |<---(1)---| - suspectId: int   |
| - firNumber: String |         | - caseId: int      |--+
| - crimeType: String |         | - criminalId: int  |--+--(N)
| - crimeDescription: |         | - role: String     |  |
|   String            |         | - arrestDate: Date |  |
| - location: String  |         | - bailAmount:      |  |
| - dateOfIncident:   |         |   BigDecimal       |  |
|   LocalDateTime     |         | - bailStatus: Str  |  |
| - dateReported:     |         +---------------------+  |
|   LocalDateTime     |                                  |
| - complainantName:  |                                  |
|   String            |                                  |
| - complainantPhone: |                                  |
|   String            |                                  |
| - complainantAddr:  |                                  |
|   String            |                                  |
| - status: String    |                                  |
| - assignedOfficerId:|                                  |
|   Integer           |                                  |
+---------------------+                                  |
         | (1)                                            |
         |                                                |
         v (N)                                           |
+---------------------+                                  |
|       Case          |<--------------------------------+
+---------------------+         (1)
| - caseId: int       |
| - caseNumber: String|
| - firId: int        |
| - caseTitle: String |
| - caseDescription:  |
|   String            |
| - priority: String  |
| - status: String    |
| - assignedOfficerId:|
|   Integer           |
| - courtNumber:      |
|   String            |
| - courtDate:        |
|   LocalDate         |
+---------------------+
         | (1)
         |
         v (N)
+---------------------+
|      Evidence       |
+---------------------+
| - evidenceId: int  |
| - caseId: int       |
| - evidenceType: Str |
| - description: Str |
| - locationFound: Str|
| - collectedBy:      |
|   Integer           |
| - collectedDate:    |
|   LocalDateTime     |
| - storageLocation:  |
|   String            |
| - status: String    |
+---------------------+
         |
         | (N)
         |
+---------------------+
|    CaseUpdate       |
+---------------------+
| - updateId: int    |
| - caseId: int       |
| - officerId: int   |
| - updateType: String|
| - description: Str |
| - createdAt:        |
|   LocalDateTime     |
+---------------------+
```

### Relationship Summary:
- **UserAccount** → Authenticates users (ADMIN, OFFICER, COURT)
- **Officer** → Assigned to FIRs and Cases (1-to-Many)
- **FIR** → Linked to Cases (1-to-Many)
- **Case** → Linked to one FIR, many Suspects, Evidence, and CaseUpdates
- **Criminal** → Linked to Suspects (Many-to-Many via Suspect)
- **Suspect** → Links Case and Criminal (Many-to-Many relationship)
- **Evidence** → Belongs to Case (Many-to-One)
- **CaseUpdate** → Tracks Case history (Many-to-One)

---

## 3. Sequence Diagram - User Login Flow

```
+================================================================================+
|              SEQUENCE DIAGRAM - USER LOGIN                                    |
+================================================================================+

User          LoginDialog      AuthService     UserAccountDAO    Database
 |                 |                 |                |              |
 |                 |                 |                |              |
 |---Login-------->|                 |                |              |
 |                 |                 |                |              |
 |                 |---authenticate->|                |              |
 |                 |                 |                |              |
 |                 |                 |---findByUsername------------->|
 |                 |                 |                |              |
 |                 |                 |<---UserAccount---------------|
 |                 |                 |                |              |
 |                 |                 |---verifyPassword->|          |
 |                 |                 |                |              |
 |                 |                 |<---true/false---|          |
 |                 |                 |                |              |
 |                 |<---success------|                |              |
 |                 |                 |                |              |
 |<---MainWindow----|                 |                |              |
 |                 |                 |                |              |
```

---

## 4. Sequence Diagram - Create Case Flow (COURT Role)

```
+================================================================================+
|          SEQUENCE DIAGRAM - CREATE CASE (COURT)                                |
+================================================================================+

Court User    CasesPanel    InvestigationService   CaseDAO    FIRDAO    Database
    |              |                 |                |          |          |
    |              |                 |                |          |          |
    |---Add Case-->|                 |                |          |          |
    |              |                 |                |          |          |
    |              |---Validate FIR->|                |          |          |
    |              |                 |                |          |          |
    |              |                 |---findByFirNumber-------->|          |
    |              |                 |                |          |          |
    |              |                 |<---FIR---------|          |          |
    |              |                 |                |          |          |
    |              |<---FIR Valid----|                |          |          |
    |              |                 |                |          |          |
    |              |---createCase--->|                |          |          |
    |              |                 |                |          |          |
    |              |                 |---create------>|          |          |
    |              |                 |                |          |          |
    |              |                 |                |---INSERT->|          |
    |              |                 |                |          |          |
    |              |                 |                |<---ID----|          |
    |              |                 |                |          |          |
    |              |                 |<---Case--------|          |          |
    |              |                 |                |          |          |
    |              |<---Success------|                |          |          |
    |              |                 |                |          |          |
    |              |---Refresh Table-|                |          |          |
    |              |                 |                |          |          |
```

---

## 5. Sequence Diagram - Manage Officer Flow (OFFICER/ADMIN)

```
+================================================================================+
|        SEQUENCE DIAGRAM - MANAGE OFFICER                                       |
+================================================================================+

User          OfficersPanel   OfficerService   OfficerDAO    Database
 |                 |                 |              |            |
 |                 |                 |              |            |
 |---Add Officer-->|                 |              |            |
 |                 |                 |              |            |
 |                 |---validate----->|              |            |
 |                 |                 |              |            |
 |                 |<---valid--------|              |            |
 |                 |                 |              |            |
 |                 |---createOfficer>|              |            |
 |                 |                 |              |            |
 |                 |                 |---create---->|            |
 |                 |                 |              |            |
 |                 |                 |              |---INSERT->|
 |                 |                 |              |            |
 |                 |                 |              |<---ID-----|
 |                 |                 |              |            |
 |                 |                 |<---Officer---|            |
 |                 |                 |              |            |
 |                 |<---Success------|              |            |
 |                 |                 |              |            |
 |                 |---Refresh------|              |            |
 |                 |                 |              |            |
 |                 |                 |---findAll--->|            |
 |                 |                 |              |            |
 |                 |                 |              |---SELECT->|
 |                 |                 |              |            |
 |                 |                 |              |<---List--|
 |                 |                 |              |            |
 |                 |                 |<---List------|            |
 |                 |                 |              |            |
 |                 |<---Updated-----|              |            |
 |                 |                 |              |            |
```

---

## 6. Class Diagram - Service Layer

```
+================================================================================+
|                    CLASS DIAGRAM - SERVICES                                    |
+================================================================================+

+---------------------+
|   AuthService       |
+---------------------+
| + authenticate()     |
| + ensureDefaultAdmin()|
+----------+----------+
           | uses
           v
+---------------------+
|  UserAccountDAO     |
+---------------------+

+---------------------+
|   AdminService      |
+---------------------+
| + getAllUsers()     |
| + createUser()      |
| + updateUser()      |
| + deleteUser()      |
+----------+----------+
           | uses
           v
+---------------------+
|  UserAccountDAO     |
+---------------------+

+---------------------+
|   OfficerService    |
+---------------------+
| + getAllOfficers()  |
| + createOfficer()   |
| + updateOfficer()    |
| + deleteOfficer()   |
+----------+----------+
           | uses
           v
+---------------------+
|    OfficerDAO       |
+---------------------+

+---------------------+
|  CriminalService    |
+---------------------+
| + getAllCriminals() |
| + createCriminal()  |
| + updateCriminal()  |
| + deleteCriminal()  |
+----------+----------+
           | uses
           v
+---------------------+
|   CriminalDAO       |
+---------------------+

+---------------------+
|    FIRService       |
+---------------------+
| + getAllFIRs()      |
| + createFIR()       |
| + updateFIR()       |
| + deleteFIR()       |
+----------+----------+
           | uses
           v
+---------------------+
|      FIRDAO         |
+---------------------+

+---------------------+
| InvestigationService |
+---------------------+
| + getAllCases()     |
| + createCase()       |
| + updateCase()       |
| + deleteCase()       |
| + addUpdate()        |
+----------+----------+
           | uses
           +----> CaseDAO
           +----> CaseUpdateDAO

+---------------------+
|   SearchService     |
+---------------------+
| + searchAll()        |
| + searchByCrimeType() |
| + searchByStatus()    |
| + searchByOfficer()   |
| + getCaseDetails()    |
+----------+----------+
           | uses
           +----> FIRDAO
           +----> CaseDAO
           +----> CriminalDAO
           +----> EvidenceDAO
           +----> OfficerDAO
           +----> SuspectDAO
           +----> CaseUpdateDAO
```

---

## 7. Class Diagram - UI Layer

```
+================================================================================+
|                    CLASS DIAGRAM - UI LAYER                                    |
+================================================================================+

+---------------------+
|  DesktopLauncher    |
+---------------------+
| + main()            |
+----------+----------+
           | creates
           v
+---------------------+
|   LoginDialog       |
+---------------------+
| - username: JTextField|
| - password: JPasswordField|
| - role: JComboBox    |
| + getRole()          |
| + getUsername()      |
+----------+----------+
           | creates
           v
+---------------------+
|    MainWindow       |
+---------------------+
| - userRole: Role    |
| - username: String  |
| + buildMenu()        |
+----------+----------+
           | contains
           +----> StatsPanel
           +----> OfficersPanel (ADMIN/OFFICER)
           +----> CriminalsPanel
           +----> FIRsPanel
           +----> CasesPanel
           +----> SuspectsPanel
           +----> AdminPanel (ADMIN only)

+---------------------+
|   OfficersPanel     |
+---------------------+
| - officerService     |
| - tableModel         |
| + loadOfficers()      |
| + onAdd()             |
| + onEdit()            |
| + onDelete()          |
+----------+----------+
           | uses
           v
+---------------------+
|   OfficerService    |
+---------------------+

+---------------------+
|   CriminalsPanel    |
+---------------------+
| - service            |
| - model              |
| + load()             |
| + addCriminal()       |
| + editCriminal()     |
| + removeCriminal()   |
+----------+----------+
           | uses
           v
+---------------------+
|  CriminalService    |
+---------------------+

+---------------------+
|     CasesPanel      |
+---------------------+
| - investigationService|
| - model              |
| + load()             |
| + showAddDialog()     |
| + showEditDialog()    |
| + removeCase()        |
+----------+----------+
           | uses
           v
+---------------------+
| InvestigationService |
+---------------------+

+---------------------+
|      UIUtils         |
+---------------------+
| + applyTableDefaults()|
| + bindSearch()       |
+---------------------+
```

---

## 8. Component Diagram

```
+================================================================================+
|                    COMPONENT DIAGRAM                                          |
+================================================================================+

+-------------------------------------------------------------+
|                    PRESENTATION LAYER                       |
|  +-------------+  +-------------+  +-------------+         |
|  |  MainWindow |  | LoginDialog |  |  UI Panels  |         |
|  +-------------+  +-------------+  +-------------+         |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                    SERVICE LAYER                            |
|  +-------------+  +-------------+  +-------------+         |
|  |AuthService  |  |OfficerService|  |CriminalService|        |
|  +-------------+  +-------------+  +-------------+         |
|  +-------------+  +-------------+  +-------------+         |
|  |  FIRService |  |Investigation |  |SearchService|         |
|  +-------------+  |   Service   |  +-------------+         |
|                    +-------------+                           |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                    DATA ACCESS LAYER                         |
|  +-------------+  +-------------+  +-------------+         |
|  |  CaseDAO    |  |  FIRDAO     |  |CriminalDAO |         |
|  +-------------+  +-------------+  +-------------+         |
|  +-------------+  +-------------+  +-------------+         |
|  | OfficerDAO  |  |SuspectDAO   |  |UserAccount  |         |
|  +-------------+  +-------------+  |    DAO      |         |
|                                     +-------------+         |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                    DATABASE CONNECTION                        |
|  +-------------------------------------------------+         |
|  |      DatabaseConnection (HikariCP)               |         |
|  +-------------------------------------------------+         |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                      MYSQL DATABASE                          |
|  +----------+  +----------+  +----------+  +----------+   |
|  | Officers |  |Criminals |  |   FIRs   |  |  Cases   |   |
|  +----------+  +----------+  +----------+  +----------+   |
|  +----------+  +----------+  +----------+  +----------+   |
|  |Suspects |  | Evidence |  |CaseUpdate|  |   Users  |   |
|  +----------+  +----------+  +----------+  +----------+   |
+-------------------------------------------------------------+
```

---

## 9. Activity Diagram - Case Creation Process

```
+================================================================================+
|          ACTIVITY DIAGRAM - CREATE CASE                                       |
+================================================================================+

[Start] --> [Court User clicks "Add Case"]
    |
    v
[Open Add Case Dialog]
    |
    v
[Enter Case Details]
    |
    v
[Enter FIR Number/ID]
    |
    v
[Validate FIR Exists?]
    | No
    +--> [Show Error: FIR Not Found]
    +--> [Return to Dialog]
    | Yes
    v
[Validate Required Fields]
    | No
    +--> [Show Validation Error]
    +--> [Return to Dialog]
    | Yes
    v
[Validate Date Format]
    | No
    +--> [Show Date Error]
    +--> [Return to Dialog]
    | Yes
    v
[Create Case Object]
    |
    v
[Call InvestigationService.createCase()]
    |
    v
[Call CaseDAO.create()]
    |
    v
[Insert into Database]
    |
    v
[Case Created Successfully]
    |
    v
[Refresh Cases Table]
    |
    v
[Show Success Message]
    |
    v
[End]
```

---

## 10. State Diagram - Case Status

```
+================================================================================+
|              STATE DIAGRAM - CASE STATUS                                      |
+================================================================================+

                    [Open]
                      |
                      | assignOfficer()
                      v
              [Under Investigation]
                      |
        +-------------+-------------+
        |                           |
        | updateStatus()            | updateStatus()
        v                           v
   [Closed]                    [On Hold]
        |                           |
        |                           | updateStatus()
        |                           v
        |                      [Reopened]
        |                           |
        +-------->[Closed]<----------+
                      |
                      | courtDate set
                      v
                  [Court Hearing]
                      |
                      | updateStatus()
                      v
                  [Resolved]
```

---

## Summary

### Key Relationships:
1. **UserAccount** authenticates users with roles (ADMIN, OFFICER, COURT)
2. **Officer** can be assigned to multiple FIRs and Cases
3. **FIR** can have multiple Cases
4. **Case** belongs to one FIR, has many Suspects, Evidence, and Updates
5. **Suspect** links Cases and Criminals (many-to-many)
6. **Evidence** belongs to one Case
7. **CaseUpdate** tracks Case history

### Role Permissions:
- **ADMIN**: Full system access including user management
- **OFFICER**: Manage officers, criminals, FIRs, suspects (read-only cases)
- **COURT**: Manage cases, read-only access to other data

### Architecture Layers:
1. **Presentation**: Swing UI Panels
2. **Service**: Business logic layer
3. **DAO**: Data Access Object pattern
4. **Database**: MySQL with HikariCP connection pooling

---

**Generated:** 2025  
**Format:** Markdown/Text (Convertible to PDF)  
**Tool:** ASCII diagrams with square boxes
