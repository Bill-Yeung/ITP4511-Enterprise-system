package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.NotificationBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class NotificationDAO {

    public ArrayList<NotificationBean> getByUserId(int userId) {
        ArrayList<NotificationBean> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ArrayList<NotificationBean> getByUserId(int userId, int page, int limit) {
        ArrayList<NotificationBean> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, (page - 1) * limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public int countByUserId(int userId) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    public int countUnread(int userId) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    public NotificationBean createAndReturn(int userId, String message, String type) {
        NotificationBean notification = null;
        String insertSql = "INSERT INTO notifications (user_id, message, type) VALUES (?, ?, ?)";
        String selectSql = "SELECT * FROM notifications WHERE notification_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.setString(3, type);
            if (ps.executeUpdate() >= 1) {
                int newId = -1;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) newId = keys.getInt(1);
                }
                if (newId > 0) {
                    try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                        selectPs.setInt(1, newId);
                        try (ResultSet rs = selectPs.executeQuery()) {
                            if (rs.next()) notification = mapRow(rs);
                        }
                    }
                }
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return notification;
    }

    public boolean hasReminderForAppointmentToday(int userId, int appointmentId) {
        boolean exists = false;
        String sql = "SELECT COUNT(*) FROM notifications "
                   + "WHERE user_id = ? AND type = 'Reminder' "
                   + "AND DATE(created_at) = CURDATE() "
                   + "AND message LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, "%appointment #" + appointmentId + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) exists = rs.getInt(1) > 0;
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return exists;
    }

    public boolean markAsRead(int notificationId, int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() >= 1;
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() >= 1;
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteNotification(int notificationId, int userId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() >= 1;
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private NotificationBean mapRow(ResultSet rs) throws SQLException {
        NotificationBean n = new NotificationBean();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}
