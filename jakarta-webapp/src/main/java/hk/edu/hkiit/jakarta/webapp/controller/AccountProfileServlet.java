package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.AdminBean;
import hk.edu.hkiit.jakarta.webapp.bean.StaffBean;
import hk.edu.hkiit.jakarta.webapp.bean.UserBean;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;
import hk.edu.hkiit.jakarta.webapp.util.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Servlet for staff and admin account profile
@WebServlet(name = "AccountProfileServlet", urlPatterns = {"/account/profile"})
public class AccountProfileServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        // Redirect to error page if profile not found
        UserBean profile = getProfile(userId, role);
        if (profile == null) {
            request.setAttribute("errorTitle", "Profile Not Found");
            request.setAttribute("errorMsg", "Your account exists, but the related profile record cannot be found.");
            request.setAttribute("backUrl", request.getContextPath() + "/home");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp")
                   .forward(request, response);
            return;
        }

        request.setAttribute("accountProfile", profile);

        String roleLabel = "Role";
        String roleValue = role;
        Object clinicValue = "";

        if ("Admin".equals(role)) {
            AdminBean admin = (AdminBean) profile;
            roleLabel = "Admin Level";
            roleValue = admin.getAdminLevel();
            clinicValue = admin.getClinicId() == null ? "All clinics" : admin.getClinicId();
        } else if ("Staff".equals(role)) {
            StaffBean staff = (StaffBean) profile;
            roleLabel = "Position";
            roleValue = staff.getPosition();
            clinicValue = staff.getClinicId();
        }

        request.setAttribute("roleLabel", roleLabel);
        request.setAttribute("roleValue", roleValue);
        request.setAttribute("clinicValue", clinicValue);

        request.getRequestDispatcher("/WEB-INF/views/account/profile.jsp")
           .forward(request, response);
    
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String currentPassword = StringUtil.trimNullable(request.getParameter("currentPassword"));
        String newPassword     = StringUtil.trimNullable(request.getParameter("newPassword"));
        String confirmPassword = StringUtil.trimNullable(request.getParameter("confirmPassword"));

        if (!ValidationUtil.isEmpty(currentPassword) && !ValidationUtil.isEmpty(newPassword)) {
            UserBean profile = userDAO.getUserById(userId);
            if (profile == null || !BCrypt.checkpw(currentPassword, profile.getPasswordHash())) {
                request.setAttribute("errorMsg", "Current password is incorrect.");
                doGet(request, response);
                return;
            }
            if (ValidationUtil.isEmpty(newPassword) || newPassword.length() < 8) {
                request.setAttribute("errorMsg", "New password must be at least 8 characters.");
                doGet(request, response);
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                request.setAttribute("errorMsg", "New passwords do not match.");
                doGet(request, response);
                return;
            }
            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            userDAO.updatePasswordHash(userId, newHash);
            response.sendRedirect(request.getContextPath() + "/account/profile?updated=true");
            return;
        }

        String fullName = StringUtil.trimNullable(request.getParameter("fullName"));
        String email = StringUtil.trimNullable(request.getParameter("email"));
        String phone = StringUtil.trimNullable(request.getParameter("phone"));

        if (ValidationUtil.isEmpty(fullName)) {
            request.setAttribute("errorMsg", "Full name is required.");
            doGet(request, response);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute("errorMsg", "Please enter a valid email address.");
            doGet(request, response);
            return;
        }

        boolean isUpdated = userDAO.updateUserProfile(userId, fullName, email, phone);
        if (isUpdated) {

            session.setAttribute("fullName", fullName);
            session.setAttribute("profile", getProfile(userId, role));
            response.sendRedirect(request.getContextPath() + "/account/profile?updated=true");

        } else {

            request.setAttribute("errorMsg", "Profile update failed. Please try again.");
            doGet(request, response);

        }

    }

    private UserBean getProfile(int userId, String role) {

        if ("Admin".equals(role)) {
            AdminBean admin = userDAO.getAdminProfile(userId);
            return admin;
        }
        if ("Staff".equals(role)) {
            StaffBean staff = userDAO.getStaffProfile(userId);
            return staff;
        }
        return null;

    }

}
