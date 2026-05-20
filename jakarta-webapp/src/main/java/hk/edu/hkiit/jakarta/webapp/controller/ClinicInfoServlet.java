package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicHoursBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Servlet for showing clinic information to all staffs and admins
@WebServlet(name = "ClinicInfoServlet", urlPatterns = {"/staff/clinics"})
public class ClinicInfoServlet extends HttpServlet {

    private ClinicDAO clinicDAO = new ClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();
        if (clinics.isEmpty()) {
            request.setAttribute("errorTitle", "No Clinics Available");
            request.setAttribute("errorMsg", "No clinic records are available.");
            request.setAttribute("backUrl", request.getContextPath() + "/staff/dashboard");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp")
                   .forward(request, response);
            return;
        }

        Map<Integer, ArrayList<ClinicServiceBean>> servicesByClinic = new HashMap<>();
        Map<Integer, ArrayList<ClinicHoursBean>> hoursByClinic = new HashMap<>();
        for (ClinicBean clinic : clinics) {
            servicesByClinic.put(clinic.getClinicId(), clinicDAO.getServicesByClinic(clinic.getClinicId()));
            hoursByClinic.put(clinic.getClinicId(), clinicDAO.getClinicHours(clinic.getClinicId()));
        }

        request.setAttribute("clinics", clinics);
        request.setAttribute("servicesByClinic", servicesByClinic);
        request.setAttribute("hoursByClinic", hoursByClinic);
        request.getRequestDispatcher("/WEB-INF/views/staff/clinics.jsp")
               .forward(request, response);

    }

}
