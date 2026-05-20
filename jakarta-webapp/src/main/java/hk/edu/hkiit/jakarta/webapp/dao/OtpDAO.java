package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class OtpDAO {

    public boolean saveOtp(int userId, String otpCode) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String invalidateSql = "UPDATE password_reset_tokens SET used = 1 "
                             + "WHERE user_id = ? AND used = 0";
        String insertSql     = "INSERT INTO password_reset_tokens "
                             + "(user_id, otp_code, expires_at) VALUES (?, ?, ?)";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(invalidateSql);
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(insertSql);
            ps.setInt(1, userId);
            ps.setString(2, otpCode);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(5)));
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }

    public boolean verifyOtp(int userId, String otpCode) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean isSuccess = false;
        String selectSql = "SELECT token_id FROM password_reset_tokens "
                         + "WHERE user_id = ? AND otp_code = ? AND used = 0 "
                         + "  AND expires_at > NOW() "
                         + "ORDER BY created_at DESC LIMIT 1";
        String markUsed  = "UPDATE password_reset_tokens SET used = 1 WHERE token_id = ?";
        try {
            conn = DBConnection.getConnection();
            int tokenId = -1;
            ps = conn.prepareStatement(selectSql);
            ps.setInt(1, userId);
            ps.setString(2, otpCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                tokenId = rs.getInt("token_id");
            }
            rs.close();
            ps.close();

            if (tokenId >= 0) {
                ps = conn.prepareStatement(markUsed);
                ps.setInt(1, tokenId);
                int rowCount = ps.executeUpdate();
                if (rowCount >= 1) isSuccess = true;
                ps.close();
            }
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return isSuccess;
    }
}
