package hk.edu.hkiit.jakarta.webapp.util;

import hk.edu.hkiit.jakarta.webapp.bean.NotificationBean;
import jakarta.servlet.AsyncContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationBroadcaster {

    private static final ConcurrentHashMap<Integer, CopyOnWriteArrayList<AsyncContext>> listeners
            = new ConcurrentHashMap<>();

    public static void register(int userId, AsyncContext async) {
        listeners.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(async);
    }

    public static void unregister(int userId, AsyncContext async) {
        CopyOnWriteArrayList<AsyncContext> list = listeners.get(userId);
        if (list != null) {
            list.remove(async);
            if (list.isEmpty()) listeners.remove(userId);
        }
    }

    public static void push(int userId, NotificationBean n) {
        CopyOnWriteArrayList<AsyncContext> list = listeners.get(userId);
        if (list == null || list.isEmpty()) return;

        String payload = toJson(n);
        for (AsyncContext async : list) {
            try {
                PrintWriter writer = async.getResponse().getWriter();
                writer.write("data: " + payload + "\n\n");
                writer.flush();
                if (writer.checkError()) {
                    list.remove(async);
                    safeComplete(async);
                }
            } catch (IOException e) {
                list.remove(async);
                safeComplete(async);
            }
        }
    }

    private static String toJson(NotificationBean n) {
        String title = n.getType() != null ? n.getType() : "Notification";
        return "{\"notification_id\":" + n.getNotificationId()
            + ",\"title\":\"" + esc(title) + "\""
            + ",\"message\":\"" + esc(n.getMessage()) + "\""
            + ",\"type\":\"" + esc(n.getType()) + "\""
            + ",\"is_read\":" + n.isRead()
            + ",\"created_at\":\"" + (n.getCreatedAt() != null ? n.getCreatedAt().toString() : "") + "\"}";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void safeComplete(AsyncContext async) {
        try { async.complete(); } catch (Exception ignored) {}
    }
}
