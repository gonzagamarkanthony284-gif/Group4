package hpms.test;

import hpms.auth.AuthService;
import hpms.util.BackupUtil;
import hpms.util.DataStore;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthBackupTest {
    public static void main(String[] args) throws Exception {
        // seed admin
        AuthService.seedAdmin();
        System.out.println("Seeded admin");
        // login
        System.out.println(AuthService.login("admin", "admin123"));
        // register user
        AuthService.current = DataStore.users.get("admin");
        System.out.println(AuthService.register("tester", "testpass", "DOCTOR"));
        // change password
        System.out.println(AuthService.changePassword("tester", "testpass", "newpass123"));
        // backup to file
        Path p = Paths.get(System.getProperty("java.io.tmpdir"), "hpms_backup.json");
        boolean saved = BackupUtil.saveToFile(p);
        System.out.println("Saved: " + saved + " -> " + p.toString());
        // clear and load
        BackupUtil.fromJson(BackupUtil.toJson());
        System.out.println("Restore in-memory done. Users: " + DataStore.users.keySet());
    }
}
