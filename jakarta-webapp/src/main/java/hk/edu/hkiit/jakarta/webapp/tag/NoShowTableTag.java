package hk.edu.hkiit.jakarta.webapp.tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

// Custom tag for no-show
// Attributes: rows ([0]=clinicName, [1]=serviceName, [2]=noshowCount)
public class NoShowTableTag extends SimpleTagSupport {

    private ArrayList<String[]> rows;

    public void setRows(ArrayList<String[]> rows) {
        this.rows = rows;
    }

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        try {

            if (rows == null || rows.isEmpty()) {
                out.println("<p class=\"no-data\">No no-show records found for the selected period.</p>");
                return;
            }

            // Table header
            out.println("<table class=\"data-table\"><thead><tr>"
                      + "<th>Clinic</th>"
                      + "<th>Service</th>"
                      + "<th>No-show Count</th>"
                      + "</tr></thead><tbody>");

            int total = 0;

            // Render each data
            for (String[] row : rows) {

                int count = Integer.parseInt(row[2]);
                total += count;

                out.println("<tr>"
                          + "<td>" + HtmlUtil.escape(row[0]) + "</td>"
                          + "<td>" + HtmlUtil.escape(row[1]) + "</td>"
                          + "<td><span class=\"badge badge-noshow\">" + count + "</span></td>"
                          + "</tr>");

            }

            out.println("<tr class=\"table-total-row\">"
                      + "<td colspan=\"2\" class=\"text-right\">Total</td>"
                      + "<td><span class=\"badge badge-noshow\">" + total + "</span></td>"
                      + "</tr>");

            out.println("</tbody></table>");

        } catch (IOException ex) {
            throw new JspException("Error in NoShowTableTag", ex);
        }
        
    }

}
