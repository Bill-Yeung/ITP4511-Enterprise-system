package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.IncidentBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class IncidentDAO {

    private static final String SELECT_WITH_JOIN =
        "SELECT i.*, "
      + "       reporter.full_name AS reporter_name, "
      + "       c.name             AS clinic_name, "
      + "       patient.full_name  AS patient_name "
      + "FROM incidents i "
      + "JOIN users   reporter ON i.reported_by = reporter.user_id "
      + "JOIN clinics c        ON i.clinic_id   = c.clinic_id "
      + "LEFT JOIN users patient ON i.patient_id = patient.user_id ";

    public int createIncident(int reportedBy, int clinicId,
                              Integer patientId, Integer appointmentId,
                              String category, String severity, String description) {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement selectPs = null;
        ResultSet rs = null;
        int incidentId = -1;
        String sql;
        if (patientId != null && appointmentId != null) {
            sql = "INSERT INTO incidents "
                + "(reported_by, clinic_id, patient_id, appointment_id, category, severity, description) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else if (patientId != null) {
            sql = "INSERT INTO incidents "
                + "(reported_by, clinic_id, patient_id, appointment_id, category, severity, description) "
                + "VALUES (?, ?, ?, NULL, ?, ?, ?)";
        } else if (appointmentId != null) {
            sql = "INSERT INTO incidents "
                + "(reported_by, clinic_id, patient_id, appointment_id, category, severity, description) "
                + "VALUES (?, ?, NULL, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO incidents "
                + "(reported_by, clinic_id, patient_id, appointment_id, category, severity, description) "
                + "VALUES (?, ?, NULL, NULL, ?, ?, ?)";
        }
        String selectSql = "SELECT incident_id FROM incidents "
                         + "WHERE reported_by = ? AND clinic_id = ? "
                         + "AND category = ? AND severity = ? AND description = ? "
                         + "ORDER BY incident_id DESC LIMIT 1";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, reportedBy);
            ps.setInt(2, clinicId);
            if (patientId != null && appointmentId != null) {
                ps.setInt(3, patientId);
                ps.setInt(4, appointmentId);
                ps.setString(5, category);
                ps.setString(6, severity);
                ps.setString(7, description);
            } else if (patientId != null) {
                ps.setInt(3, patientId);
                ps.setString(4, category);
                ps.setString(5, severity);
                ps.setString(6, description);
            } else if (appointmentId != null) {
                ps.setInt(3, appointmentId);
                ps.setString(4, category);
                ps.setString(5, severity);
                ps.setString(6, description);
            } else {
                ps.setString(3, category);
                ps.setString(4, severity);
                ps.setString(5, description);
            }
            if (ps.executeUpdate() >= 1) {
                selectPs = conn.prepareStatement(selectSql);
                selectPs.setInt(1, reportedBy);
                selectPs.setInt(2, clinicId);
                selectPs.setString(3, category);
                selectPs.setString(4, severity);
                selectPs.setString(5, description);
                rs = selectPs.executeQuery();
                if (rs.next()) incidentId = rs.getInt("incident_id");
            }
            if (rs != null) rs.close();
            if (selectPs != null) selectPs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return incidentId;
    }

    public boolean updateStatus(int incidentId, String newStatus, String resolutionNotes, Integer clinicId) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql;
        if ("Resolved".equals(newStatus)) {
            sql = "UPDATE incidents SET status = ?, resolution_notes = ?, resolved_at = NOW() "
                + "WHERE incident_id = ?";
        } else {
            sql = "UPDATE incidents SET status = ?, resolution_notes = ?, resolved_at = NULL "
                + "WHERE incident_id = ?";
        }
        if (clinicId != null && clinicId > 0) {
            sql += " AND clinic_id = ?";
        }
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setString(2, resolutionNotes);
            ps.setInt(3, incidentId);
            if (clinicId != null && clinicId > 0) {
                ps.setInt(4, clinicId);
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

    public ArrayList<IncidentBean> getFilteredIncidents(Integer clinicId, String status, String category) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<IncidentBean> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_WITH_JOIN + "WHERE 1=1 ");
        if (clinicId != null && clinicId > 0) {
            sql.append("AND i.clinic_id = ? ");
        }
        if (status != null && !status.isEmpty()) {
            sql.append("AND i.status = ? ");
        }
        if (category != null && !category.isEmpty()) {
            sql.append("AND i.category = ? ");
        }
        sql.append("ORDER BY i.created_at DESC");
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());
            int index = 1;
            if (clinicId != null && clinicId > 0) {
                ps.setInt(index++, clinicId);
            }
            if (status != null && !status.isEmpty()) {
                ps.setString(index++, status);
            }
            if (category != null && !category.isEmpty()) {
                ps.setString(index++, category);
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

    public ArrayList<IncidentBean> getByReporter(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<IncidentBean> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + "WHERE i.reported_by = ? ORDER BY i.created_at DESC";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
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

    public ArrayList<String[]> getPatientIncidentSummary(Integer clinicId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql =
            "SELECT i.patient_id, u.full_name, "
          + "       COUNT(*) AS total, "
          + "       SUM(CASE WHEN i.status <> 'Resolved' THEN 1 ELSE 0 END) AS open_cnt, "
          + "       SUM(CASE WHEN i.status =  'Resolved' THEN 1 ELSE 0 END) AS resolved_cnt "
          + "FROM incidents i "
          + "JOIN users u ON i.patient_id = u.user_id "
          + "WHERE i.patient_id IS NOT NULL ";
        if (clinicId != null && clinicId > 0) {
            sql += "AND i.clinic_id = ? ";
        }
        sql += "GROUP BY i.patient_id, u.full_name "
             + "ORDER BY total DESC, u.full_name";
        ArrayList<String[]> rows = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (clinicId != null && clinicId > 0) {
                ps.setInt(1, clinicId);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("patient_id")),
                    rs.getString("full_name"),
                    String.valueOf(rs.getInt("total")),
                    String.valueOf(rs.getInt("open_cnt")),
                    String.valueOf(rs.getInt("resolved_cnt"))
                });
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return rows;
    }

    private IncidentBean mapRow(ResultSet rs) throws SQLException {
        IncidentBean i = new IncidentBean();
        i.setIncidentId(rs.getInt("incident_id"));
        i.setReportedBy(rs.getInt("reported_by"));
        i.setClinicId(rs.getInt("clinic_id"));
        int pid = rs.getInt("patient_id");
        i.setPatientId(rs.wasNull() ? null : pid);
        int aid = rs.getInt("appointment_id");
        i.setAppointmentId(rs.wasNull() ? null : aid);
        i.setCategory(rs.getString("category"));
        i.setSeverity(rs.getString("severity"));
        i.setDescription(rs.getString("description"));
        i.setStatus(rs.getString("status"));
        i.setResolutionNotes(rs.getString("resolution_notes"));
        i.setCreatedAt(rs.getTimestamp("created_at"));
        i.setResolvedAt(rs.getTimestamp("resolved_at"));
        i.setReportedByName(safe(rs, "reporter_name"));
        i.setClinicName(safe(rs, "clinic_name"));
        i.setPatientName(safe(rs, "patient_name"));
        return i;
    }

    private String safe(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException ex) { return null; }
    }
}
