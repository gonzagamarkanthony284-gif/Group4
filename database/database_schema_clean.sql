-- HPMS Database Schema - Updated with FRONT_DESK Role
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
    ct_scan_file_path VARCHAR(500),
    ct_scan_status VARCHAR(50),
    ct_scan_summary TEXT,
    mri_file_path VARCHAR(500),
    mri_status VARCHAR(50),
    mri_summary TEXT,
    ultrasound_file_path VARCHAR(500),
    ultrasound_status VARCHAR(50),
    ultrasound_summary TEXT,
    ecg_file_path VARCHAR(500),
    ecg_status VARCHAR(50),
    ecg_summary TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_department (department),
    INDEX idx_role (role),
    INDEX idx_active (is_active)
);

-- Appointments table
CREATE TABLE appointments (
    id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20) NOT NULL,
    appointment_datetime DATETIME NOT NULL,
    department VARCHAR(100),
    consultation_type ENUM('FIRST_TIME', 'FOLLOW_UP', 'EMERGENCY', 'REFERRAL') DEFAULT 'FIRST_TIME',
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED') DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_staff (staff_id),
    INDEX idx_datetime (appointment_datetime),
    INDEX idx_status (status)
);

-- Medical Records table
CREATE TABLE medical_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20) NOT NULL,
    record_type ENUM('CONSULTATION', 'DIAGNOSIS', 'TREATMENT', 'PRESCRIPTION', 'LAB_RESULT', 'IMAGING', 'VITAL_SIGNS') NOT NULL,
    title VARCHAR(200),
    content TEXT NOT NULL,
    diagnosis TEXT,
    treatment TEXT,
    prescription TEXT,
    follow_up_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_staff (staff_id),
    INDEX idx_type (record_type),
    INDEX idx_created (created_at)
);

-- Prescriptions table
CREATE TABLE prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20) NOT NULL,
    medication_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),
    instructions TEXT,
    prescribed_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_staff (staff_id),
    INDEX idx_medication (medication_name),
    INDEX idx_active (is_active)
);

-- Lab Tests table
CREATE TABLE lab_tests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20) NOT NULL,
    test_name VARCHAR(200) NOT NULL,
    test_type ENUM('BLOOD', 'URINE', 'STOOL', 'IMAGING', 'OTHER') NOT NULL,
    description TEXT,
    ordered_date DATE NOT NULL,
    result_date DATE,
    result_text TEXT,
    result_file_path VARCHAR(500),
    status ENUM('ORDERED', 'PROCESSING', 'COMPLETED', 'CANCELLED') DEFAULT 'ORDERED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    INDEX idx_patient (patient_id),
    INDEX idx_staff (staff_id),
    INDEX idx_type (test_type),
    INDEX idx_status (status)
);

-- Billing table
CREATE TABLE billing (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20),
    billing_type ENUM('CONSULTATION', 'LAB_TEST', 'PRESCRIPTION', 'PROCEDURE', 'ROOM', 'OTHER') NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    billing_date DATE NOT NULL,
    status ENUM('PENDING', 'PAID', 'CANCELLED') DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id),
    INDEX idx_staff (staff_id),
    INDEX idx_type (billing_type),
    INDEX idx_status (status),
    INDEX idx_date (billing_date)
);

-- Status History table
CREATE TABLE status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    staff_id VARCHAR(20) NOT NULL,
    status VARCHAR(100) NOT NULL,
    notes TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_patient (patient_id),
    INDEX idx_staff (staff_id)
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

-- Insert default admin user
INSERT INTO users (username, password, salt, role, display_password) VALUES 
('admin', 'hashed_password_here', 'salt_here', 'ADMIN', 'admin123');

-- Insert sample data for testing
INSERT INTO patients (id, name, age, gender, contact, address, chief_complaint) VALUES 
('P1001', 'John Doe', 35, 'MALE', '555-0101', '123 Main St', 'Fever and headache'),
('P1002', 'Jane Smith', 28, 'FEMALE', '555-0102', '456 Oak Ave', 'Chest pain'),
('P1003', 'Robert Johnson', 45, 'MALE', '555-0103', '789 Pine Rd', 'Back pain');

INSERT INTO staff (id, name, role, department, phone, email, license_number, specialty) VALUES 
('S1001', 'Dr. Alice Wilson', 'DOCTOR', 'Cardiology', '555-1001', 'alice@hpms.com', 'DOC001', 'Cardiology'),
('S1002', 'Nurse Sarah Brown', 'NURSE', 'Emergency', '555-1002', 'sarah@hpms.com', 'NUR001', 'Emergency Care'),
('S1003', 'Tom Davis', 'CASHIER', 'Billing', '555-1003', 'tom@hpms.com', 'CAS001', 'Billing'),
('S1004', 'Lisa Anderson', 'FRONT_DESK', 'Reception', '555-1004', 'lisa@hpms.com', 'FRD001', 'Front Desk Operations');

