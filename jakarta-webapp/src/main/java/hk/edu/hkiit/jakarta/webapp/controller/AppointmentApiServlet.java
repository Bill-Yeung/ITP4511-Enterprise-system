package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.SettingDAO;
import hk.edu.hkiit.jakarta.webapp.util.JsonUtil;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Servlet for patient appointment operations
@WebServlet(name = "AppointmentApiServlet", urlPatterns = {"/api/appointments"})
public class AppointmentApiServlet extends HttpServlet {

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();
    private SettingDAO settingDAO = new SettingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        ArrayList<AppointmentBean> list = appointmentDAO.getAppointmentsByPatient(userId);

        // Construct and return Json data

        ArrayList<Map<String, Object>> jsonList = new ArrayList<>();
        for (AppointmentBean appointment : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("appointmentId", appointment.getAppointmentId());
            row.put("clinicServiceId", appointment.getClinicServiceId());
            row.put("clinicName", appointment.getClinicName());
            row.put("serviceName", appointment.getServiceName());
            row.put("doctorName", appointment.getDoctorName());
            row.put("appointmentDate", appointment.getAppointmentDate().toString());
            row.put("timeSlot", appointment.getTimeSlot().toString());
            row.put("status", appointment.getStatus());
            row.put("cancelReason", appointment.getCancelReason());
            row.put("remarks", appointment.getRemarks());
            row.put("createdAt", appointment.getCreatedAt() != null ? appointment.getCreatedAt().toString() : "");
            jsonList.add(row);
        }
        JsonUtil.writeJson(response, jsonList);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");

        if (action == null) {
            JsonUtil.writeError(response, 400, "action parameter required");
            return;
        }

        // Three actions allowed for patient: book, cancel and reschedule

