package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.util.NotificationHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

// Servlet for managing patients' checkin
@WebServlet(name = "CheckinServlet", urlPatterns = {"/staff/checkin"})
public class CheckinServlet extends HttpServlet {

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");

        Integer staffClinicId = (Integer) session.getAttribute("clinicId");
        int viewClinicId;

        if ("Admin".equals(role)) {

            ArrayList<ClinicBean> allClinics = clinicDAO.getAllClinics();
            request.setAttribute("allClinics", allClinics);

            if (staffClinicId != null && staffClinicId > 0) {
                // clinic-level admin
                viewClinicId = staffClinicId;
            } else {
                // system-level admin
                String clinicParam = request.getParameter("clinic");
                viewClinicId = 1;
                if (clinicParam != null && !clinicParam.isEmpty()) {
                    try {
                        viewClinicId = Integer.parseInt(clinicParam);
                    } catch (NumberFormatException e) {
                        viewClinicId = 1;
                    }
                }
            }

        } else {
            viewClinicId = staffClinicId;
        }

        // Load today appointments for checkin

        Date today = new Date(System.currentTimeMillis());
        ArrayList<AppointmentBean> appointments = appointmentDAO.getAppointmentsByClinicAndDate(viewClinicId, today);

        request.setAttribute("appointments", appointments);
        request.setAttribute("viewDate", today.toString());
        request.setAttribute("viewClinicId", viewClinicId);
        request.setAttribute("isAdmin", "Admin".equals(role));

        request.getRequestDispatcher("/WEB-INF/views/staff/checkin.jsp")
               .forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        int staffUserId = (Integer) session.getAttribute("userId");

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        try {

            int appointmentId = Integer.parseInt(idParam);
            AppointmentBean appt = appointmentDAO.getAppointmentById(appointmentId);

            if (appt == null) {
                session.setAttribute("staffError", "Appointment not found.");
            } else {

                switch (action) {
                    case "checkin":
                        handleCheckin(appointmentId, appt, staffUserId, session);
                        break;
                    case "complete":
                        handleComplete(appointmentId, appt, staffUserId, session);
                        break;
                    case "noshow":
                        handleNoShow(appointmentId, appt, staffUserId, session);
                        break;
                    case "cancel":
                        String reason = request.getParameter("reason");
                        if (reason == null || reason.trim().isEmpty()) {
                            reason = "Cancelled by clinic";
                        }
                        handleCancel(appointmentId, appt, staffUserId, reason.trim(), session);
                        break;
                    default:
                        session.setAttribute("staffError", "Unknown appointment action.");
                }

            }
        } catch (NumberFormatException e) {
            session.setAttribute("staffError", "Invalid appointment ID.");
        }

        // Redirect after database modified

        String redirect = request.getContextPath() + "/staff/checkin";
        String clinicParam = request.getParameter("clinic");
        if (clinicParam != null && !clinicParam.isEmpty()) {
            redirect += "?clinic=" + clinicParam;
        }
        response.sendRedirect(redirect);

    }

    private void handleCheckin(int appointmentId, AppointmentBean appt, int staffUserId, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.markArrived(appointmentId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Patient checked in.");
            auditLogDAO.logAction(staffUserId, "Check-in action: checkin on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Checked in for your appointment on " + appt.getAppointmentDate() + " " + appt.getTimeSlot() + " - please wait to be called");
        } else {
            session.setAttribute("staffError", "Check-in failed.");
        }

    }

    private void handleComplete(int appointmentId, AppointmentBean appt, int staffUserId, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.markCompleted(appointmentId, null);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Marked as Completed.");
            auditLogDAO.logAction(staffUserId, "Check-in action: complete on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Visit completed for appointment on " + appt.getAppointmentDate() + " " + appt.getTimeSlot() + " - thank you");
        } else {
            session.setAttribute("staffError", "Update failed.");
        }

    }

    private void handleNoShow(int appointmentId, AppointmentBean appt, int staffUserId, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.markNoShow(appointmentId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Marked as No-show.");
            auditLogDAO.logAction(staffUserId, "Check-in action: noshow on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Missed appointment on " + appt.getAppointmentDate() + " " + appt.getTimeSlot() + " - repeated no-shows may affect future booking");
        } else {
            session.setAttribute("staffError", "Update failed.");
        }

    }

    private void handleCancel(int appointmentId, AppointmentBean appt, int staffUserId,
                              String reason, HttpSession session) {

        boolean isUpdated = appointmentDAO.cancelAppointment(appointmentId, reason, staffUserId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Appointment cancelled.");
            auditLogDAO.logAction(staffUserId, "Check-in action: cancel on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Appointment cancelled by clinic - " + reason);
        } else {
            session.setAttribute("staffError", "Cancel failed.");
        }

    }

}
