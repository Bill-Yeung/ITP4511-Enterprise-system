package hk.edu.hkiit.jakarta.webapp.util;

import hk.edu.hkiit.jakarta.webapp.bean.NotificationBean;
import hk.edu.hkiit.jakarta.webapp.dao.NotificationDAO;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;

import java.util.ArrayList;

public class NotificationHelper {

    private static final NotificationDAO DAO = new NotificationDAO();
    private static final UserDAO USER_DAO = new UserDAO();

    public static void notify(int userId, String type, String message) {
        NotificationBean n = DAO.createAndReturn(userId, message, type);
        if (n != null) NotificationBroadcaster.push(userId, n);
    }

    public static void notifyClinicStaff(int clinicId, String type, String message) {
        ArrayList<Integer> recipients = USER_DAO.getStaffAndAdminIdsForClinic(clinicId);
        for (Integer uid : recipients) {
            notify(uid, type, message);
        }
    }
}
