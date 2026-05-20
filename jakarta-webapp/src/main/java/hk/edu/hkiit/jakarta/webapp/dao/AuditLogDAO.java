package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.AuditLogBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

// DAO for audit log
public class AuditLogDAO {

    // Common SQL
    private static final String SELECT_WITH_JOIN =
        "SELECT al.*, u.full_name, u.role "
      + "FROM audit_logs al "
      + "JOIN users u ON al.user_id = u.user_id "
      + "LEFT JOIN staff_profiles sp ON u.user_id = sp.user_id "
      + "LEFT JOIN admin_profiles ap ON u.user_id = ap.user_id ";

    // Create audit log

    public boolean logAction(int userId, String actionDescription) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "INSERT INTO audit_logs (user_id, action_description) VALUES (?, ?)";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, actionDescription);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) {
                isSuccess = true;
            }
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return isSuccess;

    }

    // Retrieve audit logs

    public ArrayList<AuditLogBean> getFilteredLogs(String role, String sort, Integer clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AuditLogBean> list = new ArrayList<>();

        String sql = SELECT_WITH_JOIN;
        String prefix = "WHERE ";
        if (role != null && !role.isEmpty()) {
            sql += prefix + "u.role = ? ";
            prefix = "AND ";
        }
        if (clinicId != null && clinicId > 0) {
            sql += prefix + "(sp.clinic_id = ? OR ap.clinic_id = ?) ";
        }

        if ("id_asc".equals(sort)) {
            sql += "ORDER BY al.log_id ASC";
        } else if ("id_desc".equals(sort)) {
            sql += "ORDER BY al.log_id DESC";
        } else if ("time_asc".equals(sort)) {
            sql += "ORDER BY al.action_timestamp ASC";
        } else if ("user_asc".equals(sort)) {
            sql += "ORDER BY u.full_name ASC, al.action_timestamp DESC";
        } else if ("role_asc".equals(sort)) {
            sql += "ORDER BY u.role ASC, al.action_timestamp DESC";
        } else {
            sql += "ORDER BY al.action_timestamp DESC";
        }

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            int index = 1;
            if (role != null && !role.isEmpty()) {
                ps.setString(index++, role);
            }
            if (clinicId != null && clinicId > 0) {
                ps.setInt(index++, clinicId);
                ps.setInt(index++, clinicId);
            }
            rs = ps.executeQuery();
            list = mapRows(rs);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    // Helpers for bean

    private ArrayList<AuditLogBean> mapRows(ResultSet rs) throws SQLException {
        ArrayList<AuditLogBean> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private AuditLogBean mapRow(ResultSet rs) throws SQLException {
        AuditLogBean log = new AuditLogBean();
        log.setLogId(rs.getInt("log_id"));
        log.setUserId(rs.getInt("user_id"));
        log.setRole(rs.getString("role"));
        log.setActionDescription(rs.getString("action_description"));
        log.setActionTimestamp(rs.getTimestamp("action_timestamp"));
        log.setFullName(rs.getString("full_name"));
        return log;
    }
    
}
