package hk.edu.hkiit.jakarta.webapp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectWriter objectWriter = mapper.writer();

    public static String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("\"", "\\\"");

    }

    public static void write(HttpServletResponse response, String json) throws IOException {
        response.getWriter().write(json);
    }

    public static void writeJson(HttpServletResponse response, Object data) throws IOException {
        objectWriter.writeValue(response.getWriter(), data);
    }

    public static void writeJson(PrintWriter out, Object data) throws IOException {
        objectWriter.writeValue(out, data);
    }

    public static String toJson(Object data) throws IOException {
        return objectWriter.writeValueAsString(data);
    }

    public static void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        writeJson(response, error);
    }
    
}
