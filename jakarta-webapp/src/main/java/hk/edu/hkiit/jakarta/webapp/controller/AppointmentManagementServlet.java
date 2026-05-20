package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.util.NotificationHelper;
import hk.edu.hkiit.jakarta.webapp.util.SlotUpdateBroadcaster;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

// Servlet for staff and admin appointment operations
@WebServlet(name = "AppointmentManagementServlet", urlPatterns = {"/staff/appointments"})
public class AppointmentManagementServlet extends HttpServlet {

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        String role = (String) session.getAttribute("role");
        Integer staffClinicId = (Integer) session.getAttribute("clinicId");

        // Manage differences between staff (can only see certain clinic)
        // and admin (can see certain clinic or all clinics)

        int clinicId;
        if ("Admin".equals(role)) {

            ArrayList<ClinicBean> allClinics = clinicDAO.getAllClinics();
            request.setAttribute("allClinics", allClinics);

            // Redirect to error page if clinic not found
            if (allClinics.isEmpty()) {
                request.setAttribute("errorTitle", "No Clinics Available");
                request.setAttribute("errorMsg", "No clinic records are available. Please create a clinic before viewing appointments.");
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

        // Validate date range (default is today to today)
        Date today = new Date(System.currentTimeMillis());
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate;
        Date endDate;

        if (startDateParam != null && !startDateParam.isEmpty()) {
            try {
                startDate = Date.valueOf(startDateParam);
            } catch (IllegalArgumentException e) {
                startDate = today;
            }
        } else {
            startDate = today;
        }

        if (endDateParam != null && !endDateParam.isEmpty()) {
            try {
                endDate = Date.valueOf(endDateParam);
            } catch (IllegalArgumentException e) {
                endDate = startDate;
            }
        } else {
            endDate = startDate;
        }

        if (endDate.before(startDate)) {
            endDate = startDate;
        }

        // Get both pending approval and confirmed appointments

        ArrayList<AppointmentBean> appointments = appointmentDAO.getAppointmentsByClinicAndDateRange(clinicId, startDate, endDate);
        ArrayList<AppointmentBean> pending = appointmentDAO.getPendingByClinic(clinicId);
        ArrayList<ClinicServiceBean> clinicServices = clinicDAO.getServicesByClinic(clinicId);
        ArrayList<String[]> doctors = clinicDAO.getDoctorsForClinicPicker(clinicId);

        request.setAttribute("appointments", appointments);
        request.setAttribute("pendingAppointments", pending);
        request.setAttribute("clinicServices", clinicServices);
        request.setAttribute("doctors", doctors);
        request.setAttribute("startDate", startDate.toString());
        request.setAttribute("endDate", endDate.toString());
        request.setAttribute("viewClinicId", clinicId);
        request.setAttribute("isAdmin", "Admin".equals(role));

        request.getRequestDispatcher("/WEB-INF/views/staff/appointments.jsp")
               .forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        int staffUserId = (Integer) session.getAttribute("userId");
        Integer staffClinicId = (Integer) session.getAttribute("clinicId");

        int clinicId = staffClinicId == null ? 1 : staffClinicId;
        if ("Admin".equals(role)) {
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
                }

            }
        }

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
                            reason = "Cancelled by clinic staff";
                        }
                        handleCancel(appointmentId, appt, staffUserId, reason.trim(), session);
                        break;
                    case "approve":
                        handleApprove(appointmentId, appt, staffUserId, session);
                        break;
                    case "reject":
                        String rejectReason = request.getParameter("reason");
                        if (rejectReason == null || rejectReason.trim().isEmpty()) {
                            rejectReason = "Rejected by clinic staff";
                        }
                        handleReject(appointmentId, appt, staffUserId, rejectReason.trim(), session);
                        break;
                    case "update":
                        handleUpdate(request, appointmentId, appt, clinicId, staffUserId, session);
                        break;
                    default:
                        session.setAttribute("staffError", "Unknown appointment action.");

                }
            }
        } catch (NumberFormatException e) {
            session.setAttribute("staffError", "Invalid appointment ID.");
        }

        // Redirect to same page after handling different types of actions

        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        String redirectUrl = request.getContextPath() + "/staff/appointments";
        String separator = "?";

        if (startDateParam != null && !startDateParam.isEmpty()) {
            redirectUrl += separator + "startDate=" + startDateParam;
            separator = "&";
        }
        if (endDateParam != null && !endDateParam.isEmpty()) {
            redirectUrl += separator + "endDate=" + endDateParam;
            separator = "&";
        }
        if ("Admin".equals(role)) {
            redirectUrl += separator + "clinic=" + clinicId;
        }
        response.sendRedirect(redirectUrl);

    }

    private void handleCheckin(int appointmentId, AppointmentBean appt, int staffUserId, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.markArrived(appointmentId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Patient checked in.");
            auditLogDAO.logAction(staffUserId, "Staff action: checkin on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "You have been checked in at " + appt.getClinicName() + ".");
        } else {
            session.setAttribute("staffError", "Check-in failed. Patient may not be in Booked status.");
        }

    }

    private void handleComplete(int appointmentId, AppointmentBean appt, int staffUserId, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.markCompleted(appointmentId, null);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Appointment marked as Completed.");
            auditLogDAO.logAction(staffUserId, "Staff action: complete on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Your visit at " + appt.getClinicName() + " is marked as completed.");
        } else {
            session.setAttribute("staffError", "Update failed.");
        }

    }

    private void handleNoShow(int appointmentId, AppointmentBean appt, int staffUserId, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.markNoShow(appointmentId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Marked as No-show.");
            auditLogDAO.logAction(staffUserId, "Staff action: noshow on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "You were marked No-show for your appointment on " + appt.getAppointmentDate() + " " + appt.getTimeSlot() + ".");
        } else {
            session.setAttribute("staffError", "Update failed.");
        }

    }

    private void handleCancel(int appointmentId, AppointmentBean appt, int staffUserId,
                              String reason, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.cancelAppointment(appointmentId, reason, staffUserId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Appointment cancelled.");
            auditLogDAO.logAction(staffUserId, "Staff action: cancel on appointment #" + appointmentId);
            // Release of slots
            SlotUpdateBroadcaster.notifySlotChange(appt.getClinicServiceId(), appt.getAppointmentDate().toString());
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Your appointment on " + appt.getAppointmentDate() + " " + appt.getTimeSlot() + " was cancelled by clinic staff - " + reason);
        } else {
            session.setAttribute("staffError", "Cancel failed.");
        }

    }

    private void handleApprove(int appointmentId, AppointmentBean appt, int staffUserId, HttpSession session) {
        
        boolean isUpdated = appointmentDAO.approveAppointment(appointmentId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Booking approved.");
            auditLogDAO.logAction(staffUserId, "Staff action: approve on appointment #" + appointmentId);
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Your booking request for " + appt.getServiceName() + " at " + appt.getClinicName() + " on " + appt.getAppointmentDate() + " " + appt.getTimeSlot() + " has been approved.");
        } else {
            session.setAttribute("staffError", "Approve failed (request may already be processed).");
        }

    }

    private void handleReject(int appointmentId, AppointmentBean appt, int staffUserId,
                              String reason, HttpSession session) {

        boolean isUpdated = appointmentDAO.rejectAppointment(appointmentId, reason, staffUserId);
        if (isUpdated) {
            session.setAttribute("staffMsg", "Booking rejected.");
            auditLogDAO.logAction(staffUserId, "Staff action: reject on appointment #" + appointmentId);
            // Release of slots
            SlotUpdateBroadcaster.notifySlotChange(appt.getClinicServiceId(), appt.getAppointmentDate().toString());
            NotificationHelper.notify(appt.getPatientId(), "Appointment", "Your booking request on " + appt.getAppointmentDate() + " " + appt.getTimeSlot() + " was rejected - " + reason);
        } else {
            session.setAttribute("staffError", "Reject failed.");
        }

    }

    private void handleUpdate(HttpServletRequest request, int appointmentId, AppointmentBean appt,
                              int clinicId, int staffUserId, HttpSession session) {

        try {

            int clinicServiceId = Integer.parseInt(request.getParameter("clinicServiceId"));
            Date appointmentDate = Date.valueOf(request.getParameter("appointmentDate"));
            String timeValue = request.getParameter("timeSlot");
            Time timeSlot = Time.valueOf(timeValue.length() == 5 ? timeValue + ":00" : timeValue);
            String status = request.getParameter("status");
            String remarks = request.getParameter("remarks");
            String doctorParam = request.getParameter("doctorId");
            Integer doctorId = null;
            if (doctorParam != null && !doctorParam.trim().isEmpty()) {
                doctorId = Integer.parseInt(doctorParam);
            }

            // Prevent unexpected changes for appointments

            if (!isAppointmentStatus(status)) {
                session.setAttribute("staffError", "Invalid appointment status.");
                return;
            }

            ClinicServiceBean currentClinicService = clinicDAO.getClinicServiceById(appt.getClinicServiceId());
            if (currentClinicService == null || currentClinicService.getClinicId() != clinicId) {
                session.setAttribute("staffError", "Appointment is not available for this clinic.");
                return;
            }

            ClinicServiceBean clinicService = clinicDAO.getClinicServiceById(clinicServiceId);
            if (clinicService == null || clinicService.getClinicId() != clinicId) {
                session.setAttribute("staffError", "Selected service is not available for this clinic.");
                return;
            }

            // Update database and broadcast changes if successful

            boolean isUpdated = appointmentDAO.updateAppointmentDetails(appointmentId, clinicServiceId, doctorId, appointmentDate, timeSlot, status, remarks);
            if (isUpdated) {
                session.setAttribute("staffMsg", "Appointment updated.");
                auditLogDAO.logAction(staffUserId, "Staff action: update appointment #" + appointmentId);
                SlotUpdateBroadcaster.notifySlotChange(appt.getClinicServiceId(), appt.getAppointmentDate().toString());
                SlotUpdateBroadcaster.notifySlotChange(clinicServiceId, appointmentDate.toString());
                NotificationHelper.notify(appt.getPatientId(), "Appointment", "Your appointment was updated to " + appointmentDate + " " + timeSlot + ".");
            } else {
                session.setAttribute("staffError", "Update failed.");
            }
            
        } catch (Exception ex) {
            session.setAttribute("staffError", "Invalid appointment update details.");
        }

    }

    private boolean isAppointmentStatus(String status) {
        return "Pending".equals(status)
            || "Booked".equals(status)
            || "Arrived".equals(status)
            || "Completed".equals(status)
            || "No-show".equals(status)
            || "Cancelled".equals(status);
    }

}
