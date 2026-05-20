package hk.edu.hkiit.jakarta.webapp.util;

import java.sql.Time;

public class TimeUtil {

    public static Time parseSqlTime(String value) {

        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return Time.valueOf(value.length() == 5 ? value + ":00" : value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        
    }

}
