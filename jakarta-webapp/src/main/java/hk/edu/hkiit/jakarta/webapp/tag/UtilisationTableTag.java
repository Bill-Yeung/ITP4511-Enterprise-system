package hk.edu.hkiit.jakarta.webapp.tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

// Custom tag for utilization table
// Attributes: rows ([0]=clinicName, [1]=serviceName, [2]=bookedCount, [3]=totalSlots, [4]=ratePct)
public class UtilisationTableTag extends SimpleTagSupport {

    private ArrayList<String[]> rows;

    public void setRows(ArrayList<String[]> rows) {
        this.rows = rows;
    }

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        try {

            if (rows == null || rows.isEmpty()) {
                out.println("<p class=\"no-data\">No data available for the selected period.</p>");
                return;
            }

            // Table header
            out.println("<table class=\"data-table\"><thead><tr>"
                      + "<th>Clinic</th>"
                      + "<th>Service</th>"
                      + "<th>Booked</th>"
                      + "<th>Total Slots</th>"
                      + "<th class=\"utilisation-column\">Utilisation</th>"
                      + "</tr></thead><tbody>");

            // Render each data
            for (String[] row : rows) {

                double pct = Double.parseDouble(row[4]);
                double barWidth = Math.min(pct, 100);
                String levelClass = utilisationLevel(pct);

                out.println("<tr>"
                          + "<td>" + HtmlUtil.escape(row[0]) + "</td>"
                          + "<td>" + HtmlUtil.escape(row[1]) + "</td>"
                          + "<td>" + row[2] + "</td>"
                          + "<td>" + row[3] + "</td>"
                          + "<td><div class=\"utilisation-bar " + levelClass + "\">"
                          + "<progress class=\"utilisation-progress\" value=\"" + barWidth + "\" max=\"100\"></progress>"
                          + "<span class=\"utilisation-pct\">" + row[4] + "%</span>"
                          + "</div></td></tr>");

            }

            out.println("</tbody></table>");

        } catch (IOException ex) {
            throw new JspException("Error in UtilisationTableTag", ex);
        }

    }

    private String utilisationLevel(double pct) {

        if (pct >= 80) {
            return "level-high";
        } else if (pct >= 50) {
            return "level-medium";
        } else if (pct >= 20) {
            return "level-low";
        } else {
            return "level-critical";
        }

    }

}
