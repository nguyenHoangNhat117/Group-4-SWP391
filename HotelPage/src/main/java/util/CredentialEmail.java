package util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.net.URI;
import java.util.Properties;

/**
 * Uses the same Gmail SMTP settings as the existing OTP module.
 */
public final class CredentialEmail {

    private CredentialEmail() {
    }

    private static String setting(String name) throws MessagingException {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) {
            v = System.getProperty(name);
        }
        if (v == null || v.isBlank()) {
            throw new MessagingException("Missing configuration: " + name);
        }
        return v.trim();
    }

    public static void send(String email, String username, String password, String role) throws MessagingException {
        String from = setting("SMTP_USER"), secret = setting("SMTP_APP_PASSWORD").replace(" ", "");
        String base = setting("APP_BASE_URL");
        try {
            URI u = URI.create(base);
            if (!("https".equals(u.getScheme()) || "http".equals(u.getScheme())) || u.getHost() == null || u.getUserInfo() != null || u.getQuery() != null || u.getFragment() != null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new MessagingException("APP_BASE_URL must be the application base URL.");
        }
        Properties p = new Properties();
        p.setProperty("mail.smtp.host", "smtp.gmail.com");
        p.setProperty("mail.smtp.port", "587");
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.smtp.starttls.enable", "true");
        p.setProperty("mail.smtp.starttls.required", "true");
        p.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        p.setProperty("mail.smtp.connectiontimeout", "10000");
        p.setProperty("mail.smtp.timeout", "10000");
        p.setProperty("mail.smtp.writetimeout", "10000");
        Session session = Session.getInstance(p, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, secret);
            }
        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email, true));
        message.setSubject("Luxe Escape — Your account credentials", "UTF-8");
        message.setText("Luxe Escape Hotel\n\nUsername: " + username + "\nInitial password: " + password + "\nRole: " + role + "\nSign in: " + base.replaceAll("/+$", "") + "/login\n\nPlease change your password after signing in. Do not share these credentials.", "UTF-8");
        Transport.send(message);
    }
}
