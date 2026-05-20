package hk.edu.hkiit.jakarta.webapp.tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

// Custom tag for batch import
// Attributes: results, success, failed
public class ImportResultsTag extends SimpleTagSupport {

    private ArrayList<String> results;
    private int success;
    private int failed;

    public void setResults(ArrayList<String> results) {
        this.results = results;
    }
    public void setSuccess(int success) {
        this.success = success;
    }
    public void setFailed(int failed) {
        this.failed = failed;
    }

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        try {

            if (results == null || results.isEmpty()) {
                return;
            }

            int total = success + failed;
            out.println("<div class=\"import-summary\">"
                      + "<strong>Import Complete:</strong> " + total + " rows processed &mdash; "
                      + "<span class=\"text-success\">" + success + " succeeded</span>, "
                      + "<span class=\"text-danger\">" + failed + " failed</span>"
                      + "</div>");

            // Table header
            out.println("<table class=\"data-table\"><thead><tr>"
                      + "<th>#</th>"
                      + "<th>Status</th>"
                      + "<th>Detail</th>"
                      + "</tr></thead><tbody>");

            int row = 1;
            for (String result : results) {

                String status;
                String badgeClass;
                if (result.startsWith("OK:")) {
                    status = "Success";
                    badgeClass = "badge-completed";
                } else if (result.startsWith("FAIL:")) {
                    status = "Failed";
                    badgeClass = "badge-noshow";
                } else {
                    status = "Skipped";
                    badgeClass = "badge-cancelled";
                }

                out.println("<tr>"
                          + "<td>" + row + "</td>"
                          + "<td><span class=\"badge " + badgeClass + "\">" + status + "</span></td>"
                          + "<td>" + HtmlUtil.escape(result) + "</td>"
                          + "</tr>");
                row++;

            }

            out.println("</tbody></table>");

        } catch (IOException ex) {
            throw new JspException("Error in ImportResultsTag", ex);
        }
        
    }

}
