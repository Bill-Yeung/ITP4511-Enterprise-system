package hk.edu.hkiit.jakarta.webapp.tag;

import hk.edu.hkiit.jakarta.webapp.bean.SettingBean;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

// Custom tag for settings
// Attributes: settings
public class SettingsFormTag extends SimpleTagSupport {

    private ArrayList<SettingBean> settings;

    public void setSettings(ArrayList<SettingBean> settings) {
        this.settings = settings;
    }

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        try {

            if (settings == null || settings.isEmpty()) {
                out.println("<p class=\"no-data\">No settings configured.</p>");
                return;
            }

            // Table header
            out.println("<table class=\"data-table\"><thead><tr>"
                      + "<th>Setting</th>"
                      + "<th>Value</th>"
                      + "<th>Description</th>"
                      + "</tr></thead><tbody>");

            // Render each data
            for (SettingBean s : settings) {

                String key = s.getSettingKey();
                String value = HtmlUtil.escape(s.getSettingValue());
                String desc = HtmlUtil.escape(s.getDescription());

                String displayName = formatKey(s.getSettingKey());
                String inputType = isNumeric(s.getSettingValue()) ? "number" : "text";

                out.println("<tr>"
                          + "<td><strong>" + displayName + "</strong></td>"
                          + "<td><input type=\"" + inputType + "\" "
                          + "name=\"setting_" + key + "\" "
                          + "value=\"" + value + "\" "
                          + "class=\"form-control setting-value-input\" /></td>"
                          + "<td class=\"setting-description\">" + desc + "</td>"
                          + "</tr>");

            }

            out.println("</tbody></table>");

        } catch (IOException ex) {
            throw new JspException("Error in SettingsFormTag", ex);
        }

    }

    private String formatKey(String key) {

        if (key == null) {
            return "";
        }

        String[] parts = key.split("_");
        String label = "";
        
        for (String part : parts) {

            if (!label.isEmpty()) {
                label += " ";
            }
            label += Character.toUpperCase(part.charAt(0)) + part.substring(1);

        }

        return label;

    }

    private boolean isNumeric(String s) {

        if (s == null) {
            return false;
        }

        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
        
    }

}
