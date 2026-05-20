package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicHoursBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.bean.WalkinQueueBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.WalkinQueueDAO;
import hk.edu.hkiit.jakarta.webapp.util.JsonUtil;
import hk.edu.hkiit.jakarta.webapp.util.NotificationHelper;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "QueueApiServlet", urlPatterns = {"/api/queue/*"})
public class QueueApiServlet extends HttpServlet {

    private ClinicDAO clinicDAO = new ClinicDAO();
    private WalkinQueueDAO queueDAO = new WalkinQueueDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String path = request.getPathInfo();
        if (path == null) path = "/";

        switch (path) {
            case "/clinics":    handleGetClinics(response);               break;
            case "/services":   handleGetServices(request, response);         break;
            case "/my-tickets": handleGetMyTickets(request, response);        break;
            case "/status":     handleGetStatus(request, response);           break;
            default:            JsonUtil.writeError(response, 404, "Not found");
        }
    }

    private void handleGetClinics(HttpServletResponse response) throws IOException {
        ArrayList<ClinicBean> clinics = clinicDAO.getQueueEnabledClinics();
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (ClinicBean c : clinics) {
            arr.add(Json.createObjectBuilder()
                .add("clinic_id", c.getClinicId())
                .add("name", c.getName() != null ? c.getName() : "")
                .add("district", c.getLocation() != null ? c.getLocation() : ""));
        }
        JsonUtil.write(response, arr.build().toString());
    }

    private void handleGetServices(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String clinicIdStr = request.getParameter("clinicId");
        if (clinicIdStr == null) { JsonUtil.writeError(response, 400, "clinicId required"); return; }
        int clinicId;
        try { clinicId = Integer.parseInt(clinicIdStr); }
        catch (NumberFormatException e) { JsonUtil.writeError(response, 400, "Invalid clinicId"); return; }

        ArrayList<ClinicServiceBean> services = clinicDAO.getServicesByClinic(clinicId);
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (ClinicServiceBean cs : services) {
            arr.add(Json.createObjectBuilder()
                .add("clinic_service_id", cs.getClinicServiceId())
                .add("service_id", cs.getServiceId())
                .add("name", cs.getServiceName() != null ? cs.getServiceName() : "")
                .add("slotDurationMins", cs.getSlotDurationMins()));
        }
        JsonUtil.write(response, arr.build().toString());
    }

    private void handleGetMyTickets(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer patientId = getSessionUserId(request);
        if (patientId == null) { JsonUtil.writeError(response, 401, "Unauthorized"); return; }

        ArrayList<WalkinQueueBean> tickets = queueDAO.getPatientQueueToday(patientId);
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (WalkinQueueBean t : tickets) {
            String status = t.getStatus() != null ? t.getStatus().toUpperCase() : "";
            arr.add(Json.createObjectBuilder()
                .add("ticket_id",   t.getQueueId())
                .add("queue_number", t.getQueueNumber())
                .add("status",      status)
                .add("clinic_id",   t.getClinicId())
                .add("service_id",  t.getServiceId())
                .add("clinicName",  t.getClinicName()  != null ? t.getClinicName()  : "")
                .add("serviceName", t.getServiceName() != null ? t.getServiceName() : ""));
        }
        JsonUtil.write(response, arr.build().toString());
    }

    private void handleGetStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String clinicIdStr  = request.getParameter("clinicId");
        String serviceIdStr = request.getParameter("serviceId");
        if (clinicIdStr == null || serviceIdStr == null) {
            JsonUtil.writeError(response, 400, "clinicId and serviceId required"); return;
        }
        int clinicId, serviceId;
        try {
            clinicId  = Integer.parseInt(clinicIdStr);
            serviceId = Integer.parseInt(serviceIdStr);
        } catch (NumberFormatException e) { JsonUtil.writeError(response, 400, "Invalid parameters"); return; }

        int[] status = queueDAO.getQueueStatus(clinicId, serviceId);

        jakarta.json.JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("currentNumber",   status[0])
            .add("currentlyServing", status[0])
            .add("totalWaiting",    status[1])
            .add("waitingCount",    status[1]);

        int slotMins = 15;
        for (ClinicServiceBean cs : clinicDAO.getServicesByClinic(clinicId)) {
            if (cs.getServiceId() == serviceId) { slotMins = cs.getSlotDurationMins(); break; }
        }

        Integer patientId = getSessionUserId(request);
        if (patientId != null) {
            WalkinQueueBean ticket = queueDAO.getPatientTicketForService(patientId, clinicId, serviceId);
            if (ticket != null) {
                String patientStatus = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "";
                int position = "WAITING".equals(patientStatus)
                    ? queueDAO.getPositionInQueue(ticket.getClinicServiceId(), ticket.getQueueNumber())
                    : 0;
                int estWait = position * slotMins;
                builder.add("patientStatus",        patientStatus)
                       .add("patientPosition",       position)
                       .add("estimatedWaitMinutes",  estWait)
                       .add("patientQueueNumber",    ticket.getQueueNumber());
            }
        }

        JsonUtil.write(response, builder.build().toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String path = request.getPathInfo();
        if ("/join".equals(path)) {
            handleJoinQueue(request, response);
        } else if ("/cancel".equals(path)) {
            handleCancelTicket(request, response);
        } else {
            JsonUtil.writeError(response, 404, "Not found");
        }
    }

    private void handleJoinQueue(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer patientId = getSessionUserId(request);
        if (patientId == null) { JsonUtil.writeError(response, 401, "Unauthorized"); return; }

        String body = new String(request.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        int clinicId, serviceId;
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject json = reader.readObject();
            clinicId  = json.getInt("clinicId");
            serviceId = json.getInt("serviceId");
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, "Invalid JSON body"); return;
        }

        ClinicBean clinic = clinicDAO.getClinicById(clinicId);
        if (clinic == null || !clinic.isQueueEnabled()) {
            JsonUtil.writeError(response, 403, "Walk-in queue is not enabled for this clinic"); return;
        }

        if (!isClinicAcceptingWalkins(clinicId)) {
            JsonUtil.writeError(response, 403,
                "Clinic is closed. Walk-in is allowed only when the clinic is open or opens within 15 minutes."); return;
        }

        int clinicServiceId = -1;
        int joinSlotMins = 15;
        for (ClinicServiceBean cs : clinicDAO.getServicesByClinic(clinicId)) {
            if (cs.getServiceId() == serviceId) {
                clinicServiceId = cs.getClinicServiceId();
                joinSlotMins = cs.getSlotDurationMins();
                break;
            }
        }
        if (clinicServiceId == -1) {
            JsonUtil.writeError(response, 400, "Service not available at this clinic"); return;
        }

        if (queueDAO.isAlreadyQueued(patientId, clinicServiceId)) {
            JsonUtil.writeError(response, 409, "Already in queue for this service today"); return;
        }

        // The DB unique key (patient, clinic_service, date) blocks a second
        // row even after the first has been Served. Detect that case here so
        // the patient sees a meaningful message instead of "Failed to join".
        String existingStatus = queueDAO.getTodayTicketStatus(patientId, clinicServiceId);
        if ("Served".equalsIgnoreCase(existingStatus)) {
            JsonUtil.writeError(response, 409,
                "You have already been served for this service today.");
            return;
        }

        int queueId = queueDAO.joinQueue(patientId, clinicServiceId);
        if (queueId == -1) {
            if ("Served".equalsIgnoreCase(queueDAO.getTodayTicketStatus(patientId, clinicServiceId))) {
                JsonUtil.writeError(response, 409,
                    "You have already been served for this service today.");
            } else {
                JsonUtil.writeError(response, 500, "Failed to join queue");
            }
            return;
        }

        WalkinQueueBean ticket = queueDAO.getQueueById(queueId);
        auditLogDAO.logAction(patientId, "Joined queue #" + ticket.getQueueNumber()
            + " at " + (ticket.getClinicName() != null ? ticket.getClinicName() : "clinic")
            + " for " + (ticket.getServiceName() != null ? ticket.getServiceName() : "service"));
        NotificationHelper.notify(patientId, "Queue",
            "Joined queue #" + ticket.getQueueNumber()
            + " at " + (ticket.getClinicName() != null ? ticket.getClinicName() : "clinic")
            + " - please wait to be called");
        NotificationHelper.notifyClinicStaff(ticket.getClinicId(), "Queue",
            "New walk-in #" + ticket.getQueueNumber()
            + " for " + (ticket.getServiceName() != null ? ticket.getServiceName() : "service")
            + " has joined the queue.");
        String joinedStatus = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "";
        int joinPosition = queueDAO.getPositionInQueue(ticket.getClinicServiceId(), ticket.getQueueNumber());
        int joinEstWait = joinPosition * joinSlotMins;
        JsonUtil.write(response, Json.createObjectBuilder()
            .add("ticket_id",             ticket.getQueueId())
            .add("queue_number",          ticket.getQueueNumber())
            .add("queueNumber",           ticket.getQueueNumber())
            .add("status",                joinedStatus)
            .add("clinic_id",             ticket.getClinicId())
            .add("service_id",            ticket.getServiceId())
            .add("clinicName",            ticket.getClinicName()  != null ? ticket.getClinicName()  : "")
            .add("serviceName",           ticket.getServiceName() != null ? ticket.getServiceName() : "")
            .add("estimatedWaitMinutes",  joinEstWait)
            .build().toString());
    }

    private void handleCancelTicket(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer patientId = getSessionUserId(request);
        if (patientId == null) { JsonUtil.writeError(response, 401, "Unauthorized"); return; }

        int ticketId;
        try { ticketId = Integer.parseInt(request.getParameter("ticketId")); }
        catch (NumberFormatException e) { JsonUtil.writeError(response, 400, "Invalid ticket ID"); return; }

        if (queueDAO.cancelQueue(ticketId, patientId)) {
            auditLogDAO.logAction(patientId, "Cancelled queue ticket #" + ticketId);
            NotificationHelper.notify(patientId, "Queue",
                "Queue ticket #" + ticketId + " cancelled");
            JsonUtil.write(response, "{\"message\":\"Ticket cancelled\"}");
        } else {
            JsonUtil.writeError(response, 400, "Cannot cancel - ticket not found, not yours, or already processed");
        }
    }

    private boolean isClinicAcceptingWalkins(int clinicId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String dayName = now.getDayOfWeek()
            .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
        ArrayList<ClinicHoursBean> sessions = clinicDAO.getClinicHoursByDay(clinicId, dayName);
        if (sessions == null || sessions.isEmpty()) return false;

        java.time.LocalTime nowTime = now.toLocalTime();
        java.time.LocalTime cutoff = nowTime.plusMinutes(15);
        for (ClinicHoursBean s : sessions) {
            if (s.getOpenTime() == null || s.getCloseTime() == null) continue;
            java.time.LocalTime open = s.getOpenTime().toLocalTime();
            java.time.LocalTime close = s.getCloseTime().toLocalTime();
            if (!nowTime.isBefore(open) && nowTime.isBefore(close)) return true;
            if (nowTime.isBefore(open) && !cutoff.isBefore(open))   return true;
        }
        return false;
    }

    private Integer getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Integer) session.getAttribute("userId");
    }

}
