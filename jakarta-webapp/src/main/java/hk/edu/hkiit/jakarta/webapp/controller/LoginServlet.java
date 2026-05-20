package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;

import hk.edu.hkiit.jakarta.webapp.bean.UserBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;
import hk.edu.hkiit.jakarta.webapp.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            redirectByRole((String) session.getAttribute("role"), request, response);
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMsg", "Username and password are required.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        UserBean user = userDAO.getUserByUsername(username.trim());

        if (user == null) {
            request.setAttribute("errorMsg", "Invalid username or password.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        boolean passwordMatch = BCrypt.checkpw(password, user.getPasswordHash());

        if (!passwordMatch) {
            request.setAttribute("errorMsg", "Invalid username or password.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId",   user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role",     user.getRole());

        switch (user.getRole()) {
            case "Patient": {
                hk.edu.hkiit.jakarta.webapp.bean.PatientBean patient =
                    userDAO.getPatientProfile(user.getUserId());
                session.setAttribute("profile", patient);
                break;
            }
            case "Staff": {
                hk.edu.hkiit.jakarta.webapp.bean.StaffBean staff =
                    userDAO.getStaffProfile(user.getUserId());
                if (staff == null) {
                    request.setAttribute("errorMsg", "Staff profile not found. Please contact an administrator.");
                    request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                    return;
                }
                session.setAttribute("profile", staff);
                session.setAttribute("clinicId", staff.getClinicId());
                session.setAttribute("position", staff.getPosition());
                break;
            }
            case "Admin": {
                hk.edu.hkiit.jakarta.webapp.bean.AdminBean admin =
                    userDAO.getAdminProfile(user.getUserId());
                if (admin == null) {
                    request.setAttribute("errorMsg", "Admin profile not found. Please contact an administrator.");
                    request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                    return;
                }
                session.setAttribute("profile", admin);
                session.setAttribute("adminLevel", admin.getAdminLevel());
                if (admin.getClinicId() != null) {
                    session.setAttribute("clinicId", admin.getClinicId());
                }
                break;
            }
        }

        new AuditLogDAO().logAction(user.getUserId(), "Logged in as " + user.getRole());
        redirectByRole(user.getRole(), request, response);
    }

    private void redirectByRole(String role, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String ctx = request.getContextPath();
        switch (role) {
            case "Patient": {
                String reactUrl = getServletContext().getInitParameter("reactUrl");
                HttpSession session = request.getSession(false);
                int uid          = (Integer) session.getAttribute("userId");
                String username  = (String)  session.getAttribute("username");
                String fullName  = (String)  session.getAttribute("fullName");
                String userJson  = "{\"userId\":" + uid
                    + ",\"username\":\"" + JsonUtil.escape(username) + "\""
                    + ",\"fullName\":\"" + JsonUtil.escape(fullName)  + "\""
                    + ",\"role\":\"Patient\""
                    + "}";
                String encoded = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(userJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                response.sendRedirect(reactUrl + "/auth-callback?u=" + encoded);
                break;
            }
            case "Staff":   response.sendRedirect(ctx + "/staff/dashboard");   break;
            case "Admin":   response.sendRedirect(ctx + "/admin/dashboard");   break;
            default:        response.sendRedirect(ctx + "/login");             break;
        }
    }
}
