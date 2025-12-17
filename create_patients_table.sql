-- Create patients table for HPMS database
-- This table stores patient information and integrates with the existing system

CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER', 'LGBTQ_PLUS') NOT NULL,
    contact VARCHAR(100) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    birthday VARCHAR(50) NOT NULL,
    patient_type ENUM('INPATIENT', 'OUTPATIENT', 'EMERGENCY') NOT NULL,
    registration_type VARCHAR(100) DEFAULT 'Walk-in Patient',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_contact (contact),
    INDEX idx_patient_type (patient_type),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
