package hk.edu.hkiit.jakarta.webapp.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(filterName = "RoleFilter", urlPatterns = {"/admin/*", "/staff/*", "/account/*", "/api/patient/*"})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();

        HttpSession session = request.getSession(false);
        String role = session != null ? (String) session.getAttribute("role") : null;

        if (role == null) {
            showAccessDenied(request, response);
            return;
        }

        // Filtering based on different roles' permissions

        boolean allowed;
        if (path.startsWith("/admin/")) {
            allowed = "Admin".equals(role);
        } else if (path.startsWith("/staff/")) {
            allowed = "Staff".equals(role) || "Admin".equals(role);
        } else if (path.startsWith("/account/")) {
            allowed = "Staff".equals(role) || "Admin".equals(role);
        } else if (path.startsWith("/api/patient/")) {
            allowed = "Patient".equals(role);
        } else {
            allowed = true;
        }

        if (!allowed) {
            showAccessDenied(request, response);
            return;
        }

        chain.doFilter(servletRequest, servletResponse);

    }

    private void showAccessDenied(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String path = request.getServletPath();
        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Access Denied\"}");
            return;
        }

        request.setAttribute("errorTitle", "Access Denied");
        request.setAttribute("errorMsg", "You do not have permission to access this page.");
        request.setAttribute("backUrl", request.getContextPath() + "/home");
        request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);

    }

}
