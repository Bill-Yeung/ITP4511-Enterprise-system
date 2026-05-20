package hk.edu.hkiit.jakarta.webapp.dao;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.db.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;

// DAO for appointments
public class AppointmentDAO {

    // Common SQL
    private static final String SELECT_WITH_JOIN =
        "SELECT a.*, u.full_name AS patient_name, "
      + "c.name AS clinic_name, s.name AS service_name, "
      + "d.full_name AS doctor_name "
      + "FROM appointments a "
      + "JOIN users u ON a.patient_id = u.user_id "
      + "JOIN clinic_services cs ON a.clinic_service_id = cs.clinic_service_id "
      + "JOIN clinics c ON cs.clinic_id = c.clinic_id "
      + "JOIN services s ON cs.service_id = s.service_id "
      + "LEFT JOIN users d ON a.doctor_id = d.user_id ";

    // Create booking

    public int bookAppointment(int patientId, int clinicServiceId,
                               Integer doctorId, Date appointmentDate, Time timeSlot,
                               String initialStatus) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        int appointmentId = -1;
        String sql;

        if (doctorId != null) {
            sql = "INSERT INTO appointments "
                + "(patient_id, clinic_service_id, doctor_id, appointment_date, time_slot, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO appointments "
                + "(patient_id, clinic_service_id, doctor_id, appointment_date, time_slot, status) "
                + "VALUES (?, ?, NULL, ?, ?, ?)";
        }

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, patientId);
            ps.setInt(2, clinicServiceId);
            if (doctorId != null) {
                ps.setInt(3, doctorId);
                ps.setDate(4, appointmentDate);
                ps.setTime(5, timeSlot);
                ps.setString(6, initialStatus);
            } else {
                ps.setDate(3, appointmentDate);
                ps.setTime(4, timeSlot);
                ps.setString(5, initialStatus);
            }

            if (ps.executeUpdate() >= 1) {
                keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    appointmentId = keys.getInt(1);
                }
            }

            if (keys != null) keys.close();
            ps.close();
            conn.close();

        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return appointmentId;

    }

    // Retrieve bookings

    public ArrayList<AppointmentBean> getFilteredAppointments(
            Integer clinicId, Integer serviceId, Integer year, Integer month, String status) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AppointmentBean> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN;
        String prefix = "WHERE ";

        if (clinicId != null && clinicId > 0) {
            sql += prefix + "cs.clinic_id = ? ";
            prefix = "AND ";
        }
        if (serviceId != null && serviceId > 0) {
            sql += prefix + "cs.service_id = ? ";
            prefix = "AND ";
        }
        if (year != null && year > 0) {
            sql += prefix + "YEAR(a.appointment_date) = ? ";
            prefix = "AND ";
        }
        if (month != null && month > 0) {
            sql += prefix + "MONTH(a.appointment_date) = ? ";
            prefix = "AND ";
        }
        if (status != null && !status.isEmpty()) {
            sql += prefix + "a.status = ? ";
        }
        sql += "ORDER BY a.appointment_id";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            int index = 1;
            if (clinicId != null && clinicId > 0) {
                ps.setInt(index++, clinicId);
            }
            if (serviceId != null && serviceId > 0) {
                ps.setInt(index++, serviceId);
            }
            if (year != null && year > 0) {
                ps.setInt(index++, year);
            }
            if (month != null && month > 0) {
                ps.setInt(index++, month);
            }
            if (status != null && !status.isEmpty()) {
                ps.setString(index++, status);
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

    public AppointmentBean getAppointmentById(int appointmentId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        AppointmentBean appointment = null;
        String sql = SELECT_WITH_JOIN + "WHERE a.appointment_id = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            rs = ps.executeQuery();
            if (rs.next()) {
                appointment = mapRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return appointment;

    }

    public ArrayList<AppointmentBean> getAppointmentsByPatient(int patientId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AppointmentBean> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN
                   + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.time_slot";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
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

    public ArrayList<AppointmentBean> getAppointmentsByClinicAndDate(int clinicId, Date date) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AppointmentBean> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN
                   + "WHERE cs.clinic_id = ? AND a.appointment_date = ? "
                   + "ORDER BY a.appointment_id";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setDate(2, date);
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

    public ArrayList<AppointmentBean> getAppointmentsByClinicAndDateRange(int clinicId, Date startDate, Date endDate) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AppointmentBean> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN
                   + "WHERE cs.clinic_id = ? AND a.appointment_date BETWEEN ? AND ? "
                   + "ORDER BY a.appointment_id";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
            ps.setDate(2, startDate);
            ps.setDate(3, endDate);
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

    public ArrayList<AppointmentBean> getPendingByClinic(int clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AppointmentBean> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN
                   + "WHERE cs.clinic_id = ? AND a.status = 'Pending' "
                   + "ORDER BY a.appointment_id";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicId);
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

    public ArrayList<AppointmentBean> getBookedWithinNext24Hours(int patientId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<AppointmentBean> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN
                   + "WHERE a.patient_id = ? AND a.status = 'Booked' "
                   + "AND TIMESTAMP(a.appointment_date, a.time_slot) "
                   + "    BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 24 HOUR) "
                   + "ORDER BY a.appointment_date, a.time_slot";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
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

    // Ensure patient cannot book appointment for the same timeslot for more than 1 service
    public boolean isSlotTaken(int patientId, Date date, Time timeSlot) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exists = false;
        String sql = "SELECT 1 FROM appointments "
                   + "WHERE patient_id = ? AND appointment_date = ? AND time_slot = ? "
                   + "AND status NOT IN ('Cancelled')";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
            ps.setDate(2, date);
            ps.setTime(3, timeSlot);
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

    public int getSlotBookingCount(int clinicServiceId, Date date, Time timeSlot) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM appointments "
                   + "WHERE clinic_service_id = ? AND appointment_date = ? AND time_slot = ? "
                   + "AND status NOT IN ('Cancelled')";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicServiceId);
            ps.setDate(2, date);
            ps.setTime(3, timeSlot);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return count;

    }

    // Update bookings

    public boolean updateAppointmentDetails(int appointmentId, int clinicServiceId,
                                            Integer doctorId, Date appointmentDate,
                                            Time timeSlot, String status, String remarks) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE appointments SET clinic_service_id = ?, doctor_id = ?, "
                   + "appointment_date = ?, time_slot = ?, status = ?, remarks = ? "
                   + "WHERE appointment_id = ?";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, clinicServiceId);
            if (doctorId != null) {
                ps.setInt(2, doctorId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDate(3, appointmentDate);
            ps.setTime(4, timeSlot);
            ps.setString(5, status);
            ps.setString(6, remarks);
            ps.setInt(7, appointmentId);
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

    private boolean updateStatus(int appointmentId, String newStatus, String requiredStatus) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE appointments SET status = ? "
                   + "WHERE appointment_id = ? AND status = ?";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, appointmentId);
            ps.setString(3, requiredStatus);
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

    public boolean approveAppointment(int appointmentId) {
        return updateStatus(appointmentId, "Booked", "Pending");
    }

    public boolean rejectAppointment(int appointmentId, String reason, int rejectedByUserId) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE appointments SET status = 'Cancelled', cancel_reason = ?, cancelled_by = ? "
                   + "WHERE appointment_id = ? AND status = 'Pending'";
        
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, reason);
            ps.setInt(2, rejectedByUserId);
            ps.setInt(3, appointmentId);
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

    public boolean markArrived(int appointmentId) {
        return updateStatus(appointmentId, "Arrived", "Booked");
    }

    public boolean markNoShow(int appointmentId) {
        return updateStatus(appointmentId, "No-show", "Booked");
    }

    public boolean markCompleted(int appointmentId, String remarks) {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE appointments SET status = 'Completed', remarks = ? "
                   + "WHERE appointment_id = ? AND status = 'Arrived'";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, remarks);
            ps.setInt(2, appointmentId);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return isSuccess;

    }

    public int rescheduleAppointment(int oldAppointmentId, int patientId,
                                     int clinicServiceId, Integer doctorId,
                                     Date newDate, Time newTimeSlot) {

        String cancelSql = "UPDATE appointments SET status = 'Cancelled', "
                         + "cancel_reason = 'Rescheduled by patient', cancelled_by = ? "
                         + "WHERE appointment_id = ? AND status IN ('Booked','Pending')";

        String insertSql;
        if (doctorId != null) {
            insertSql = "INSERT INTO appointments "
                      + "(patient_id, clinic_service_id, doctor_id, appointment_date, time_slot, status) "
                      + "VALUES (?, ?, ?, ?, ?, 'Booked')";
        } else {
            insertSql = "INSERT INTO appointments "
                      + "(patient_id, clinic_service_id, doctor_id, appointment_date, time_slot, status) "
                      + "VALUES (?, ?, NULL, ?, ?, 'Booked')";
        }

        String selectSql = "SELECT appointment_id FROM appointments "
                         + "WHERE patient_id = ? AND clinic_service_id = ? "
                         + "AND appointment_date = ? AND time_slot = ? "
                         + "ORDER BY appointment_id DESC LIMIT 1";

        Connection conn = null;
        int newAppointmentId = -1;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement cancelPs = conn.prepareStatement(cancelSql);
            cancelPs.setInt(1, patientId);
            cancelPs.setInt(2, oldAppointmentId);
            int cancelled = cancelPs.executeUpdate();
            cancelPs.close();
            if (cancelled < 1) {
                conn.rollback();
                conn.close();
                return -1;
            }

            PreparedStatement insertPs = conn.prepareStatement(insertSql);
            if (doctorId != null) {
                insertPs.setInt(1, patientId);
                insertPs.setInt(2, clinicServiceId);
                insertPs.setInt(3, doctorId);
                insertPs.setDate(4, newDate);
                insertPs.setTime(5, newTimeSlot);
            } else {
                insertPs.setInt(1, patientId);
                insertPs.setInt(2, clinicServiceId);
                insertPs.setDate(3, newDate);
                insertPs.setTime(4, newTimeSlot);
            }
            int inserted = insertPs.executeUpdate();
            insertPs.close();
            if (inserted < 1) {
                conn.rollback();
                conn.close();
                return -1;
            }

            PreparedStatement selectPs = conn.prepareStatement(selectSql);
            selectPs.setInt(1, patientId);
            selectPs.setInt(2, clinicServiceId);
            selectPs.setDate(3, newDate);
            selectPs.setTime(4, newTimeSlot);
            ResultSet rs = selectPs.executeQuery();
            if (rs.next()) {
                newAppointmentId = rs.getInt("appointment_id");
            }
            rs.close();
            selectPs.close();

            conn.commit();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            try { if (conn != null) { conn.rollback(); conn.close(); } } catch (SQLException ignore) {}
        }

        return newAppointmentId;

    }

    // Delete bookings

    public boolean cancelAppointment(int appointmentId, String cancelReason, int cancelledByUserId) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        String sql = "UPDATE appointments SET status = 'Cancelled', cancel_reason = ?, cancelled_by = ? "
                   + "WHERE appointment_id = ? AND status IN ('Booked','Pending')";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, cancelReason);
            ps.setInt(2, cancelledByUserId);
            ps.setInt(3, appointmentId);
            int rowCount = ps.executeUpdate();
            if (rowCount >= 1) isSuccess = true;
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return isSuccess;

    }

    // Counters

    public int[] getStatusCounts(Integer clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT a.status, COUNT(*) AS cnt "
                   + "FROM appointments a "
                   + "JOIN clinic_services cs ON a.clinic_service_id = cs.clinic_service_id "
                   + "WHERE 1=1 ";

        if (clinicId != null && clinicId > 0) {
            sql += "AND cs.clinic_id = ? ";
        }
        sql += "GROUP BY a.status";

         // total, booked, arrived, completed, noshow, cancelled
        int[] counts = new int[6];

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (clinicId != null && clinicId > 0) {
                ps.setInt(1, clinicId);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                counts[0] += cnt;
                switch (rs.getString("status")) {
                    case "Booked":
                        counts[1] = cnt;
                        break;
                    case "Arrived":
                        counts[2] = cnt;
                        break;
                    case "Completed":
                        counts[3] = cnt;
                        break;
                    case "No-show":
                        counts[4] = cnt;
                        break;
                    case "Cancelled":
                        counts[5] = cnt;
                        break;
                }
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return counts;

    }

    public int getTodayCount(Integer clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM appointments a "
                   + "JOIN clinic_services cs ON a.clinic_service_id = cs.clinic_service_id "
                   + "WHERE a.appointment_date = CURDATE() ";

        if (clinicId != null && clinicId > 0) {
            sql += "AND cs.clinic_id = ? ";
        }

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (clinicId != null && clinicId > 0) {
                ps.setInt(1, clinicId);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return count;

    }

    public int[] getTodayStatusCounts(Integer clinicId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int[] counts = new int[6];
        String sql = "SELECT a.status, COUNT(*) AS cnt "
                   + "FROM appointments a "
                   + "JOIN clinic_services cs ON a.clinic_service_id = cs.clinic_service_id "
                   + "WHERE a.appointment_date = CURDATE() ";

        if (clinicId != null && clinicId > 0) {
            sql += "AND cs.clinic_id = ? ";
        }
        sql += "GROUP BY a.status";

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (clinicId != null && clinicId > 0) {
                ps.setInt(1, clinicId);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                counts[0] += cnt;
                switch (rs.getString("status")) {
                    case "Booked":
                        counts[1] = cnt;
                        break;
                    case "Arrived":
                        counts[2] = cnt;
                        break;
                    case "Completed":
                        counts[3] = cnt;
                        break;
                    case "No-show":
                        counts[4] = cnt;
                        break;
                    case "Cancelled":
                        counts[5] = cnt;
                        break;
                }
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return counts;

    }

    public int countActiveBookings(int patientId) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM appointments "
                   + "WHERE patient_id = ? AND status IN ('Pending','Booked','Arrived')";
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
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

    // Reporting - Utilization Rate

    public ArrayList<String[]> getUtilisationRate(Integer clinicId, Integer year, Integer month) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT cs.clinic_service_id, c.name AS clinic_name, s.name AS service_name, "
                   + "       cs.quota_per_slot, cs.slot_duration_mins, cs.clinic_id, "
                   + "       COUNT(a.appointment_id) AS booked_count "
                   + "FROM clinic_services cs "
                   + "JOIN clinics c ON cs.clinic_id = c.clinic_id "
                   + "JOIN services s ON cs.service_id = s.service_id "
                   + "LEFT JOIN appointments a ON a.clinic_service_id = cs.clinic_service_id "
                   + "  AND a.status != 'Cancelled' ";

        if (year != null && year > 0) {
            sql += "AND YEAR(a.appointment_date) = ? ";
        }
        if (month != null && month > 0) {
            sql += "AND MONTH(a.appointment_date) = ? ";
        }
        if (clinicId != null && clinicId > 0) {
            sql += "WHERE cs.clinic_id = ? ";
        }
        sql += "GROUP BY cs.clinic_service_id, c.name, s.name, cs.quota_per_slot, cs.slot_duration_mins, cs.clinic_id ";
        sql += "ORDER BY c.name, s.name";

        ArrayList<String[]> results = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            int index = 1;
            if (year != null && year > 0) {
                ps.setInt(index++, year);
            }
            if (month != null && month > 0) {
                ps.setInt(index++, month);
            }
            if (clinicId != null && clinicId > 0) {
                ps.setInt(index++, clinicId);
            }
            rs = ps.executeQuery();
            while (rs.next()) {

                int bookedCount = rs.getInt("booked_count");
                int quotaPerSlot = rs.getInt("quota_per_slot");
                int slotDurationMins = rs.getInt("slot_duration_mins");
                int clinicId2 = rs.getInt("clinic_id");
                int totalSlots = calculateTotalSlots(conn, clinicId2, quotaPerSlot, slotDurationMins, year, month);
                double rate = totalSlots > 0 ? (bookedCount * 100.0 / totalSlots) : 0;

                results.add(new String[]{
                    rs.getString("clinic_name"),
                    rs.getString("service_name"),
                    String.valueOf(bookedCount),
                    String.valueOf(totalSlots),
                    String.format("%.1f", rate)
                });

            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return results;

    }

    private int calculateTotalSlots(Connection conn, int clinicId, int quotaPerSlot,
                                     int slotDurationMins, Integer year, Integer month)
            throws SQLException {
                
        if (slotDurationMins <= 0 || quotaPerSlot <= 0) {
            return 0;
        }

        String sql = "SELECT day_of_week, open_time, close_time FROM clinic_hours WHERE clinic_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, clinicId);
        ResultSet rs = ps.executeQuery();

        // Convert clinic hours into total operating minutes

        HashMap<Integer, Integer> dayMinutes = new HashMap<>();
        while (rs.next()) {
            String dayName = rs.getString("day_of_week");
            int dow = dayNameToValue(dayName);
            Time open = rs.getTime("open_time");
            Time close = rs.getTime("close_time");
            int mins = (int) ((close.getTime() - open.getTime()) / 60000);
            Integer current = dayMinutes.get(dow);
            dayMinutes.put(dow, current == null ? mins : current + mins);
        }
        rs.close();
        ps.close();

        LocalDate startDate = getUtilisationStartDate(conn, clinicId, year, month);
        LocalDate endDate = getUtilisationEndDate(year, month);

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0;
        }

        int totalSlots = 0;
        LocalDate date = startDate;

        // Traverse all the days to calculate total slots

        while (!date.isAfter(endDate)) {
            if (month != null && month > 0 && date.getMonthValue() != month) {
                date = date.plusDays(1);
                continue;
            }
            int dow = date.getDayOfWeek().getValue();
            Integer mins = dayMinutes.get(dow);
            if (mins != null && mins > 0 && slotDurationMins > 0) {
                int slotsPerDay = mins / slotDurationMins;
                totalSlots += slotsPerDay * quotaPerSlot;
            }
            date = date.plusDays(1);
        }

        return totalSlots;

    }

    private LocalDate getUtilisationStartDate(Connection conn, int clinicId,
                                              Integer year, Integer month)
            throws SQLException {

        if (year != null && year > 0 && month != null && month > 0) {
            return LocalDate.of(year, month, 1);
        }
        if (year != null && year > 0) {
            return LocalDate.of(year, 1, 1);
        }

        LocalDate first = LocalDate.now().minusYears(2);
        if (month != null && month > 0) {
            return LocalDate.of(first.getYear(), month, 1);
        }

        return LocalDate.of(first.getYear(), 1, 1);

    }

    // Allow partial month reporting
    private LocalDate getUtilisationEndDate(Integer year, Integer month) {

        LocalDate today = LocalDate.now();

        if (year != null && year > 0 && month != null && month > 0) {
            YearMonth selectedMonth = YearMonth.of(year, month);
            YearMonth currentMonth = YearMonth.from(today);
            if (selectedMonth.isAfter(currentMonth)) {
                return null;
            }
            if (selectedMonth.equals(currentMonth)) {
                return today;
            }
            return selectedMonth.atEndOfMonth();
        }

        if (year != null && year > 0) {
            int currentYear = today.getYear();
            if (year > currentYear) {
                return null;
            }
            if (year == currentYear) {
                return today;
            }
            return LocalDate.of(year, 12, 31);
        }

        return today;

    }

    // Reporting - No-show

    public ArrayList<String[]> getNoShowSummary(Integer clinicId, Integer year, Integer month) {
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT c.name AS clinic_name, s.name AS service_name, COUNT(*) AS cnt "
                   + "FROM appointments a "
                   + "JOIN clinic_services cs ON a.clinic_service_id = cs.clinic_service_id "
                   + "JOIN clinics c ON cs.clinic_id = c.clinic_id "
                   + "JOIN services s ON cs.service_id = s.service_id "
                   + "WHERE a.status = 'No-show' ";

        if (clinicId != null && clinicId > 0) {
            sql += "AND cs.clinic_id = ? ";
        }
        if (year != null && year > 0) {
            sql += "AND YEAR(a.appointment_date) = ? ";
        }
        if (month != null && month > 0) {
            sql += "AND MONTH(a.appointment_date) = ? ";
        }
        sql += "GROUP BY c.name, s.name ORDER BY c.name, s.name";

        ArrayList<String[]> results = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            int index = 1;
            if (clinicId != null && clinicId > 0) {
                ps.setInt(index++, clinicId);
            }
            if (year != null && year > 0) {
                ps.setInt(index++, year);
            }
            if (month != null && month > 0) {
                ps.setInt(index++, month);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                results.add(new String[]{
                    rs.getString("clinic_name"),
                    rs.getString("service_name"),
                    String.valueOf(rs.getInt("cnt"))
                });
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return results;

    }

    // Reporting - Patient behavior (no-shows / cancellations)

    public ArrayList<String[]> getPatientBehaviorSummary(Integer clinicId, Integer days, int minFlags) {
        StringBuilder sql = new StringBuilder(
              "SELECT a.patient_id, u.full_name, "
            + "       COUNT(*) AS total, "
            + "       SUM(CASE WHEN a.status = 'No-show'   THEN 1 ELSE 0 END) AS no_show_cnt, "
            + "       SUM(CASE WHEN a.status = 'Cancelled' THEN 1 ELSE 0 END) AS cancelled_cnt, "
            + "       MAX(CASE WHEN a.status = 'No-show'   THEN a.appointment_date END) AS last_no_show, "
            + "       MAX(CASE WHEN a.status = 'Cancelled' THEN a.appointment_date END) AS last_cancelled "
            + "FROM appointments a "
            + "JOIN users u ON a.patient_id = u.user_id "
            + "JOIN clinic_services cs ON a.clinic_service_id = cs.clinic_service_id "
            + "WHERE 1=1 ");
        if (clinicId != null && clinicId > 0) {
            sql.append("AND cs.clinic_id = ? ");
        }
        if (days != null && days > 0) {
            sql.append("AND a.appointment_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) ");
        }
        sql.append("GROUP BY a.patient_id, u.full_name ")
           .append("HAVING (no_show_cnt + cancelled_cnt) >= ? ")
           .append("ORDER BY (no_show_cnt + cancelled_cnt) DESC, u.full_name");

        ArrayList<String[]> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (clinicId != null && clinicId > 0) ps.setInt(idx++, clinicId);
            if (days != null && days > 0)        ps.setInt(idx++, days);
            ps.setInt(idx, Math.max(minFlags, 1));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date lastNoShow   = rs.getDate("last_no_show");
                    Date lastCancel   = rs.getDate("last_cancelled");
                    results.add(new String[]{
                        String.valueOf(rs.getInt("patient_id")),
                        rs.getString("full_name"),
                        String.valueOf(rs.getInt("total")),
                        String.valueOf(rs.getInt("no_show_cnt")),
                        String.valueOf(rs.getInt("cancelled_cnt")),
                        lastNoShow == null ? "" : lastNoShow.toString(),
                        lastCancel == null ? "" : lastCancel.toString()
                    });
                }
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return results;
    }

    // Helpers for bean

    private ArrayList<AppointmentBean> mapRows(ResultSet rs) throws SQLException {
        ArrayList<AppointmentBean> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private AppointmentBean mapRow(ResultSet rs) throws SQLException {
        AppointmentBean a = new AppointmentBean();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setClinicServiceId(rs.getInt("clinic_service_id"));
        int doctorId = rs.getInt("doctor_id");
        a.setDoctorId(rs.wasNull() ? null : doctorId);
        a.setAppointmentDate(rs.getDate("appointment_date"));
        a.setTimeSlot(rs.getTime("time_slot"));
        a.setStatus(rs.getString("status"));
        a.setCancelReason(rs.getString("cancel_reason"));
        int cancelledBy = rs.getInt("cancelled_by");
        a.setCancelledBy(rs.wasNull() ? null : cancelledBy);
        a.setRemarks(rs.getString("remarks"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setPatientName(rs.getString("patient_name"));
        a.setClinicName(rs.getString("clinic_name"));
        a.setServiceName(rs.getString("service_name"));
        a.setDoctorName(rs.getString("doctor_name"));
        return a;
    }

    // Helper for day

    private int dayNameToValue(String dayName) {

        if (dayName == null) {
            return 0;
        }

        switch (dayName) {
            case "Monday":
                return 1;
            case "Tuesday":
                return 2;
            case "Wednesday":
                return 3;
            case "Thursday":
                return 4;
            case "Friday":
                return 5;
            case "Saturday":
                return 6;
            case "Sunday":
                return 7;
            default:
                return 0;
        }

    }

}
