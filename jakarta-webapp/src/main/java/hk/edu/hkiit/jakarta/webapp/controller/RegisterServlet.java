package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;
import java.sql.Date;

import org.mindrot.jbcrypt.BCrypt;

import hk.edu.hkiit.jakarta.webapp.bean.UserBean;
import hk.edu.hkiit.jakarta.webapp.dao.OtpDAO;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;
import hk.edu.hkiit.jakarta.webapp.util.OtpUtil;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;
import hk.edu.hkiit.jakarta.webapp.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private OtpDAO otpDAO = new OtpDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            String reactUrl = getServletContext().getInitParameter("reactUrl");
            response.sendRedirect(reactUrl + "/profile");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username        = StringUtil.trimNullable(request.getParameter("username"));
        String password        = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName        = StringUtil.trimNullable(request.getParameter("fullName"));
        String email           = StringUtil.trimNullable(request.getParameter("email"));
        String phone           = StringUtil.trimNullable(request.getParameter("phone"));
        String idNumber        = StringUtil.trimNullable(request.getParameter("idNumber"));
        String dobStr          = StringUtil.trimNullable(request.getParameter("dateOfBirth"));
        String gender          = request.getParameter("gender");
        String address         = StringUtil.trimNullable(request.getParameter("address"));
        String emergencyName   = StringUtil.trimNullable(request.getParameter("emergencyContactName"));
        String emergencyPhone  = StringUtil.trimNullable(request.getParameter("emergencyContactPhone"));

        String error = validate(username, password, confirmPassword, fullName, email);
        if (error != null) {
            forwardWithError(request, response, error);
            return;
        }
        if (userDAO.usernameExists(username)) {
            forwardWithError(request, response, "Username is already taken. Please choose another.");
            return;
        }
        if (userDAO.emailExists(email)) {
            forwardWithError(request, response, "This email address is already registered.");
            return;
        }

        Date dateOfBirth = null;
        if (dobStr != null && !dobStr.isEmpty()) {
            try { dateOfBirth = Date.valueOf(dobStr); }
            catch (IllegalArgumentException ignored) {}
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        boolean created = userDAO.registerPatient(
                username, passwordHash, fullName, email, phone,
                idNumber, dateOfBirth, gender, address, emergencyName, emergencyPhone);

        if (!created) {
            forwardWithError(request, response, "Registration failed due to a system error. Please try again.");
            return;
        }

        UserBean newUser = userDAO.getUserByUsername(username);
        if (newUser == null) {
            forwardWithError(request, response, "Account created but could not load user. Please contact support.");
            return;
        }

        String otp = OtpUtil.generateOtp();
        otpDAO.saveOtp(newUser.getUserId(), otp);
        OtpUtil.sendOtpEmail(email, otp, "Email Verification");

        HttpSession session = request.getSession(true);
        session.setAttribute("otpUserId",  newUser.getUserId());
        session.setAttribute("otpEmail",   email);
        session.setAttribute("otpAction",  "register");

        response.sendRedirect(request.getContextPath() + "/otp-verify");
    }

    private String validate(String username, String password, String confirmPassword,
                             String fullName, String email) {
        if (username == null || username.length() < 4)
            return "Username must be at least 4 characters.";
        if (!username.matches("[A-Za-z0-9_]+"))
            return "Username may only contain letters, numbers and underscores.";
        if (password == null || password.length() < 8)
            return "Password must be at least 8 characters.";
        if (!password.equals(confirmPassword))
            return "Passwords do not match.";
        if (ValidationUtil.isEmpty(fullName))
            return "Full name is required.";
        if (!ValidationUtil.isValidEmail(email))
            return "Please enter a valid email address.";
        return null;
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String msg)
            throws ServletException, IOException {
        request.setAttribute("errorMsg", msg);
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

}
