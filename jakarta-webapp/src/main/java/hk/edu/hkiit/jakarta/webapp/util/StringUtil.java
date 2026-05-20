package hk.edu.hkiit.jakarta.webapp.util;

public class StringUtil {

    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static String trimNullable(String value) {
        return value == null ? null : value.trim();
    }

    public static String nullIfEmpty(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
        
    }

    public static Integer parseIntOrNull(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }

    }

}
