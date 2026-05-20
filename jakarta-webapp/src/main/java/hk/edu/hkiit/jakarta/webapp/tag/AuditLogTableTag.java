package hk.edu.hkiit.jakarta.webapp.tag;

import hk.edu.hkiit.jakarta.webapp.bean.AuditLogBean;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

// Custom tag for audit log
// Attributes: logs
public class AuditLogTableTag extends SimpleTagSupport {

    private ArrayList<AuditLogBean> logs;

    public void setLogs(ArrayList<AuditLogBean> logs) {
        this.logs = logs;
    }

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        try {
            
            if (logs == null || logs.isEmpty()) {
                out.println("<p class=\"no-data\">No activity records found.</p>");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);

            // Table header
            out.println("<table class=\"data-table\"><thead><tr>"
                      + "<th>ID</th>"
                      + "<th>Time</th>"
                      + "<th>User</th>"
                      + "<th>Role</th>"
                      + "<th>Action</th>"
                      + "</tr></thead><tbody>");

            // Render each data
            for (AuditLogBean log : logs) {
                out.println("<tr>"
                          + "<td>" + log.getLogId() + "</td>"
                          + "<td class=\"text-nowrap\">" + sdf.format(log.getActionTimestamp()) + "</td>"
                          + "<td>" + HtmlUtil.escape(log.getFullName()) + "</td>"
                          + "<td>" + log.getRole() + "</td>"
                          + "<td>" + HtmlUtil.escape(log.getActionDescription()) + "</td>"
                          + "</tr>");
            }

            out.println("</tbody></table>");

        } catch (IOException ex) {
            throw new JspException("Error in AuditLogTableTag", ex);
        }
        
    }

}
