package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicHoursBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.bean.DoctorScheduleBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Servlet for showing clinic information to patients
@WebServlet(name = "ClinicApiServlet", urlPatterns = {"/api/clinics"})
public class ClinicApiServlet extends HttpServlet {

    private ClinicDAO clinicDAO = new ClinicDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":
                handleListClinics(response);
                break;
            case "services":
                handleServices(request, response);
                break;
            case "hours":
                handleHours(request, response);
                break;
            case "slots":
                handleSlots(request, response);
                break;
            case "doctors":
                handleDoctors(request, response);
                break;
            default:
                JsonUtil.writeError(response, 400, "Unknown action");
        }

    }

    private void handleListClinics(HttpServletResponse response) throws IOException {

        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();
        JsonUtil.writeJson(response, clinics);

    }

    private void handleServices(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String clinicIdStr = request.getParameter("clinicId");

        // Validate parameter
        if (clinicIdStr == null) {
            JsonUtil.writeError(response, 400, "clinicId required");
            return;
        }

        int clinicId = Integer.parseInt(clinicIdStr);
        ArrayList<ClinicServiceBean> services = clinicDAO.getServicesByClinic(clinicId);
        JsonUtil.writeJson(response, services);
        
    }

    private void handleHours(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String clinicIdStr = request.getParameter("clinicId");

        // Validate parameter
        if (clinicIdStr == null) {
            JsonUtil.writeError(response, 400, "clinicId required");
            return;
        }

        int clinicId = Integer.parseInt(clinicIdStr);
        ArrayList<ClinicHoursBean> hours = clinicDAO.getClinicHours(clinicId);

        ArrayList<Map<String, Object>> rows = new ArrayList<>();
        for (ClinicHoursBean hour : hours) {
            Map<String, Object> row = new HashMap<>();
            row.put("dayOfWeek", hour.getDayOfWeek());
            row.put("openTime", hour.getOpenTime().toString());
            row.put("closeTime", hour.getCloseTime().toString());
            rows.add(row);
        }
        JsonUtil.writeJson(response, rows);

    }

    // Generate available time slots after users select clinic, service and date
    private void handleSlots(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String csIdStr = request.getParameter("clinicServiceId");
        String dateStr = request.getParameter("date");

        // Validate parameter
        if (csIdStr == null || dateStr == null) {
            JsonUtil.writeError(response, 400, "clinicServiceId and date required");
            return;
        }

        int clinicServiceId = Integer.parseInt(csIdStr);
        LocalDate localDate = LocalDate.parse(dateStr);
        Date sqlDate = Date.valueOf(localDate);

        ClinicServiceBean cs = clinicDAO.getClinicServiceById(clinicServiceId);
        if (cs == null) {
            JsonUtil.writeError(response, 404, "Clinic service not found");
            return;
        }

        // Get day of week name and operating sessions

        String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        ArrayList<ClinicHoursBean> sessions = clinicDAO.getClinicHoursByDay(cs.getClinicId(), dayName);

        int durationMins = cs.getSlotDurationMins();
        int quota = cs.getQuotaPerSlot();

        ArrayList<Map<String, Object>> rows = new ArrayList<>();

        for (ClinicHoursBean session : sessions) {

            long openMillis = session.getOpenTime().getTime();
            long closeMillis = session.getCloseTime().getTime();
            long intervalMs = durationMins * 60 * 1000L;

            for (long t = openMillis; t + intervalMs <= closeMillis; t += intervalMs) {
                
                Time slotTime = new Time(t);
                int booked = appointmentDAO.getSlotBookingCount(clinicServiceId, sqlDate, slotTime);
                int remaining = quota - booked;

                Map<String, Object> row = new HashMap<>();
                row.put("time", slotTime.toString());
                row.put("booked", booked);
                row.put("quota", quota);
                row.put("remaining", remaining);
                rows.add(row);

            }

        }
        JsonUtil.writeJson(response, rows);
    }

    // Return available doctors for patient to choose on the selected date
    private void handleDoctors(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String clinicIdStr = request.getParameter("clinicId");
        String dateStr = request.getParameter("date");
        
        // Validate parameters
        if (clinicIdStr == null) {
            JsonUtil.writeError(response, 400, "clinicId required");
            return;
        }
        if (dateStr == null) {
            JsonUtil.writeError(response, 400, "date required");
            return;
        }

        int clinicId = Integer.parseInt(clinicIdStr);
        LocalDate localDate = LocalDate.parse(dateStr);
        String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        ArrayList<DoctorScheduleBean> doctors = clinicDAO.getDoctorsByClinicAndDay(clinicId, dayName);

        ArrayList<Map<String, Object>> rows = new ArrayList<>();
        for (DoctorScheduleBean doctor : doctors) {
            Map<String, Object> row = new HashMap<>();
            row.put("doctorId", doctor.getDoctorId());
            row.put("doctorName", doctor.getDoctorName());
            row.put("startTime", doctor.getStartTime().toString());
            row.put("endTime", doctor.getEndTime().toString());
            rows.add(row);
        }
        JsonUtil.writeJson(response, rows);

    }

}
