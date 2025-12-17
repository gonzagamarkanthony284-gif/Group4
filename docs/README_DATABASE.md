# Database Migration Quick Start

## What Was Done

I've set up the foundation for migrating your HPMS project from in-memory storage to MySQL database:

### ✅ Completed

1. **DBConnection.java** - Database connection utility class
   - Location: `src/hpms/util/DBConnection.java`
   - Connects to: `jdbc:mysql://localhost:3306/hpms_db`
   - User: `root`, Password: (empty - XAMPP default)

2. **database_schema.sql** - Complete database schema
   - 30+ tables covering all models
   - Foreign keys and indexes
   - Default departments seeded
   - Location: `database_schema.sql`

3. **AuthServiceDB.java** - Example database-backed service
   - Location: `src/hpms/auth/AuthServiceDB.java`
   - Shows how to convert from DataStore to SQL
   - Fully functional authentication with database

4. **setup_database.bat** - Automated setup script
   - Checks MySQL status
   - Runs schema script
   - Verifies installation

5. **DatabaseConnectionTest.java** - Connection verification
   - Location: `src/hpms/test/DatabaseConnectionTest.java`
   - Tests connection and basic operations

6. **VS Code Settings** - JDBC driver configuration
   - Location: `.vscode/settings.json`
   - Includes MySQL JDBC driver reference

### ⚠️ Still Needs Work

You need to:
1. Run the database setup script
2. Decide on migration approach (gradual vs complete)
3. Migrate remaining 14 service classes
4. Test each service thoroughly
5. Update UI code if needed

## Quick Start (5 Minutes)

### Step 1: Start MySQL
1. Open XAMPP Control Panel
2. Click **Start** next to MySQL
3. Wait for green "Running" status

### Step 2: Create Database
Run the setup script:
```cmd
cd C:\xampp\htdocs\HPMS
setup_database.bat
```

This will:
- Create `hpms_db` database
- Create all tables
- Insert default data

### Step 3: Test Connection
Compile and run the test:
```cmd
# Compile
javac -cp "lib\*;src" src\hpms\test\DatabaseConnectionTest.java -d bin

# Run
java -cp "lib\*;bin" hpms.test.DatabaseConnectionTest
```

Expected output:
```
✓ Database connection successful!
✓ Found 30+ tables
✓ Users table accessible
✓ Insert successful
```

### Step 4: Test Database Authentication
Try the database-backed authentication:
```java
// In your code or a test file:
AuthServiceDB.seedAdmin();
List<String> result = AuthServiceDB.login("admin", "admin123");
// Should return: ["Login successful"]
```

## Migration Options

### Option A: Gradual Migration (Recommended for Production)

**Pros:**
- Safer, test each service individually
- Keep existing functionality working
- Easy to rollback if issues found

**Cons:**
- Takes more time
- Temporarily maintain two codebases

**How:**
1. Keep original services (e.g., `AuthService.java`)
2. Create database versions (e.g., `AuthServiceDB.java`)
3. Test database version thoroughly
4. Switch to database version once tested
5. Remove old version

### Option B: Direct Replacement (Faster)

**Pros:**
- Faster completion
- Clean codebase immediately
- Forces complete migration

**Cons:**
- Risky, everything breaks until all done
- Harder to debug
- No fallback to old system

**How:**
1. Backup entire project
2. Modify all service files at once
3. Replace DataStore calls with SQL
4. Test entire application together
5. Fix bugs as they appear

## Example: Migrating a Service

Here's how to migrate `PatientService.java`:

### Before (DataStore):
```java
public static List<String> add(String name, String age, String gender, String contact, String address) {
    List<String> out = new ArrayList<>();
    String id = IDGenerator.next("P", DataStore.pCounter);
    Patient p = new Patient(id, name, Integer.parseInt(age), 
        Gender.valueOf(gender.toUpperCase()), contact, address, LocalDateTime.now());
    DataStore.patients.put(id, p);
    out.add("Patient created: " + id);
    return out;
}
```

### After (Database):
```java
public static List<String> add(String name, String age, String gender, String contact, String address) {
    List<String> out = new ArrayList<>();
    
    try (Connection conn = DBConnection.getConnection()) {
        // Generate next ID
        String id = IDGenerator.next("P", DataStore.pCounter); // Or use AUTO_INCREMENT
        
        // Insert patient
        String sql = "INSERT INTO patients (id, name, age, gender, contact, address, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setInt(3, Integer.parseInt(age));
            stmt.setString(4, gender.toUpperCase());
            stmt.setString(5, contact);
            stmt.setString(6, address);
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            
            out.add("Patient created: " + id);
        }
    } catch (SQLException e) {
        out.add("Error: " + e.getMessage());
        e.printStackTrace();
    }
    
    return out;
}
```

