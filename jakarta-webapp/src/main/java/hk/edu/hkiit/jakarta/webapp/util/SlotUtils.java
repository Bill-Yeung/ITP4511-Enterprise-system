package hk.edu.hkiit.jakarta.webapp.util;

import hk.edu.hkiit.jakarta.webapp.bean.ClinicHoursBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.ClinicDAO;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Generates latest slot availability rows for a clinic-service on a given date
public class SlotUtils {

    public static ArrayList<Map<String, Object>> generateSlotRows(int clinicServiceId, String dateStr) {
        
        ClinicDAO clinicDAO = new ClinicDAO();
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        ClinicServiceBean cs = clinicDAO.getClinicServiceById(clinicServiceId);

        LocalDate localDate = LocalDate.parse(dateStr);
        Date sqlDate = Date.valueOf(localDate);

        ArrayList<Map<String, Object>> rows = new ArrayList<>();
        if (cs == null) {
            return rows;
        }

        String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        ArrayList<ClinicHoursBean> sessions = clinicDAO.getClinicHoursByDay(cs.getClinicId(), dayName);

        int durationMins = cs.getSlotDurationMins();
        int quota = cs.getQuotaPerSlot();

        // Generate slots with latest information

        for (ClinicHoursBean session : sessions) {

            long openMillis = session.getOpenTime().getTime();
            long closeMillis = session.getCloseTime().getTime();
            long intervalMs = durationMins * 60 * 1000L;

            for (long t = openMillis; t + intervalMs <= closeMillis; t += intervalMs) {

                Time slotTime = new Time(t);
                int booked = appointmentDAO.getSlotBookingCount(clinicServiceId, sqlDate, slotTime);
                int remaining = quota - booked;

                Map<String, Object> row = new HashMap<>();
                row.put("time", slotTime.toString());
                row.put("booked", booked);
                row.put("quota", quota);
                row.put("remaining", remaining);
                rows.add(row);

            }

        }

        return rows;

    }
    
}
