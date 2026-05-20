package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.BatchImportDAO;
import hk.edu.hkiit.jakarta.webapp.util.CsvUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

// Servlet for handling file upload (MultipartConfig annotation for files)
@MultipartConfig
@WebServlet(name = "BatchImportServlet", urlPatterns = {"/admin/import"})
public class BatchImportServlet extends HttpServlet {

    private AuditLogDAO auditLogDAO = new AuditLogDAO();
    private BatchImportDAO batchImportDAO = new BatchImportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!"System".equals(session.getAttribute("adminLevel"))) {
            session.setAttribute("adminError", "Only System administrators can use batch import.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        request.setAttribute("importError", session.getAttribute("importError"));
        request.setAttribute("importResults", session.getAttribute("importResults"));
        request.setAttribute("importSuccess", session.getAttribute("importSuccess"));
        request.setAttribute("importFailed", session.getAttribute("importFailed"));
        request.setAttribute("importType", session.getAttribute("importType"));

        session.removeAttribute("importError");
        session.removeAttribute("importResults");
        session.removeAttribute("importSuccess");
        session.removeAttribute("importFailed");
        session.removeAttribute("importType");

        request.getRequestDispatcher("/WEB-INF/views/admin/import.jsp")
               .forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!"System".equals(session.getAttribute("adminLevel"))) {
            session.setAttribute("adminError", "Only System administrators can use batch import.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        String importType = request.getParameter("importType");
        Part filePart = request.getPart("csvFile");

        // Redirect when there is no file
        if (filePart == null || filePart.getSize() == 0) {
            session.setAttribute("importError", "Please select a CSV file to upload.");
            response.sendRedirect(request.getContextPath() + "/admin/import");
            return;
        }

        ArrayList<String[]> rows;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8))) {
            rows = CsvUtil.readRows(reader);
        }

        int userId = (int) session.getAttribute("userId");
        ArrayList<String> results = new ArrayList<>();
        int success = 0;
        int failed = 0;

        try {

            // Each case will check if the columns are sufficient for the upload

            switch (importType != null ? importType : "") {
                case "services":
                    for (String[] row : rows) {

                        if (row.length < 2) {
                            results.add("SKIP: Insufficient columns - " + String.join(",", row));
                            failed++;
                            continue;
                        }

                        String name = row[0];
                        String description = row[1];

                        if (batchImportDAO.importService(name, description)) {
                            results.add("OK: Service '" + name + "' imported.");
                            success++;
                        } else {
                            results.add("FAIL: Service '" + name + "' - may already exist or DB error.");
                            failed++;
                        }

                    }
                    break;

                case "clinic_services":
                    for (String[] row : rows) {

                        if (row.length < 4) {
                            results.add("SKIP: Insufficient columns - " + String.join(",", row));
                            failed++;
                            continue;
                        }

                        try {

                            int clinicId = Integer.parseInt(row[0]);
                            int serviceId = Integer.parseInt(row[1]);
                            int quota = Integer.parseInt(row[2]);
                            int duration = Integer.parseInt(row[3]);

                            if (batchImportDAO.importClinicService(clinicId, serviceId, quota, duration)) {
                                results.add("OK: Clinic " + clinicId + " / Service " + serviceId + " imported.");
                                success++;
                            } else {
                                results.add("FAIL: Clinic " + clinicId + " / Service " + serviceId + " - duplicate or DB error.");
                                failed++;
                            }
                        } catch (NumberFormatException e) {
                            results.add("SKIP: Invalid number in row - " + String.join(",", row));
                            failed++;
                        }

                    }
                    break;

                case "users":
                    for (String[] row : rows) {

                        if (row.length < 5) {
                            results.add("SKIP: Insufficient columns - " + String.join(",", row));
                            failed++;
                            continue;
                        }

                        String username = row[0];
                        String password = row[1];
                        String role = row[2];
                        String fullName = row[3];
                        String email = row[4];
                        String phone = row.length > 5 ? row[5] : "";

                        if (batchImportDAO.importUser(username, password, role, fullName, email, phone)) {
                            results.add("OK: User '" + username + "' (" + role + ") imported.");
                            success++;
                        } else {
                            results.add("FAIL: User '" + username + "' - duplicate username/email or DB error.");
                            failed++;
                        }
                        
                    }
                    break;

                default:
                    session.setAttribute("importError", "Please select an import type.");
                    response.sendRedirect(request.getContextPath() + "/admin/import");
                    return;

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            session.setAttribute("importError", "Unexpected error: " + ex.getMessage());
        }

        auditLogDAO.logAction(userId, "Batch import (" + importType + "): " + success + " succeeded, " + failed + " failed, " + rows.size() + " total rows.");

        session.setAttribute("importResults", results);
        session.setAttribute("importSuccess", success);
        session.setAttribute("importFailed", failed);
        session.setAttribute("importType", importType);

        // Redirect to avoid duplicated form submission on refresh
        response.sendRedirect(request.getContextPath() + "/admin/import");

    }

}
