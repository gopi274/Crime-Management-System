import random
import os
from datetime import datetime, timedelta

# KERALA STATEWIDE POLICE ERP - GRADUATION MASTER REGISTRY V22.1.10
# RESTORING PROFESSIONAL ENTITIES & ENSURING FULL COLUMN COMPLIANCE

HINDU_M = ["Rahul", "Arjun", "Siddharth", "Madhav", "Vishnu", "Manoj", "Suresh", "Ramesh", "Anil", "Biju", "Sandeep", "Kiran", "Prashanth", "Vineeth", "Raveendran"]
HINDU_F = ["Anjali", "Sneha", "Deepthi", "Gouri", "Lekshmi", "Karthika", "Meera", "Divya", "Ammu", "Parvathy"]
HINDU_S = ["Nair", "Menon", "Pillai", "Kurup", "Nambiar", "Varma", "Panicker"]
MUSLIM_M = ["Faisal", "Hassan", "Abdul", "Ibrahim", "Shajahan", "Mustafa", "Basheer", "Moideen", "Asif"]
MUSLIM_F = ["Nasrin", "Fatima", "Jasmin", "Aysha", "Zeenath", "Shamna", "Sajna", "Farzana"]
MUSLIM_S = ["Koya", "Beevi", "Rawther", "Musliyar", "Siddique", "Kutty"]
CHRISTIAN_M = ["Jacob", "Kevin", "Peter", "Chacko", "Varkey", "Joy", "Raju", "George", "Thomas"]
CHRISTIAN_F = ["Ann", "Sherly", "Mary", "Rose", "Lincy", "Maria", "Lency", "Shanti"]
CHRISTIAN_S = ["Varghese", "Mathew", "Joseph", "Antony", "George", "Thomas"]

LOCATIONS = ["M.G. Road", "Railway Station", "Central Bus Stand", "Lighthouse Beach", "Pattom Junction", "Vyttila Hub", "East Fort", "Marine Drive"]
ADDRESSES = ["12/456, Green View Gardens, Pattom, TVM", "Flat 4B, Skyline Apts, Vyttila, Kochi", "House 78, River Side, Kozhikode", "Near Post Office, Beach Road, Kollam"]
ALIASES = ["Blade", "Kaduva", "Puli", "Shadow", "Rider", "Ghost"]

STATIONS_DATA = {
    "Thiruvananthapuram City": ["Museum", "Fort", "Thampanoor", "Nemom"],
    "Kochi City": ["Central", "North", "Fort Kochi", "Kalamassery"],
    "Kozhikode City": ["Town", "Nadakkavu", "Kasaba", "Feroke"]
}

COURTS_DATA = [("High Court of Kerala", "Kochi", "Ernakulam", "STATE_HIGH_COURT"), ("District & Sessions Court", "TVM", "Thiruvananthapuram", "DISTRICT_COURT"), ("POCSO Special Court", "Kochi", "Ernakulam", "SPECIALIZED_COURT")]
PRISONS_DATA = [("Central Prison Poojappura", "TVM", "CENTRAL"), ("District Jail Kochi", "Ernakulam", "DISTRICT")]
LABS_DATA = [("SFSL HQ", "TVM", "SFSL"), ("RFSL Kochi", "Kochi", "RFSL")]

BNS_REGISTRY = [] # To be populated by bns_tmp.txt if it exists

def esc(s): return str(s).replace("'", "''")

def get_auth_name(gender):
    comm = random.choice(["H", "M", "C"])
    if comm == "H": fn, sns = random.choice(HINDU_M if gender == "M" else HINDU_F), HINDU_S
    elif comm == "M": fn, sns = random.choice(MUSLIM_M if gender == "M" else MUSLIM_F), MUSLIM_S
    else: fn, sns = random.choice(CHRISTIAN_M if gender == "M" else CHRISTIAN_F), CHRISTIAN_S
    return fn, random.choice(sns)

