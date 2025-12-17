# HPMS Sample Accounts

## Default Admin Account

The system comes with a default admin account that is automatically created:

**Admin Account:**
- **Username:** `admin`
- **Password:** `admin123`
- **Role:** ADMIN
- **Access:** Full system access - can manage all users, view audit logs, access all modules

## How to Create Additional Test Accounts

### Option 1: Via Admin Panel (Recommended)

1. Login with admin account (`admin` / `admin123`)
2. Navigate to **Administration** → **User Accounts** tab
3. Click **Add User** button
4. Fill in:
   - Username: (e.g., `doctor1`, `nurse1`, `staff1`)
   - Password: (choose a password)
   - Role: Select from dropdown (DOCTOR, NURSE, STAFF, CASHIER, PATIENT)
5. Click **Register**

### Option 2: Via Database (Direct SQL)

You can insert users directly into the database:

```sql
USE hpms_db;

-- Create a Doctor account
INSERT INTO users (username, password, salt, role, display_password) 
VALUES ('doctor1', 
        '[hashed_password]', 
        '[salt]', 
        'DOCTOR', 
        'doctor123');

-- Create a Nurse account
INSERT INTO users (username, password, salt, role, display_password) 
VALUES ('nurse1', 
        '[hashed_password]', 
        '[salt]', 
        'NURSE', 
        'nurse123');

-- Create a Staff account
INSERT INTO users (username, password, salt, role, display_password) 
VALUES ('staff1', 
        '[hashed_password]', 
        '[salt]', 
        'STAFF', 
        'staff123');

-- Create a Cashier account
INSERT INTO users (username, password, salt, role, display_password) 
VALUES ('cashier1', 
        '[hashed_password]', 
        '[salt]', 
        'CASHIER', 
        'cashier123');
```

**Note:** For direct database insertion, you need to hash the password using the same method as the system. It's easier to use the Admin Panel.

## Sample Test Accounts (Recommended)

Here are some sample accounts you can create for testing:

### Doctor Account
- **Username:** `doctor1`
- **Password:** `doctor123`
- **Role:** DOCTOR
- **Access:** View assigned patients, add diagnoses/prescriptions, manage appointments

### Nurse Account
- **Username:** `nurse1`
- **Password:** `nurse123`
- **Role:** NURSE
- **Access:** Patient registration, room management, basic patient care

### Staff Account
- **Username:** `staff1`
- **Password:** `staff123`
- **Role:** STAFF
- **Access:** Patient registration, billing, appointments, lab requests

### Cashier Account
- **Username:** `cashier1`
- **Password:** `cashier123`
- **Role:** CASHIER
- **Access:** Billing, payment processing, financial reports

### Patient Account
- **Username:** `patient1` (or use patient ID like `P0001`)
- **Password:** `patient123`
- **Role:** PATIENT
- **Access:** View own medical records, lab results, bills, appointments

**Note:** Patient accounts are usually auto-created when staff registers a patient. The patient ID becomes the username.

## Quick Setup Script

To quickly create test accounts, you can use this Java code snippet:

```java
// Run this in a test class or main method
hpms.auth.AuthService.seedAdmin(); // Creates admin account
hpms.auth.AuthService.login("admin", "admin123");

// Create test accounts
hpms.auth.AuthService.register("doctor1", "doctor123", "DOCTOR");
hpms.auth.AuthService.register("nurse1", "nurse123", "NURSE");
hpms.auth.AuthService.register("staff1", "staff123", "STAFF");
hpms.auth.AuthService.register("cashier1", "cashier123", "CASHIER");
```

## Account Roles and Permissions

### ADMIN
- Full system access
- User management
- Audit logs access
- System settings
- All other role permissions

### DOCTOR
- View assigned patients
- Add diagnoses
- Create prescriptions
- Manage appointments (accept/reject)
- View patient medical history
- Cannot access admin settings

### NURSE
- Register patients
- Update patient information
- Room management
- View patient records
- Basic patient care functions

### STAFF
- Register patients
- Handle billing
- Laboratory requests
- Appointments
- Admission/discharge
- Cannot access admin settings

### CASHIER
- Billing operations
- Payment processing
- Financial reports
- View patient billing history

### PATIENT
- View own medical records
- View lab results
- View billing history
- View visit history
- View appointments
- **Cannot:** Register themselves, modify records, or access other patients' data

## First Time Setup

1. **Start MySQL** (via XAMPP Control Panel)
2. **Run database setup:** `scripts\setup_database.bat`
3. **Start the application:** `scripts\run_hpms.bat`
4. **Login with admin:** 
   - Username: `admin`
   - Password: `admin123`
5. **Create additional test accounts** via Administration panel

## Security Note

⚠️ **Important:** Change the default admin password in production!

The default `admin123` password is for development/testing only. In a production environment, you should:

1. Login as admin
2. Go to Administration → User Accounts
3. Change the admin password to a strong password
4. Create individual accounts for each staff member
5. Never share admin credentials

## Troubleshooting

### "Invalid credentials" error
- Make sure you're using the correct username and password
- Check if the account exists in the database
- Verify MySQL is running

### "Only admin can register" error
- You must login as admin first to create other accounts
- Use `admin` / `admin123` to login

### Account not found
- Run `setup_database.bat` to create the database
- The admin account is created automatically when the database is initialized

