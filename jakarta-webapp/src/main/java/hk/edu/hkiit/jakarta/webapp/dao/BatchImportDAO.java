package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// DAO for batch import
public class BatchImportDAO {

    public boolean importService(String name, String description) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "INSERT INTO services (name, description) "
                   + "SELECT ?, ? FROM DUAL "
                   + "WHERE NOT EXISTS ("
                   + "SELECT 1 FROM services WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))"
                   + ")";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, name);
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

    public boolean importClinicService(int clinicId, int serviceId, int quota, int duration) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "INSERT INTO clinic_services (clinic_id, service_id, quota_per_slot, slot_duration_mins) "
                   + "VALUES (?, ?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quota);
            ps.setInt(4, duration);
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

    public boolean importUser(String username, String password, String role,
                              String fullName, String email, String phone) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password_hash, role, full_name, email, phone) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, role);
            ps.setString(4, fullName);
            ps.setString(5, email);
            ps.setString(6, phone);
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

}