        switch (action) {

            case "book":
                handleBook(request, response, userId);
                break;
            case "cancel":
                handleCancel(request, response, userId);
                break;
            case "reschedule":
                handleReschedule(request, response, userId);
                break;
            default:
                JsonUtil.writeError(response, 400, "Unknown action: " + action);

        }

    }

    private void handleBook(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        String csIdStr = request.getParameter("clinicServiceId");
        String dateStr = request.getParameter("date");
        String timeStr = request.getParameter("time");
        String doctorStr = request.getParameter("doctorId");

        if (csIdStr == null || dateStr == null || timeStr == null) {
            JsonUtil.writeError(response, 400, "clinicServiceId, date, and time are required");
            return;
        }

        int clinicServiceId;
        LocalDate localDate;
        Time timeSlot;
        Integer doctorId = null;

        // Validation of parameters
        try {
            clinicServiceId = Integer.parseInt(csIdStr);
            localDate = LocalDate.parse(dateStr);
            timeSlot = Time.valueOf(timeStr.length() == 5 ? timeStr + ":00" : timeStr);
            if (doctorStr != null && !doctorStr.isEmpty()) {
                doctorId = Integer.parseInt(doctorStr);
            }
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, "Invalid parameter format");
            return;
        }

        Date sqlDate = Date.valueOf(localDate);

        // Block appointments that are before current system time

        if (localDate.isBefore(LocalDate.now())) {
            JsonUtil.writeError(response, 400, "Appointment date must be today or in the future");
            return;
        }

        if (localDate.equals(LocalDate.now()) && timeSlot.toLocalTime().isBefore(java.time.LocalTime.now())) {
            JsonUtil.writeError(response, 400, "Selected time has already passed today");
            return;
        }

        // Block too many active bookings for one patient

        int maxActiveBookings = settingDAO.getIntValue("max_active_bookings", 3);
        int activeCount = appointmentDAO.countActiveBookings(userId);
        if (activeCount >= maxActiveBookings) {
            JsonUtil.writeError(response, 400, "You have reached the maximum of " + maxActiveBookings + " active bookings");
            return;
        }

        // Prevent more than one booking for patient at the same time

        if (appointmentDAO.isSlotTaken(userId, sqlDate, timeSlot)) {
            JsonUtil.writeError(response, 400, "You already have an appointment at this date and time");
            return;
        }

        // Block appointments when no such services are provided or already full

        ClinicServiceBean clinicService = clinicDAO.getClinicServiceById(clinicServiceId);
        if (clinicService == null) {
            JsonUtil.writeError(response, 404, "Clinic service not found");
            return;
        }

        String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (!clinicDAO.isWithinOperatingHours(clinicService.getClinicId(), dayName, timeSlot)) {
            JsonUtil.writeError(response, 400, "Selected time is outside clinic operating hours");
            return;
        }

        if (doctorId != null && !clinicDAO.isDoctorAvailable(doctorId, clinicService.getClinicId(), dayName, timeSlot)) {
            JsonUtil.writeError(response, 400, "Selected doctor is not available for this clinic and time");
            return;
        }

        int booked = appointmentDAO.getSlotBookingCount(clinicServiceId, sqlDate, timeSlot);
        if (booked >= clinicService.getQuotaPerSlot()) {
            JsonUtil.writeError(response, 400, "This time slot is fully booked");
            return;
        }

        // Perform the booking in database

        boolean needsApproval = clinicService.isRequiresApproval();
        String initialStatus = needsApproval ? "Pending" : "Booked";
        int appointmentId = appointmentDAO.bookAppointment(userId, clinicServiceId, doctorId, sqlDate, timeSlot, initialStatus);
        
        // Handle database error
        if (appointmentId == -1) {
            JsonUtil.writeError(response, 500, "Failed to book appointment. Please try again.");
            return;
        }

        // SSE notification for both notifications and booking slots

        SlotUpdateBroadcaster.notifySlotChange(clinicServiceId, dateStr);
        if (needsApproval) {
            NotificationHelper.notify(userId, "Appointment",
                "Booking request submitted for " + clinicService.getServiceName()
                + " at " + clinicService.getClinicName()
                + " on " + dateStr + " " + timeStr
                + " - awaiting staff approval.");
            NotificationHelper.notifyClinicStaff(clinicService.getClinicId(), "Appointment",
                "New booking request for " + clinicService.getServiceName()
                + " on " + dateStr + " " + timeStr + " - awaiting approval.");
        } else {
            NotificationHelper.notify(userId, "Appointment",
                "Appointment confirmed at " + clinicService.getClinicName()
                + " for " + clinicService.getServiceName()
                + " on " + dateStr + " " + timeStr);
            NotificationHelper.notifyClinicStaff(clinicService.getClinicId(), "Appointment",
                "New appointment booked for " + clinicService.getServiceName()
                + " on " + dateStr + " " + timeStr + ".");
        }

        // Log and return result

        auditLogDAO.logAction(userId,
            (needsApproval ? "Submitted Pending appointment #" : "Booked appointment #")
            + appointmentId + " on " + dateStr + " at " + timeStr);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appointmentId", appointmentId);
        result.put("status", initialStatus);
        JsonUtil.writeJson(response, result);

    }

    private void handleCancel(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        String idStr = request.getParameter("appointmentId");
        String reason = request.getParameter("reason");

        if (idStr == null) {
            JsonUtil.writeError(response, 400, "appointmentId required");
            return;
        }

        int appointmentId;

        // Validation of parameters
        try {
            appointmentId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(response, 400, "Invalid appointment ID");
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            reason = "Cancelled by patient";
        }

        // Prevent unexpected operations on bookings

        AppointmentBean appointment = appointmentDAO.getAppointmentById(appointmentId);
        if (appointment == null || appointment.getPatientId() != userId) {
            JsonUtil.writeError(response, 403, "Appointment not found or not yours");
            return;
        }

        if (!"Booked".equals(appointment.getStatus()) && !"Pending".equals(appointment.getStatus())) {
            JsonUtil.writeError(response, 400, "Only booked or pending appointments can be cancelled");
            return;
        }

        // Block cancellation of services after cutoff time

        LocalDateTime appointmentDateTime = appointment.getAppointmentDate().toLocalDate().atTime(appointment.getTimeSlot().toLocalTime());
        int cancellationCutoffHours = settingDAO.getIntValue("cancellation_cutoff_hours", 2);
        if (LocalDateTime.now().plusHours(cancellationCutoffHours).isAfter(appointmentDateTime)) {
            JsonUtil.writeError(response, 400, "Cannot cancel within " + cancellationCutoffHours + " hours of appointment time");
            return;
        }

        // Cancel the booking in database

        boolean isCancelled = appointmentDAO.cancelAppointment(appointmentId, reason, userId);
        
        // Handle database error
        if (!isCancelled) {
            JsonUtil.writeError(response, 500, "Cancellation failed. Please try again.");
            return;
        }

        // SSE notification for both notifications and booking slots

        SlotUpdateBroadcaster.notifySlotChange(appointment.getClinicServiceId(), appointment.getAppointmentDate().toString());

        NotificationHelper.notify(userId, "Appointment",
            "Appointment on " + appointment.getAppointmentDate() + " "
            + appointment.getTimeSlot() + " cancelled - " + reason);
        ClinicServiceBean cancelledCs = clinicDAO.getClinicServiceById(appointment.getClinicServiceId());
        if (cancelledCs != null) {
            NotificationHelper.notifyClinicStaff(cancelledCs.getClinicId(), "Appointment",
                "Patient cancelled appointment on " + appointment.getAppointmentDate()
                + " " + appointment.getTimeSlot() + " - " + reason);
        }

        // Log and return result

        auditLogDAO.logAction(userId, "Cancelled appointment #" + appointmentId + " - " + reason);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        JsonUtil.writeJson(response, result);

    }

    private void handleReschedule(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        String oldIdStr = request.getParameter("appointmentId");
        String csIdStr = request.getParameter("clinicServiceId");
        String dateStr = request.getParameter("date");
        String timeStr = request.getParameter("time");
        String doctorStr = request.getParameter("doctorId");

        if (oldIdStr == null || csIdStr == null || dateStr == null || timeStr == null) {
            JsonUtil.writeError(response, 400, "appointmentId, clinicServiceId, date, and time are required");
            return;
        }

        int oldAppointmentId;
        int clinicServiceId;
        LocalDate localDate;
        Time timeSlot;
        Integer doctorId = null;

        // Validation of parameters
        try {
            oldAppointmentId = Integer.parseInt(oldIdStr);
            clinicServiceId = Integer.parseInt(csIdStr);
            localDate = LocalDate.parse(dateStr);
            timeSlot = Time.valueOf(timeStr.length() == 5 ? timeStr + ":00" : timeStr);
            if (doctorStr != null && !doctorStr.isEmpty()) {
                doctorId = Integer.parseInt(doctorStr);
            }
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, "Invalid parameter format");
            return;
        }

        Date sqlDate = Date.valueOf(localDate);

        // Prevent unexpected operations on bookings

        AppointmentBean oldAppointment = appointmentDAO.getAppointmentById(oldAppointmentId);
        if (oldAppointment == null || oldAppointment.getPatientId() != userId) {
            JsonUtil.writeError(response, 403, "Appointment not found or not yours");
            return;
        }

        if (!"Booked".equals(oldAppointment.getStatus())) {
            JsonUtil.writeError(response, 400, "Only booked appointments can be rescheduled");
            return;
        }

        // Block rescheduling of services after cutoff time

        LocalDateTime appointmentDateTime = oldAppointment.getAppointmentDate().toLocalDate().atTime(oldAppointment.getTimeSlot().toLocalTime());
        int rescheduleCutoffHours = settingDAO.getIntValue("reschedule_cutoff_hours", 24);
        if (LocalDateTime.now().plusHours(rescheduleCutoffHours).isAfter(appointmentDateTime)) {
            JsonUtil.writeError(response, 400, "Cannot reschedule within " + rescheduleCutoffHours + " hours of appointment time");
            return;
        }

        if (!localDate.isAfter(LocalDate.now())) {
            JsonUtil.writeError(response, 400, "New appointment date must be in the future");
            return;
        }

        // Block appointments when changing to another service or already full

        ClinicServiceBean clinicService = clinicDAO.getClinicServiceById(clinicServiceId);
        if (clinicService == null) {
            JsonUtil.writeError(response, 404, "Clinic service not found");
            return;
        }

        ClinicServiceBean oldClinicService = clinicDAO.getClinicServiceById(oldAppointment.getClinicServiceId());
        if (oldClinicService == null || oldClinicService.getServiceId() != clinicService.getServiceId()) {
            JsonUtil.writeError(response, 400, "Reschedule must keep the same service");
            return;
        }

        String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (!clinicDAO.isWithinOperatingHours(clinicService.getClinicId(), dayName, timeSlot)) {
            JsonUtil.writeError(response, 400, "Selected time is outside clinic operating hours");
            return;
        }

        if (doctorId != null && !clinicDAO.isDoctorAvailable(doctorId, clinicService.getClinicId(), dayName, timeSlot)) {
            JsonUtil.writeError(response, 400, "Selected doctor is not available for this clinic and time");
            return;
        }

        int booked = appointmentDAO.getSlotBookingCount(clinicServiceId, sqlDate, timeSlot);
        if (booked >= clinicService.getQuotaPerSlot()) {
            JsonUtil.writeError(response, 400, "New time slot is fully booked");
            return;
        }

        if (appointmentDAO.isSlotTaken(userId, sqlDate, timeSlot)) {
            JsonUtil.writeError(response, 400, "You already have an appointment at this date and time");
            return;
        }

        // Reschedule the booking in database

        int newAppointmentId = appointmentDAO.rescheduleAppointment(oldAppointmentId, userId, clinicServiceId, doctorId, sqlDate, timeSlot);
        
        // Handle database error
        if (newAppointmentId == -1) {
            JsonUtil.writeError(response, 500, "Reschedule failed. Please try again.");
            return;
        }

        // SSE notification for both notifications and booking slots

        SlotUpdateBroadcaster.notifySlotChange(oldAppointment.getClinicServiceId(), oldAppointment.getAppointmentDate().toString());
        SlotUpdateBroadcaster.notifySlotChange(clinicServiceId, dateStr);

        NotificationHelper.notify(userId, "Appointment",
            "Appointment moved to " + dateStr + " " + timeStr
            + " at " + clinicService.getClinicName());

        // Log and return result

        auditLogDAO.logAction(userId, "Rescheduled appointment #" + oldAppointmentId
                + " to #" + newAppointmentId + " on " + dateStr);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appointmentId", newAppointmentId);
        JsonUtil.writeJson(response, result);

    }

}
