package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.ServiceBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ServiceDAO;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "ReportServlet", urlPatterns = {"/admin/reports"})
public class ReportServlet extends HttpServlet {

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();
    private ServiceDAO serviceDAO = new ServiceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        String adminLevel = (String) session.getAttribute("adminLevel");
        Integer adminClinicId = (Integer) session.getAttribute("clinicId");

        Integer filterClinicId = StringUtil.parseIntOrNull(request.getParameter("clinicId"));
        Integer filterServiceId = StringUtil.parseIntOrNull(request.getParameter("serviceId"));
        Integer filterYear = StringUtil.parseIntOrNull(request.getParameter("year"));
        Integer filterMonth = StringUtil.parseIntOrNull(request.getParameter("month"));
        String filterStatus = request.getParameter("status");

        if (filterClinicId != null && filterClinicId <= 0) filterClinicId = null;
        if (filterServiceId != null && filterServiceId <= 0) filterServiceId = null;
        if (filterYear != null && filterYear <= 0) filterYear = null;
        if (filterMonth != null && filterMonth <= 0) filterMonth = null;

        // Limit clinic-level admin's access
        if ("Clinic".equals(adminLevel) && adminClinicId != null) {
            filterClinicId = adminClinicId;
        }

        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();
        ArrayList<ServiceBean> services = serviceDAO.getAllServices();
        request.setAttribute("clinics", clinics);
        request.setAttribute("services", services);

        String tab = request.getParameter("tab");
        if (tab == null || tab.isEmpty()) {
            tab = "records";
        }

        request.setAttribute("filterClinicId", filterClinicId);
        request.setAttribute("filterServiceId", filterServiceId);
        request.setAttribute("filterYear", filterYear);
        request.setAttribute("filterMonth", filterMonth);
        request.setAttribute("filterStatus", filterStatus != null ? filterStatus : "");
        request.setAttribute("tab", tab);
        request.setAttribute("adminLevel", adminLevel);

        switch (tab) {
            case "utilisation":
                ArrayList<String[]> utilisation = appointmentDAO.getUtilisationRate(filterClinicId, filterYear, filterMonth);
                request.setAttribute("utilisation", utilisation);
                prepareUtilisationChart(request, utilisation);
                break;
            case "noshow":
                ArrayList<String[]> noshow = appointmentDAO.getNoShowSummary(filterClinicId, filterYear, filterMonth);
                request.setAttribute("noshow", noshow);
                prepareNoShowChart(request, noshow);
                break;
            // for case "records"
            default:
                ArrayList<AppointmentBean> appointments = appointmentDAO.getFilteredAppointments(filterClinicId, filterServiceId, filterYear, filterMonth, filterStatus);
                request.setAttribute("appointments", appointments);
                break;
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp")
               .forward(request, response);

    }

    private void prepareUtilisationChart(HttpServletRequest request, ArrayList<String[]> utilisation) {
        
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Double> data = new ArrayList<>();

        if (utilisation != null) {
            for (String[] row : utilisation) {
                // Add clinic name - service name labels
                labels.add(row[0] + " - " + row[1]);
                // Add utilization percentage data
                data.add(Double.parseDouble(row[4]));
            }
        }

        request.setAttribute("utilisationChartLabels", labels);
        request.setAttribute("utilisationChartData", data);

    }

    private void prepareNoShowChart(HttpServletRequest request, ArrayList<String[]> noshow) {
        
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> data = new ArrayList<>();
        int total = 0;

        if (noshow != null) {

            // Calculate total no-show
            for (String[] row : noshow) {
                total += Integer.parseInt(row[2]);
            }

            for (String[] row : noshow) {

                int count = Integer.parseInt(row[2]);
                // Round to 1 decimal place
                double percent = total > 0 ? Math.round(count * 1000.0 / total) / 10.0 : 0;

                // Add clinic name - service name (percentage) labels
                labels.add(row[0] + " - " + row[1] + " (" + percent + "%)");
                // Add count data
                data.add(count);

            }

        }

        request.setAttribute("noshowChartLabels", labels);
        request.setAttribute("noshowChartData", data);
        
    }

}
