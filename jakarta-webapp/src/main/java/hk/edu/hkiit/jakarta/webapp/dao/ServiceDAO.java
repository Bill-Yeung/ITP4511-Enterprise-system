package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.ServiceBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

// DAO for services
public class ServiceDAO {

    // Create service

    public int createService(String name, String description) {

        if (serviceNameExists(name, null)) {
            return -1;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement selectPs = null;
        ResultSet rs = null;
        int serviceId = -1;
        String sql = "INSERT INTO services (name, description) VALUES (?, ?)";
        String selectSql = "SELECT service_id FROM services WHERE LOWER(name) = LOWER(?) "
                         + "ORDER BY service_id DESC LIMIT 1";

        try {

            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);

            // Return the service id on success
            if (ps.executeUpdate() >= 1) {
                selectPs = conn.prepareStatement(selectSql);
                selectPs.setString(1, name);
                rs = selectPs.executeQuery();
                if (rs.next()) {
                    serviceId = rs.getInt("service_id");
                }
            }
            if (rs != null) {
                rs.close();
            }
            if (selectPs != null) {
                selectPs.close();
            }
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return serviceId;

    }

    public boolean serviceNameExists(String name, Integer excludeServiceId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 FROM services WHERE LOWER(name) = LOWER(?)";

        if (excludeServiceId != null) {
            sql += " AND service_id <> ?";
        }

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            if (excludeServiceId != null) {
                ps.setInt(2, excludeServiceId);
            }
            rs = ps.executeQuery();
            exists = rs.next();
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return exists;

    }

    // Retrieve services

    public ArrayList<ServiceBean> getAllServices() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ServiceBean> list = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY name";

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

    public ServiceBean getServiceById(int serviceId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ServiceBean service = null;
        String sql = "SELECT * FROM services WHERE service_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, serviceId);
            rs = ps.executeQuery();
            if (rs.next()) {
                service = mapRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return service;

    }

    // Update services

    public boolean updateService(int serviceId, String name, String description) {
        if (serviceNameExists(name, serviceId)) return false;

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE services SET name = ?, description = ? WHERE service_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, serviceId);
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

    // Delete services

    public boolean deleteService(int serviceId) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "DELETE FROM services WHERE service_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, serviceId);
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

    private ServiceBean mapRow(ResultSet rs) throws SQLException {
        ServiceBean s = new ServiceBean();
        s.setServiceId(rs.getInt("service_id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        return s;
    }
    
}
