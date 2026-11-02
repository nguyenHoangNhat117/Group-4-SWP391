package util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

// Updated by Nguyen Hoang Nhat
public final class EmailUtility {

    private EmailUtility() {
        // Utility class.
    }

    public static void sendOtpEmail(
            String to,
            String otp
    ) throws MessagingException {

        String from = setting("SMTP_USER");

        // Preserve support for Gmail app passwords entered with spaces.
        String password = setting("SMTP_APP_PASSWORD").replace(" ", "");

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

        Session mailSession = Session.getInstance(
                props,
                new Authenticator() {
            @Override
            protected PasswordAuthentication
                    getPasswordAuthentication() {

                return new PasswordAuthentication(from, password);
            }
        }
        );

        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(new InternetAddress(from));

        message.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(to, true)
        );

        message.setSubject(
                "Luxe Escape - Password reset OTP",
                "UTF-8"
        );

        message.setText(
                "Your password reset OTP is: " + otp
                + "\n\nThis code expires in 5 minutes."
                + "\nYou have a maximum of 3 verification attempts."
                + "\nDo not share this code."
                + "\n\nIgnore this email if you did not request "
                + "a password reset.",
                "UTF-8"
        );

        // Propagate delivery failures to the servlet.
        Transport.send(message);
    }

    private static String setting(
            String name
    ) throws MessagingException {

        String value = System.getenv(name);

        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(name);
        }

        if (value == null || value.trim().isEmpty()) {
            throw new MessagingException(
                    "Missing email configuration: " + name
            );
        }

        return value.trim();
    }
}
