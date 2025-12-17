# HPMS - Hospital Patient Management System

## Overview
A comprehensive Hospital Patient Management System built with Java Swing and MySQL database.

## Features
- Patient Management
- Staff Management
- Appointment Scheduling
- Billing System
- Room Management
- Lab Test Management
- Prescription Management
- Doctor Schedules
- Reports and Analytics

## Technology Stack
- **Language:** Java
- **GUI Framework:** Java Swing
- **Database:** MySQL (MariaDB compatible)
- **JDBC Driver:** MySQL Connector/J 9.5.0
- **Build Tool:** javac (manual compilation)

## Database Setup

### Prerequisites
1. XAMPP installed (for MySQL/MariaDB)
2. Java JDK 17 or higher
3. MySQL JDBC Driver (included in `lib/` folder)

### Quick Start

1. **Start MySQL Server**
   - Open XAMPP Control Panel
   - Start MySQL service

2. **Create Database**
   ```bash
   cd C:\xampp\htdocs\HPMS
   setup_database.bat
   ```
   Or manually:
   ```bash
   mysql -u root < database_schema.sql
   ```

3. **Compile and Run**
   ```bash
   javac -encoding UTF-8 -cp "lib\*;src" -d bin src\hpms\app\Launcher.java
   java -cp "lib\*;bin" hpms.app.Launcher
   ```

4. **Login**
   - Username: `admin`
   - Password: `admin123`

## Project Structure
```
HPMS/
├── src/
│   └── hpms/
│       ├── app/          # Application entry points
│       ├── auth/         # Authentication services
│       ├── model/        # Data models
│       ├── service/      # Business logic
│       ├── ui/           # User interface components
│       └── util/         # Utilities
├── lib/                  # JDBC driver
├── bin/                  # Compiled classes
├── database_schema.sql   # Database schema
└── setup_database.bat    # Database setup script
```

## Database Configuration

The database connection is configured in `src/hpms/config/DatabaseConfig.java`:
```java
JDBC_URL: jdbc:mysql://localhost:3306/hpms_db?useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000
JDBC_USER: root
JDBC_PASSWORD: (empty)
```

## Database Schema

The system includes 28 tables:
- Users, Patients, Staff
- Appointments, Bills, Rooms
- Prescriptions, Medicines
- Lab Tests and Results
- Communications, Discharges
- And more...

## Access Methods

### 1. Java Application
Run the compiled application to access the GUI interface.

### 2. phpMyAdmin
Access the database through web interface:
- URL: http://localhost/phpmyadmin
- Username: `root`
- Password: (leave empty)

### 3. MySQL Command Line
```bash
C:\xampp\mysql\bin\mysql.exe -u root hpms_db
```

## Documentation

- `README_DATABASE.md` - Database migration guide
- `DATABASE_MIGRATION_GUIDE.md` - Detailed migration instructions
- `DATABASE_CONNECTIVITY_REPORT.md` - Connectivity analysis
- `DATABASE_SETUP_SUMMARY.md` - Setup overview

## Development Status

### Completed ✅
- Database schema and infrastructure
- Database connection utility (DBConnection.java)
- Authentication service (AuthService)
- All data models
- UI components
- In-memory data storage (DataStore)

### In Progress 🚧
- Migration from in-memory to database storage
- Database-backed services (see AuthServiceDB.java example)

## Contributing

This is an educational/personal project. Feel free to fork and modify.

## License

Educational/Personal Use

## Contact

GitHub: [@gonzagamarkanthony284-gif](https://github.com/gonzagamarkanthony284-gif)

---

**Note:** This system uses in-memory storage by default. Database integration is available but requires service migration. See documentation for details.
