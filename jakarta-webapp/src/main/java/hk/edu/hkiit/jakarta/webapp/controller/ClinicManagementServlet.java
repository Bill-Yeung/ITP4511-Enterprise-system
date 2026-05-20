package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ServiceDAO;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;
import hk.edu.hkiit.jakarta.webapp.util.TimeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Servlet for admin to manage clinic (information, walk-in, services, hours)
@WebServlet(name = "ClinicManagementServlet", urlPatterns = {"/admin/clinics"})
public class ClinicManagementServlet extends HttpServlet {

    private ClinicDAO clinicDAO = new ClinicDAO();
    private ServiceDAO serviceDAO = new ServiceDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!"System".equals(session.getAttribute("adminLevel"))) {
            session.setAttribute("adminError", "System admin access required.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        String action = request.getParameter("action") == null ? "" : request.getParameter("action");
        switch (action) {
            case "edit":
                showClinicEdit(request, response);
                break;
            case "services":
                showServicesPage(request, response);
                break;
            case "hours":
                showHoursPage(request, response);
                break;
            default:
                showClinicList(request, response);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!"System".equals(session.getAttribute("adminLevel"))) {
            session.setAttribute("adminError", "System admin access required.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        String action = request.getParameter("action") == null ? "" : request.getParameter("action");
        switch (action) {
            case "save":
                saveClinic(request, response);
                break;
            case "delete":
            case "toggleQueue":
                handleClinicAction(request, response);
                break;
            case "addService":
            case "updateService":
            case "deleteService":
                handleServiceAction(request, response);
                break;
            case "addHours":
            case "editHours":
            case "deleteHours":
                handleHoursAction(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/clinics");
        }

    }

    private void showClinicEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        ClinicBean clinic = null;
        String idStr = request.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                clinic = clinicDAO.getClinicById(id);
                if (clinic == null) {
                    session.setAttribute("adminError", "Clinic not found.");
                    response.sendRedirect(request.getContextPath() + "/admin/clinics");
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("adminError", "Invalid clinic ID.");
                response.sendRedirect(request.getContextPath() + "/admin/clinics");
                return;
            }
        }

        request.setAttribute("clinic", clinic);
        request.getRequestDispatcher("/WEB-INF/views/admin/clinic-edit.jsp")
           .forward(request, response);

    }

    private void showServicesPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        ArrayList<ClinicBean> allClinics = clinicDAO.getAllClinics();
        request.setAttribute("clinics", allClinics);

        String cidStr = request.getParameter("clinicId");
        if (cidStr == null || cidStr.isEmpty()) {
            if (allClinics.isEmpty()) {
                request.getRequestDispatcher("/WEB-INF/views/admin/clinic-services.jsp")
                       .forward(request, response);
                return;
            }
            cidStr = String.valueOf(allClinics.get(0).getClinicId());
        }

        try {

            int clinicId = Integer.parseInt(cidStr);
            ClinicBean clinic = clinicDAO.getClinicById(clinicId);
            if (clinic == null) {
                session.setAttribute("adminError", "Clinic not found.");
                response.sendRedirect(request.getContextPath() + "/admin/clinics?action=services");
                return;
            }

            request.setAttribute("clinicId", clinicId);
            request.setAttribute("clinic", clinic);
            request.setAttribute("clinicServices", clinicDAO.getServicesByClinic(clinicId));
            request.setAttribute("allServices", serviceDAO.getAllServices());

        } catch (NumberFormatException e) {
            session.setAttribute("adminError", "Invalid clinic ID.");
            response.sendRedirect(request.getContextPath() + "/admin/clinics?action=services");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/clinic-services.jsp")
           .forward(request, response);

    }

    private void showHoursPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String cidStr = request.getParameter("clinicId");
        ArrayList<ClinicBean> allClinics = clinicDAO.getAllClinics();
        request.setAttribute("clinics", allClinics);

        if (cidStr == null || cidStr.isEmpty()) {
            if (allClinics.isEmpty()) {
                request.getRequestDispatcher("/WEB-INF/views/admin/clinic-hours.jsp")
                       .forward(request, response);
                return;
            }
            cidStr = String.valueOf(allClinics.get(0).getClinicId());
        }

        try {

            int clinicId = Integer.parseInt(cidStr);
            ClinicBean clinic = clinicDAO.getClinicById(clinicId);
            if (clinic == null) {
                session.setAttribute("adminError", "Clinic not found.");
                response.sendRedirect(request.getContextPath() + "/admin/clinics?action=hours");
                return;
            }

            request.setAttribute("clinicId", clinicId);
            request.setAttribute("clinic", clinic);
            request.setAttribute("hours", clinicDAO.getClinicHours(clinicId));

        } catch (NumberFormatException e) {
            session.setAttribute("adminError", "Invalid clinic ID.");
            response.sendRedirect(request.getContextPath() + "/admin/clinics?action=hours");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/clinic-hours.jsp")
           .forward(request, response);

    }

    private void showClinicList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();
        clinics.sort((a, b) -> Integer.compare(a.getClinicId(), b.getClinicId()));
        request.setAttribute("clinics", clinics);
        request.getRequestDispatcher("/WEB-INF/views/admin/clinics.jsp")
               .forward(request, response);

    }

    private void saveClinic(HttpServletRequest request, HttpServletResponse response) throws IOException {
       
        HttpSession session = request.getSession(false);

        int adminId = (Integer) session.getAttribute("userId");
        String idStr = request.getParameter("id");
        String name = StringUtil.trimToEmpty(request.getParameter("name"));
        String location = StringUtil.trimToEmpty(request.getParameter("location"));
        boolean queueEnabled = request.getParameter("queueEnabled") != null;

        if (name.isEmpty()) {
            session.setAttribute("adminError", "Clinic name is required.");
            String target = request.getContextPath() + "/admin/clinics?action=edit";
            if (idStr != null && !idStr.isEmpty()) {
                target += "&id=" + idStr;
            }
            response.sendRedirect(target);
            return;
        }

        boolean isSaved;
        if (idStr == null || idStr.isEmpty()) {
            int newId = clinicDAO.createClinic(name, location, queueEnabled);
            isSaved = newId > 0;
            if (isSaved) {
                session.setAttribute("adminMsg", "Clinic saved.");
                auditLogDAO.logAction(adminId, "Created clinic '" + name + "' #" + newId);
            }
        } else {
            try {
                int id = Integer.parseInt(idStr);
                isSaved = clinicDAO.updateClinic(id, name, location, queueEnabled);
                if (isSaved) {
                    session.setAttribute("adminMsg", "Clinic saved.");
                    auditLogDAO.logAction(adminId, "Updated clinic #" + id);
                }
            } catch (NumberFormatException e) {
                session.setAttribute("adminError", "Invalid clinic ID.");
                response.sendRedirect(request.getContextPath() + "/admin/clinics");
                return;
            }

        }

        if (!isSaved) {
            session.setAttribute("adminError", "Save failed.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/clinics");

    }

    private void handleClinicAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");
        String idStr = request.getParameter("id");
        int adminId = (Integer) session.getAttribute("userId");

        try {

            if ("delete".equals(action) && idStr != null) {

                int id = Integer.parseInt(idStr);
                boolean isDeleted = clinicDAO.deleteClinic(id);
                if (isDeleted) {
                    session.setAttribute("adminMsg", "Clinic #" + id + " deleted.");
                    auditLogDAO.logAction(adminId, "Deleted clinic #" + id);
                }
                else {
                    session.setAttribute("adminError", "Delete failed (clinic may have related rows).");
                }

            } else if ("toggleQueue".equals(action) && idStr != null) {

                int id = Integer.parseInt(idStr);
                boolean enabled = "true".equals(request.getParameter("enabled"));
                boolean isUpdated = clinicDAO.setQueueEnabled(id, enabled);
                if (isUpdated) {
                    session.setAttribute("adminMsg", "Queue " + (enabled ? "enabled" : "disabled") + ".");
                    auditLogDAO.logAction(adminId, "Set clinic #" + id + " queue=" + enabled);
                } else {
                    session.setAttribute("adminError", "Update failed.");
                }
            
            }

        } catch (NumberFormatException e) {
            session.setAttribute("adminError", "Invalid ID.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/clinics");

    }

    private void handleServiceAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        HttpSession session = request.getSession(false);
        int adminId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        String clinicIdStr = request.getParameter("clinicId");

        try {

            int clinicId = Integer.parseInt(clinicIdStr);
            switch (action) {
                case "addService": {
                    int serviceId = Integer.parseInt(request.getParameter("serviceId"));
                    int quota = Integer.parseInt(request.getParameter("quotaPerSlot"));
                    int dur = Integer.parseInt(request.getParameter("slotDurationMins"));
                    boolean approval = request.getParameter("requiresApproval") != null;
                    boolean isAdded = clinicDAO.addClinicService(clinicId, serviceId, quota, dur, approval);
                    if (isAdded) {
                        session.setAttribute("adminMsg", "Service added.");
                        auditLogDAO.logAction(adminId, "Added service #" + serviceId + " to clinic #" + clinicId);
                    } else {
                        session.setAttribute("adminError", "Add failed (service may already exist for this clinic).");
                    }
                    break;
                }
                case "updateService": {
                    int csId = Integer.parseInt(request.getParameter("clinicServiceId"));
                    int quota = Integer.parseInt(request.getParameter("quotaPerSlot"));
                    int dur = Integer.parseInt(request.getParameter("slotDurationMins"));
                    boolean approval = request.getParameter("requiresApproval") != null;
                    boolean isUpdated = clinicDAO.updateClinicService(csId, quota, dur, approval);
                    if (isUpdated) {
                        session.setAttribute("adminMsg", "Service updated.");
                        auditLogDAO.logAction(adminId, "Updated clinic_service #" + csId);
                    } else {
                        session.setAttribute("adminError", "Update failed.");
                    }
                    break;
                }
                case "deleteService": {
                    int csId = Integer.parseInt(request.getParameter("clinicServiceId"));
                    boolean isDeleted = clinicDAO.deleteClinicService(csId);
                    if (isDeleted) {
                        session.setAttribute("adminMsg", "Service removed.");
                        auditLogDAO.logAction(adminId, "Deleted clinic_service #" + csId);
                    } else {
                        session.setAttribute("adminError", "Remove failed (may have appointments).");
                    }
                    break;
                }
                default:
                    session.setAttribute("adminError", "Unknown action.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/clinics?action=services&clinicId=" + clinicId);
        } catch (NumberFormatException e) {
            session.setAttribute("adminError", "Invalid input.");
            response.sendRedirect(request.getContextPath() + "/admin/clinics");
        }
    }

    private void handleHoursAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        HttpSession session = request.getSession(false);
        int adminId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        String clinicIdStr = request.getParameter("clinicId");

        try {

            int clinicId = Integer.parseInt(clinicIdStr);
            switch (action) {
                case "addHours": {

                    String day = request.getParameter("dayOfWeek");
                    Time open = TimeUtil.parseSqlTime(request.getParameter("openTime"));
                    Time close = TimeUtil.parseSqlTime(request.getParameter("closeTime"));

                    // Check if the times are valid before adding the hours to database
                    if (open == null || close == null || day == null || day.isEmpty()) {
                        session.setAttribute("adminError", "Day, open and close times are required.");
                    } else if (!close.after(open)) {
                        session.setAttribute("adminError", "Close time must be after open time.");
                    } else {
                        boolean isAdded = clinicDAO.addClinicHours(clinicId, day, open, close);
                        if (isAdded) {
                            session.setAttribute("adminMsg", "Hours added.");
                            auditLogDAO.logAction(adminId, "Added " + day + " " + open + "-" + close + " for clinic #" + clinicId);
                        } else {
                            session.setAttribute("adminError", "Add failed. The operating session may overlap with existing hours.");
                        }
                    }
                    break;
                }
                case "editHours": {
                    int hoursId = Integer.parseInt(request.getParameter("clinicHoursId"));
                    String day = request.getParameter("dayOfWeek");
                    Time open = TimeUtil.parseSqlTime(request.getParameter("openTime"));
                    Time close = TimeUtil.parseSqlTime(request.getParameter("closeTime"));

                    if (open == null || close == null || day == null || day.isEmpty()) {
                        session.setAttribute("adminError", "Day, open and close times are required.");
                    } else if (!close.after(open)) {
                        session.setAttribute("adminError", "Close time must be after open time.");
                    } else {
                        boolean isUpdated = clinicDAO.updateClinicHours(hoursId, clinicId, day, open, close);
                        if (isUpdated) {
                            session.setAttribute("adminMsg", "Hours updated.");
                            auditLogDAO.logAction(adminId, "Updated clinic_hours #" + hoursId + " to " + day + " " + open + "-" + close);
                        } else {
                            session.setAttribute("adminError", "Update failed. The operating session may overlap with existing hours.");
                        }
                    }
                    break;
                }
                case "deleteHours": {
                    int hoursId = Integer.parseInt(request.getParameter("clinicHoursId"));
                    boolean isDeleted = clinicDAO.deleteClinicHours(hoursId);
                    if (isDeleted) {
                        session.setAttribute("adminMsg", "Hours removed.");
                        auditLogDAO.logAction(adminId, "Deleted clinic_hours #" + hoursId);
                    } else {
                        session.setAttribute("adminError", "Remove failed.");
                    }
                    break;
                }
                default:
                    session.setAttribute("adminError", "Unknown action.");

            }

            response.sendRedirect(request.getContextPath() + "/admin/clinics?action=hours&clinicId=" + clinicId);
        
        } catch (NumberFormatException e) {
            session.setAttribute("adminError", "Invalid input.");
            response.sendRedirect(request.getContextPath() + "/admin/clinics");
        }
        
    }

}
