package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.SettingBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

// DAO for admin settings
public class SettingDAO {

    // Retrieve settings

    public ArrayList<SettingBean> getAllSettings() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<SettingBean> list = new ArrayList<>();
        String sql = "SELECT * FROM settings ORDER BY setting_key";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
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

    public int getIntValue(String key, int defaultValue) {

        SettingBean setting = getSetting(key);
        if (setting == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(setting.getSettingValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }

    }

    public SettingBean getSetting(String key) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        SettingBean setting = null;
        String sql = "SELECT * FROM settings WHERE setting_key = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, key);
            rs = ps.executeQuery();
            if (rs.next()) {
                setting = mapRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return setting;

    }

    // Update setting

    public boolean updateSetting(String key, String value) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, value);
            ps.setString(2, key);
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

    // Helper for bean

    private SettingBean mapRow(ResultSet rs) throws SQLException {
        SettingBean s = new SettingBean();
        s.setSettingKey(rs.getString("setting_key"));
        s.setSettingValue(rs.getString("setting_value"));
        s.setDescription(rs.getString("description"));
        return s;
    }
    
}
