package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.util.AsyncUtil;
import hk.edu.hkiit.jakarta.webapp.util.NotificationBroadcaster;
import hk.edu.hkiit.jakarta.webapp.util.ReminderService;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "NotificationSseServlet",
            urlPatterns = {"/api/sse/notifications"},
            asyncSupported = true)
public class NotificationSseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        // SSE headers
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        // Establish stream
        PrintWriter writer = response.getWriter();
        writer.write(": connected\n\n");
        writer.flush();

        AsyncContext async = request.startAsync();
        async.setTimeout(600000); // 10 min ??client EventSource auto-reconnects after timeout

        int uid = userId;
        NotificationBroadcaster.register(uid, async);

        String role = (String) session.getAttribute("role");
        if ("Patient".equals(role)) {
            try {
                ReminderService.generateForPatient(uid);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        async.addListener(new AsyncListener() {
            @Override public void onComplete(AsyncEvent e) {
                NotificationBroadcaster.unregister(uid, async);
            }
            @Override public void onTimeout(AsyncEvent e) {
                NotificationBroadcaster.unregister(uid, async);
                AsyncUtil.safeComplete(async);
            }
            @Override public void onError(AsyncEvent e) {
                NotificationBroadcaster.unregister(uid, async);
                AsyncUtil.safeComplete(async);
            }
            @Override public void onStartAsync(AsyncEvent e) {}
        });
    }
}