## Files You Need to Migrate

### Priority 1 (Core functionality):
- [ ] `src/hpms/service/PatientService.java`
- [ ] `src/hpms/service/StaffService.java`
- [ ] `src/hpms/service/AppointmentService.java`
- [ ] `src/hpms/service/BillingService.java`
- [ ] `src/hpms/service/RoomService.java`

### Priority 2 (Extended features):
- [ ] `src/hpms/service/PrescriptionService.java`
- [ ] `src/hpms/service/MedicineService.java`
- [ ] `src/hpms/service/LabService.java`
- [ ] `src/hpms/service/PatientStatusService.java`

### Priority 3 (Nice to have):
- [ ] `src/hpms/service/DoctorScheduleService.java`
- [ ] `src/hpms/service/DischargeService.java`
- [ ] `src/hpms/service/CommunicationService.java`
- [ ] `src/hpms/service/InventoryService.java`
- [ ] `src/hpms/service/ReportService.java`

## Common SQL Patterns

### SELECT by ID:
```java
String sql = "SELECT * FROM patients WHERE id = ?";
try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, patientId);
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
        // Build Patient object from ResultSet
        Patient p = new Patient(rs.getString("id"), rs.getString("name"), ...);
    }
}
```

### SELECT ALL:
```java
String sql = "SELECT * FROM patients";
List<Patient> patients = new ArrayList<>();
try (Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(sql)) {
    while (rs.next()) {
        patients.add(new Patient(rs.getString("id"), ...));
    }
}
```

### INSERT:
```java
String sql = "INSERT INTO patients (id, name, age) VALUES (?, ?, ?)";
try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, id);
    stmt.setString(2, name);
    stmt.setInt(3, age);
    stmt.executeUpdate();
}
```

### UPDATE:
```java
String sql = "UPDATE patients SET name = ?, age = ? WHERE id = ?";
try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, newName);
    stmt.setInt(2, newAge);
    stmt.setString(3, patientId);
    stmt.executeUpdate();
}
```

### DELETE:
```java
String sql = "DELETE FROM patients WHERE id = ?";
try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, patientId);
    stmt.executeUpdate();
}
```

## Testing Strategy

For each service you migrate:

1. **Test CRUD operations:**
   - Create a record
   - Read it back
   - Update it
   - Delete it

2. **Test edge cases:**
   - Invalid IDs
   - Null values
   - Duplicate entries
   - Foreign key constraints

3. **Test with existing tests:**
   ```cmd
   java -cp "lib\*;bin" hpms.test.PatientServiceTest
   ```

## Troubleshooting

### "java.sql classes not accessible"
**Solution:** Make sure MySQL JDBC driver is referenced:
- Check `lib/mysql-connector-j-9.5.0.jar` exists
- Check `.vscode/settings.json` includes `lib/**/*.jar`
- Reload VS Code window

### "Connection refused"
**Solution:** MySQL not running
- Start MySQL in XAMPP Control Panel
- Check port 3306 is not blocked

### "Unknown database 'hpms_db'"
**Solution:** Database not created
- Run `setup_database.bat`
- Or manually: `mysql -u root < database_schema.sql`

### "Access denied for user 'root'@'localhost'"
**Solution:** Wrong password
- Update `DBConnection.java` with correct password
- XAMPP default is empty password

## Next Steps

1. ✅ Read this guide
2. ⏱️ Run `setup_database.bat`
3. ⏱️ Run `DatabaseConnectionTest`
4. ⏱️ Choose migration approach
5. ⏱️ Start migrating services
6. ⏱️ Test thoroughly
7. ⏱️ Update UI if needed

## Need Help?

Refer to:
- `DATABASE_MIGRATION_GUIDE.md` - Detailed guide
- `AuthServiceDB.java` - Example implementation
- `database_schema.sql` - Table structures
- `DatabaseConnectionTest.java` - Test examples

---

**Created:** December 7, 2025  
**Status:** Infrastructure ready, services need migration  
**Estimated Time:** 25-40 hours for complete migration
