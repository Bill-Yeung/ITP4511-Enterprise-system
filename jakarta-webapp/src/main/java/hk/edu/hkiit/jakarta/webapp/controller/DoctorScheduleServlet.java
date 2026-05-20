package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.DoctorScheduleBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.util.TimeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;

// Servlet for managing doctor schedule which is available for services
@WebServlet(name = "DoctorScheduleServlet", urlPatterns = {"/staff/doctor-schedules"})
public class DoctorScheduleServlet extends HttpServlet {

    private ClinicDAO clinicDAO = new ClinicDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        boolean isAdmin = "Admin".equals(role);
        String adminLevel = (String) session.getAttribute("adminLevel");
        Integer clinicId = (Integer) session.getAttribute("clinicId");
        boolean isSystemAdmin = isAdmin && "System".equals(adminLevel);

        ArrayList<DoctorScheduleBean> rows;
        ArrayList<String[]> doctors = new ArrayList<>();

        // Different access rights for clinic vs system level admin
        if (isSystemAdmin) {
            rows = clinicDAO.getAllDoctorSchedules();
            doctors = clinicDAO.getAllDoctorsForPicker();
        } else {
            rows = clinicDAO.getDoctorSchedulesByClinic(clinicId);
            if (isAdmin) {
                doctors = clinicDAO.getDoctorsForClinicPicker(clinicId);
            }
            request.setAttribute("viewClinicId", clinicId);
        }
        rows.sort((a, b) -> Integer.compare(a.getScheduleId(), b.getScheduleId()));

        request.setAttribute("rows", rows);
        request.setAttribute("doctors", doctors);
        request.setAttribute("isAdmin", isAdmin);
        request.setAttribute("isSystemAdmin", isSystemAdmin);
        request.getRequestDispatcher("/WEB-INF/views/staff/doctor-schedules.jsp")
           .forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        String adminLevel = (String) session.getAttribute("adminLevel");
        Integer clinicId = (Integer) session.getAttribute("clinicId");
        boolean isClinicAdmin = "Admin".equals(role) && "Clinic".equals(adminLevel);

        // Only allow admin to change doctor schedules
        if (!"Admin".equals(role)) {
            request.setAttribute("errorTitle", "Access Denied");
            request.setAttribute("errorMsg", "Only administrators can change doctor schedules.");
            request.setAttribute("backUrl", request.getContextPath() + "/staff/doctor-schedules");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp")
                   .forward(request, response);
            return;
        }

        int adminId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        if (action == null) action = "";

        try {

            switch (action) {
                case "add": {

                    int doctorId = Integer.parseInt(request.getParameter("doctorId"));
                    String day = request.getParameter("dayOfWeek");
                    Time start = TimeUtil.parseSqlTime(request.getParameter("startTime"));
                    Time end = TimeUtil.parseSqlTime(request.getParameter("endTime"));

                    if (isClinicAdmin && !clinicDAO.isDoctorInClinic(doctorId, clinicId)) {
                        session.setAttribute("adminError", "Doctor is not in your clinic.");
                        break;
                    }

                    if (start == null || end == null || day == null || day.isEmpty()) {
                        session.setAttribute("adminError", "Day, start, and end time are required.");
                        break;
                    }
                    if (!end.after(start)) {
                        session.setAttribute("adminError", "End time must be after start time.");
                        break;
                    }

                    boolean isAdded = clinicDAO.addDoctorSchedule(doctorId, day, start, end);
                    if (isAdded) {
                        session.setAttribute("adminMsg", "Schedule added.");
                        auditLogDAO.logAction(adminId, "Added doctor schedule (doctor #" + doctorId + ", " + day + ")");
                    } else {
                        session.setAttribute("adminError",
                            "Add failed (the doctor may already have a schedule for that day).");
                    }
                    break;
                }
                case "update": {

                    int scheduleId = Integer.parseInt(request.getParameter("scheduleId"));
                    Time start = TimeUtil.parseSqlTime(request.getParameter("startTime"));
                    Time end = TimeUtil.parseSqlTime(request.getParameter("endTime"));

                    if (isClinicAdmin && !clinicDAO.isDoctorScheduleInClinic(scheduleId, clinicId)) {
                        session.setAttribute("adminError", "Schedule is not in your clinic.");
                        break;
                    }

                    if (start == null || end == null || !end.after(start)) {
                        session.setAttribute("adminError", "Invalid time range.");
                        break;
                    }

                    boolean isUpdated = clinicDAO.updateDoctorSchedule(scheduleId, start, end);
                    session.setAttribute(isUpdated ? "adminMsg" : "adminError",
                                   isUpdated ? "Schedule updated." : "Update failed.");
                    if (isUpdated) auditLogDAO.logAction(adminId,
                            "Updated doctor schedule #" + scheduleId);
                    break;

                }
                case "delete": {

                    int scheduleId = Integer.parseInt(request.getParameter("scheduleId"));
                    if (isClinicAdmin && !clinicDAO.isDoctorScheduleInClinic(scheduleId, clinicId)) {
                        session.setAttribute("adminError", "Schedule is not in your clinic.");
                        break;
                    }
                    boolean isDeleted = clinicDAO.deleteDoctorSchedule(scheduleId);
                    session.setAttribute(isDeleted ? "adminMsg" : "adminError", isDeleted ? "Schedule removed." : "Delete failed.");
                    if (isDeleted) {
                        auditLogDAO.logAction(adminId, "Deleted doctor schedule #" + scheduleId);
                    }
                    break;
                }
                default:
                    session.setAttribute("adminError", "Unknown action.");
            }

        } catch (NumberFormatException nfe) {
            session.setAttribute("adminError", "Invalid numeric input.");
        }

        response.sendRedirect(request.getContextPath() + "/staff/doctor-schedules");

    }

}
