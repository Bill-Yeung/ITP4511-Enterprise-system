package hk.edu.hkiit.jakarta.webapp.filter;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Authorization filter that blocks unauthorized access and redirect to login
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"}, asyncSupported = true)
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_URL = Set.of(
        "/", "/index.jsp", "/home",
        "/login", "/logout",
        "/register", "/otp-verify",
        "/forgot-password", "/reset-password"
    );

    private static final String[] PUBLIC_PREFIXES = {
        "/css/", "/js/", "/images/", "/static/", "/assets/", "/cchc.svg"
    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();

        // Allow navigation if link is public or user has logined with session

        if (isPublic(path)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Return unauthorized message for API request
        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");

    }

    private boolean isPublic(String path) {

        if (PUBLIC_URL.contains(path)) {
            return true;
        }

        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        return false;

    }

}
