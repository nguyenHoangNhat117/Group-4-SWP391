package util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public final class EmailUtility {

    private EmailUtility() {
    }

    public static void sendOtpEmail(String to, String otp) throws MessagingException {
        String from = setting("SMTP_USER");
        String password = setting("SMTP_APP_PASSWORD").replace(" ", "");
        System.out.println("SMTP sender: " + from);
System.out.println("SMTP password length: " + password.length());

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.port", "587");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.starttls.required", "true");
        props.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        props.setProperty("mail.smtp.connectiontimeout", "10000");
        props.setProperty("mail.smtp.timeout", "10000");
        props.setProperty("mail.smtp.writetimeout", "10000");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to, true));
        message.setSubject("Luxe Escape - Password reset OTP", "UTF-8");
        message.setText("Your OTP is: " + otp + "\nValid for 5 minutes. Do not share this code."
                + "\nIgnore this email if you did not request a password reset.", "UTF-8");
        Transport.send(message);
    }

    private static String setting(String name) throws MessagingException {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(name);
        }
        if (value == null || value.trim().isEmpty()) {
            throw new MessagingException("Missing email configuration: " + name);
        }
        return value.trim();
    }
}