-- Insert sample appointments
INSERT INTO appointments (id, patient_id, staff_id, appointment_datetime, department, consultation_type, status) VALUES 
('A1001', 'P1001', 'S1001', '2024-01-15 10:00:00', 'Cardiology', 'FIRST_TIME', 'SCHEDULED'),
('A1002', 'P1002', 'S1001', '2024-01-15 14:00:00', 'Cardiology', 'FOLLOW_UP', 'SCHEDULED'),
('A1003', 'P1003', 'S1002', '2024-01-16 09:00:00', 'Emergency', 'EMERGENCY', 'SCHEDULED');

-- Insert sample medical records
INSERT INTO medical_records (patient_id, staff_id, record_type, title, content, diagnosis, treatment) VALUES 
('P1001', 'S1001', 'CONSULTATION', 'Initial Consultation', 'Patient presents with fever and headache', 'Viral infection', 'Prescribed antipyretics and rest'),
('P1002', 'S1001', 'CONSULTATION', 'Cardiac Evaluation', 'Patient reports chest pain', 'Angina', 'Prescribed nitroglycerin and cardiac monitoring'),
('P1003', 'S1002', 'CONSULTATION', 'Emergency Assessment', 'Patient with acute back pain', 'Muscle strain', 'Prescribed pain medication and physical therapy');

-- Insert sample prescriptions
INSERT INTO prescriptions (patient_id, staff_id, medication_name, dosage, frequency, duration, instructions, prescribed_date) VALUES 
('P1001', 'S1001', 'Paracetamol', '500mg', 'Every 6 hours', '5 days', 'Take with food', '2024-01-15'),
('P1002', 'S1001', 'Nitroglycerin', '0.4mg', 'As needed', '30 days', 'Place under tongue for chest pain', '2024-01-15'),
('P1003', 'S1002', 'Ibuprofen', '400mg', 'Every 8 hours', '7 days', 'Take with food', '2024-01-16');

-- Insert sample lab tests
INSERT INTO lab_tests (patient_id, staff_id, test_name, test_type, description, ordered_date, status) VALUES 
('P1001', 'S1001', 'Complete Blood Count', 'BLOOD', 'Routine blood work', '2024-01-15', 'ORDERED'),
('P1002', 'S1001', 'ECG', 'IMAGING', 'Electrocardiogram', '2024-01-15', 'ORDERED'),
('P1003', 'S1002', 'X-Ray', 'IMAGING', 'Chest X-ray', '2024-01-16', 'ORDERED');

-- Insert sample billing records
INSERT INTO billing (patient_id, staff_id, billing_type, description, amount, billing_date, status) VALUES 
('P1001', 'S1001', 'CONSULTATION', 'Cardiology consultation fee', '150.00', '2024-01-15', 'PENDING'),
('P1002', 'S1001', 'CONSULTATION', 'Cardiology follow-up fee', '100.00', '2024-01-15', 'PENDING'),
('P1003', 'S1002', 'CONSULTATION', 'Emergency consultation fee', '200.00', '2024-01-16', 'PENDING');

-- Insert sample status history
INSERT INTO status_history (patient_id, staff_id, status, notes) VALUES 
('P1001', 'S1001', 'Under observation', 'Patient being monitored for fever'),
('P1002', 'S1001', 'Stable', 'Patient condition stable'),
('P1003', 'S1002', 'Discharged', 'Patient discharged with medication');

-- Insert sample staff notes
INSERT INTO staff_notes (patient_id, staff_id, note) VALUES 
('P1001', 'S1001', 'Patient responded well to treatment'),
('P1002', 'S1001', 'Patient advised to follow up in one week'),
('P1003', 'S1002', 'Patient education provided');

-- Insert sample critical alerts
INSERT INTO critical_alerts (patient_id, alert_message, created_at, is_resolved) VALUES 
('P1001', 'Patient temperature above 38°C', '2024-01-15 10:00:00', FALSE),
('P1002', 'Patient requires immediate cardiac evaluation', '2024-01-15 14:00:00', FALSE),
('P1003', 'Patient reports severe pain', '2024-01-16 09:00:00', FALSE);

-- Database setup complete
SELECT 'HPMS Database created successfully!' as status;
