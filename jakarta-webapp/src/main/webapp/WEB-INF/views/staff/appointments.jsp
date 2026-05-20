<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean"%>
<%@ taglib prefix="clinic" uri="/WEB-INF/tlds/clinic-taglib.tld" %>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Appointments &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">

        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <div class="page-header">
                <h1>Appointment Management</h1>
            </div>

            <%
                String staffMsg = (String) session.getAttribute("staffMsg");
                String staffError = (String) session.getAttribute("staffError");
                if (staffMsg != null) { session.removeAttribute("staffMsg"); %>
                    <div class="alert alert-success"><%= staffMsg %></div>
            <% } if (staffError != null) { session.removeAttribute("staffError"); %>
                    <div class="alert alert-error"><%= staffError %></div>
            <% } %>

            <%
                String startDate = (String) request.getAttribute("startDate");
                String endDate = (String) request.getAttribute("endDate");
                if (startDate == null) {
                    startDate = new java.sql.Date(System.currentTimeMillis()).toString();
                }
                if (endDate == null) {
                    endDate = startDate;
                }
                Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
                Integer viewClinicId = (Integer) request.getAttribute("viewClinicId");
                String appointmentActionUrl = ctx + "/staff/appointments";
                ArrayList<ClinicServiceBean> clinicServices = (ArrayList<ClinicServiceBean>) request.getAttribute("clinicServices");
                ArrayList<String[]> doctors = (ArrayList<String[]>) request.getAttribute("doctors");
                if (isAdmin != null && isAdmin && viewClinicId != null) {
                    appointmentActionUrl += "?clinic=" + viewClinicId + "&startDate=" + startDate + "&endDate=" + endDate;
                } else {
                    appointmentActionUrl += "?startDate=" + startDate + "&endDate=" + endDate;
                }
            %>

            <form class="filter-bar" method="get"
                action="<%= ctx %>/staff/appointments">
                <% if (isAdmin != null && isAdmin) { %>
                    <%
                        ArrayList<ClinicBean> allClinics = (ArrayList<ClinicBean>) request.getAttribute("allClinics");
                    %>
                    <div class="form-group">
                        <label for="clinic">Clinic</label>
                        <select id="clinic" name="clinic">
                            <% if (allClinics != null) {
                                for (ClinicBean c : allClinics) { %>
                                    <option value="<%= c.getClinicId() %>"
                                        <%= (viewClinicId != null && c.getClinicId() == viewClinicId) ? "selected" : "" %>>
                                        <%= c.getName() %>
                                    </option>
                            <%  } } %>
                        </select>
                    </div>
                <% } %>
                <div class="form-group">
                    <label for="startDate">Start Date</label>
                    <input type="date" id="startDate" name="startDate" value="<%= startDate %>">
                </div>
                <div class="form-group">
                    <label for="endDate">End Date</label>
                    <input type="date" id="endDate" name="endDate" value="<%= endDate %>">
                </div>
                <button type="submit" class="btn btn-primary">View</button>
                <a href="<%= ctx %>/staff/appointments<%= (isAdmin != null && isAdmin && viewClinicId != null) ? "?clinic=" + viewClinicId : "" %>"
                class="btn btn-outline">Today</a>
            </form>

            <%-- Pending booking requests --%>
            <%
                ArrayList<AppointmentBean> pending = (ArrayList<AppointmentBean>) request.getAttribute("pendingAppointments");
                int pendingCount = pending == null ? 0 : pending.size();
            %>
            <div class="card" style="margin-bottom:16px">
                <div class="card-header">
                    Pending Booking Requests
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= pendingCount %> awaiting approval
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <clinic:appointmentTable appointments="<%= pending %>"
                        showStaffActions="true"
                        clinicServices="<%= clinicServices %>" doctors="<%= doctors %>"
                        actionUrl="<%= appointmentActionUrl %>"
                        modalId="pendingAppointmentModal" />
                </div>
            </div>

            <%-- Appointment table --%>
            <div class="card">
                <div class="card-header">
                    Appointments from <%= startDate %> to <%= endDate %>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <%
                        ArrayList<AppointmentBean> appointments =
                            (ArrayList<AppointmentBean>) request.getAttribute("appointments");
                    %>
                    <clinic:appointmentTable appointments="<%= appointments %>"
                        showStaffActions="true"
                        clinicServices="<%= clinicServices %>" doctors="<%= doctors %>"
                        actionUrl="<%= appointmentActionUrl %>"
                        modalId="dailyAppointmentModal" />
                </div>
            </div>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />
    <script src="<%= ctx %>/js/appointment-actions.js"></script>

</body>
</html>
