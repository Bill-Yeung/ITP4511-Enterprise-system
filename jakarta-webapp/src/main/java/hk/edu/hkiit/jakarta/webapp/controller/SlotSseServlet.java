package hk.edu.hkiit.jakarta.webapp.controller;

import hk.edu.hkiit.jakarta.webapp.util.AsyncUtil;
import hk.edu.hkiit.jakarta.webapp.util.JsonUtil;
import hk.edu.hkiit.jakarta.webapp.util.SlotUpdateBroadcaster;
import hk.edu.hkiit.jakarta.webapp.util.StringUtil;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

// Servlet for handling real time changes of timeslot availability
@WebServlet(name = "SlotSseServlet", urlPatterns = {"/api/sse/slots"}, asyncSupported = true)
public class SlotSseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String csIdStr = request.getParameter("clinicServiceId");
        Integer clinicServiceId = StringUtil.parseIntOrNull(csIdStr);
        if (clinicServiceId == null || clinicServiceId <= 0) {
            JsonUtil.writeError(response, 400, "clinicServiceId required");
            return;
        }

        // Setup SSE headers
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        // Send to establish SSE connection
        PrintWriter writer = response.getWriter();
        writer.write(": connected\n\n");
        writer.flush();

        // Start async context (with 5 minutes timeout) and register for future updates
        AsyncContext async = request.startAsync();
        async.setTimeout(300000);
        SlotUpdateBroadcaster.register(clinicServiceId, async);

        async.addListener(new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) {
                SlotUpdateBroadcaster.unregister(clinicServiceId, async);
            }

            @Override
            public void onTimeout(AsyncEvent event) {
                SlotUpdateBroadcaster.unregister(clinicServiceId, async);
                AsyncUtil.safeComplete(async);
            }

            @Override
            public void onError(AsyncEvent event) {
                SlotUpdateBroadcaster.unregister(clinicServiceId, async);
                AsyncUtil.safeComplete(async);
            }

            @Override
            public void onStartAsync(AsyncEvent event) {}

        });

    }

}
