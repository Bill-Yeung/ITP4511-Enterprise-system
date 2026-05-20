package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.NotificationDAO;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

// Servlet to display overall dashboard for admin
@WebServlet(name = "OverallDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class OverallDashboardServlet extends HttpServlet {

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        int userId = (Integer) session.getAttribute("userId");
        Integer clinicId = (Integer) session.getAttribute("clinicId");
        String adminLevel = (String) session.getAttribute("adminLevel");

        // Limit access for clinic-level admin
        Integer scopeClinicId = "Clinic".equals(adminLevel) ? clinicId : null;

        // Status counts

        int[] statusCounts = appointmentDAO.getStatusCounts(scopeClinicId);
        request.setAttribute("totalAppointments", statusCounts[0]);
        request.setAttribute("booked", statusCounts[1]);
        request.setAttribute("arrived", statusCounts[2]);
        request.setAttribute("completed", statusCounts[3]);
        request.setAttribute("noshow", statusCounts[4]);
        request.setAttribute("cancelled", statusCounts[5]);

        int todayCount = appointmentDAO.getTodayCount(scopeClinicId);
        request.setAttribute("todayCount", todayCount);

        // User counts

        if (scopeClinicId == null) {
            int totalPatients = userDAO.getUsersByRole("Patient").size();
            int totalStaff = userDAO.getUsersByRole("Staff").size();
            request.setAttribute("totalPatients", totalPatients);
            request.setAttribute("totalStaff", totalStaff);
        }

        // Clinic counts

        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();
        request.setAttribute("totalClinics", clinics.size());

        request.setAttribute("adminLevel", adminLevel);
        request.setAttribute("notifList", notificationDAO.getByUserId(userId));
        request.setAttribute("unreadCount", notificationDAO.countUnread(userId));

        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")
               .forward(request, response);

    }
}
