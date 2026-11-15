package util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/** Email riêng cho kích hoạt khách tại quầy; không thay email reset/UC18. */
public final class WalkInActivationMail {
    private WalkInActivationMail() { }
    public static void send(String email,String otp)throws MessagingException {
        String user=setting("SMTP_USER");
        String password=setting("SMTP_APP_PASSWORD").replace(" ","");
        Properties p=new Properties();
        p.setProperty("mail.smtp.host","smtp.gmail.com");p.setProperty("mail.smtp.port","587");
        p.setProperty("mail.smtp.auth","true");p.setProperty("mail.smtp.starttls.enable","true");
        p.setProperty("mail.smtp.starttls.required","true");p.setProperty("mail.smtp.ssl.checkserveridentity","true");
        p.setProperty("mail.smtp.connectiontimeout","10000");p.setProperty("mail.smtp.timeout","10000");
        p.setProperty("mail.smtp.writetimeout","10000");
        Session session=Session.getInstance(p,new Authenticator(){
            @Override protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(user,password);}
        });
        MimeMessage message=new MimeMessage(session);
        message.setFrom(new InternetAddress(user,true));message.setRecipient(Message.RecipientType.TO,new InternetAddress(email,true));
        message.setSubject("Luxe Escape - Kích hoạt tài khoản khách tại quầy","UTF-8");
        message.setText("Mã xác minh email để tạo tài khoản gắn với hồ sơ khách tại quầy: "+otp
            +"\nMã có hiệu lực 5 phút, tối đa 3 lần nhập. Không chia sẻ mã này."
            +"\nKích hoạt sẽ cho tài khoản truy cập lịch sử đặt phòng của hồ sơ này."
            +"\nNếu không yêu cầu, hãy bỏ qua email; hồ sơ của bạn chưa thay đổi.","UTF-8");
        Transport.send(message);
    }
    private static String setting(String name)throws MessagingException {
        String value=System.getenv(name);if(value==null||value.isBlank())value=System.getProperty(name);
        if(value==null||value.isBlank())throw new MessagingException("Missing SMTP configuration");return value.trim();
    }
}
