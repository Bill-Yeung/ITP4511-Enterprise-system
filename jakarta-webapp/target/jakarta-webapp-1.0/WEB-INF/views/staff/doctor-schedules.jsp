<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.DoctorScheduleBean"%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Doctor Schedules &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/header.jsp" />
<div id="page-wrapper">
    <jsp:include page="/WEB-INF/views/menu.jsp" />
    <main id="main-content">

        <div class="page-header">
            <h1>Doctor Schedules</h1>
        </div>

        <%
            String adminMsg = (String) session.getAttribute("adminMsg");
            String adminError = (String) session.getAttribute("adminError");
            if (adminMsg != null) { session.removeAttribute("adminMsg"); %>
            <div class="alert alert-success"><%= adminMsg %></div>
        <% } if (adminError != null) { session.removeAttribute("adminError"); %>
            <div class="alert alert-error"><%= adminError %></div>
        <% } %>

        <%
            ArrayList<DoctorScheduleBean> rows = (ArrayList<DoctorScheduleBean>) request.getAttribute("rows");
            ArrayList<String[]> doctors = (ArrayList<String[]>) request.getAttribute("doctors");
            Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
            Boolean isSystemAdmin = (Boolean) request.getAttribute("isSystemAdmin");
            Integer viewClinicId = (Integer) request.getAttribute("viewClinicId");
            if (rows == null) rows = new ArrayList<DoctorScheduleBean>();
            if (doctors == null) doctors = new ArrayList<String[]>();
        %>

        <%-- Add new schedule row --%>
        <% if (isAdmin != null && isAdmin) { %>
        <div class="card" style="margin-bottom:16px">
            <div class="card-header">Add Schedule</div>
            <div class="card-body">
                <form method="post" action="<%= ctx %>/staff/doctor-schedules"
                      class="filter-bar doctor-schedule-form">
                    <input type="hidden" name="action" value="add" />

                    <div class="form-group">
                        <label>Doctor</label>
                        <select name="doctorId" class="form-control" required>
                            <option value="">Select doctor</option>
                            <% for (String[] d : doctors) { %>
                                <option value="<%= d[0] %>"><%= d[1] %></option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Day</label>
                        <select name="dayOfWeek" class="form-control" required>
                            <% String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
                               for (String d : days) { %>
                                <option value="<%= d %>"><%= d %></option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Start</label>
                        <input type="time" name="startTime" class="form-control" required />
                    </div>

                    <div class="form-group">
                        <label>End</label>
                        <input type="time" name="endTime" class="form-control" required />
                    </div>

                    <button type="submit" class="btn btn-primary">Add</button>
                </form>
            </div>
        </div>
        <% } else { %>
            <div class="alert alert-info">
                Showing doctor schedules for your clinic ID: <%= viewClinicId %>.
            </div>
        <% } %>

        <%-- Existing rows --%>
        <div class="card">
            <div class="card-header">
                Current Schedules
                <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                    <%= rows.size() %> row<%= rows.size() == 1 ? "" : "s" %>
                </span>
            </div>
            <div class="card-body" style="padding:0; overflow-x:auto">
                <table class="data-table doctor-schedule-table">
                    <thead>
                        <tr>
                            <th class="schedule-id-col">ID</th>
                            <th class="schedule-doctor-col"><%= (isSystemAdmin != null && isSystemAdmin) ? "Doctor / Clinic" : "Doctor" %></th>
                            <th class="schedule-day-col">Day</th>
                            <th class="schedule-time-col">Start</th>
                            <th class="schedule-time-col">End</th>
                            <% if (isAdmin != null && isAdmin) { %>
                                <th class="schedule-actions-col">Actions</th>
                            <% } %>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (rows.isEmpty()) { %>
                        <tr><td colspan="<%= (isAdmin != null && isAdmin) ? 6 : 5 %>" style="text-align:center; padding:24px; color:var(--gray-500)">
                            No schedules found.
                        </td></tr>
                    <% } else for (DoctorScheduleBean r : rows) { %>
                        <tr>
                            <td class="schedule-id-col"><%= r.getScheduleId() %></td>
                            <td class="schedule-doctor-cell"><%= r.getDoctorName() %></td>
                            <td class="schedule-day-col"><%= r.getDayOfWeek() %></td>
                            <% if (isAdmin != null && isAdmin) { %>
                                <td><input type="time" name="startTime" value="<%= r.getStartTime() %>" class="form-control schedule-time-input" form="scheduleUpdateForm<%= r.getScheduleId() %>" required /></td>
                                <td><input type="time" name="endTime" value="<%= r.getEndTime() %>" class="form-control schedule-time-input" form="scheduleUpdateForm<%= r.getScheduleId() %>" required /></td>
                                <td class="table-actions schedule-actions">
                                    <form id="scheduleUpdateForm<%= r.getScheduleId() %>" method="post" action="<%= ctx %>/staff/doctor-schedules" style="display:inline">
                                        <input type="hidden" name="action" value="update" />
                                        <input type="hidden" name="scheduleId" value="<%= r.getScheduleId() %>" />
                                    </form>
                                    <button type="button" class="btn btn-sm btn-primary clinic-action-btn"
                                            data-confirm-submit="scheduleUpdateForm<%= r.getScheduleId() %>"
                                            data-confirm-title="Save Schedule"
                                            data-confirm-message="Save changes to this doctor schedule?">Save</button>
                                    <form id="scheduleDeleteForm<%= r.getScheduleId() %>" method="post" action="<%= ctx %>/staff/doctor-schedules" style="display:inline">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="scheduleId" value="<%= r.getScheduleId() %>" />
                                    </form>
                                    <button type="button" class="btn btn-sm btn-danger clinic-action-btn"
                                            data-confirm-submit="scheduleDeleteForm<%= r.getScheduleId() %>"
                                            data-confirm-title="Delete Schedule"
                                            data-confirm-message="Delete this schedule entry?"
                                            data-confirm-danger="true">Delete</button>
                                </td>
                            <% } else { %>
                                <td><%= r.getStartTime() %></td>
                                <td><%= r.getEndTime() %></td>
                            <% } %>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>

    </main>
</div>
<jsp:include page="/WEB-INF/views/footer.jsp" />
<script src="<%= ctx %>/js/confirm-modal.js"></script>
</body>
</html>
