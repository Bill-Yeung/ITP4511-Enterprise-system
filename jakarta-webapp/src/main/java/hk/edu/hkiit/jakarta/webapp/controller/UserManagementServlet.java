package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.AdminBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.PatientBean;
import hk.edu.hkiit.jakarta.webapp.bean.StaffBean;
import hk.edu.hkiit.jakarta.webapp.bean.UserBean;
import hk.edu.hkiit.jakarta.webapp.dao.AuditLogDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "UserManagementServlet", urlPatterns = {"/admin/users"})
public class UserManagementServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private ClinicDAO clinicDAO = new ClinicDAO();
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

        String action = request.getParameter("action");
        if ("edit".equals(action)) {
            showEditForm(request, response);
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

        String action = request.getParameter("action");
        if ("save".equals(action)) {
            saveUser(request, response);
            return;
        }

        if ("delete".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    int targetId = Integer.parseInt(idStr);
                    int adminId = (Integer) session.getAttribute("userId");
                    if (targetId == adminId) {
                        session.setAttribute("adminError", "You cannot delete your own account.");
                    } else if (userDAO.deleteUser(targetId)) {
                        session.setAttribute("adminMsg", "User #" + targetId + " deleted.");
                        auditLogDAO.logAction(adminId, "Deleted user #" + targetId);
                    } else {
                        session.setAttribute("adminError", "Delete failed.");
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("adminError", "Invalid user ID.");
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String role = request.getParameter("role");
        String clinicStr = request.getParameter("clinicId");
        String search = request.getParameter("search");
        Integer clinicId = StringUtil.parseIntOrNull(clinicStr);

        ArrayList<UserBean> users = userDAO.listUsers(role, clinicId, search);
        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();

        request.setAttribute("users", users);
        request.setAttribute("clinics", clinics);
        request.setAttribute("filterRole", role == null ? "" : role);
        request.setAttribute("filterClinicId", clinicId == null ? "" : String.valueOf(clinicId));
        request.setAttribute("filterSearch", search == null ? "" : search);

        request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        UserBean user = null;
        StaffBean staff = null;
        AdminBean admin = null;
        PatientBean patient = null;

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int userId = Integer.parseInt(idStr);
                user = userDAO.getUserById(userId);
                if (user != null) {
                    if ("Staff".equals(user.getRole())) staff = userDAO.getStaffProfile(userId);
                    else if ("Admin".equals(user.getRole())) admin = userDAO.getAdminProfile(userId);
                    else if ("Patient".equals(user.getRole())) patient = userDAO.getPatientProfile(userId);
                }
            } catch (NumberFormatException ignored) {}
        }

        ArrayList<ClinicBean> clinics = clinicDAO.getAllClinics();
        request.setAttribute("user", user);
        request.setAttribute("staff", staff);
        request.setAttribute("admin", admin);
        request.setAttribute("patient", patient);
        request.setAttribute("clinics", clinics);
        request.getRequestDispatcher("/WEB-INF/views/admin/user-edit.jsp").forward(request, response);
    }

    private void saveUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        int adminId = (Integer) session.getAttribute("userId");

        String idStr = request.getParameter("id");
        String username = StringUtil.trimToEmpty(request.getParameter("username"));
        String fullName = StringUtil.trimToEmpty(request.getParameter("fullName"));
        String email = StringUtil.trimToEmpty(request.getParameter("email"));
        String phone = StringUtil.trimToEmpty(request.getParameter("phone"));
        String role = StringUtil.trimToEmpty(request.getParameter("role"));
        String password = request.getParameter("password");
        String clinicStr = request.getParameter("clinicId");
        String position = StringUtil.trimToEmpty(request.getParameter("position"));
        String adminLevel = StringUtil.trimToEmpty(request.getParameter("adminLevel"));

        Integer clinicId = null;
        if (clinicStr != null && !clinicStr.isEmpty()) {
            try { clinicId = Integer.parseInt(clinicStr); } catch (NumberFormatException ignored) {}
        }

        boolean isSaved = false;
        String msg;

        if (idStr == null || idStr.isEmpty()) {
            // CREATE
            if (username.isEmpty() || fullName.isEmpty() || password == null || password.isEmpty() || role.isEmpty()) {
                session.setAttribute("adminError", "Username, full name, role, and password are required.");
                response.sendRedirect(request.getContextPath() + "/admin/users?action=edit");
                return;
            }
            if (userDAO.usernameExists(username)) {
                session.setAttribute("adminError", "Username already taken.");
                response.sendRedirect(request.getContextPath() + "/admin/users?action=edit");
                return;
            }
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            switch (role) {
                case "Staff":
                    if (clinicId == null) {
                        session.setAttribute("adminError", "Clinic is required for Staff.");
                        response.sendRedirect(request.getContextPath() + "/admin/users?action=edit");
                        return;
                    }
                    isSaved = userDAO.createStaff(username, hash, fullName, email, phone, clinicId,
                            position == null || position.isEmpty() ? "Front Desk" : position);
                    break;
                case "Admin":
                    if (adminLevel == null || adminLevel.isEmpty()) adminLevel = "Clinic";
                    isSaved = userDAO.createAdmin(username, hash, fullName, email, phone, clinicId, adminLevel);
                    break;
                case "Patient":
                    isSaved = userDAO.registerPatient(username, hash, fullName, email, phone);
                    break;
                default:
                    session.setAttribute("adminError", "Unknown role: " + role);
                    response.sendRedirect(request.getContextPath() + "/admin/users?action=edit");
                    return;
            }
            msg = isSaved ? "User created." : "Create failed.";
            if (isSaved) auditLogDAO.logAction(adminId, "Created " + role + " user '" + username + "'");
        } else {
            // UPDATE - only the core fields and the role-specific profile
            int userId;
            try { userId = Integer.parseInt(idStr); }
            catch (NumberFormatException e) {
                session.setAttribute("adminError", "Invalid user ID.");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }
            isSaved = userDAO.updateUserProfile(userId, fullName, email, phone);
            if (isSaved && password != null && !password.isEmpty()) {
                userDAO.updatePasswordHash(userId, BCrypt.hashpw(password, BCrypt.gensalt()));
            }
            UserBean existing = userDAO.getUserById(userId);
            if (existing != null && "Staff".equals(existing.getRole()) && clinicId != null) {
                userDAO.updateStaffProfile(userId, clinicId,
                    position == null || position.isEmpty() ? "Front Desk" : position);
            } else if (existing != null && "Admin".equals(existing.getRole())) {
                userDAO.updateAdminProfile(userId, clinicId,
                    adminLevel == null || adminLevel.isEmpty() ? "Clinic" : adminLevel);
            } else if (existing != null && "Patient".equals(existing.getRole())) {
                String idNumber = StringUtil.trimToEmpty(request.getParameter("idNumber"));
                String dobStr   = StringUtil.trimToEmpty(request.getParameter("dateOfBirth"));
                String gender   = StringUtil.trimToEmpty(request.getParameter("gender"));
                String address  = StringUtil.trimToEmpty(request.getParameter("address"));
                String emName   = StringUtil.trimToEmpty(request.getParameter("emergencyContactName"));
                String emPhone  = StringUtil.trimToEmpty(request.getParameter("emergencyContactPhone"));
                java.sql.Date dob = null;
                if (!dobStr.isEmpty()) {
                    try { dob = java.sql.Date.valueOf(dobStr); }
                    catch (IllegalArgumentException ignored) {}
                }
                userDAO.updatePatientProfile(userId,
                    idNumber.isEmpty() ? null : idNumber,
                    dob,
                    gender.isEmpty() ? null : gender,
                    address,
                    emName,
                    emPhone);
            }
            msg = isSaved ? "User updated." : "Update failed.";
            if (isSaved) auditLogDAO.logAction(adminId, "Updated user #" + userId);
        }

        if (isSaved) session.setAttribute("adminMsg", msg);
        else session.setAttribute("adminError", msg);

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

}
