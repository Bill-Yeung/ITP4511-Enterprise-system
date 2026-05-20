package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.SettingBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.SettingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

// Servlet for settings different various items including cutoff and max quota
@WebServlet(name = "SettingsServlet", urlPatterns = {"/admin/settings"})
public class SettingsServlet extends HttpServlet {

    private SettingDAO settingDAO = new SettingDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Only system-level admin can view settings
        String adminLevel = (String) session.getAttribute("adminLevel");
        if (!"System".equals(adminLevel)) {
            session.setAttribute("adminError", "Only System administrators can manage settings.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        ArrayList<SettingBean> settings = settingDAO.getAllSettings();
        request.setAttribute("settings", settings);

        request.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp")
               .forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Only system-level admin can change settings
        String adminLevel = (String) session.getAttribute("adminLevel");
        if (!"System".equals(adminLevel)) {
            session.setAttribute("adminError", "Only System administrators can manage settings.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        ArrayList<SettingBean> currentSettings = settingDAO.getAllSettings();
        int userId = (int) session.getAttribute("userId");
        int updated = 0;

        for (SettingBean setting : currentSettings) {

            String key = setting.getSettingKey();
            String newValue = request.getParameter("setting_" + key);

            if (newValue != null && !newValue.equals(setting.getSettingValue())) {
                settingDAO.updateSetting(key, newValue.trim());
                auditLogDAO.logAction(userId, "Updated setting '" + key + "' from '" + setting.getSettingValue() + "' to '" + newValue.trim() + "'");
                updated++;
            }
        }

        if (updated > 0) {
            session.setAttribute("adminMsg", updated + " setting(s) updated successfully.");
        } else {
            session.setAttribute("adminMsg", "No changes were made.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/settings");

    }
    
}
