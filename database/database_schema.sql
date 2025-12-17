-- HPMS Database Schema
-- Drop database if exists and create fresh
DROP DATABASE IF EXISTS hpms_db;
CREATE DATABASE hpms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hpms_db;

-- Users table for authentication
CREATE TABLE users (
    username VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'DOCTOR', 'NURSE', 'CASHIER', 'FRONT_DESK', 'PATIENT', 'STAFF') NOT NULL,
    display_password VARCHAR(100),
    status ENUM('ACTIVE', 'DEACTIVATED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Patients table
CREATE TABLE patients (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    age INT NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    contact VARCHAR(50),
    address TEXT,
    allergies TEXT,
    medications TEXT,
    past_medical_history TEXT,
    surgical_history TEXT,
    family_history TEXT,
    smoking_status VARCHAR(50),
    alcohol_use VARCHAR(100),
    drug_use VARCHAR(100),
    occupation VARCHAR(100),
    height_cm DOUBLE,
    weight_kg DOUBLE,
    blood_pressure VARCHAR(20),
    registration_type VARCHAR(100),
    incident_time VARCHAR(50),
    brought_by VARCHAR(100),
    initial_bp VARCHAR(20),
    initial_hr VARCHAR(20),
    initial_spo2 VARCHAR(20),
    chief_complaint TEXT,
    xray_file_path VARCHAR(500),
    xray_status VARCHAR(50),
    xray_summary TEXT,
    stool_file_path VARCHAR(500),
    stool_status VARCHAR(50),
    stool_summary TEXT,
    urine_file_path VARCHAR(500),
    urine_status VARCHAR(50),
    urine_summary TEXT,
    blood_file_path VARCHAR(500),
    blood_status VARCHAR(50),
    blood_summary TEXT,
    insurance_provider VARCHAR(200),
    insurance_id VARCHAR(100),
    insurance_group VARCHAR(100),
    policy_holder_name VARCHAR(200),
    policy_holder_dob VARCHAR(20),
    policy_relationship VARCHAR(50),
    secondary_insurance VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_contact (contact),
    INDEX idx_active (is_active)
);

-- Staff table (created early because other tables reference it)
CREATE TABLE staff (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    role ENUM('DOCTOR', 'NURSE', 'ADMIN', 'CASHIER', 'FRONT_DESK', 'STAFF') NOT NULL,
    department VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(200),
    license_number VARCHAR(100),
    specialty VARCHAR(100),
    sub_specialization VARCHAR(100),
    nursing_field VARCHAR(100),
    years_experience INT,
    years_practice INT,
    years_of_work INT,
    clinic_schedule_str TEXT,
    schedule_start_date DATETIME,
    schedule_end_date DATETIME,
    qualifications TEXT,
    certifications TEXT,
    bio TEXT,
    employee_id VARCHAR(50),
    status VARCHAR(50) DEFAULT 'Active',
    photo_path VARCHAR(500),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_role (role)
);

-- Patient attachments (for file paths)
CREATE TABLE patient_attachments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Patient progress notes
CREATE TABLE patient_progress_notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    note TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Patient lab results
CREATE TABLE patient_lab_results_text (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    result TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Patient radiology reports
CREATE TABLE patient_radiology_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    report TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Patient diagnoses
CREATE TABLE patient_diagnoses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    diagnosis TEXT NOT NULL,
    diagnosed_by VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (diagnosed_by) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id),
    INDEX idx_date (created_at)
);

-- Patient visits / arrival history
-- Each time a patient arrives, a NEW record is created here
-- The first arrival data remains in the patients table as a reference
CREATE TABLE patient_visits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    registration_type VARCHAR(100) NOT NULL,
    incident_time VARCHAR(50),
    brought_by VARCHAR(100),
    initial_bp VARCHAR(20),
    initial_hr VARCHAR(20),
    initial_spo2 VARCHAR(20),
    chief_complaint TEXT,
    attending_doctor VARCHAR(20),
    diagnosis TEXT,
    treatment_plan TEXT,
    notes TEXT,
    visit_status VARCHAR(50) DEFAULT 'Active',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (attending_doctor) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id),
    INDEX idx_visit_date (visit_date),
    INDEX idx_doctor (attending_doctor)
);

