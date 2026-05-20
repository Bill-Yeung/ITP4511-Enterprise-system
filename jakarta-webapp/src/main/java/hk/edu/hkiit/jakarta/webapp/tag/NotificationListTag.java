package hk.edu.hkiit.jakarta.webapp.tag;

import hk.edu.hkiit.jakarta.webapp.bean.NotificationBean;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

public class NotificationListTag extends SimpleTagSupport {

    private ArrayList<NotificationBean> notifications;

    public void setNotifications(ArrayList<NotificationBean> notifications) {
        this.notifications = notifications;
    }

    @Override
    public void doTag() throws JspException {
        JspWriter out = getJspContext().getOut();
        try {
            if (notifications == null || notifications.isEmpty()) {
                out.println("<p class=\"no-data\">No notifications.</p>");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");

            for (NotificationBean n : notifications) {
                String typeIcon;
                String typeClass;
                switch (n.getType()) {
                    case "Appointment":
                        typeIcon = "&#128197;";
                        typeClass = "notification-appointment";
                        break;
                    case "Queue":
                        typeIcon = "&#128101;";
                        typeClass = "notification-queue";
                        break;
                    case "Reminder":
                        typeIcon = "&#128101;";
                        typeClass = "notification-reminder";
                        break;
                    default:
                        typeIcon = "&#9881;";
                        typeClass = "notification-system";
                        break;
                }

                String readClass = n.isRead() ? "" : " notification-unread";
                String dateStr = n.getCreatedAt() != null ? sdf.format(n.getCreatedAt()) : "";

                out.println("<div class=\"notification-card " + typeClass + readClass + "\">"
                          + "<div class=\"notification-card-header\">"
                          + "<span class=\"notification-type\">" + typeIcon + " "
                          + n.getType() + "</span>"
                          + "<span class=\"notification-date\">" + dateStr + "</span>"
                          + "</div>"
                          + "<div class=\"notification-message\">"
                          + HtmlUtil.escape(n.getMessage())
                          + "</div>");

                if (!n.isRead()) {
                    out.println("<div class=\"notification-unread-label\">&#9679; Unread</div>");
                }

                out.println("</div>");
            }

        } catch (IOException ex) {
            throw new JspException("Error in NotificationListTag", ex);
        }
    }

}
