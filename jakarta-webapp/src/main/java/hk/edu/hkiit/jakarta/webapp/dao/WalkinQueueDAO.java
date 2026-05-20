package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.WalkinQueueBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class WalkinQueueDAO {

    private static final String SELECT_WITH_JOIN =
        "SELECT wq.*, u.full_name AS patient_name, "
      + "       c.name AS clinic_name, s.name AS service_name, "
      + "       c.clinic_id AS clinic_id, s.service_id AS service_id "
      + "FROM walkin_queues wq "
      + "JOIN users u            ON wq.patient_id        = u.user_id "
      + "JOIN clinic_services cs ON wq.clinic_service_id = cs.clinic_service_id "
      + "JOIN clinics c          ON cs.clinic_id          = c.clinic_id "
      + "JOIN services s         ON cs.service_id          = s.service_id ";

    public int joinQueue(int patientId, int clinicServiceId) {
        Date today = new Date(System.currentTimeMillis());
        String sql = "INSERT INTO walkin_queues "
                   + "(patient_id, clinic_service_id, queue_date, queue_number) "
                   + "VALUES (?, ?, ?, ?)";

        int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            int nextNumber = getNextQueueNumber(clinicServiceId, today);
            if (nextNumber == -1) return -1;

            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet keys = null;
            try {
                conn = DBConnection.getConnection();
                ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, patientId);
                ps.setInt(2, clinicServiceId);
                ps.setDate(3, today);
                ps.setInt(4, nextNumber);
                if (ps.executeUpdate() >= 1) {
                    keys = ps.getGeneratedKeys();
                    int queueId = keys.next() ? keys.getInt(1) : -1;
                    keys.close();
                    ps.close();
                    conn.close();
                    return queueId;
                }
                ps.close();
                conn.close();
                return -1;
            } catch (SQLIntegrityConstraintViolationException dup) {
                try { if (keys != null) keys.close(); } catch (SQLException ignore) {}
                try { if (ps != null) ps.close(); } catch (SQLException ignore) {}
                try { if (conn != null) conn.close(); } catch (SQLException ignore) {}
                String msg = dup.getMessage() != null ? dup.getMessage().toLowerCase() : "";
                if (msg.contains("uq_patient_service_active") || msg.contains("uq_patient_service_date")) {
                    if (msg.contains("uq_patient_service_date")) {
                        return reactivateSkippedTicket(patientId, clinicServiceId, today, nextNumber);
                    }
                    return -1;
                }
                if (attempt == maxAttempts) {
                    dup.printStackTrace();
                    return -1;
                }
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
                try { if (keys != null) keys.close(); } catch (SQLException ignore) {}
                try { if (ps != null) ps.close(); } catch (SQLException ignore) {}
                try { if (conn != null) conn.close(); } catch (SQLException ignore) {}
                return -1;
            }
        }
        return -1;
    }

    private int reactivateSkippedTicket(int patientId, int clinicServiceId, Date today, int newNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "UPDATE walkin_queues "
                   + "SET status = 'Waiting', queue_number = ?, joined_at = CURRENT_TIMESTAMP "
                   + "WHERE patient_id = ? AND clinic_service_id = ? AND queue_date = ? "
                   + "AND status = 'Skipped'";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, newNumber);
            ps.setInt(2, patientId);
            ps.setInt(3, clinicServiceId);
            ps.setDate(4, today);
            int updated = ps.executeUpdate();
            ps.close();
            if (updated < 1) {
                conn.close();
                return -1;
            }
            String findSql = "SELECT queue_id FROM walkin_queues "
                           + "WHERE patient_id = ? AND clinic_service_id = ? AND queue_date = ?";
            ps = conn.prepareStatement(findSql);
            ps.setInt(1, patientId);
            ps.setInt(2, clinicServiceId);
            ps.setDate(3, today);
            ResultSet rs = ps.executeQuery();
            int queueId = rs.next() ? rs.getInt("queue_id") : -1;
            rs.close();
            ps.close();
            conn.close();
            return queueId;
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            try { if (ps != null) ps.close(); } catch (SQLException ignore) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignore) {}
            return -1;
        }
    }

    private int getNextQueueNumber(int clinicServiceId, Date date) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int nextNumber = -1;
        String sql = "SELECT COALESCE(MAX(queue_number), 0) + 1 "
                   + "FROM walkin_queues WHERE clinic_service_id = ? AND queue_date = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicServiceId);
            ps.setDate(2, date);
            rs = ps.executeQuery();
            if (rs.next()) nextNumber = rs.getInt(1);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return nextNumber;
    }

    public WalkinQueueBean callNext(int clinicServiceId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        WalkinQueueBean queue = null;
        Date today = new Date(System.currentTimeMillis());

        // Atomic: update only the lowest-numbered Waiting ticket in one statement,
        // then retrieve it. This prevents two concurrent callNext() calls from
        // picking the same patient.
        String updateSql = "UPDATE walkin_queues SET status = 'Called' "
                         + "WHERE clinic_service_id = ? AND queue_date = ? AND status = 'Waiting' "
                         + "ORDER BY queue_number ASC LIMIT 1";
        String selectSql = "SELECT queue_id FROM walkin_queues "
                         + "WHERE clinic_service_id = ? AND queue_date = ? AND status = 'Called' "
                         + "ORDER BY queue_number ASC LIMIT 1";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(updateSql);
            ps.setInt(1, clinicServiceId);
            ps.setDate(2, today);
            int updated = ps.executeUpdate();
            ps.close();

            if (updated >= 1) {
                ps = conn.prepareStatement(selectSql);
                ps.setInt(1, clinicServiceId);
                ps.setDate(2, today);
                rs = ps.executeQuery();
                if (rs.next()) {
                    queue = getQueueById(rs.getInt("queue_id"));
                }
                rs.close();
                ps.close();
            }
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return queue;
    }

    public boolean skipPatient(int queueId) {
        return updateStatus(queueId, "Skipped");
    }

    public boolean markServed(int queueId) {
        return updateStatus(queueId, "Served");
    }

    public boolean recallPatient(int queueId) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        String sql = "UPDATE walkin_queues SET status = 'Waiting' "
                   + "WHERE queue_id = ? AND status = 'Skipped'";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queueId);
            ok = ps.executeUpdate() >= 1;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return ok;
    }

    private boolean updateStatus(int queueId, String newStatus) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE walkin_queues SET status = ? WHERE queue_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, queueId);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public ArrayList<WalkinQueueBean> getTodayQueue(int clinicServiceId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<WalkinQueueBean> list = new ArrayList<>();
        Date today = new Date(System.currentTimeMillis());
        String sql = SELECT_WITH_JOIN
                   + "WHERE wq.clinic_service_id = ? AND wq.queue_date = ? "
                   + "ORDER BY wq.queue_number";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicServiceId);
            ps.setDate(2, today);
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

    public ArrayList<WalkinQueueBean> getTodayQueueByClinic(int clinicId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<WalkinQueueBean> list = new ArrayList<>();
        Date today = new Date(System.currentTimeMillis());
        String sql = SELECT_WITH_JOIN
                   + "WHERE c.clinic_id = ? AND wq.queue_date = ? "
                   + "ORDER BY wq.queue_number";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setDate(2, today);
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

    public ArrayList<WalkinQueueBean> getPatientQueueToday(int patientId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<WalkinQueueBean> list = new ArrayList<>();
        Date today = new Date(System.currentTimeMillis());
        String sql = SELECT_WITH_JOIN
                   + "WHERE wq.patient_id = ? AND wq.queue_date = ? "
                   + "ORDER BY wq.queue_number";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
            ps.setDate(2, today);
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

    public WalkinQueueBean getQueueById(int queueId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        WalkinQueueBean queue = null;
        String sql = SELECT_WITH_JOIN + "WHERE wq.queue_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queueId);
            rs = ps.executeQuery();
            if (rs.next()) queue = mapRow(rs);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return queue;
    }

    public boolean isAlreadyQueued(int patientId, int clinicServiceId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        Date today = new Date(System.currentTimeMillis());
        String sql = "SELECT 1 FROM walkin_queues "
                   + "WHERE patient_id = ? AND clinic_service_id = ? AND queue_date = ? "
                   + "AND status IN ('Waiting', 'Called')";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
            ps.setInt(2, clinicServiceId);
            ps.setDate(3, today);
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

    public String getTodayTicketStatus(int patientId, int clinicServiceId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String status = null;
        Date today = new Date(System.currentTimeMillis());
        String sql = "SELECT status FROM walkin_queues "
                   + "WHERE patient_id = ? AND clinic_service_id = ? AND queue_date = ? "
                   + "ORDER BY queue_id DESC LIMIT 1";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
            ps.setInt(2, clinicServiceId);
            ps.setDate(3, today);
            rs = ps.executeQuery();
            if (rs.next()) status = rs.getString(1);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return status;
    }

    public boolean cancelQueue(int queueId, int patientId) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "DELETE FROM walkin_queues "
                   + "WHERE queue_id = ? AND patient_id = ? AND status = 'Waiting'";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queueId);
            ps.setInt(2, patientId);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public int[] getQueueStatus(int clinicId, int serviceId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int[] status = new int[]{0, 0};
        String sql = "SELECT "
                   + "  COALESCE(MAX(CASE WHEN wq.status = 'Called' THEN wq.queue_number END), 0) AS currently_serving, "
                   + "  COUNT(CASE WHEN wq.status = 'Waiting' THEN 1 END) AS waiting_count "
                   + "FROM walkin_queues wq "
                   + "JOIN clinic_services cs ON wq.clinic_service_id = cs.clinic_service_id "
                   + "WHERE cs.clinic_id = ? AND cs.service_id = ? AND wq.queue_date = CURDATE()";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            rs = ps.executeQuery();
            if (rs.next()) {
                status = new int[]{rs.getInt("currently_serving"), rs.getInt("waiting_count")};
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return status;
    }

    public WalkinQueueBean getPatientTicketForService(int patientId, int clinicId, int serviceId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        WalkinQueueBean ticket = null;
        Date today = new Date(System.currentTimeMillis());
        String sql = SELECT_WITH_JOIN
                   + "WHERE wq.patient_id = ? AND c.clinic_id = ? AND s.service_id = ? "
                   + "AND wq.queue_date = ? "
                   + "ORDER BY wq.queue_id DESC LIMIT 1";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
            ps.setInt(2, clinicId);
            ps.setInt(3, serviceId);
            ps.setDate(4, today);
            rs = ps.executeQuery();
            if (rs.next()) ticket = mapRow(rs);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return ticket;
    }

    public int getPositionInQueue(int clinicServiceId, int queueNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int position = 0;
        Date today = new Date(System.currentTimeMillis());
        String sql = "SELECT COUNT(*) FROM walkin_queues "
                   + "WHERE clinic_service_id = ? AND queue_date = ? "
                   + "AND status = 'Waiting' AND queue_number < ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicServiceId);
            ps.setDate(2, today);
            ps.setInt(3, queueNumber);
            rs = ps.executeQuery();
            if (rs.next()) position = rs.getInt(1) + 1;
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return position;
    }

    public int countActiveTodayByClinic(int clinicId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT COUNT(*) "
                   + "FROM walkin_queues wq "
                   + "JOIN clinic_services cs ON wq.clinic_service_id = cs.clinic_service_id "
                   + "WHERE cs.clinic_id = ? AND wq.queue_date = CURDATE() "
                   + "AND wq.status IN ('Waiting', 'Called')";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    private ArrayList<WalkinQueueBean> mapRows(ResultSet rs) throws SQLException {
        ArrayList<WalkinQueueBean> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private WalkinQueueBean mapRow(ResultSet rs) throws SQLException {
        WalkinQueueBean wq = new WalkinQueueBean();
        wq.setQueueId(rs.getInt("queue_id"));
        wq.setPatientId(rs.getInt("patient_id"));
        wq.setClinicServiceId(rs.getInt("clinic_service_id"));
        wq.setQueueDate(rs.getDate("queue_date"));
        wq.setQueueNumber(rs.getInt("queue_number"));
        wq.setStatus(rs.getString("status"));
        wq.setJoinedAt(rs.getTimestamp("joined_at"));
        wq.setClinicId(rs.getInt("clinic_id"));
        wq.setServiceId(rs.getInt("service_id"));
        wq.setPatientName(rs.getString("patient_name"));
        wq.setClinicName(rs.getString("clinic_name"));
        wq.setServiceName(rs.getString("service_name"));
        return wq;
    }
}
