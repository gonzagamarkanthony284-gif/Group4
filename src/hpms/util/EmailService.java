package hpms.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email Service using JavaMail API
 * Handles sending emails for account creation, password reset, and appointment reminders
 * 
 * NOTE: Currently disabled due to missing javax.mail dependency
 * All methods return true (success) but log the email details to console
 */
public class EmailService {

    /**
     * Send email using JavaMail API
     */
    private static boolean sendEmail(String toEmail, String subject, String body) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty())
                return false;

            // Preferred: environment variables
            // HPMS_SMTP_HOST, HPMS_SMTP_PORT, HPMS_SMTP_USER, HPMS_SMTP_PASS, HPMS_SMTP_FROM
            // Optional: HPMS_SMTP_TLS (true/false)
            // Fallback: JVM properties hpms.smtp.host, hpms.smtp.port, hpms.smtp.user, hpms.smtp.pass, hpms.smtp.from
            String host = firstNonEmpty(System.getenv("HPMS_SMTP_HOST"), System.getProperty("hpms.smtp.host"));
            String port = firstNonEmpty(System.getenv("HPMS_SMTP_PORT"), System.getProperty("hpms.smtp.port"));
            String user = firstNonEmpty(System.getenv("HPMS_SMTP_USER"), System.getProperty("hpms.smtp.user"));
            String pass = firstNonEmpty(System.getenv("HPMS_SMTP_PASS"), System.getProperty("hpms.smtp.pass"));
            String from = firstNonEmpty(System.getenv("HPMS_SMTP_FROM"), System.getProperty("hpms.smtp.from"));
            String tls = firstNonEmpty(System.getenv("HPMS_SMTP_TLS"), System.getProperty("hpms.smtp.tls"));

            // Defaults for Gmail STARTTLS
            if (host == null)
                host = "smtp.gmail.com";
            if (port == null)
                port = "587";
            if (from == null)
                from = user;
            boolean useStartTls = tls == null ? true : Boolean.parseBoolean(tls);

            if (user == null || user.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
                System.err.println("EmailService: Missing SMTP credentials (HPMS_SMTP_USER/HPMS_SMTP_PASS)");
                return false;
            }

            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", String.valueOf(useStartTls));
            props.put("mail.smtp.ssl.trust", host);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject(subject == null ? "" : subject, "UTF-8");

            // HTML body
            msg.setContent(body == null ? "" : body, "text/html; charset=UTF-8");

            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            System.err.println("EmailService: Failed to send email: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("EmailService: Unexpected error sending email: " + e.getMessage());
            return false;
        }
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty())
            return a.trim();
        if (b != null && !b.trim().isEmpty())
            return b.trim();
        return null;
    }

    /**
     * Send account creation email with credentials
     */
    public static boolean sendAccountCreationEmail(String toEmail, String username, String password, String role, String fullName) {
        String subject = "Your HPMS Account Has Been Created";
        String body = String.format(
            "<html><body style='font-family: Arial, sans-serif;'>" +
            "<h2>Welcome to HPMS</h2>" +
            "<p>Dear %s,</p>" +
            "<p>Your account has been successfully created in the Hospital Patient Management System.</p>" +
            "<p><strong>Account Details:</strong></p>" +
            "<ul>" +
            "<li><strong>Username:</strong> %s</li>" +
            "<li><strong>Password:</strong> %s</li>" +
            "<li><strong>Role:</strong> %s</li>" +
            "</ul>" +
            "<p>Please keep these credentials secure and change your password after first login.</p>" +
            "<p>If you did not request this account, please contact the system administrator immediately.</p>" +
            "<p>Best regards,<br>HPMS Administration</p>" +
            "</body></html>",
            fullName != null ? fullName : "User",
            username,
            password,
            role
        );
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send password reset email with reset code
     */
    public static boolean sendPasswordResetCodeEmail(String toEmail, String username, String resetCode) {
        String subject = "HPMS Password Reset Request";
        String body = String.format(
            "<html><body style='font-family: Arial, sans-serif;'>" +
            "<h2>Password Reset Request</h2>" +
            "<p>Dear %s,</p>" +
            "<p>You have requested to reset your password for your HPMS account.</p>" +
            "<p><strong>Reset Code:</strong> <span style='font-size: 18px; font-weight: bold; color: #0066cc;'>%s</span></p>" +
            "<p>This code will expire in 1 hour. Please use this code to reset your password.</p>" +
            "<p>If you did not request this password reset, please ignore this email or contact the system administrator.</p>" +
            "<p>Best regards,<br>HPMS Administration</p>" +
            "</body></html>",
            username,
            resetCode
        );
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send password reset email with new password
     */
    public static boolean sendPasswordResetEmail(String toEmail, String username, String newPassword) {
        String subject = "HPMS Password Reset";
        String body = String.format(
            "<html><body style='font-family: Arial, sans-serif;'>" +
            "<h2>Password Reset Request</h2>" +
            "<p>Dear User,</p>" +
            "<p>Your password for the Hospital Patient Management System has been reset.</p>" +
            "<p><strong>Login Details:</strong></p>" +
            "<ul>" +
            "<li><strong>Username:</strong> %s</li>" +
            "<li><strong>New Password:</strong> %s</li>" +
            "</ul>" +
            "<p><strong>Important:</strong> Please change your password after logging in for security.</p>" +
            "<p>If you did not request this password reset, please contact the system administrator immediately.</p>" +
            "<p>Best regards,<br>HPMS Administration</p>" +
            "</body></html>",
            username,
            newPassword
        );
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send appointment reminder email
     */
    public static boolean sendAppointmentReminderEmail(String toEmail, String patientName, String doctorName, 
                                                       String appointmentDate, String appointmentTime, String department) {
        String subject = "Appointment Reminder - HPMS";
        String body = String.format(
            "<html><body style='font-family: Arial, sans-serif;'>" +
            "<h2>Appointment Reminder</h2>" +
            "<p>Dear %s,</p>" +
            "<p>This is a reminder for your upcoming appointment:</p>" +
            "<p><strong>Appointment Details:</strong></p>" +
            "<ul>" +
            "<li><strong>Doctor:</strong> %s</li>" +
            "<li><strong>Department:</strong> %s</li>" +
            "<li><strong>Date:</strong> %s</li>" +
            "<li><strong>Time:</strong> %s</li>" +
            "</ul>" +
            "<p>Please arrive 15 minutes before your scheduled time.</p>" +
            "<p>If you need to reschedule or cancel, please contact the hospital.</p>" +
            "<p>Best regards,<br>HPMS Administration</p>" +
            "</body></html>",
            patientName,
            doctorName,
            department != null ? department : "N/A",
            appointmentDate,
            appointmentTime
        );
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send doctor credentials email (doctors receive credentials only via email)
     */
    public static boolean sendDoctorCredentialsEmail(String toEmail, String username, String password, String doctorName) {
        String subject = "Your HPMS Doctor Account Credentials";
        String body = String.format(
            "<html><body style='font-family: Arial, sans-serif;'>" +
            "<h2>Doctor Account Created</h2>" +
            "<p>Dear Dr. %s,</p>" +
            "<p>Your doctor account has been created in the Hospital Patient Management System.</p>" +
            "<p><strong>Login Credentials:</strong></p>" +
            "<ul>" +
            "<li><strong>Username:</strong> %s</li>" +
            "<li><strong>Password:</strong> %s</li>" +
            "</ul>" +
            "<p><strong>Important:</strong> Please change your password after your first login for security.</p>" +
            "<p>You can now access the system to view assigned patients, add diagnoses, prescriptions, and manage appointments.</p>" +
            "<p>If you have any questions, please contact the system administrator.</p>" +
            "<p>Best regards,<br>HPMS Administration</p>" +
            "</body></html>",
            doctorName,
            username,
            password
        );
        return sendEmail(toEmail, subject, body);
    }
}

