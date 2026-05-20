<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ServiceBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.util.JsonUtil"%>
<%@ taglib prefix="clinic" uri="/WEB-INF/tlds/clinic-taglib.tld" %>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.7/dist/chart.umd.min.js"></script>
</head>
<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">
        
        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <jsp:useBean id="profile" class="hk.edu.hkiit.jakarta.webapp.bean.AdminBean" scope="session" />

            <div class="page-header">
                <h1>Reports &amp; Analytics</h1>
                <span class="badge badge-booked">
                    <jsp:getProperty name="profile" property="adminLevel" /> Admin
                </span>
            </div>

            <%
                String tab = (String) request.getAttribute("tab");
                if (tab == null) {
                    tab = "records";
                }

                Integer filterClinicId = (Integer) request.getAttribute("filterClinicId");
                Integer filterServiceId = (Integer) request.getAttribute("filterServiceId");
                Integer filterYear = (Integer) request.getAttribute("filterYear");
                Integer filterMonth = (Integer) request.getAttribute("filterMonth");
                String filterStatus = (String)  request.getAttribute("filterStatus");

                ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
                ArrayList<ServiceBean> services = (ArrayList<ServiceBean>) request.getAttribute("services");

                String baseUrl = request.getContextPath() + "/admin/reports";
                int currentYear = java.time.Year.now().getValue();
                String adminLevel = profile.getAdminLevel();
            %>

            <div class="report-tabs">
                <a href="<%= baseUrl %>?tab=records" class="report-tab <%= "records".equals(tab) ? "active" : "" %>">
                    Appointment Records
                </a>
                <a href="<%= baseUrl %>?tab=utilisation" class="report-tab <%= "utilisation".equals(tab) ? "active" : "" %>">
                    Utilisation Rate
                </a>
                <a href="<%= baseUrl %>?tab=noshow" class="report-tab <%= "noshow".equals(tab) ? "active" : "" %>">
                    No-show Summary
                </a>
            </div>

            <%-- Filter Bar --%>
            <form method="get" action="<%= baseUrl %>">
                <input type="hidden" name="tab" value="<%= tab %>">
                <div class="filter-bar">

                    <% if (!"Clinic".equals(adminLevel)) { %>
                    <div class="form-group">
                        <label>Clinic</label>
                        <select name="clinicId">
                            <option value="">All Clinics</option>
                            <% for (ClinicBean c : clinics) { %>
                                <option value="<%= c.getClinicId() %>"
                                    <%= (filterClinicId != null && filterClinicId == c.getClinicId()) ? "selected" : "" %>>
                                    <%= c.getName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <% } %>

                    <% if ("records".equals(tab)) { %>
                    <div class="form-group">
                        <label>Service</label>
                        <select name="serviceId">
                            <option value="">All Services</option>
                            <% for (ServiceBean s : services) { %>
                                <option value="<%= s.getServiceId() %>"
                                    <%= (filterServiceId != null && filterServiceId == s.getServiceId()) ? "selected" : "" %>>
                                    <%= s.getName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Status</label>
                        <select name="status">
                            <option value="">All Statuses</option>
                            <% String[] statuses = {"Booked", "Arrived", "Completed", "No-show", "Cancelled"};
                                for (String st : statuses) { %>
                                <option value="<%= st %>" <%= st.equals(filterStatus) ? "selected" : "" %>>
                                    <%= st %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <% } %>

                    <div class="form-group">
                        <label>Year</label>
                        <select name="year">
                            <option value="">All Years</option>
                            <% for (int y = currentYear; y >= currentYear - 2; y--) { %>
                                <option value="<%= y %>" <%= (filterYear != null && filterYear == y) ? "selected" : "" %>>
                                    <%= y %>
                                </option>
                                <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Month</label>
                        <select name="month">
                            <option value="">All Months</option>
                            <% String[] months = {"", "January", "February", "March", "April", "May", "June",
                                                "July", "August", "September", "October", "November", "December"};
                            for (int m = 1; m <= 12; m++) { %>
                                <option value="<%= m %>" <%= (filterMonth != null && filterMonth == m) ? "selected" : "" %>>
                                    <%= months[m] %>
                                </option>
                            <% } %>
                        </select>
                    </div>

                    <button type="submit" class="btn btn-primary">Filter</button>
                </div>
            </form>

            <%-- Appointment records --%>
            <% if ("records".equals(tab)) {
                ArrayList<AppointmentBean> appointments = (ArrayList<AppointmentBean>) request.getAttribute("appointments");
            %>
            <div class="card">
                <div class="card-header">
                    Appointment Records
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= appointments != null ? appointments.size() : 0 %> records
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <clinic:appointmentTable appointments="<%= appointments %>" showStaffActions="false" />
                </div>
            </div>
            <% } %>

            <%-- Utilization rate --%>
            <% if ("utilisation".equals(tab)) {
                ArrayList<String[]> utilisation = (ArrayList<String[]>) request.getAttribute("utilisation");
            %>
            <div class="card">
                <div class="card-header">
                    Utilisation Rate (Booked Slots &divide; Total Offered Slots)
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <clinic:utilisationTable rows="<%= utilisation %>" />
                </div>
            </div>

            <% if (utilisation != null && !utilisation.isEmpty()) { %>
            <div class="card" style="margin-top:20px">
                <div class="card-header">Utilisation Rate by Clinic and Service</div>
                <div class="card-body">
                    <canvas id="utilisationChart" height="100"></canvas>
                    <script>
                        new Chart(document.getElementById('utilisationChart'), {
                            type: 'bar',
                            data: {
                                labels: <%= JsonUtil.toJson(request.getAttribute("utilisationChartLabels")) %>,
                                datasets: [{
                                    label: 'Utilisation Rate (%)',
                                    data: <%= JsonUtil.toJson(request.getAttribute("utilisationChartData")) %>,
                                    backgroundColor: ['#2563eb', '#16a34a', '#ea580c', '#7c3aed', '#dc2626']
                                }]
                            },
                            options: {
                                indexAxis: 'y',
                                plugins: {
                                    legend: { display: false },
                                    tooltip: { callbacks: { label: function(ctx) { return ctx.raw + '%'; } } }
                                },
                                scales: {
                                    x: { beginAtZero: true, max: 100, ticks: { callback: function(v) { return v + '%'; } } }
                                }
                            }
                        });
                    </script>
                </div>
            </div>
            <% } %>

            <% } %>

            <%-- No-show summary --%>
            <% if ("noshow".equals(tab)) {
                ArrayList<String[]> noshow = (ArrayList<String[]>) request.getAttribute("noshow");
            %>
            <div class="card">
                <div class="card-header">
                    No-show Summary
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <clinic:noShowTable rows="<%= noshow %>" />
                </div>
            </div>

            <% if (noshow != null && !noshow.isEmpty()) { %>
            <div class="card" style="margin-top:20px">
                <div class="card-header">No-show Distribution by Clinic and Service</div>
                <div class="card-body" style="max-width:480px; margin:0 auto;">
                    <canvas id="noshowChart" height="220"></canvas>
                    <script>
                        new Chart(document.getElementById('noshowChart'), {
                            type: 'doughnut',
                            data: {
                                labels: <%= JsonUtil.toJson(request.getAttribute("noshowChartLabels")) %>,
                                datasets: [{
                                    data: <%= JsonUtil.toJson(request.getAttribute("noshowChartData")) %>,
                                    backgroundColor: ['#2563eb', '#16a34a', '#ea580c', '#7c3aed', '#dc2626']
                                }]
                            },
                            options: {
                                plugins: {
                                    legend: { position: 'bottom' }
                                }
                            }
                        });
                    </script>
                </div>
            </div>
            <% } %>
            <% } %>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
