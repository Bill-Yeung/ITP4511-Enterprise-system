package hk.edu.hkiit.jakarta.webapp.util;

public class ValidationUtil {

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
    
}