-- Patient treatment plans
CREATE TABLE patient_treatment_plans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    treatment_plan TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Patient discharge summaries
CREATE TABLE patient_discharge_summaries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    summary TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Appointments table
CREATE TABLE appointments (
    id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20) NOT NULL,
    date_time DATETIME NOT NULL,
    department VARCHAR(100),
    consultation_type ENUM('FOLLOW_UP', 'NEW_PATIENT', 'EMERGENCY', 'ROUTINE') DEFAULT 'FOLLOW_UP',
    notes TEXT,
    diagnosis TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    outcome VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_date (date_time),
    INDEX idx_patient (patient_id),
    INDEX idx_staff (staff_id)
);

-- Bills table
CREATE TABLE bills (
    id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    total DOUBLE NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    payment_method ENUM('CASH', 'CARD', 'INSURANCE', 'CHECK', 'ONLINE') DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id)
);

-- Bill items table
CREATE TABLE bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DOUBLE NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
);

-- Rooms table
CREATE TABLE rooms (
    id VARCHAR(20) PRIMARY KEY,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'RESERVED') NOT NULL,
    occupant_patient_id VARCHAR(20),
    FOREIGN KEY (occupant_patient_id) REFERENCES patients(id) ON DELETE SET NULL,
    INDEX idx_status (status)
);

-- Medicine functionality removed

-- Prescriptions table
CREATE TABLE prescriptions (
    id VARCHAR(20) PRIMARY KEY,
    appointment_id VARCHAR(20),
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    medicine_id VARCHAR(20) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(200),
    duration_days INT,
    instructions TEXT,
    indication TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    prescribed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES staff(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id)
);

-- Lab Test Types table
CREATE TABLE lab_test_types (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    normal_range VARCHAR(200),
    unit VARCHAR(50),
    category VARCHAR(100),
    price DOUBLE
);

-- Lab Test Requests table
CREATE TABLE lab_test_requests (
    id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    test_type_id VARCHAR(20) NOT NULL,
    requested_by VARCHAR(20),
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    priority ENUM('ROUTINE', 'URGENT', 'STAT') DEFAULT 'ROUTINE',
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (test_type_id) REFERENCES lab_test_types(id) ON DELETE CASCADE,
    FOREIGN KEY (requested_by) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id),
    INDEX idx_status (status)
);

-- Lab Results table
CREATE TABLE lab_results (
    id VARCHAR(20) PRIMARY KEY,
    request_id VARCHAR(20) NOT NULL,
    result_value VARCHAR(500),
    result_text TEXT,
    is_abnormal BOOLEAN DEFAULT FALSE,
    completed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    technician_id VARCHAR(20),
    verified_by VARCHAR(20),
    FOREIGN KEY (request_id) REFERENCES lab_test_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (technician_id) REFERENCES staff(id) ON DELETE SET NULL,
    FOREIGN KEY (verified_by) REFERENCES staff(id) ON DELETE SET NULL
);

-- Doctor Schedules table
CREATE TABLE doctor_schedules (
    id VARCHAR(20) PRIMARY KEY,
    doctor_id VARCHAR(20) NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    max_appointments INT DEFAULT 20,
    FOREIGN KEY (doctor_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_doctor (doctor_id),
    INDEX idx_day (day_of_week)
);

-- Patient Status table
CREATE TABLE patient_status (
    patient_id VARCHAR(20) PRIMARY KEY,
    status ENUM('ACTIVE', 'ADMITTED', 'DISCHARGED', 'CRITICAL', 'STABLE', 'DECEASED') NOT NULL,
    room_id VARCHAR(20),
    admission_date TIMESTAMP NULL,
    discharge_date TIMESTAMP NULL,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- Status History table
CREATE TABLE status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(20),
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id)
);

