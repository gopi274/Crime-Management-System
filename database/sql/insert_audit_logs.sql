USE crime_management;
INSERT INTO audit_logs (actor, action, details, created_at) VALUES
('Admin', 'LOGIN', 'User admin logged into the system', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('Admin', 'CREATE_CASE', 'Created high priority case for robbery', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('OFF001', 'LOGIN', 'Officer Rajesh Kumar logged into the system', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('OFF001', 'UPDATE_CASE', 'Updated status of case #3 to Under Investigation', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('Admin', 'ADD_OFFICER', 'Added new officer OFF006 to Homicide Division', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('Admin', 'UPDATE_CASE', 'Closed case #5 successfully', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('Admin', 'EXPORT_REPORT', 'Exported monthly crime statistics report', NOW());
