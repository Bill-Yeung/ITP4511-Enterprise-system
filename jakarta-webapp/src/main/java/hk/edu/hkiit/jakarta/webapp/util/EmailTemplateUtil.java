package hk.edu.hkiit.jakarta.webapp.util;

import hk.edu.hkiit.jakarta.webapp.bean.NotificationBean;
import java.text.SimpleDateFormat;

public class EmailTemplateUtil {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    public static String formatNotification(NotificationBean notification, String recipientName) {
        String typeColor;
        String typeIcon;
        switch (notification.getType()) {
            case "Appointment":
                typeColor = "#2563eb";
                typeIcon = "&#128197;";
                break;
            case "Queue":
                typeColor = "#7c3aed";
                typeIcon = "&#128101;";
                break;
            case "Reminder":
                typeColor = "#f59e0b";
                typeIcon = "&#128276;";
                break;
            default:
                typeColor = "#6b7280";
                typeIcon = "&#9881;";
                break;
        }

        String dateStr = notification.getCreatedAt() != null
            ? DATE_FMT.format(notification.getCreatedAt()) : "";

        return "<!DOCTYPE html>"
            + "<html><head><meta charset=\"UTF-8\"></head>"
            + "<body style=\"margin:0; padding:0; background:#f3f4f6; font-family:Arial,sans-serif\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f3f4f6; padding:32px 0\">"
            + "<tr><td align=\"center\">"
            + "<table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.1)\">"
            + "<tr><td style=\"background:" + typeColor + "; padding:20px 32px; color:#ffffff\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
            + "<tr><td style=\"font-size:20px; font-weight:bold; color:#ffffff\">CCHC Clinic System</td>"
            + "<td align=\"right\" style=\"font-size:12px; color:rgba(255,255,255,0.8)\">" + dateStr + "</td></tr>"
            + "</table>"
            + "</td></tr>"
            + "<tr><td style=\"padding:24px 32px 0\">"
            + "<span style=\"display:inline-block; padding:4px 12px; background:" + typeColor + "22; color:" + typeColor + "; border-radius:12px; font-size:12px; font-weight:600\">"
            + typeIcon + " " + escape(notification.getType())
            + "</span>"
            + "</td></tr>"
            + "<tr><td style=\"padding:16px 32px 0; font-size:15px; color:#374151\">"
            + "Dear " + escape(recipientName != null ? recipientName : "User") + ","
            + "</td></tr>"
            + "<tr><td style=\"padding:12px 32px 24px; font-size:14px; color:#4b5563; line-height:1.6\">"
            + escape(notification.getMessage())
            + "</td></tr>"
            + "<tr><td style=\"padding:0 32px\"><hr style=\"border:none; border-top:1px solid #e5e7eb\"></td></tr>"
            + "<tr><td style=\"padding:16px 32px 24px; font-size:11px; color:#9ca3af; text-align:center\">"
            + "This is an automated notification from the CCHC Clinic Appointment System.<br>"
            + "Please do not reply to this email."
            + "</td></tr>"
            + "</table>"
            + "</td></tr></table>"
            + "</body></html>";
    }

    public static String formatSimpleMessage(String subject, String recipientName,
                                              String messageBody, String type) {
        NotificationBean nb = new NotificationBean();
        nb.setMessage(messageBody);
        nb.setType(type != null ? type : "System");
        nb.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        return formatNotification(nb, recipientName);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
