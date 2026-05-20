package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.PatientBean;
import hk.edu.hkiit.jakarta.webapp.bean.UserBean;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;
import hk.edu.hkiit.jakarta.webapp.util.JsonUtil;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;
import hk.edu.hkiit.jakarta.webapp.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Date;

@WebServlet(
    name = "PatientAccountApiServlet",
    urlPatterns = {"/api/patient/profile", "/api/patient/change-password"}
)
public class PatientAccountApiServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"/api/patient/profile".equals(request.getServletPath())) {
            response.setStatus(405);
            return;
        }

        handleGetProfile(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();
        if ("/api/patient/profile".equals(path)) {
            handleUpdateProfile(request, response);
        } else if ("/api/patient/change-password".equals(path)) {
            handleChangePassword(request, response);
        } else {
            response.setStatus(404);
        }
    }

    private void handleGetProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(401);
            response.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        PatientBean p = userDAO.getPatientProfile(userId);

        if (p == null) {
            response.setStatus(404);
            response.getWriter().write("{\"error\":\"Profile not found\"}");
            return;
        }

        response.getWriter().write("{" +
            "\"userId\":" + p.getUserId() +
            ",\"username\":\"" + JsonUtil.escape(p.getUsername()) + "\"" +
            ",\"fullName\":\"" + JsonUtil.escape(p.getFullName()) + "\"" +
            ",\"email\":\"" + JsonUtil.escape(p.getEmail()) + "\"" +
            ",\"phone\":\"" + JsonUtil.escape(p.getPhone()) + "\"" +
            ",\"idNumber\":\"" + JsonUtil.escape(p.getIdNumber()) + "\"" +
            ",\"dateOfBirth\":\"" + (p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "") + "\"" +
            ",\"gender\":\"" + JsonUtil.escape(p.getGender()) + "\"" +
            ",\"address\":\"" + JsonUtil.escape(p.getAddress()) + "\"" +
            ",\"emergencyContactName\":\"" + JsonUtil.escape(p.getEmergencyContactName()) + "\"" +
            ",\"emergencyContactPhone\":\"" + JsonUtil.escape(p.getEmergencyContactPhone()) + "\"" +
            ",\"createdAt\":\"" + (p.getCreatedAt() != null ? new java.text.SimpleDateFormat("dd MMM yyyy").format(p.getCreatedAt()) : "") + "\"" +
            "}");
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(401);
            response.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String fullName = StringUtil.trimNullable(request.getParameter("fullName"));
        String email    = StringUtil.trimNullable(request.getParameter("email"));
        String phone    = StringUtil.trimNullable(request.getParameter("phone"));
        String idNumber       = StringUtil.trimNullable(request.getParameter("idNumber"));
        String dobStr         = StringUtil.trimNullable(request.getParameter("dateOfBirth"));
        String gender         = request.getParameter("gender");
        String address        = StringUtil.trimNullable(request.getParameter("address"));
        String emergencyName  = StringUtil.trimNullable(request.getParameter("emergencyContactName"));
        String emergencyPhone = StringUtil.trimNullable(request.getParameter("emergencyContactPhone"));

        if (ValidationUtil.isEmpty(fullName)) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"Full name is required.\"}");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"Please enter a valid email address.\"}");
            return;
        }

        Date dateOfBirth = null;
        if (dobStr != null && !dobStr.isEmpty()) {
            try { dateOfBirth = Date.valueOf(dobStr); }
            catch (IllegalArgumentException ignored) {}
        }

        boolean basicOk   = userDAO.updateUserProfile(userId, fullName, email, phone);
        boolean profileOk = userDAO.updatePatientProfile(userId, idNumber, dateOfBirth,
                                                          gender, address, emergencyName, emergencyPhone);

        if (!basicOk || !profileOk) {
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Profile update failed. Please try again.\"}");
            return;
        }

        session.setAttribute("fullName", fullName);
        response.getWriter().write("{\"success\":true}");
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(401);
            response.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        int userId        = (Integer) session.getAttribute("userId");
        String currentPwd = request.getParameter("currentPassword");
        String newPwd     = request.getParameter("newPassword");
        String confirmPwd = request.getParameter("confirmPassword");

        if (currentPwd == null || currentPwd.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"Current password is required.\"}");
            return;
        }
        if (newPwd == null || newPwd.length() < 8) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"New password must be at least 8 characters.\"}");
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"New passwords do not match.\"}");
            return;
        }

        UserBean user = userDAO.getUserById(userId);
        if (user == null || !BCrypt.checkpw(currentPwd, user.getPasswordHash())) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"Current password is incorrect.\"}");
            return;
        }

        if (BCrypt.checkpw(newPwd, user.getPasswordHash())) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"New password must be different from your current password.\"}");
            return;
        }

        String newHash = BCrypt.hashpw(newPwd, BCrypt.gensalt());
        if (!userDAO.updatePasswordHash(userId, newHash)) {
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Password update failed. Please try again.\"}");
            return;
        }

        response.getWriter().write("{\"success\":true}");
    }
}
