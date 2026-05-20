package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.NotificationDAO;
import hk.edu.hkiit.jakarta.webapp.dao.WalkinQueueDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

// Servlet to display overall dashboard for staff
@WebServlet(name = "ClinicDashboardServlet", urlPatterns = {"/staff/dashboard"})
public class ClinicDashboardServlet extends HttpServlet {

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();
    private WalkinQueueDAO queueDAO = new WalkinQueueDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        Integer staffClinicId = (Integer) session.getAttribute("clinicId");

        int clinicId;
        if ("Admin".equals(role)) {

            ArrayList<ClinicBean> allClinics = clinicDAO.getAllClinics();
            request.setAttribute("allClinics", allClinics);

            // Redirect to error page if clinic not found
            if (allClinics.isEmpty()) {
                request.setAttribute("errorTitle", "No Clinics Available");
                request.setAttribute("errorMsg", "No clinic records are available. Please create a clinic before viewing the dashboard.");
                request.setAttribute("backUrl", request.getContextPath() + "/admin/clinics");
                request.getRequestDispatcher("/WEB-INF/views/error.jsp")
                       .forward(request, response);
                return;
            }

            if (staffClinicId != null && staffClinicId > 0) {
                // clinic-level admin
                clinicId = staffClinicId;
            } else {

                // system-level admin

                String clinicParam = request.getParameter("clinic");
                if (clinicParam != null && !clinicParam.isEmpty()) {
                    try {
                        clinicId = Integer.parseInt(clinicParam);
                    } catch (NumberFormatException e) {
                        clinicId = 1;
                    }
                } else {
                    clinicId = 1;
                }
                
            }
        } else {
            clinicId = staffClinicId;
        }

        int[] statusCounts = appointmentDAO.getTodayStatusCounts(clinicId);
        request.setAttribute("totalToday", statusCounts[0]);
        request.setAttribute("booked", statusCounts[1]);
        request.setAttribute("arrived", statusCounts[2]);
        request.setAttribute("completed", statusCounts[3]);
        request.setAttribute("noshow", statusCounts[4]);
        request.setAttribute("cancelled", statusCounts[5]);
        request.setAttribute("queueWaiting", queueDAO.countActiveTodayByClinic(clinicId));
        request.setAttribute("notifList", notificationDAO.getByUserId(userId));
        request.setAttribute("unreadCount", notificationDAO.countUnread(userId));
        request.setAttribute("viewClinicId", clinicId);
        request.setAttribute("isAdmin", "Admin".equals(role));

        request.getRequestDispatcher("/WEB-INF/views/staff/dashboard.jsp")
               .forward(request, response);

    }

}
