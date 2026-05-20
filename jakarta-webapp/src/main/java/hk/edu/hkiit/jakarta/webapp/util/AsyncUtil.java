package hk.edu.hkiit.jakarta.webapp.util;

import jakarta.servlet.AsyncContext;

public class AsyncUtil {

    public static void safeComplete(AsyncContext async) {

        try {
            async.complete();
        } catch (IllegalStateException ignored) {
        }
        
    }

}
