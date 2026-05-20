package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "PatientBehaviorServlet", urlPatterns = {"/admin/patient-behavior"})
public class PatientBehaviorServlet extends HttpServlet {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ClinicDAO clinicDAO = new ClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = session == null ? null : (String) session.getAttribute("role");
        if (!"Admin".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String adminLevel = (String) session.getAttribute("adminLevel");
        Integer adminClinicId = (Integer) session.getAttribute("clinicId");
        boolean isClinicAdmin = "Clinic".equals(adminLevel);

        Integer clinicId = StringUtil.parseIntOrNull(request.getParameter("clinicId"));
        if (isClinicAdmin) {
            clinicId = adminClinicId;
        }

        Integer days = StringUtil.parseIntOrNull(request.getParameter("days"));
        if (days == null) days = 90;

        Integer minFlags = StringUtil.parseIntOrNull(request.getParameter("minFlags"));
        if (minFlags == null || minFlags < 1) minFlags = 2;

        Integer patientId = StringUtil.parseIntOrNull(request.getParameter("patientId"));

        ArrayList<String[]> summary = appointmentDAO.getPatientBehaviorSummary(clinicId, days, minFlags);
        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();

        ArrayList<AppointmentBean> patientHistory = null;
        String patientName = null;
        if (patientId != null) {
            patientHistory = appointmentDAO.getAppointmentsByPatient(patientId);
            if (patientHistory != null && !patientHistory.isEmpty()) {
                patientName = patientHistory.get(0).getPatientName();
            }
        }

        request.setAttribute("summary", summary);
        request.setAttribute("clinics", clinics);
        request.setAttribute("filterClinicId", clinicId);
        request.setAttribute("filterDays", days);
        request.setAttribute("filterMinFlags", minFlags);
        request.setAttribute("isClinicAdmin", isClinicAdmin);
        request.setAttribute("selectedPatientId", patientId);
        request.setAttribute("selectedPatientName", patientName);
        request.setAttribute("patientHistory", patientHistory);

        request.getRequestDispatcher("/WEB-INF/views/admin/patient-behavior.jsp")
               .forward(request, response);
    }
}
