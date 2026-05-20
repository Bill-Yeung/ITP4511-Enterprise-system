package hk.edu.hkiit.jakarta.webapp.tag;

import hk.edu.hkiit.jakarta.webapp.bean.WalkinQueueBean;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

public class QueueTableTag extends SimpleTagSupport {

    private ArrayList<WalkinQueueBean> queues;
    private boolean isStaff = false;
    private String actionUrl = "";

    public void setQueues(ArrayList<WalkinQueueBean> queues) {
        this.queues = queues;
    }

    public void setIsStaff(boolean isStaff) {
        this.isStaff = isStaff;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    @Override
    public void doTag() throws JspException {
        JspWriter out = getJspContext().getOut();
        try {
            if (queues == null || queues.isEmpty()) {
                out.println("<p class=\"no-data\">No queue entries found.</p>");
                return;
            }

            out.println("<table class=\"data-table\"><thead><tr>"
                      + "<th>No.</th>"
                      + "<th>Patient</th>"
                      + "<th>Clinic</th>"
                      + "<th>Service</th>"
                      + "<th>Wait (min)</th>"
                      + "<th>Status</th>");
            if (isStaff) out.println("<th>Actions</th>");
            out.println("</tr></thead><tbody>");

            for (WalkinQueueBean wq : queues) {
                String badgeClass = getStatusBadgeClass(wq.getStatus());

                out.println("<tr>"
                          + "<td>" + wq.getQueueNumber() + "</td>"
                          + "<td>" + HtmlUtil.escape(wq.getPatientName()) + "</td>"
                          + "<td>" + HtmlUtil.escape(wq.getClinicName()) + "</td>"
                          + "<td>" + HtmlUtil.escape(wq.getServiceName()) + "</td>"
                          + "<td>" + wq.getEstimatedWaitMins() + "</td>"
                          + "<td><span class=\"badge " + badgeClass + "\">"
                          + wq.getStatus() + "</span></td>");

                if (isStaff) {
                    out.println("<td>");
                    if ("Waiting".equals(wq.getStatus())) {
                        out.println("<a href=\"" + actionLink("callNext", "csId", wq.getClinicServiceId(), wq.getClinicServiceId()) + "\">Call</a> | ");
                        out.println("<a href=\"" + actionLink("skip", "id", wq.getQueueId(), wq.getClinicServiceId()) + "\">Skip</a>");
                    } else if ("Called".equals(wq.getStatus())) {
                        out.println("<a href=\"" + actionLink("served", "id", wq.getQueueId(), wq.getClinicServiceId()) + "\">Mark Served</a>");
                    } else if ("Skipped".equals(wq.getStatus())) {
                        out.println("<a href=\"" + actionLink("recall", "id", wq.getQueueId(), wq.getClinicServiceId()) + "\">Re-call</a>");
                    } else {
                        out.println("&mdash;");
                    }
                    out.println("</td>");
                }
                out.println("</tr>");
            }

            out.println("</tbody></table>");

        } catch (IOException ex) {
            throw new JspException("Error in QueueTableTag", ex);
        }
    }

    private String getStatusBadgeClass(String status) {
        if (status == null) return "";
        switch (status) {
            case "Waiting": return "badge-booked";
            case "Called":  return "badge-arrived";
            case "Served":  return "badge-completed";
            case "Skipped": return "badge-noshow";
            default:        return "";
        }
    }

    private String actionLink(String action, String idName, int idValue, int serviceId) {
        String base = actionUrl.isEmpty() ? "" : actionUrl;
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "action=" + action + "&" + idName + "=" + idValue
                + "&serviceId=" + serviceId;
    }

}
