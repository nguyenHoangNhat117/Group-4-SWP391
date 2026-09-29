/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
/**
 *
 * @author Trần Nguyễn
 */
public class EmailUtility {
    public static void sendOtpEmail(String to, String otp) {
        final String from = "loitester0001@gmail.com";
        final String pass = "jaon eztb azfv igxw"; // App password from Gmail

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Your OTP Code for Password Reset");
            message.setText("Dear user,\n\nYour One-Time Password (OTP) is: " + otp +
                            "\n\nPlease use this code to reset your password. This code is valid for a limited time.\n\n" +
                            "If you did not request a password reset, please ignore this email.\n\n" +
                            "Best regards,\nLuxe Escape Support Team");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
