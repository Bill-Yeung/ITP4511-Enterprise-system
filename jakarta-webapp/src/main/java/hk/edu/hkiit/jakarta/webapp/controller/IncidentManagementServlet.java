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

@WebServlet(name = "IncidentManagementServlet", urlPatterns = {"/admin/incidents"})
public class IncidentManagementServlet extends HttpServlet {

    private IncidentDAO incidentDAO = new IncidentDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer clinicId = StringUtil.parseIntOrNull(request.getParameter("clinicId"));
        String status = StringUtil.nullIfEmpty(request.getParameter("status"));
        String category = StringUtil.nullIfEmpty(request.getParameter("category"));
        HttpSession session = request.getSession(false);
        String adminLevel = (String) session.getAttribute("adminLevel");
        Integer adminClinicId = (Integer) session.getAttribute("clinicId");
        boolean isClinicAdmin = "Clinic".equals(adminLevel);

        if (isClinicAdmin) {
            clinicId = adminClinicId;
        }

        ArrayList<IncidentBean> incidents = incidentDAO.getFilteredIncidents(clinicId, status, category);
        ArrayList<String[]> summary = incidentDAO.getPatientIncidentSummary(isClinicAdmin ? adminClinicId : null);
        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();

        request.setAttribute("incidents", incidents);
        request.setAttribute("summary", summary);
        request.setAttribute("clinics", clinics);
        request.setAttribute("filterClinicId", clinicId);
        request.setAttribute("filterStatus", status);
        request.setAttribute("filterCategory", category);
        request.setAttribute("isClinicAdmin", isClinicAdmin);

        request.getRequestDispatcher("/WEB-INF/views/admin/incidents.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int adminId = (Integer) session.getAttribute("userId");
        String adminLevel = (String) session.getAttribute("adminLevel");
        Integer adminClinicId = (Integer) session.getAttribute("clinicId");
        Integer scopeClinicId = "Clinic".equals(adminLevel) ? adminClinicId : null;
        try {
            int incidentId = Integer.parseInt(request.getParameter("incidentId"));
            String newStatus = request.getParameter("status");
            String resolution = request.getParameter("resolutionNotes");
            if (newStatus == null) newStatus = "Open";

            boolean isUpdated = incidentDAO.updateStatus(incidentId, newStatus, resolution, scopeClinicId);
            if (isUpdated) {
                session.setAttribute("adminMsg", "Incident #" + incidentId + " set to " + newStatus + ".");
                auditLogDAO.logAction(adminId,
                    "Incident #" + incidentId + " marked " + newStatus);
            } else {
                session.setAttribute("adminError", "Update failed.");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("adminError", "Invalid incident ID.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/incidents");
    }

}
