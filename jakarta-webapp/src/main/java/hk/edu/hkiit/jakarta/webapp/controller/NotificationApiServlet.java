package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.bean.NotificationBean;
import hk.edu.hkiit.jakarta.webapp.dao.NotificationDAO;
import hk.edu.hkiit.jakarta.webapp.util.JsonUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@WebServlet(name = "NotificationApiServlet", urlPatterns = {"/api/notification/*"})
public class NotificationApiServlet extends HttpServlet {

    private NotificationDAO notificationDAO = new NotificationDAO();
    private static SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = getSessionUserId(request);
        if (userId == null) {
            JsonUtil.writeError(response, 401, "Unauthorized");
            return;
        }

        try {
            int page = 1;
            int limit = 20;

            String pageStr = request.getParameter("page");
            String limitStr = request.getParameter("limit");

            if (pageStr != null) {
                try { page = Integer.parseInt(pageStr); } catch (NumberFormatException e) {}
            }
            if (limitStr != null) {
                try { limit = Integer.parseInt(limitStr); } catch (NumberFormatException e) {}
            }

            ArrayList<NotificationBean> notifications = notificationDAO.getByUserId(userId, page, limit);
            int total = notificationDAO.countByUserId(userId);
            int unread = notificationDAO.countUnread(userId);

            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (NotificationBean n : notifications) {
                String title = n.getType() != null ? n.getType() : "Notification";
                JsonObjectBuilder obj = Json.createObjectBuilder()
                    .add("notification_id", n.getNotificationId())
                    .add("title", title)
                    .add("message", n.getMessage())
                    .add("type", n.getType())
                    .add("is_read", n.isRead())
                    .add("created_at", n.getCreatedAt() != null ? DATE_FMT.format(n.getCreatedAt()) : "");
                arr.add(obj);
            }

            JsonUtil.write(response, Json.createObjectBuilder()
                .add("notifications", arr.build())
                .add("page", page)
                .add("limit", limit)
                .add("total", total)
                .add("unread", unread)
                .build().toString());

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "Error fetching notifications");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = getSessionUserId(request);
        if (userId == null) {
            JsonUtil.writeError(response, 401, "Unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if ("markAllRead".equals(action)) {
            notificationDAO.markAllAsRead(userId);
            JsonUtil.write(response, "{\"message\":\"All notifications marked as read\"}");
            return;
        }

        if ("markRead".equals(action)) {
            try {
                int notificationId = Integer.parseInt(request.getParameter("notificationId"));
                if (notificationDAO.markAsRead(notificationId, userId)) {
                    JsonUtil.write(response, "{\"message\":\"Notification marked as read\"}");
                } else {
                    JsonUtil.writeError(response, 404, "Notification not found");
                }
            } catch (NumberFormatException e) {
                JsonUtil.writeError(response, 400, "Invalid notification ID");
            }
            return;
        }

        if ("delete".equals(action)) {
            try {
                int notificationId = Integer.parseInt(request.getParameter("notificationId"));
                if (notificationDAO.deleteNotification(notificationId, userId)) {
                    JsonUtil.write(response, "{\"message\":\"Notification deleted\"}");
                } else {
                    JsonUtil.writeError(response, 404, "Notification not found");
                }
            } catch (NumberFormatException e) {
                JsonUtil.writeError(response, 400, "Invalid notification ID");
            }
            return;
        }

        JsonUtil.writeError(response, 404, "Not found");
    }

    private Integer getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Integer) session.getAttribute("userId");
    }

}