-- Staff Notes table
CREATE TABLE staff_notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20) NOT NULL,
    note TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id)
);

-- Critical Alerts table
CREATE TABLE critical_alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    alert_message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_resolved BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_resolved (is_resolved)
);

-- Discharges table
CREATE TABLE discharges (
    id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    discharge_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    discharge_summary TEXT,
    follow_up_instructions TEXT,
    medications_prescribed TEXT,
    discharge_condition VARCHAR(200),
    discharged_by VARCHAR(20),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (discharged_by) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id)
);

-- Patient File Attachments table (Medical Information / File Storage System)
CREATE TABLE patient_file_attachments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100),
    category VARCHAR(100),
    file_size BIGINT DEFAULT 0,
    mime_type VARCHAR(100),
    description TEXT,
    uploaded_by VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    file_hash VARCHAR(255),
    is_encrypted BOOLEAN DEFAULT FALSE,
    status ENUM('Active', 'Archived', 'Deleted') DEFAULT 'Active',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_uploaded_at (uploaded_at),
    INDEX idx_patient_category (patient_id, category)
);

-- Communications table
CREATE TABLE communications (
    id VARCHAR(20) PRIMARY KEY,
    sender_id VARCHAR(20) NOT NULL,
    recipient_id VARCHAR(20) NOT NULL,
    subject VARCHAR(500),
    message TEXT NOT NULL,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES staff(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_recipient (recipient_id),
    INDEX idx_read (is_read)
);

-- Activity Log table
CREATE TABLE activity_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    log_message TEXT NOT NULL,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_time (logged_at)
);

-- Password Resets table
CREATE TABLE password_resets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    reset_code VARCHAR(10) NOT NULL,
    email VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_username (username),
    INDEX idx_reset_code (reset_code),
    INDEX idx_expires_at (expires_at)
);

-- Audit Logs table (for detailed audit trail)
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(50),
    details TEXT,
    ip_address VARCHAR(50),
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_action (action),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_logged_at (logged_at)
);

-- Departments table (reference data)
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Patient Status table (for tracking patient status changes and history)
CREATE TABLE patient_status (
    patient_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(20),
    note TEXT,
    PRIMARY KEY (patient_id, created_at),
    INDEX idx_patient_status (patient_id)
);

-- Insert default departments
INSERT INTO departments (name) VALUES 
    ('Cardiology'),
    ('Neurology'),
    ('Orthopedics'),
    ('Pediatrics'),
    ('Oncology'),
    ('ER'),
    ('Admin'),
    ('Nursing'),
    ('Billing');

-- Create stored procedure for auto-incrementing custom IDs
DELIMITER //

CREATE PROCEDURE get_next_id(IN prefix VARCHAR(10), OUT next_id VARCHAR(20))
BEGIN
    DECLARE counter INT;
    SET counter = (SELECT COALESCE(MAX(CAST(SUBSTRING(id, 2) AS UNSIGNED)), 0) + 1
                   FROM (
                       SELECT id FROM patients WHERE id LIKE CONCAT(prefix, '%')
                       UNION ALL
                       SELECT id FROM staff WHERE id LIKE CONCAT(prefix, '%')
                       UNION ALL
                       SELECT id FROM appointments WHERE id LIKE CONCAT(prefix, '%')
                       UNION ALL
                       SELECT id FROM bills WHERE id LIKE CONCAT(prefix, '%')
                       UNION ALL
                       SELECT id FROM rooms WHERE id LIKE CONCAT(prefix, '%')
                   ) AS all_ids);
    SET next_id = CONCAT(prefix, LPAD(counter, 4, '0'));
END //

DELIMITER ;

-- Grant permissions (adjust as needed for your MySQL setup)
-- GRANT ALL PRIVILEGES ON hpms_db.* TO 'root'@'localhost';
-- FLUSH PRIVILEGES;

