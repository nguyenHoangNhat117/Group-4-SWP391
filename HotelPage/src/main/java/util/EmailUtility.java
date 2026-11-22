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

        send(to, "Luxe Escape - Mã xác minh đặt lại mật khẩu",
                "Mã xác minh đặt lại mật khẩu của bạn là: " + otp
                + "\n\nMã có hiệu lực trong 5 phút."
                + "\nBạn có tối đa 3 lần xác minh."
                + "\nVui lòng giữ mã xác minh cho riêng mình."
                + "\n\nBạn có thể bỏ qua email này nếu không yêu cầu đặt lại mật khẩu.");
    }

    /**
     * UC18: called only after the account transaction commits.
     */
    public static void sendAccountCreatedEmail(String to, String username, String password, String role)
            throws MessagingException {
        String base = setting("APP_BASE_URL");
        try {
            java.net.URI uri = java.net.URI.create(base);
            if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getQuery() != null || uri.getFragment() != null
                    || uri.getUserInfo() != null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new MessagingException("Invalid APP_BASE_URL");
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        send(to, "Luxe Escape - Tài khoản mới của bạn",
                "Tài khoản Luxe Escape của bạn đã được tạo."
                + "\nTên đăng nhập: " + username + "\nMật khẩu ban đầu: " + password
                + "\nVai trò: " + MessageTranslations.text(role) + "\nĐăng nhập: " + base + "/login"
                + "\n\nSau khi đăng nhập, vui lòng dùng chức năng Đổi mật khẩu để đổi mật khẩu ban đầu."
                + "\nVui lòng giữ kín email và mật khẩu của bạn.");
    }

    private static void send(String to, String subject, String body) throws MessagingException {
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

        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
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
