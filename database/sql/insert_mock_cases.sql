USE crime_management;
INSERT INTO cases (case_number, fir_id, case_title, case_description, priority, status, assigned_officer_id, created_at) VALUES
('MOCK-001', 1, 'Mock Case 1', 'Description', 'Low', 'Closed', 1, DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('MOCK-002', 1, 'Mock Case 2', 'Description', 'Medium', 'Closed', 1, DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('MOCK-003', 2, 'Mock Case 3', 'Description', 'High', 'Solved', 2, DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('MOCK-004', 3, 'Mock Case 4', 'Description', 'Critical', 'Under Investigation', 3, DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('MOCK-005', 3, 'Mock Case 5', 'Description', 'Medium', 'Closed', 2, DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('MOCK-006', 4, 'Mock Case 6', 'Description', 'High', 'Open', 1, DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('MOCK-007', 4, 'Mock Case 7', 'Description', 'Medium', 'Solved', 2, DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('MOCK-008', 4, 'Mock Case 8', 'Description', 'Low', 'Closed', 3, DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('MOCK-009', 5, 'Mock Case 9', 'Description', 'High', 'Open', 4, DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('MOCK-010', 5, 'Mock Case 10', 'Description', 'Critical', 'Under Investigation', 1, DATE_SUB(NOW(), INTERVAL 1 MONTH));
