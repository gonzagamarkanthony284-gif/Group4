# HPMS Project Structure

## Directory Organization

The project has been organized into logical packages and directories for better maintainability.

### Root Directory Structure

```
HPMS/
├── bin/                    # Compiled Java classes
├── database/              # Database schema and scripts
│   └── database_schema.sql
├── docs/                  # Documentation files
│   ├── IMPLEMENTATION_SUMMARY.md
│   ├── PROJECT_STRUCTURE.md (this file)
│   ├── README.md
│   └── README_DATABASE.md
├── lib/                   # External libraries (JAR files)
│   └── mysql-connector-j-9.5.0.jar
├── out/                   # Alternative output directory
├── scripts/               # Batch scripts for running the application
│   ├── launch_hpms.bat
│   ├── quick_start.bat
│   ├── run_hpms.bat
│   ├── setup_database.bat
│   └── start_hpms.bat
└── src/                   # Java source code
    └── hpms/
        ├── app/           # Application entry points
        ├── auth/           # Authentication services
        ├── model/          # Data models
        ├── service/        # Business logic services
        ├── test/           # Test classes
        ├── ui/             # User interface components
        └── util/           # Utility classes
```

### Source Code Package Structure

#### `hpms.app`
Application entry points:
- `Launcher.java` - Main application launcher
- `HPMSApp.java` - Application class

#### `hpms.auth`
Authentication and authorization:
- `AuthService.java` - In-memory authentication service
- `AuthServiceDB.java` - Database-backed authentication service
- `AuthSession.java` - Session management
- `PasswordResetService.java` - Password reset with email verification
- `PasswordUtil.java` - Password hashing utilities
- `RoleGuard.java` - Role-based access control
- `User.java` - User model
- `Verifier.java` - System verification

#### `hpms.model`
Data models (entities):
- `Appointment.java`
- `Bill.java`, `BillItem.java`
- `Communication.java`
- `Discharge.java`
- `FileAttachment.java`
- `LabResult.java`, `LabTestRequest.java`, `LabTestType.java`
- `Medicine.java`
- `Patient.java`, `PatientStatus.java`, `PatientVisit.java`
- `Prescription.java`
- `Room.java`, `RoomStatus.java`
- `Service.java`
- `Staff.java`, `StaffNote.java`, `StaffRole.java`
- `StatusHistoryEntry.java`
- `UserRole.java`
- Enums: `ConsultationType.java`, `Gender.java`, `PaymentMethod.java`

#### `hpms.service`
Business logic services:
- `AppointmentService.java`
- `AttachmentService.java`
- `BillingService.java`
- `CommunicationService.java`
- `DischargeService.java`
- `InventoryService.java`
- `LabService.java`
- `MedicineService.java`
- `PatientService.java`
- `PatientStatusService.java`
- `PrescriptionService.java`
- `ReportService.java`
- `RoomService.java`
- `ServiceService.java`
- `StaffService.java`
- `VisitService.java`

#### `hpms.test`
Test classes:
- `AppointmentTest.java`
- `AuthBackupTest.java`
- `ChangePasswordNoOldTest.java`
- `DatabaseConnectionTest.java`
- `GuiSmokeTest.java`
- `OutpatientRulesTest.java`
- `PatientAttachmentsTest.java`
- `PatientClinicalUploadTest.java`
- `PatientEditTypeTest.java`
- `PatientPasswordTest.java`
- `StaffServiceTest.java`
- `TestSummaryExtractorTest.java`

#### `hpms.ui`
User interface components:

**Main UI:**
- `AdminGUI.java` - Admin interface
- `MainGUI.java` - Main application window
- `LoginWindow.java` - Login window (legacy, delegates to login package)

**Sub-packages:**

- `hpms.ui.cashier/` - Cashier dashboard and panels
- `hpms.ui.clinical/` - Clinical system components
- `hpms.ui.components/` - Reusable UI components
- `hpms.ui.dialogs/` - Dialog windows
  - `PatientDetailsDialog.java`
  - `PatientDetailsDialogNew.java`
- `hpms.ui.doctor/` - Doctor interface panels
- `hpms.ui.factory/` - UI factory classes
- `hpms.ui.login/` - Login and authentication UI
  - `DoctorSignUpWindow.java`
  - `LoginWindow.java` (active)
  - `ServicesWindow.java`
  - `UserLoginWindow.java`
- `hpms.ui.nurse/` - Nurse dashboard
- `hpms.ui.panels/` - Main content panels
- `hpms.ui.patient/` - Patient interface
  - `PatientDashboardWindow.java` (active)
- `hpms.ui.staff/` - Staff management UI

#### `hpms.util`
Utility classes:
- `AuditLogService.java` - Audit logging to database
- `BackupUtil.java` - Data backup utilities
- `CommandConsole.java` - Console utilities
- `DataStore.java` - In-memory data storage
- `DBConnection.java` - Database connection management
- `EmailService.java` - Email sending service (JavaMail)
- `IDGenerator.java` - ID generation utilities
- `LogManager.java` - Logging manager
- `TestSummaryExtractor.java` - Test utilities
- `Validators.java` - Input validation utilities

## File Organization Rules

1. **Documentation** → `docs/` directory
2. **Database Scripts** → `database/` directory
3. **Batch Scripts** → `scripts/` directory
4. **Source Code** → `src/hpms/` with appropriate sub-packages
5. **UI Dialogs** → `hpms.ui.dialogs` package
6. **Tests** → `hpms.test` package (consolidated from `hpms.tests`)

## Removed Files

The following unnecessary files have been removed:
- `doctor_signup.html` - Web-based signup (not used in Swing app)
- `user_login.html` - Web-based login (not used in Swing app)
- `signup_script.js`, `user_login_script.js` - JavaScript files
- `signup_style.css`, `user_login_style.css` - CSS files
- `PATIENT_FORM_REPLACEMENT.java` - Temporary replacement code
- `replace_method.ps1` - PowerShell script
- `src/hpms/ui/PatientDashboardWindow.java` - Duplicate (kept version in `hpms.ui.patient`)
- `src/hpms/ui/panels/PatientsPanel.java.backup` - Backup file
- `src/javacode/` - Empty directory

## Notes

- The `hpms.ui.LoginWindow` class delegates to `hpms.ui.login.LoginWindow` for the actual login functionality
- All dialogs are now organized in the `hpms.ui.dialogs` package
- Test files have been consolidated into a single `hpms.test` package
- Batch scripts have been updated to reference the new `database/` directory location

