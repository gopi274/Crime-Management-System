USE crime_management;
INSERT INTO cases (case_number, fir_id, case_title, priority, status, created_at) VALUES
('MC-1', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('MC-2', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('MC-3', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('MC-4', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('MC-5', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('MC-6', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('MC-7', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('MC-8', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('MC-9', 1, 'mock', 'Low', 'Closed', DATE_SUB(NOW(), INTERVAL 1 MONTH));
