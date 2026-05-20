package hk.edu.hkiit.jakarta.webapp.util;

import java.security.SecureRandom;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class OtpUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static boolean smtpInitialized = false;
    private static String SMTP_HOST     = "smtp.gmail.com";
    private static int    SMTP_PORT     = 465;
    private static String SMTP_USER     = "";
    private static String SMTP_PASSWORD = "";

    public static void init(String smtpHost, int smtpPort, String smtpUser, String smtpPassword) {
        SMTP_HOST       = smtpHost;
        SMTP_PORT       = smtpPort;
        SMTP_USER       = smtpUser;
        SMTP_PASSWORD   = smtpPassword;
        smtpInitialized = true;
    }

    public static String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    public static void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        new Thread(() -> {
            if (!smtpInitialized) {
                System.err.println("\nOTP EMAIL NOT SENT ===");
                System.err.println("SMTP credentials not configured!");
                System.err.println("To: " + toEmail);
                System.err.println("OTP Code: " + otpCode);
                System.err.println("Add SMTP context parameters to web.xml or call OtpUtil.init()");
                System.err.println("==========================\n");
                return;
            }

            Properties props = new Properties();
            props.put("mail.smtp.auth",              "true");
            props.put("mail.smtp.ssl.enable",        "true");
            props.put("mail.smtp.host",              SMTP_HOST);
            props.put("mail.smtp.port",              String.valueOf(SMTP_PORT));
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout",           "10000");
            props.put("mail.smtp.ssl.trust",         SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                }
            });

            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(SMTP_USER, "CCHC Clinic System"));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                msg.setSubject("CCHC - Your " + purpose + " Code");
                msg.setContent(buildHtml(otpCode, purpose), "text/html;charset=UTF-8");
                Transport.send(msg);
            } catch (Throwable ex) {
                System.err.println("\nOTP EMAIL FAILED ===");
                System.err.println("To: " + toEmail);
                System.err.println("Purpose: " + purpose);
                System.err.println("Error: " + ex.getMessage());
                ex.printStackTrace(System.err);
                System.err.println("==========================\n");
            }
        }).start();
    }

    private static String buildHtml(String otpCode, String purpose) {
        boolean isRegister = "Email Verification".equals(purpose);

        String headerColor = isRegister ? "#2563eb" : "#dc2626";
        String heading     = isRegister ? "Welcome to CCHC Clinic System" : "Password Reset Request";
        String intro       = isRegister
            ? "Thank you for registering. Please use the verification code below to activate your account."
            : "We received a request to reset your password. Use the code below to proceed.";
        String footer      = isRegister
            ? "If you did not create an account, you can safely ignore this email."
            : "If you did not request a password reset, please ignore this email. Your password will not change.";

        return "<!DOCTYPE html>"
            + "<html><head><meta charset=\"UTF-8\"></head>"
            + "<body style=\"margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f3f4f6;padding:32px 0\">"
            + "<tr><td align=\"center\">"
            + "<table width=\"520\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.12)\">"
            + "<tr><td style=\"background:" + headerColor + ";padding:24px 32px;color:#ffffff;font-size:20px;font-weight:bold\">"
            + "CCHC Clinic System"
            + "</td></tr>"
            + "<tr><td style=\"padding:28px 32px 8px;font-size:16px;font-weight:600;color:#111827\">" + heading + "</td></tr>"
            + "<tr><td style=\"padding:8px 32px 24px;font-size:14px;color:#4b5563;line-height:1.6\">" + intro + "</td></tr>"
            + "<tr><td style=\"padding:0 32px 24px;text-align:center\">"
            + "<div style=\"display:inline-block;padding:16px 40px;background:#f9fafb;border:2px dashed " + headerColor + ";border-radius:8px;font-size:32px;font-weight:700;letter-spacing:8px;color:" + headerColor + "\">"
            + otpCode
            + "</div>"
            + "</td></tr>"
            + "<tr><td style=\"padding:0 32px 16px;font-size:13px;color:#6b7280;text-align:center\">"
            + "This code expires in <strong>5 minutes</strong>."
            + "</td></tr>"
            + "<tr><td style=\"padding:0 32px\"><hr style=\"border:none;border-top:1px solid #e5e7eb\"></td></tr>"
            + "<tr><td style=\"padding:16px 32px 24px;font-size:11px;color:#9ca3af;text-align:center\">"
            + footer + "<br>Please do not reply to this email."
            + "</td></tr>"
            + "</table>"
            + "</td></tr></table>"
            + "</body></html>";
    }
}
