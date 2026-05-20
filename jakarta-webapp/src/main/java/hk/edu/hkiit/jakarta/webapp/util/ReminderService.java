package hk.edu.hkiit.jakarta.webapp.util;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.dao.AppointmentDAO;
import hk.edu.hkiit.jakarta.webapp.dao.NotificationDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ReminderService {

    private static final AppointmentDAO APPT_DAO = new AppointmentDAO();
    private static final NotificationDAO NOTI_DAO = new NotificationDAO();

    public static int generateForPatient(int patientId) {
        ArrayList<AppointmentBean> upcoming = APPT_DAO.getBookedWithinNext24Hours(patientId);
        if (upcoming == null || upcoming.isEmpty()) return 0;

        SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");

        int created = 0;
        for (AppointmentBean a : upcoming) {
            if (NOTI_DAO.hasReminderForAppointmentToday(patientId, a.getAppointmentId())) {
                continue;
            }

            String when = dateFmt.format(a.getAppointmentDate())
                        + " at " + timeFmt.format(a.getTimeSlot());
            String clinic = a.getClinicName() != null ? a.getClinicName() : "your clinic";
            String service = a.getServiceName() != null ? a.getServiceName() : "your appointment";

            String message = "Reminder: appointment #" + a.getAppointmentId()
                           + " — " + service + " at " + clinic + " on " + when + ".";

            NotificationHelper.notify(patientId, "Reminder", message);
            created++;
        }
        return created;
    }
}
