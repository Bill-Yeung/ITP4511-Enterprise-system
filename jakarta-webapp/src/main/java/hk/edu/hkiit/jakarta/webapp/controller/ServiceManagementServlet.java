package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.ServiceBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
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

// Servlet for admin to manage available services
@WebServlet(name = "ServiceManagementServlet", urlPatterns = {"/admin/services"})
public class ServiceManagementServlet extends HttpServlet {

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
        if ("edit".equals(action)) {
            showEdit(request, response);
        } else {
            showList(request, response);
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
                saveService(request, response);
                break;
            case "delete":
                deleteService(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/services");
        }

    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        ServiceBean service = null;

        // Validate parameter
        Integer serviceId = StringUtil.parseIntOrNull(request.getParameter("id"));
        if (serviceId != null) {
            service = serviceDAO.getServiceById(serviceId);
            if (service == null) {
                session.setAttribute("adminError", "Service not found.");
                response.sendRedirect(request.getContextPath() + "/admin/services");
                return;
            }
        }

        request.setAttribute("service", service);
        request.getRequestDispatcher("/WEB-INF/views/admin/service-edit.jsp")
               .forward(request, response);

    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ArrayList<ServiceBean> services = serviceDAO.getAllServices();
        services.sort((a, b) -> Integer.compare(a.getServiceId(), b.getServiceId()));
        request.setAttribute("services", services);
        request.getRequestDispatcher("/WEB-INF/views/admin/services.jsp")
               .forward(request, response);

    }

    private void saveService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        HttpSession session = request.getSession(false);
        int adminId = (Integer) session.getAttribute("userId");
        Integer serviceId = StringUtil.parseIntOrNull(request.getParameter("id"));
        String name = StringUtil.trimToEmpty(request.getParameter("name"));
        String description = StringUtil.trimToEmpty(request.getParameter("description"));

        if (name.isEmpty()) {
            session.setAttribute("adminError", "Service name is required.");
            String target = request.getContextPath() + "/admin/services?action=edit";
            if (serviceId != null) target += "&id=" + serviceId;
            response.sendRedirect(target);
            return;
        }

        boolean isSaved;
        if (serviceId == null) {
            int newId = serviceDAO.createService(name, description);
            isSaved = newId > 0;
            if (isSaved) {
                auditLogDAO.logAction(adminId, "Created service '" + name + "' #" + newId);
            }
        } else {
            isSaved = serviceDAO.updateService(serviceId, name, description);
            if (isSaved) {
                auditLogDAO.logAction(adminId, "Updated service #" + serviceId);
            }
        }

        if (isSaved) {
            session.setAttribute("adminMsg", "Service saved.");
        } else {
            session.setAttribute("adminError", "Save failed. Service name may already exist.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/services");

    }

    private void deleteService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        HttpSession session = request.getSession(false);
        int adminId = (Integer) session.getAttribute("userId");
        
        Integer serviceId = StringUtil.parseIntOrNull(request.getParameter("id"));
        if (serviceId == null) {
            session.setAttribute("adminError", "Invalid service ID.");
        } else {
            boolean isDeleted = serviceDAO.deleteService(serviceId);
            if (isDeleted) {
                session.setAttribute("adminMsg", "Service deleted.");
                auditLogDAO.logAction(adminId, "Deleted service #" + serviceId);
            } else {
                session.setAttribute("adminError", "Delete failed. Service may be used by clinics.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/services");

    }
    
}
