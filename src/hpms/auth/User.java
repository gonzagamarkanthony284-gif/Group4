package hpms.auth;

import hpms.model.UserRole;

public class User {
    public final String username;
    // stored as hashed password (hex)
    public String password;
    // Base64 salt used by PBKDF2
    public String salt;
    public UserRole role;
    public String displayPassword;
    public String status = "ACTIVE"; // ACTIVE or DEACTIVATED

    public User(String username, String password, String salt, UserRole role) {
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.role = role;
        this.displayPassword = null;
        this.status = "ACTIVE";
    }
}
