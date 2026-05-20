package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.IncidentBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.IncidentDAO;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "IncidentReportServlet", urlPatterns = {"/staff/incidents"})
public class IncidentReportServlet extends HttpServlet {

    private IncidentDAO incidentDAO = new IncidentDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        Integer clinicId = (Integer) session.getAttribute("clinicId");
        ArrayList<IncidentBean> incidents = incidentDAO.getByReporter(userId);
        request.setAttribute("viewClinicId", resolveClinicId(request, role, clinicId));
        request.setAttribute("isAdmin", "Admin".equals(role));
        request.setAttribute("incidents", incidents);
        request.getRequestDispatcher("/WEB-INF/views/staff/incidents.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int userId  = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        Integer sessionClinicId = (Integer) session.getAttribute("clinicId");
        int clinicId = resolveClinicId(request, role, sessionClinicId);

        String category = StringUtil.trimToEmpty(request.getParameter("category"));
        String severity = StringUtil.trimToEmpty(request.getParameter("severity"));
        String description = StringUtil.trimToEmpty(request.getParameter("description"));
        Integer patientId = StringUtil.parseIntOrNull(request.getParameter("patientId"));
        Integer appointmentId = StringUtil.parseIntOrNull(request.getParameter("appointmentId"));

        if (description.isEmpty()) {
            session.setAttribute("staffError", "Incident description is required.");
            response.sendRedirect(incidentRedirectUrl(request, role, clinicId));
            return;
        }
        if (category.isEmpty()) category = "Other";
        if (severity.isEmpty()) severity = "Low";

        int newId = incidentDAO.createIncident(
                userId, clinicId, patientId, appointmentId, category, severity, description);
        if (newId > 0) {
            session.setAttribute("staffMsg", "Incident #" + newId + " reported.");
            auditLogDAO.logAction(userId,
                "Reported incident #" + newId + " (" + severity + " / " + category + ")");
        } else {
            session.setAttribute("staffError", "Failed to record incident. Please try again.");
        }
        response.sendRedirect(incidentRedirectUrl(request, role, clinicId));
    }

    private String incidentRedirectUrl(HttpServletRequest request, String role, int clinicId) {
        String url = request.getContextPath() + "/staff/incidents";
        if ("Admin".equals(role)) {
            url += "?clinic=" + clinicId;
        }
        return url;
    }

    private int resolveClinicId(HttpServletRequest request, String role, Integer staffClinicId) {
        if ("Admin".equals(role)) {
            ArrayList<ClinicBean> allClinics = clinicDAO.getAllClinics();
            request.setAttribute("allClinics", allClinics);

            String clinicParam = request.getParameter("clinic");
            if (clinicParam != null && !clinicParam.isEmpty()) {
                try {
                    return Integer.parseInt(clinicParam);
                } catch (NumberFormatException ignore) {}
            }
            if (staffClinicId != null && staffClinicId > 0) return staffClinicId;
            if (!allClinics.isEmpty()) return allClinics.get(0).getClinicId();
            return 1;
        }
        return staffClinicId;
    }

}
