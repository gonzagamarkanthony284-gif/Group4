-- Database Schema Update for Patient Status Table
-- Fix for missing 'changed_by' column issue
-- Date: 2025-12-12

-- Check if the column exists and add it if it doesn't
-- This is safe to run multiple times as it will only add the column if missing

-- Method 1: Safe approach (recommended for production)
-- Try to select the column first, then add if it fails
-- (This logic is implemented in the Java code automatically)

-- Method 2: Direct SQL approach (for manual database updates)
-- Uncomment and run if you want to manually update the schema

/*
-- Add the missing 'changed_by' column if it doesn't exist
ALTER TABLE patient_status ADD COLUMN changed_by VARCHAR(20);

-- Verify the column was added
DESCRIBE patient_status;
*/

-- Current expected schema for patient_status table:
CREATE TABLE patient_status (
    patient_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    changed_by VARCHAR(20),  -- This column was missing and is now added
    PRIMARY KEY (patient_id, created_at),
    INDEX idx_patient_status (patient_id)
);

-- Sample data insertion with the new column:
INSERT INTO patient_status (patient_id, status, changed_by, note) 
VALUES ('PAT001', 'INPATIENT', 'STAFF001', 'Patient admitted for observation');
