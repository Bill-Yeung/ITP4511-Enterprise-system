<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.NotificationBean"%>
<%@ taglib uri="/WEB-INF/tlds/clinic-taglib.tld" prefix="clinic" %>

<div class="card">
    <div class="card-header dashboard-notification-header">
        <span>Notifications</span>
        <span class="dashboard-notification-actions">
            <span id="notification-unread-count" class="badge badge-info">
                Unread: <%= request.getAttribute("unreadCount") %>
            </span>
            <button type="button" id="dashboard-notification-toggle"
                    class="btn btn-xs btn-outline"
                    aria-expanded="true"
                    aria-controls="dashboard-notification-list">Hide</button>
        </span>
    </div>
    <div id="dashboard-notification-list" class="card-body">
        <%
            ArrayList<NotificationBean> notifList =
                (ArrayList<NotificationBean>) request.getAttribute("notifList");
        %>
        <clinic:notificationList notifications="<%= notifList %>" />
    </div>
</div>

<script>
(function () {
    var list = document.getElementById('dashboard-notification-list');
    var unread = document.getElementById('notification-unread-count');
    var toggle = document.getElementById('dashboard-notification-toggle');

    if (toggle && list) {
        toggle.addEventListener('click', function () {
            var collapsed = !list.classList.contains('is-hidden');
            list.classList.toggle('is-hidden', collapsed);
            toggle.textContent = collapsed ? 'Show' : 'Hide';
            toggle.setAttribute('aria-expanded', collapsed ? 'false' : 'true');
        });
    }

    if (!list || !window.EventSource) return;

    function escapeHtml(value) {
        return String(value || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function colorFor(type) {
        if (type === 'Appointment') return '#2563eb';
        if (type === 'Queue') return '#7c3aed';
        if (type === 'Reminder') return '#f59e0b';
        return '#6b7280';
    }

    function iconFor(type) {
        if (type === 'Appointment') return '&#128197;';
        if (type === 'Queue') return '&#128101;';
        if (type === 'Reminder') return '&#128276;';
        return '&#9881;';
    }

    function prependNotification(notification) {
        var type = notification.type || 'System';
        var color = colorFor(type);
        var card = document.createElement('div');
        card.style.cssText = 'background:#fff; border:1px solid #e5e7eb; border-radius:8px; margin-bottom:12px; overflow:hidden; border-left:4px solid ' + color + ';';
        card.innerHTML =
            '<div style="padding:12px 16px; background:' + color + '11; display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #e5e7eb">' +
                '<span style="font-size:12px; font-weight:600; color:' + color + '">' + iconFor(type) + ' ' + escapeHtml(type) + '</span>' +
                '<span style="font-size:11px; color:#9ca3af">' + escapeHtml(notification.created_at || '') + '</span>' +
            '</div>' +
            '<div style="padding:14px 16px; font-size:14px; color:#374151; line-height:1.5">' + escapeHtml(notification.message) + '</div>' +
            '<div style="padding:0 16px 10px; font-size:11px; color:' + color + '; font-weight:600">&#9679; Unread</div>';

        var empty = list.querySelector('.no-data');
        if (empty) empty.remove();
        list.insertBefore(card, list.firstChild);
    }

    function incrementUnread() {
        if (!unread) return;
        var match = unread.textContent.match(/\d+/);
        var count = match ? parseInt(match[0], 10) : 0;
        unread.textContent = 'Unread: ' + (count + 1);
    }

    var source = new EventSource('<%= request.getContextPath() %>/api/sse/notifications');
    source.onmessage = function (event) {
        try {
            var notification = JSON.parse(event.data);
            prependNotification(notification);
            if (!notification.is_read) incrementUnread();
        } catch (ignore) {}
    };
})();
</script>
