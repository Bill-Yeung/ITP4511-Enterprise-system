package hk.edu.hkiit.jakarta.webapp.util;

import jakarta.servlet.AsyncContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SlotUpdateBroadcaster {

    // SSE can register, remove, and push messages from different servlet threads.
    private static final ConcurrentHashMap<Integer, CopyOnWriteArrayList<AsyncContext>> listeners
            = new ConcurrentHashMap<>();

    public static void register(int clinicServiceId, AsyncContext async) {
        CopyOnWriteArrayList<AsyncContext> list = listeners.get(clinicServiceId);
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
            listeners.put(clinicServiceId, list);
        }
        list.add(async);
    }

    public static void unregister(int clinicServiceId, AsyncContext async) {
        CopyOnWriteArrayList<AsyncContext> list = listeners.get(clinicServiceId);
        if (list != null) {
            list.remove(async);
            if (list.isEmpty()) {
                listeners.remove(clinicServiceId);
            }
        }
    }

    public static void notifySlotChange(int clinicServiceId, String date) {
        
        CopyOnWriteArrayList<AsyncContext> list = listeners.get(clinicServiceId);
        if (list == null || list.isEmpty()) {
            return;
        }

        // Prepare broadcast message with the latest slot status.
        String payloadJson;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("date", date);
            payload.put("slots", SlotUtils.generateSlotRows(clinicServiceId, date));
            payloadJson = JsonUtil.toJson(payload);
        } catch (IOException e) {
            return;
        }

        // Push the slot update to each open SSE connection.
        for (AsyncContext async : list) {
            try {

                PrintWriter writer = async.getResponse().getWriter();
                // The blank line marks the end of one SSE message.
                writer.write("data: " + payloadJson + "\n\n");
                writer.flush();

                if (writer.checkError()) {
                    list.remove(async);
                    AsyncUtil.safeComplete(async);
                }

            } catch (IOException e) {
                list.remove(async);
                AsyncUtil.safeComplete(async);
            }
        }

    }

}
