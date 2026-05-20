package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.bean.AuditLogBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Servlet for audit trail table in admin side
@WebServlet(name = "AuditTrailServlet", urlPatterns = {"/admin/audit"})
public class AuditTrailServlet extends HttpServlet {

    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String role = request.getParameter("role");
        String sort = request.getParameter("sort");
        HttpSession session = request.getSession(false);
        String adminLevel = (String) session.getAttribute("adminLevel");
        Integer clinicId = (Integer) session.getAttribute("clinicId");
        Integer scopeClinicId = "Clinic".equals(adminLevel) ? clinicId : null;

        if (role == null) {
            role = "";
        }
        if (sort == null) {
            sort = "id_asc";
        }

        ArrayList<AuditLogBean> logs = auditLogDAO.getFilteredLogs(role, sort, scopeClinicId);
        request.setAttribute("auditLogs", logs);
        request.setAttribute("selectedRole", role);
        request.setAttribute("selectedSort", sort);
        request.setAttribute("isClinicAdmin", "Clinic".equals(adminLevel));

        request.getRequestDispatcher("/WEB-INF/views/admin/audit.jsp")
               .forward(request, response);

    }

}
