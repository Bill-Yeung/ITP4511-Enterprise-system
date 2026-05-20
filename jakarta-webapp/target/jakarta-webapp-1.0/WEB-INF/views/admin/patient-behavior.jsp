<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean"%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Patient Behavior Log &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/header.jsp" />
<div id="page-wrapper">
    <jsp:include page="/WEB-INF/views/menu.jsp" />
    <main id="main-content">

        <%
            ArrayList<String[]> summary = (ArrayList<String[]>) request.getAttribute("summary");
            ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
            Integer filterClinicId = (Integer) request.getAttribute("filterClinicId");
            Integer filterDays = (Integer) request.getAttribute("filterDays");
            Integer filterMinFlags = (Integer) request.getAttribute("filterMinFlags");
            Boolean isClinicAdmin = (Boolean) request.getAttribute("isClinicAdmin");
            Integer selectedPatientId = (Integer) request.getAttribute("selectedPatientId");
            String selectedPatientName = (String) request.getAttribute("selectedPatientName");
            ArrayList<AppointmentBean> patientHistory =
                    (ArrayList<AppointmentBean>) request.getAttribute("patientHistory");

            if (summary == null) summary = new ArrayList<String[]>();
            if (clinics == null) clinics = new ArrayList<ClinicBean>();
            if (filterDays == null) filterDays = 90;
            if (filterMinFlags == null) filterMinFlags = 2;
        %>

        <div class="page-header">
            <h1>Patient Behavior Log</h1>
            <p style="color:var(--gray-500); margin-top:4px">
                Review patients with repeated no-shows or frequent cancellations.
            </p>
        </div>

        <%-- Filter bar --%>
        <form class="card" method="get" action="<%= ctx %>/admin/patient-behavior"
              style="margin-bottom:16px">
            <div class="card-body">
                <div class="filter-bar">
                    <% if (isClinicAdmin == null || !isClinicAdmin) { %>
                        <div class="form-group">
                            <label>Clinic</label>
                            <select name="clinicId" class="form-control">
                                <option value="">All clinics</option>
                                <% for (ClinicBean c : clinics) { %>
                                    <option value="<%= c.getClinicId() %>"
                                        <%= filterClinicId != null && filterClinicId == c.getClinicId() ? "selected" : "" %>>
                                        <%= c.getName() %>
                                    </option>
                                <% } %>
                            </select>
                        </div>
                    <% } %>
                    <div class="form-group">
                        <label>Window</label>
                        <select name="days" class="form-control">
                            <% int[] dayOpts = {30, 90, 180, 365, 0};
                               String[] dayLabels = {"Last 30 days","Last 90 days","Last 180 days","Last 365 days","All time"};
                               for (int i = 0; i < dayOpts.length; i++) {
                                   int d = dayOpts[i];
                                   boolean sel = (filterDays != null && filterDays.intValue() == d); %>
                                <option value="<%= d %>" <%= sel ? "selected" : "" %>><%= dayLabels[i] %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Min flagged events</label>
                        <select name="minFlags" class="form-control">
                            <% int[] minOpts = {1, 2, 3, 5};
                               for (int m : minOpts) { %>
                                <option value="<%= m %>"
                                    <%= filterMinFlags != null && filterMinFlags.intValue() == m ? "selected" : "" %>>
                                    <%= m %>+
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary">Filter</button>
                    <a href="<%= ctx %>/admin/patient-behavior" class="btn btn-outline">Reset</a>
                </div>
            </div>
        </form>

        <div class="card" style="margin-bottom:16px">
            <div class="card-header">
                Flagged Patients
                <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                    <%= summary.size() %> patient<%= summary.size() == 1 ? "" : "s" %>
                </span>
            </div>
            <div class="card-body" style="padding:0; overflow-x:auto">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Patient</th>
                            <th>Total Appts</th>
                            <th>No-shows</th>
                            <th>Cancellations</th>
                            <th>Last No-show</th>
                            <th>Last Cancelled</th>
                            <th>Risk</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (summary.isEmpty()) { %>
                        <tr><td colspan="8" style="text-align:center; padding:24px; color:var(--gray-500)">
                            No patients meet the filter criteria.
                        </td></tr>
                    <% } else for (String[] row : summary) {
                            int noShow = Integer.parseInt(row[3]);
                            int cancel = Integer.parseInt(row[4]);
                            int flags  = noShow + cancel;
                            String risk; String riskClass;
                            if (noShow >= 3 || flags >= 5) { risk = "High";   riskClass = "alert-error"; }
                            else if (flags >= 3)            { risk = "Medium"; riskClass = "alert-warning"; }
                            else                            { risk = "Low";    riskClass = "alert-info"; }
                            String drillUrl = ctx + "/admin/patient-behavior?patientId=" + row[0]
                                            + "&days=" + filterDays + "&minFlags=" + filterMinFlags
                                            + (filterClinicId != null ? "&clinicId=" + filterClinicId : "");
                    %>
                        <tr>
                            <td><%= row[1] %> <small style="color:var(--gray-500)">#<%= row[0] %></small></td>
                            <td><%= row[2] %></td>
                            <td><strong><%= row[3] %></strong></td>
                            <td><%= row[4] %></td>
                            <td><%= row[5].isEmpty() ? "&mdash;" : row[5] %></td>
                            <td><%= row[6].isEmpty() ? "&mdash;" : row[6] %></td>
                            <td><span class="alert <%= riskClass %>" style="padding:2px 8px; font-size:12px"><%= risk %></span></td>
                            <td><a class="btn btn-sm btn-outline" href="<%= drillUrl %>">History</a></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>

        <% if (selectedPatientId != null) { %>
            <div class="card">
                <div class="card-header">
                    Appointment History &mdash;
                    <%= selectedPatientName != null ? selectedPatientName : "Patient #" + selectedPatientId %>
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= patientHistory == null ? 0 : patientHistory.size() %> record<%= patientHistory != null && patientHistory.size() == 1 ? "" : "s" %>
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th><th>Date</th><th>Time</th>
                                <th>Clinic</th><th>Service</th><th>Doctor</th>
                                <th>Status</th><th>Cancel Reason</th><th>Remarks</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (patientHistory == null || patientHistory.isEmpty()) { %>
                            <tr><td colspan="9" style="text-align:center; padding:24px; color:var(--gray-500)">
                                No appointment history found.
                            </td></tr>
                        <% } else for (AppointmentBean a : patientHistory) {
                                String s = a.getStatus();
                                String statusStyle = "";
                                if ("No-show".equals(s))   statusStyle = "color:#b91c1c; font-weight:600";
                                else if ("Cancelled".equals(s)) statusStyle = "color:#a16207; font-weight:600";
                                else if ("Completed".equals(s)) statusStyle = "color:#15803d";
                        %>
                            <tr>
                                <td><%= a.getAppointmentId() %></td>
                                <td><%= a.getAppointmentDate() %></td>
                                <td><%= a.getTimeSlot() %></td>
                                <td><%= a.getClinicName() %></td>
                                <td><%= a.getServiceName() %></td>
                                <td><%= a.getDoctorName() == null ? "&mdash;" : a.getDoctorName() %></td>
                                <td style="<%= statusStyle %>"><%= s %></td>
                                <td><%= a.getCancelReason() == null ? "" : a.getCancelReason() %></td>
                                <td style="white-space:pre-wrap; max-width:240px"><%= a.getRemarks() == null ? "" : a.getRemarks() %></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>

    </main>
</div>
<jsp:include page="/WEB-INF/views/footer.jsp" />
</body>
</html>
