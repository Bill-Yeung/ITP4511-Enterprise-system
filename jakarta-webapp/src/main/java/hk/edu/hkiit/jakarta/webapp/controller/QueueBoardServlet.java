package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.WalkinQueueBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.WalkinQueueDAO;
import hk.edu.hkiit.jakarta.webapp.util.NotificationHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "QueueBoardServlet", urlPatterns = {"/staff/queue"})
public class QueueBoardServlet extends HttpServlet {

    private WalkinQueueDAO queueDAO = new WalkinQueueDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        String role = (String) session.getAttribute("role");
        Integer staffClinicId = (Integer) session.getAttribute("clinicId");
        int clinicId = resolveClinicId(request, role, staffClinicId);

        // Handle queue actions
        String action = request.getParameter("action");
        if (action != null) {
            handleAction(action, request, session, role, staffClinicId);
            String serviceParam = request.getParameter("serviceId");
            String redirectUrl = request.getContextPath() + "/staff/queue";
            String separator = "?";
            if (serviceParam != null) {
                redirectUrl += separator + "serviceId=" + serviceParam;
                separator = "&";
            }
            if ("Admin".equals(role)) {
                redirectUrl += separator + "clinic=" + clinicId;
            }
            response.sendRedirect(redirectUrl);
            return;
        }

        // Get services for this clinic (filter dropdown)
        ArrayList<ClinicServiceBean> services = clinicDAO.getServicesByClinic(clinicId);
        request.setAttribute("services", services);

        // If a service is selected, show its queue
        String serviceParam = request.getParameter("serviceId");
        if (serviceParam != null && !serviceParam.isEmpty()) {
            try {
                int clinicServiceId = Integer.parseInt(serviceParam);
                ArrayList<WalkinQueueBean> queue = queueDAO.getTodayQueue(clinicServiceId);
                request.setAttribute("queueList", queue);
                request.setAttribute("selectedServiceId", clinicServiceId);
            } catch (NumberFormatException e) {
                // ignore
            }
        } else {
            // Show all queue entries for the clinic
            ArrayList<WalkinQueueBean> allQueue = queueDAO.getTodayQueueByClinic(clinicId);
            request.setAttribute("queueList", allQueue);
        }

        request.setAttribute("viewClinicId", clinicId);
        request.setAttribute("isAdmin", "Admin".equals(role));

