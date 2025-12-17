package hpms.auth;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {
    private static final SecureRandom RAND = new SecureRandom();
    private static final int ITER = 10000;
    private static final int KEY_LEN = 256;

    public static String generateSalt() {
        byte[] s = new byte[16]; RAND.nextBytes(s); return Base64.getEncoder().encodeToString(s);
    }

    public static String hash(String plain, String salt) {
        if (plain == null) return null;
        try {
            char[] chars = plain.toCharArray();
            byte[] saltBytes = salt == null ? new byte[0] : Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(chars, saltBytes, ITER, KEY_LEN);
            SecretKeyFactory skf;
            try {
                skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            } catch (Exception ex) {
                skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            }
            byte[] res = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(res);
        } catch (Exception e) {
            return null;
        }
    }
}