def generate_sql():
    # Load BNS sections from bns_tmp.txt (handles Java array format)
    sections = []
    if os.path.exists("bns_tmp.txt"):
        with open("bns_tmp.txt", "r") as f:
            for line in f:
                # Format: "SEC 1: Short title, commencement and application",
                if ':"' in line or '"SEC' in line:
                    try:
                        content = line.strip()
                        if content.startswith('"') and content.endswith('",'):
                            content = content[1:-2]
                        elif content.startswith('"') and content.endswith('"'):
                            content = content[1:-1]
                        
                        if ":" in content:
                            code, desc = content.split(":", 1)
                            sections.append((code.strip(), desc.strip(), desc.strip()))
                    except:
                        continue
    
    if not sections:
        # Fallback to a simplified list if file missing or unparseable
        sections = [("SEC 1", "Short Title", "Commencement"), ("SEC 302", "Murder", "Punishment for murder")]

    # DATA RETENTION: We remove the DELETE FROM calls to avoid wiping the DB.
    # We will use INSERT IGNORE logic equivalent for H2 (MERGE INTO) or just INSERT and assume it works for fresh start.
    # To satisfy "retained", I will comment out the DELETE statements or use a smarter approach.
    sql = ["SET REFERENTIAL_INTEGRITY FALSE;"]
    
    # We keep the legal_sections wipe because it's a fixed registry that might change structure
    sql.append("DELETE FROM legal_sections;") 

    for code, title, desc in sections:
        sql.append(f"INSERT INTO legal_sections (code, title, description, category) VALUES ('{esc(code)}', '{esc(title)}', '{esc(desc)}', 'PRIMARY');")

    for i, (n, c, d, l) in enumerate(COURTS_DATA, 1):
        sql.append(f"MERGE INTO courts (court_id, name, city, state, jurisdiction_level) KEY(court_id) VALUES ({i}, '{esc(n)}', '{esc(c)}', 'Kerala', '{l}');")
    
    for i, (n, l, t) in enumerate(PRISONS_DATA, 1):
        sql.append(f"MERGE INTO jails (jail_id, name, type, location, capacity, security_level) KEY(jail_id) VALUES ({i}, '{esc(n)}', '{t}', '{esc(l)}', 2000, 'HIGH');")
    
    for i, (n, l, t) in enumerate(LABS_DATA, 1):
        sql.append(f"MERGE INTO forensic_labs (lab_id, name, type, location, facility_type) KEY(lab_id) VALUES ({i}, '{esc(n)}', '{t}', '{esc(l)}', 'GENERAL');")

    # Scaling up to full graduation level (262 stations, 2404 officers)
    station_id = 1
    officer_id = 1
    
    # Generate 262 stations across various districts
    extra_districts = ["Palakkad", "Malappuram", "Thrissur", "Kollam", "Alappuzha", "Idukki", "Pathanamthitta", "Kasaragod", "Wayanad"]
    districts = list(STATIONS_DATA.keys()) + extra_districts

    # HIGH COMMAND GENERATION (DGP, ADGP, IG, DIG)
    # These are usually not tied to a single station but for system simulation, we'll assign to HQ (Station 1)
    high_ranks = [("DGP", 1), ("ADGP", 3), ("IG", 7), ("DIG", 14)]
    for rank, count in high_ranks:
        for _ in range(count):
            fn, ln = get_auth_name("M")
            sql.append(f"MERGE INTO officers (officer_id, first_name, last_name, rank, gender, station_id, badge_number, phone_number, email, is_on_duty, jurisdiction_level, salary, date_of_joining) KEY(officer_id) VALUES ({officer_id}, '{esc(fn)}', '{esc(ln)}', '{rank}', 'M', 1, 'KP/HQ/{officer_id}', '999000{officer_id:04d}', '{fn.lower()}@keralapolice.gov.in', TRUE, 'STATE', 120000, '2010-01-01');")
            sql.append(f"MERGE INTO users (username, password_hash, role, is_active, officer_id) KEY(username) VALUES ('off_{officer_id}', 'password', 'OFFICER', TRUE, {officer_id});")
            officer_id += 1

    while station_id <= 262:
        dist = random.choice(districts)
        s_name = f"Station_{station_id}"
        if dist in STATIONS_DATA and len(STATIONS_DATA[dist]) > (station_id % 4):
             s_name = STATIONS_DATA[dist][station_id % len(STATIONS_DATA[dist])]
        
        sql.append(f"MERGE INTO police_stations (station_id, name, city, state) KEY(station_id) VALUES ({station_id}, '{esc(s_name)} Police Station', '{esc(dist)}', 'Kerala');")
        
        # Determine Ranks for this station
        is_city = "City" in dist or station_id <= 20
        ranks = ["CI", "SI", "ASI", "SCPO", "CPO"]
        
        # Add leadership to station
        lead_rank = "DYSP"
        if is_city: lead_rank = random.choice(["CP", "ACP", "DYSP"])
        elif station_id % 10 == 0: lead_rank = "SP" # District lead every few stations
        
        fn_l, ln_l = get_auth_name("M")
        sql.append(f"MERGE INTO officers (officer_id, first_name, last_name, rank, gender, station_id, badge_number, phone_number, email, is_on_duty, jurisdiction_level, salary, date_of_joining) KEY(officer_id) VALUES ({officer_id}, '{esc(fn_l)}', '{esc(ln_l)}', '{lead_rank}', 'M', {station_id}, 'KP/{lead_rank}/{officer_id}', '950000{officer_id:04d}', '{fn_l.lower()}@keralapolice.gov.in', TRUE, 'STATION', 85000, '2015-01-01');")
        sql.append(f"MERGE INTO users (username, password_hash, role, is_active, officer_id) KEY(username) VALUES ('off_{officer_id}', 'password', 'OFFICER', TRUE, {officer_id});")
        officer_id += 1

        # Regular staff
        for r in ranks:
            if officer_id > 2404: break
            fn, ln = get_auth_name("M")
            sql.append(f"MERGE INTO officers (officer_id, first_name, last_name, rank, gender, station_id, badge_number, phone_number, email, is_on_duty, jurisdiction_level, salary, date_of_joining) KEY(officer_id) VALUES ({officer_id}, '{esc(fn)}', '{esc(ln)}', '{r}', 'M', {station_id}, 'KP/{r}/{officer_id}', '950000{officer_id:04d}', '{fn.lower()}@keralapolice.gov.in', TRUE, 'STATION', 55000, '2020-01-01');")
            sql.append(f"MERGE INTO users (username, password_hash, role, is_active, officer_id) KEY(username) VALUES ('off_{officer_id}', 'password', 'OFFICER', TRUE, {officer_id});")
            officer_id += 1
        station_id += 1

    # Add 40 forensic scientists with hierarchy (SFSL Nomenclature)
    forensic_ranks = ["Director", "Joint Director", "SSO", "SO", "SSA", "JSA"]
    for i in range(1, 41):
        fn, ln = get_auth_name("M")
        f_rank = forensic_ranks[i % len(forensic_ranks)]
        sql.append(f"MERGE INTO forensic_scientists (scientist_id, lab_id, first_name, last_name, specialization, rank, contact_number) KEY(scientist_id) VALUES ({i}, {(i % 2) + 1}, '{esc(fn)}', '{esc(ln)}', 'DNA/Cyber/Ballistics/Toxicology', '{f_rank}', '950000789{i}');")
        sql.append(f"MERGE INTO users (username, password_hash, role, is_active, scientist_id) KEY(username) VALUES ('sci_{i}', 'password', 'FORENSIC', TRUE, {i});")

    # Add 100 Professional Criminal Profiles
    for i in range(1, 101):
        g = random.choice(["M", "F"])
        fn, ln = get_auth_name(g)
        sql.append(f"MERGE INTO criminals (criminal_id, first_name, last_name, alias, gender, address, date_of_birth, height_cm, weight_kg, id_marks) KEY(criminal_id) VALUES ({i}, '{esc(fn)}', '{esc(ln)}', '{random.choice(ALIASES)}', '{g}', '{esc(random.choice(ADDRESSES))}', '1990-05-{ (i%28)+1 :02d}', 180, 75, 'Tattoo on right forearm');")
        sql.append(f"MERGE INTO citizens (citizen_id, name, phone, address) KEY(citizen_id) VALUES ({i}, '{esc(fn)} {esc(ln)}', '950000{i:04d}', '{esc(random.choice(ADDRESSES))}');")

    # Add 1,000 FIRs over 12 months for Rich Trend Analysis Graph
    for i in range(1, 1001):
        s_id = random.randint(1, station_id - 1)
        o_id = random.randint(1, officer_id - 1)
        # Spread dates over last 6 months specifically to pop the graph
        months_ago = (i % 6)
        dt = (datetime.now() - timedelta(days=months_ago * 30 + random.randint(1, 28))).strftime('%Y-%m-%d %H:%M:%S')
        sec = random.choice(sections)[0] if sections else "SEC 302"
        sql.append(f"MERGE INTO firs (fir_id, fir_number, crime_type, crime_description, location, status, date_reported, date_of_incident, citizen_id, station_id, assigned_officer_id) KEY(fir_id) VALUES ({i}, 'FIR/2026/G/{i:04d}', '{sec}', 'Offence under {sec} localized in {random.choice(LOCATIONS)}', '{random.choice(LOCATIONS)}', 'Under Investigation', '{dt}', '{dt}', {random.randint(1,100)}, {s_id}, {o_id});")
        sql.append(f"MERGE INTO cases (case_id, case_number, fir_id, case_title, status, priority, court_id, assigned_officer_id) KEY(case_id) VALUES ({i}, 'CASE/G/{i:04d}', {i}, 'State of Kerala vs {fn}', 'Open', '{random.choice(['Low', 'Medium', 'High', 'Critical'])}', {random.randint(1,3)}, {o_id});")
        sql.append(f"MERGE INTO suspects (case_id, criminal_id, role, charge, bail_status) KEY(case_id, criminal_id) VALUES ({i}, {random.randint(1,100)}, 'ACCUSED', '{sec}', 'IN_CUSTODY');")

    sql.append("MERGE INTO users (username, password_hash, role, is_active) KEY(username) VALUES ('admin', 'admin', 'ADMIN', TRUE);")
    sql.append("SET REFERENTIAL_INTEGRITY TRUE;")
    
    with open("src/main/resources/justice_graduation_data.sql", "w") as f: 
        f.write("\n".join(sql))

if __name__ == "__main__": 
    generate_sql()
