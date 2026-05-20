package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicHoursBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.bean.DoctorScheduleBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

// DAO for clinics related (including setup, services, operating hours) and doctor related
public class ClinicDAO {

    // Clinics

    // Create clinic

    public int createClinic(String name, String location, boolean queueEnabled) {

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement selectPs = null;
        ResultSet rs = null;
        int clinicId = -1;
        String sql = "INSERT INTO clinics (name, location, is_queue_enabled) VALUES (?, ?, ?)";
        String selectSql = "SELECT clinic_id FROM clinics "
                         + "WHERE name = ? AND location = ? "
                         + "ORDER BY clinic_id DESC LIMIT 1";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, location);
            ps.setBoolean(3, queueEnabled);
            if (ps.executeUpdate() >= 1) {
                selectPs = conn.prepareStatement(selectSql);
                selectPs.setString(1, name);
                selectPs.setString(2, location);
                rs = selectPs.executeQuery();
                if (rs.next()) {
                    clinicId = rs.getInt("clinic_id");
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

        return clinicId;

    }

    // Retrieve clinic

    public ArrayList<ClinicBean> getAllClinics() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ClinicBean> list = new ArrayList<>();
        String sql = "SELECT * FROM clinics ORDER BY name";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapClinicRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    public ClinicBean getClinicById(int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ClinicBean clinic = null;
        String sql = "SELECT * FROM clinics WHERE clinic_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();
            if (rs.next()) {
                clinic = mapClinicRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return clinic;

    }

    // Update clinic

    public boolean updateClinic(int clinicId, String name, String location, boolean queueEnabled) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE clinics SET name = ?, location = ?, is_queue_enabled = ? WHERE clinic_id = ?";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, location);
            ps.setBoolean(3, queueEnabled);
            ps.setInt(4, clinicId);
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

    public boolean setQueueEnabled(int clinicId, boolean enabled) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE clinics SET is_queue_enabled = ? WHERE clinic_id = ?";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, enabled);
            ps.setInt(2, clinicId);
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

    public ArrayList<ClinicBean> getQueueEnabledClinics() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ClinicBean> list = new ArrayList<>();
        String sql = "SELECT * FROM clinics WHERE is_queue_enabled = TRUE ORDER BY name";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapClinicRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    // Delete clinic

    public boolean deleteClinic(int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "DELETE FROM clinics WHERE clinic_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
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

    // Clinics hours

    // Create clinic hours

    public boolean addClinicHours(int clinicId, String dayOfWeek, Time openTime, Time closeTime) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "INSERT INTO clinic_hours (clinic_id, day_of_week, open_time, close_time) "
                   + "SELECT ?, ?, ?, ? FROM DUAL "
                   + "WHERE NOT EXISTS ("
                   + "SELECT 1 FROM clinic_hours "
                   + "WHERE clinic_id = ? AND day_of_week = ? "
                   + "AND ? < close_time AND ? > open_time"
                   + ")";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setString(2, dayOfWeek);
            ps.setTime(3, openTime);
            ps.setTime(4, closeTime);
            ps.setInt(5, clinicId);
            ps.setString(6, dayOfWeek);
            ps.setTime(7, openTime);
            ps.setTime(8, closeTime);
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

    // Retrieve clinic hours

    public ArrayList<ClinicHoursBean> getClinicHours(int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ClinicHoursBean> list = new ArrayList<>();
        String sql = "SELECT * FROM clinic_hours WHERE clinic_id = ? "
                   + "ORDER BY FIELD(day_of_week,'Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'), open_time";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapClinicHoursRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    public ArrayList<ClinicHoursBean> getClinicHoursByDay(int clinicId, String dayOfWeek) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ClinicHoursBean> list = new ArrayList<>();
        String sql = "SELECT * FROM clinic_hours WHERE clinic_id = ? AND day_of_week = ? ORDER BY open_time";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setString(2, dayOfWeek);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapClinicHoursRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    public boolean isWithinOperatingHours(int clinicId, String dayOfWeek, Time time) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 FROM clinic_hours "
                   + "WHERE clinic_id = ? AND day_of_week = ? AND ? >= open_time AND ? < close_time";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setString(2, dayOfWeek);
            ps.setTime(3, time);
            ps.setTime(4, time);
            rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return exists;

    }

    // Update clinic hours

    public boolean updateClinicHours(int clinicHoursId, int clinicId, String dayOfWeek, Time openTime, Time closeTime) {

        Connection conn = null;
        PreparedStatement psCheck = null;
        PreparedStatement psUpdate = null;
        ResultSet rs = null;
        boolean isSuccess = false;

        String checkSql = "SELECT 1 FROM clinic_hours "
                        + "WHERE clinic_id = ? AND day_of_week = ? AND clinic_hours_id <> ? "
                        + "AND ? < close_time AND ? > open_time";

        String updateSql = "UPDATE clinic_hours SET day_of_week = ?, open_time = ?, close_time = ? "
                         + "WHERE clinic_hours_id = ?";

        try {
            conn = DBConnection.getConnection();

            psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, clinicId);
            psCheck.setString(2, dayOfWeek);
            psCheck.setInt(3, clinicHoursId);
            psCheck.setTime(4, openTime);
            psCheck.setTime(5, closeTime);
            rs = psCheck.executeQuery();
            boolean overlap = rs.next();
            rs.close();
            psCheck.close();

            if (!overlap) {
                psUpdate = conn.prepareStatement(updateSql);
                psUpdate.setString(1, dayOfWeek);
                psUpdate.setTime(2, openTime);
                psUpdate.setTime(3, closeTime);
                psUpdate.setInt(4, clinicHoursId);
                int rowCount = psUpdate.executeUpdate();
                if (rowCount >= 1) {
                    isSuccess = true;
                }
                psUpdate.close();
            }
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return isSuccess;

    }

    // Delete clinic hours

    public boolean deleteClinicHours(int clinicHoursId) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "DELETE FROM clinic_hours WHERE clinic_hours_id = ?";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicHoursId);
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

    // Clinic services

    // Create clinic services

    public boolean addClinicService(int clinicId, int serviceId, int quotaPerSlot,
                                    int slotDurationMins, boolean requiresApproval) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "INSERT INTO clinic_services "
                   + "(clinic_id, service_id, quota_per_slot, slot_duration_mins, requires_approval) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quotaPerSlot);
            ps.setInt(4, slotDurationMins);
            ps.setBoolean(5, requiresApproval);
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

    // Retrieve clinic services

    public ArrayList<ClinicServiceBean> getServicesByClinic(int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ClinicServiceBean> list = new ArrayList<>();

        String sql = "SELECT cs.*, c.name AS clinic_name, s.name AS service_name "
                   + "FROM clinic_services cs "
                   + "JOIN clinics c  ON cs.clinic_id  = c.clinic_id "
                   + "JOIN services s ON cs.service_id = s.service_id "
                   + "WHERE cs.clinic_id = ? ORDER BY s.name";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapClinicServiceRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;
        
    }

    public ClinicServiceBean getClinicServiceById(int clinicServiceId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ClinicServiceBean service = null;
        String sql = "SELECT cs.*, c.name AS clinic_name, s.name AS service_name "
                   + "FROM clinic_services cs "
                   + "JOIN clinics c  ON cs.clinic_id  = c.clinic_id "
                   + "JOIN services s ON cs.service_id = s.service_id "
                   + "WHERE cs.clinic_service_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicServiceId);
            rs = ps.executeQuery();
            if (rs.next()) {
                service = mapClinicServiceRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return service;
    }

    // Update clinic services
    
    public boolean updateClinicService(int clinicServiceId, int quotaPerSlot,
                                       int slotDurationMins, boolean requiresApproval) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE clinic_services "
                   + "SET quota_per_slot = ?, slot_duration_mins = ?, requires_approval = ? "
                   + "WHERE clinic_service_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, quotaPerSlot);
            ps.setInt(2, slotDurationMins);
            ps.setBoolean(3, requiresApproval);
            ps.setInt(4, clinicServiceId);
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

    // Delete clinic services

    public boolean deleteClinicService(int clinicServiceId) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "DELETE FROM clinic_services WHERE clinic_service_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicServiceId);
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

    // Doctor

    // Create doctor schedule

    public boolean addDoctorSchedule(int doctorId, String day, Time start, Time end) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time) "
                   + "VALUES (?, ?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doctorId);
            ps.setString(2, day);
            ps.setTime(3, start);
            ps.setTime(4, end);
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

    // Retrieve doctor schedule

    public ArrayList<DoctorScheduleBean> getAllDoctorSchedules() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<DoctorScheduleBean> list = new ArrayList<>();
        String sql = "SELECT ds.*, u.full_name AS doctor_name, c.name AS clinic_name, sp.clinic_id "
                   + "FROM doctor_schedules ds "
                   + "JOIN users u ON ds.doctor_id = u.user_id "
                   + "JOIN staff_profiles sp ON ds.doctor_id = sp.user_id "
                   + "JOIN clinics c ON sp.clinic_id = c.clinic_id "
                   + "WHERE sp.position = 'Doctor' "
                   + "ORDER BY ds.schedule_id";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                DoctorScheduleBean ds = mapDoctorScheduleRow(rs);
                ds.setDoctorName(rs.getString("doctor_name") + " (" + rs.getString("clinic_name") + ")");
                list.add(ds);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    public ArrayList<DoctorScheduleBean> getDoctorSchedulesByClinic(int clinicId) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<DoctorScheduleBean> list = new ArrayList<>();
        String sql = "SELECT ds.*, u.full_name AS doctor_name, c.name AS clinic_name "
                   + "FROM doctor_schedules ds "
                   + "JOIN users u ON ds.doctor_id = u.user_id "
                   + "JOIN staff_profiles sp ON ds.doctor_id = sp.user_id "
                   + "JOIN clinics c ON sp.clinic_id = c.clinic_id "
                   + "WHERE sp.clinic_id = ? AND sp.position = 'Doctor' "
                   + "ORDER BY ds.schedule_id";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDoctorScheduleRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    public ArrayList<DoctorScheduleBean> getDoctorsByClinicAndDay(
            int clinicId, String dayOfWeek) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<DoctorScheduleBean> list = new ArrayList<>();

        String sql = "SELECT ds.*, u.full_name AS doctor_name "
                   + "FROM doctor_schedules ds "
                   + "JOIN staff_profiles sp ON ds.doctor_id = sp.user_id "
                   + "JOIN users u ON ds.doctor_id = u.user_id "
                   + "WHERE sp.clinic_id = ? AND sp.position = 'Doctor' AND ds.day_of_week = ? "
                   + "ORDER BY u.full_name";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setString(2, dayOfWeek);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDoctorScheduleRow(rs));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    public boolean isDoctorAvailable(int doctorId, int clinicId, String dayOfWeek, Time time) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 "
                   + "FROM doctor_schedules ds "
                   + "JOIN staff_profiles sp ON ds.doctor_id = sp.user_id "
                   + "WHERE ds.doctor_id = ? AND sp.clinic_id = ? "
                   + "AND sp.position = 'Doctor' AND ds.day_of_week = ? "
                   + "AND ? >= ds.start_time AND ? < ds.end_time";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doctorId);
            ps.setInt(2, clinicId);
            ps.setString(3, dayOfWeek);
            ps.setTime(4, time);
            ps.setTime(5, time);
            rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return exists;

    }

    public ArrayList<String[]> getAllDoctorsForPicker() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name, c.name AS clinic_name "
                   + "FROM users u "
                   + "JOIN staff_profiles sp ON u.user_id = sp.user_id "
                   + "JOIN clinics c ON sp.clinic_id = c.clinic_id "
                   + "WHERE u.role = 'Staff' AND sp.position = 'Doctor' "
                   + "ORDER BY c.name, u.full_name";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("full_name") + " (" + rs.getString("clinic_name") + ")"
                });
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;

    }

    public ArrayList<String[]> getDoctorsForClinicPicker(int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name "
                   + "FROM users u "
                   + "JOIN staff_profiles sp ON u.user_id = sp.user_id "
                   + "WHERE u.role = 'Staff' AND sp.position = 'Doctor' AND sp.clinic_id = ? "
                   + "ORDER BY u.full_name";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("full_name")
                });
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return list;
        
    }

    public boolean isDoctorInClinic(int doctorId, int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 FROM staff_profiles "
                   + "WHERE user_id = ? AND clinic_id = ? AND position = 'Doctor'";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doctorId);
            ps.setInt(2, clinicId);
            rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return exists;

    }

    public boolean isDoctorScheduleInClinic(int scheduleId, int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 "
                   + "FROM doctor_schedules ds "
                   + "JOIN staff_profiles sp ON ds.doctor_id = sp.user_id "
                   + "WHERE ds.schedule_id = ? AND sp.clinic_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, scheduleId);
            ps.setInt(2, clinicId);
            rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return exists;

    }

    // Update doctor schedule

    public boolean updateDoctorSchedule(int scheduleId, Time start, Time end) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE doctor_schedules SET start_time = ?, end_time = ? "
                   + "WHERE schedule_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setTime(1, start);
            ps.setTime(2, end);
            ps.setInt(3, scheduleId);
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

    // Delete doctor schedule

    public boolean deleteDoctorSchedule(int scheduleId) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "DELETE FROM doctor_schedules WHERE schedule_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, scheduleId);
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

    // Helpers for bean

    private ClinicBean mapClinicRow(ResultSet rs) throws SQLException {
        ClinicBean c = new ClinicBean();
        c.setClinicId(rs.getInt("clinic_id"));
        c.setName(rs.getString("name"));
        c.setLocation(rs.getString("location"));
        c.setQueueEnabled(rs.getBoolean("is_queue_enabled"));
        return c;
    }

    private ClinicHoursBean mapClinicHoursRow(ResultSet rs) throws SQLException {
        ClinicHoursBean ch = new ClinicHoursBean();
        ch.setClinicHoursId(rs.getInt("clinic_hours_id"));
        ch.setClinicId(rs.getInt("clinic_id"));
        ch.setDayOfWeek(rs.getString("day_of_week"));
        ch.setOpenTime(rs.getTime("open_time"));
        ch.setCloseTime(rs.getTime("close_time"));
        return ch;
    }

    private ClinicServiceBean mapClinicServiceRow(ResultSet rs) throws SQLException {
        ClinicServiceBean cs = new ClinicServiceBean();
        cs.setClinicServiceId(rs.getInt("clinic_service_id"));
        cs.setClinicId(rs.getInt("clinic_id"));
        cs.setServiceId(rs.getInt("service_id"));
        cs.setQuotaPerSlot(rs.getInt("quota_per_slot"));
        cs.setSlotDurationMins(rs.getInt("slot_duration_mins"));
        try {
            cs.setRequiresApproval(rs.getBoolean("requires_approval"));
        } catch (SQLException ignore) {
            cs.setRequiresApproval(false);
        }
        cs.setClinicName(rs.getString("clinic_name"));
        cs.setServiceName(rs.getString("service_name"));
        return cs;
    }

    private DoctorScheduleBean mapDoctorScheduleRow(ResultSet rs) throws SQLException {
        DoctorScheduleBean ds = new DoctorScheduleBean();
        ds.setScheduleId(rs.getInt("schedule_id"));
        ds.setDoctorId(rs.getInt("doctor_id"));
        ds.setDayOfWeek(rs.getString("day_of_week"));
        ds.setStartTime(rs.getTime("start_time"));
        ds.setEndTime(rs.getTime("end_time"));
        ds.setDoctorName(rs.getString("doctor_name"));
        return ds;
    }

}