        request.getRequestDispatcher("/WEB-INF/views/staff/queue.jsp")
               .forward(request, response);
    }

    private int resolveClinicId(HttpServletRequest request, String role, Integer staffClinicId) {
        if ("Admin".equals(role)) {
            ArrayList<ClinicBean> allClinics = clinicDAO.getAllClinics();
            request.setAttribute("allClinics", allClinics);

            String clinicParam = request.getParameter("clinic");
            if (clinicParam != null && !clinicParam.isEmpty()) {
                try {
                    return Integer.parseInt(clinicParam);
                } catch (NumberFormatException ignore) {}
            }
            if (staffClinicId != null && staffClinicId > 0) return staffClinicId;
            if (!allClinics.isEmpty()) return allClinics.get(0).getClinicId();
            return 1;
        }
        return staffClinicId;
    }

    private int getStaffUserId(HttpSession session) {
        return session != null && session.getAttribute("userId") != null ? (Integer) session.getAttribute("userId") : 0;
    }

    private boolean belongsToStaffClinic(int clinicServiceId, Integer staffClinicId) {
        if (staffClinicId == null) return false;
        for (ClinicServiceBean cs : clinicDAO.getServicesByClinic(staffClinicId)) {
            if (cs.getClinicServiceId() == clinicServiceId) return true;
        }
        return false;
    }

    private void handleAction(String action, HttpServletRequest request, HttpSession session,
                              String role, Integer staffClinicId) {
        String idParam = request.getParameter("id");
        String csIdParam = request.getParameter("csId");
        if (idParam == null && csIdParam == null) return;

        boolean isAdmin = "Admin".equals(role);

        try {
            switch (action) {
                case "callNext":
                    if (csIdParam != null) {
                        int csId = Integer.parseInt(csIdParam);
                        if (!isAdmin && !belongsToStaffClinic(csId, staffClinicId)) {
                            session.setAttribute("staffError", "Not authorised for this service.");
                            return;
                        }
                        WalkinQueueBean called = queueDAO.callNext(csId);
                        if (called != null) {
                            session.setAttribute("staffMsg",
                                "Called queue #" + called.getQueueNumber()
                                + " - " + called.getPatientName());
                            auditLogDAO.logAction(getStaffUserId(session),
                                "Called queue #" + called.getQueueNumber() + " - " + called.getPatientName());
                            NotificationHelper.notify(called.getPatientId(), "Queue",
                                "Queue #" + called.getQueueNumber()
                                + " called at " + (called.getClinicName() != null ? called.getClinicName() : "clinic")
                                + " - please proceed to the counter");
                        } else {
                            session.setAttribute("staffError",
                                "No patients waiting in queue.");
                        }
                    }
                    break;
                case "skip":
                    if (idParam != null) {
                        int queueId = Integer.parseInt(idParam);
                        WalkinQueueBean before = queueDAO.getQueueById(queueId);
                        if (!isAdmin && (before == null || !Integer.valueOf(before.getClinicId()).equals(staffClinicId))) {
                            session.setAttribute("staffError", "Not authorised for this ticket.");
                            return;
                        }
                        boolean isSkipped = queueDAO.skipPatient(queueId);
                        session.setAttribute(
                            isSkipped ? "staffMsg" : "staffError",
                            isSkipped ? "Patient skipped." : "Skip failed.");
                        if (isSkipped) {
                            auditLogDAO.logAction(getStaffUserId(session), "Skipped queue ticket #" + queueId);
                            if (before != null) {
                                NotificationHelper.notify(before.getPatientId(), "Queue",
                                    "Queue #" + before.getQueueNumber() + " was skipped - please re-check in at the counter");
                            }
                        }
                    }
                    break;
                case "recall":
                    if (idParam != null) {
                        int queueId = Integer.parseInt(idParam);
                        WalkinQueueBean before = queueDAO.getQueueById(queueId);
                        if (!isAdmin && (before == null || !Integer.valueOf(before.getClinicId()).equals(staffClinicId))) {
                            session.setAttribute("staffError", "Not authorised for this ticket.");
                            return;
                        }
                        boolean isRecalled = queueDAO.recallPatient(queueId);
                        session.setAttribute(
                            isRecalled ? "staffMsg" : "staffError",
                            isRecalled ? "Patient re-called." : "Re-call failed (ticket not Skipped).");
                        if (isRecalled) {
                            auditLogDAO.logAction(getStaffUserId(session), "Re-called queue ticket #" + queueId);
                            if (before != null) {
                                NotificationHelper.notify(before.getPatientId(), "Queue",
                                    "Queue #" + before.getQueueNumber() + " has been re-called - please return to the counter");
                            }
                        }
                    }
                    break;
                case "served":
                    if (idParam != null) {
                        int queueId = Integer.parseInt(idParam);
                        WalkinQueueBean before = queueDAO.getQueueById(queueId);
                        if (!isAdmin && (before == null || !Integer.valueOf(before.getClinicId()).equals(staffClinicId))) {
                            session.setAttribute("staffError", "Not authorised for this ticket.");
                            return;
                        }
                        boolean isServed = queueDAO.markServed(queueId);
                        session.setAttribute(
                            isServed ? "staffMsg" : "staffError",
                            isServed ? "Marked as served." : "Update failed.");
                        if (isServed) {
                            auditLogDAO.logAction(getStaffUserId(session), "Marked queue ticket #" + queueId + " as served");
                            if (before != null) {
                                NotificationHelper.notify(before.getPatientId(), "Queue",
                                    "Queue #" + before.getQueueNumber() + " has been served - thank you for visiting");
                            }
                        }
                    }
                    break;
            }
        } catch (NumberFormatException e) {
            session.setAttribute("staffError", "Invalid ID.");
        }
    }
}
