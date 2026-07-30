USE crime_management;

-- Update case dates to spread across the last 6 months (October 2023 - March 2024 roughly)
UPDATE cases SET created_at = DATE_SUB(NOW(), INTERVAL 5 MOntH) WHERE case_id = 1;
UPDATE cases SET created_at = DATE_SUB(NOW(), INTERVAL 4 MOntH) WHERE case_id = 2;
UPDATE cases SET created_at = DATE_SUB(NOW(), INTERVAL 3 MOntH) WHERE case_id = 3;
UPDATE cases SET created_at = DATE_SUB(NOW(), INTERVAL 2 MOntH) WHERE case_id = 4;
UPDATE cases SET created_at = DATE_SUB(NOW(), INTERVAL 1 MOntH) WHERE case_id = 5;

-- Also spread FIR dates to maintain consistency
UPDATE firs SET created_at = DATE_SUB(NOW(), INTERVAL 5 MOntH) WHERE fir_id = 1;
UPDATE firs SET created_at = DATE_SUB(NOW(), INTERVAL 4 MOntH) WHERE fir_id = 2;
UPDATE firs SET created_at = DATE_SUB(NOW(), INTERVAL 3 MOntH) WHERE fir_id = 3;
UPDATE firs SET created_at = DATE_SUB(NOW(), INTERVAL 2 MOntH) WHERE fir_id = 4;
UPDATE firs SET created_at = DATE_SUB(NOW(), INTERVAL 1 MOntH) WHERE fir_id = 5;
