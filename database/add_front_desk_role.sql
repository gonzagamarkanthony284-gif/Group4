-- MySQL Schema Update for FRONT_DESK Role
-- Add FRONT_DESK role to staff_role enum

-- Update staff table to include FRONT_DESK role
ALTER TABLE staff 
MODIFY staff_role ENUM('DOCTOR','NURSE','CASHIER','FRONT_DESK','ADMIN') NOT NULL;

-- Update users table to include FRONT_DESK role  
ALTER TABLE users
MODIFY role ENUM('ADMIN','DOCTOR','NURSE','CASHIER','FRONT_DESK','PATIENT','STAFF') NOT NULL;

-- Optional: Add a sample front desk staff member for testing
INSERT INTO staff (
    staff_id, 
    name, 
    staff_role, 
    department, 
    phone, 
    email, 
    license_number, 
    specialization, 
    qualifications, 
    notes, 
    is_active, 
    created_at
) VALUES (
    'S2001',
    'Sarah Johnson',
    'FRONT_DESK',
    'Reception',
    '555-0123',
    'sarah.johnson@hospital.com',
    NULL,
    NULL,
    'Front Desk Operations',
    'Sample front desk staff for testing',
    1,
    NOW()
);

-- Optional: Create corresponding user account for the front desk staff
INSERT INTO users (
    username,
    password_hash,
    salt,
    role,
    display_password
) VALUES (
    'sarah.johnson',
    -- This would be the actual hashed password - replace with real hash
    'dummy_hash_replace_with_real_hash',
    -- This would be the actual salt - replace with real salt  
    'dummy_salt_replace_with_real_salt',
    'FRONT_DESK',
    'temp123'
);

-- Verify the changes
SELECT * FROM staff WHERE staff_role = 'FRONT_DESK';
SELECT * FROM users WHERE role = 'FRONT_DESK';
