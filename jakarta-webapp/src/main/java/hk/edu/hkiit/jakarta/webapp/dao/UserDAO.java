package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.UserBean;
import hk.edu.hkiit.jakarta.webapp.bean.PatientBean;
import hk.edu.hkiit.jakarta.webapp.bean.StaffBean;
import hk.edu.hkiit.jakarta.webapp.bean.AdminBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;
import hk.edu.hkiit.jakarta.webapp.util.EncryptionUtil;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class UserDAO {

    public UserBean getUserByEmail(String email) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        UserBean user = null;
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) {
                user = mapRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return user;
    }

    public UserBean getUserByUsername(String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        UserBean user = null;
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                user = mapRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return user;
    }

    public PatientBean getPatientProfile(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PatientBean patient = null;
        String sql = "SELECT u.*, p.id_number, p.date_of_birth, p.gender, p.address, "
                   + "p.emergency_contact_name, p.emergency_contact_phone "
                   + "FROM users u JOIN patient_profiles p ON u.user_id = p.user_id "
                   + "WHERE u.user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                patient = new PatientBean();
                mapBaseFields(patient, rs);
                patient.setIdNumber(EncryptionUtil.decrypt(rs.getString("id_number")));
                patient.setDateOfBirth(rs.getDate("date_of_birth"));
                patient.setGender(rs.getString("gender"));
                patient.setAddress(EncryptionUtil.decrypt(rs.getString("address")));
                patient.setEmergencyContactName(rs.getString("emergency_contact_name"));
                patient.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return patient;
    }

    public StaffBean getStaffProfile(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StaffBean staff = null;
        String sql = "SELECT u.*, s.clinic_id, s.position "
                   + "FROM users u JOIN staff_profiles s ON u.user_id = s.user_id "
                   + "WHERE u.user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                staff = new StaffBean();
                mapBaseFields(staff, rs);
                staff.setClinicId(rs.getInt("clinic_id"));
                staff.setPosition(rs.getString("position"));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return staff;
    }

    public AdminBean getAdminProfile(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        AdminBean admin = null;
        String sql = "SELECT u.*, a.clinic_id, a.admin_level "
                   + "FROM users u JOIN admin_profiles a ON u.user_id = a.user_id "
                   + "WHERE u.user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                admin = new AdminBean();
                mapBaseFields(admin, rs);
                int clinicId = rs.getInt("clinic_id");
                admin.setClinicId(rs.wasNull() ? null : clinicId);
                admin.setAdminLevel(rs.getString("admin_level"));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return admin;
    }

    // Encrypts id_number and address; emergency contacts stored plaintext
    public boolean registerPatient(String username, String passwordHash,
                                   String fullName, String email, String phone,
                                   String idNumber, Date dateOfBirth, String gender,
                                   String address, String emergencyContactName,
                                   String emergencyContactPhone) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String userSql = "INSERT INTO users (username, password_hash, role, full_name, email, phone) "
                       + "VALUES (?, ?, 'Patient', ?, ?, ?)";
        String profileSql = "INSERT INTO patient_profiles (user_id, id_number, date_of_birth, gender, "
                           + "address, emergency_contact_name, emergency_contact_phone) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(userSql);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, email);
            ps.setString(5, phone != null ? phone : "");
            ps.executeUpdate();
            ps.close();

            int userId = findUserIdByUsername(conn, username);
            if (userId < 0) {
                conn.close();
                return false;
            }

            String encryptedId   = (idNumber != null && !idNumber.isEmpty()) ? EncryptionUtil.encrypt(idNumber) : null;
            String encryptedAddr = (address  != null && !address.isEmpty())  ? EncryptionUtil.encrypt(address)  : "";

            ps = conn.prepareStatement(profileSql);
            ps.setInt(1, userId);
            ps.setString(2, encryptedId);
            ps.setDate(3, dateOfBirth);
            ps.setString(4, gender != null && !gender.isEmpty() ? gender : null);
            ps.setString(5, encryptedAddr);
            ps.setString(6, emergencyContactName);
            ps.setString(7, emergencyContactPhone);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public boolean registerPatient(String username, String passwordHash,
                                   String fullName, String email, String phone) {
        return registerPatient(username, passwordHash, fullName, email, phone,
                              null, null, null, null, null, null);
    }

    public boolean updatePatientProfile(int userId, String idNumber, Date dateOfBirth,
                                        String gender, String address,
                                        String emergencyContactName,
                                        String emergencyContactPhone) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE patient_profiles SET id_number = ?, date_of_birth = ?, gender = ?, "
                   + "address = ?, emergency_contact_name = ?, emergency_contact_phone = ? "
                   + "WHERE user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, (idNumber != null && !idNumber.isEmpty()) ? EncryptionUtil.encrypt(idNumber) : null);
            ps.setDate(2, dateOfBirth);
            ps.setString(3, (gender != null && !gender.isEmpty()) ? gender : null);
            ps.setString(4, (address  != null && !address.isEmpty())  ? EncryptionUtil.encrypt(address)  : "");
            ps.setString(5, emergencyContactName);
            ps.setString(6, emergencyContactPhone);
            ps.setInt(7, userId);
            // Treat "0 rows affected" as a no-op success: re-saving identical
            // values still means the requested state is in the database.
            ps.executeUpdate();
            isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public ArrayList<UserBean> getUsersByRole(String role) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<UserBean> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public UserBean getUserById(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        UserBean user = null;
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                user = mapRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return user;
    }

    public boolean updateUserProfile(int userId, String fullName, String email, String phone) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ? WHERE user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, userId);
            // Treat 0 rows affected as a no-op success.
            ps.executeUpdate();
            isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public boolean updatePasswordHash(int userId, String newHash) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public boolean deleteUser(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "DELETE FROM users WHERE user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public ArrayList<UserBean> listUsers(String role, Integer clinicId, String search) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<UserBean> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT u.* FROM users u "
            + "LEFT JOIN staff_profiles sp ON u.user_id = sp.user_id "
            + "LEFT JOIN admin_profiles ap ON u.user_id = ap.user_id "
            + "WHERE 1=1 ");

        if (role != null && !role.isEmpty()) {
            sql.append(" AND u.role = ? ");
        }
        if (clinicId != null && clinicId > 0) {
            sql.append(" AND (sp.clinic_id = ? OR ap.clinic_id = ?) ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?) ");
        }
        sql.append(" ORDER BY u.role, u.full_name");

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());
            int index = 1;
            if (role != null && !role.isEmpty()) {
                ps.setString(index++, role);
            }
            if (clinicId != null && clinicId > 0) {
                ps.setInt(index++, clinicId);
                ps.setInt(index++, clinicId);
            }
            if (search != null && !search.trim().isEmpty()) {
                String like = "%" + search.trim() + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
            }
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean createStaff(String username, String passwordHash, String fullName,
                               String email, String phone, int clinicId, String position) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String userSql = "INSERT INTO users (username, password_hash, role, full_name, email, phone) "
                       + "VALUES (?, ?, 'Staff', ?, ?, ?)";
        String profSql = "INSERT INTO staff_profiles (user_id, clinic_id, position) VALUES (?, ?, ?)";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(userSql);
            ps.setString(1, username); ps.setString(2, passwordHash);
            ps.setString(3, fullName); ps.setString(4, email); ps.setString(5, phone);
            ps.executeUpdate();
            ps.close();

            int userId = findUserIdByUsername(conn, username);
            if (userId < 0) {
                conn.close();
                return false;
            }

            ps = conn.prepareStatement(profSql);
            ps.setInt(1, userId); ps.setInt(2, clinicId); ps.setString(3, position);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public boolean createAdmin(String username, String passwordHash, String fullName,
                               String email, String phone, Integer clinicId, String adminLevel) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String userSql = "INSERT INTO users (username, password_hash, role, full_name, email, phone) "
                       + "VALUES (?, ?, 'Admin', ?, ?, ?)";
        String profSql;
        if (clinicId == null) {
            profSql = "INSERT INTO admin_profiles (user_id, clinic_id, admin_level) VALUES (?, NULL, ?)";
        } else {
            profSql = "INSERT INTO admin_profiles (user_id, clinic_id, admin_level) VALUES (?, ?, ?)";
        }
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(userSql);
            ps.setString(1, username); ps.setString(2, passwordHash);
            ps.setString(3, fullName); ps.setString(4, email); ps.setString(5, phone);
            ps.executeUpdate();
            ps.close();

            int userId = findUserIdByUsername(conn, username);
            if (userId < 0) {
                conn.close();
                return false;
            }

            ps = conn.prepareStatement(profSql);
            ps.setInt(1, userId);
            if (clinicId == null) {
                ps.setString(2, adminLevel);
            } else {
                ps.setInt(2, clinicId);
                ps.setString(3, adminLevel);
            }
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public boolean updateStaffProfile(int userId, int clinicId, String position) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE staff_profiles SET clinic_id = ?, position = ? WHERE user_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId); ps.setString(2, position); ps.setInt(3, userId);
            ps.executeUpdate();
            isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public boolean updateAdminProfile(int userId, Integer clinicId, String adminLevel) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql;
        if (clinicId == null) {
            sql = "UPDATE admin_profiles SET clinic_id = NULL, admin_level = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE admin_profiles SET clinic_id = ?, admin_level = ? WHERE user_id = ?";
        }
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (clinicId == null) {
                ps.setString(1, adminLevel); ps.setInt(2, userId);
            } else {
                ps.setInt(1, clinicId); ps.setString(2, adminLevel); ps.setInt(3, userId);
            }
            ps.executeUpdate();
            isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public ArrayList<Integer> getStaffAndAdminIdsForClinic(int clinicId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Integer> ids = new ArrayList<>();
        String sql = "SELECT u.user_id FROM users u "
                   + "JOIN staff_profiles sp ON u.user_id = sp.user_id "
                   + "WHERE sp.clinic_id = ? "
                   + "UNION "
                   + "SELECT u.user_id FROM users u "
                   + "JOIN admin_profiles ap ON u.user_id = ap.user_id "
                   + "WHERE ap.clinic_id = ? OR ap.clinic_id IS NULL";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setInt(2, clinicId);
            rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return ids;
    }

    public boolean usernameExists(String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) exists = true;
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return exists;
    }

    public boolean emailExists(String email) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) exists = true;
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return exists;
    }

    private int findUserIdByUsername(Connection conn, String username) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int userId = -1;
        String sql = "SELECT user_id FROM users WHERE username = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        rs = ps.executeQuery();
        if (rs.next()) {
            userId = rs.getInt("user_id");
        }
        rs.close();
        ps.close();
        return userId;
    }

    private UserBean mapRow(ResultSet rs) throws SQLException {
        UserBean u = new UserBean();
        mapBaseFields(u, rs);
        return u;
    }

    private void mapBaseFields(UserBean u, ResultSet rs) throws SQLException {
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
    }
}
