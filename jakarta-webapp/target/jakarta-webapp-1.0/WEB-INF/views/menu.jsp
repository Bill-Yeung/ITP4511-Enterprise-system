<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String role = (String) session.getAttribute("role");
    String adminLevel = (String) session.getAttribute("adminLevel");
    String ctx = request.getContextPath();
%>

<nav class="side-menu">
    <ul>
        <li class="side-menu-section">OVERALL</li>
        <% if ("Admin".equals(role)) { %>
            <li><a href="<%= ctx %>/admin/dashboard">Overall Dashboard</a></li>
        <% } %>
        <li><a href="<%= ctx %>/staff/clinics">Clinics &amp; Services</a></li>

        <li class="side-menu-section">CLINIC OPERATIONS</li>
        <li><a href="<%= ctx %>/staff/dashboard">Clinic Dashboard</a></li>
        <li><a href="<%= ctx %>/staff/checkin">Check-in</a></li>
        <li><a href="<%= ctx %>/staff/queue">Queue Board</a></li>
        <li><a href="<%= ctx %>/staff/appointments">Appointments</a></li>
        <li><a href="<%= ctx %>/staff/doctor-schedules">Doctor Schedules</a></li>
        <li><a href="<%= ctx %>/staff/incidents">Report Incident</a></li>

        <% if ("Admin".equals(role)) { %>
            <li class="side-menu-section">ADMINISTRATION</li>
            <% if ("System".equals(adminLevel)) { %>
                <li><a href="<%= ctx %>/admin/users">Manage Users</a></li>
                <li><a href="<%= ctx %>/admin/clinics">Manage Clinics</a></li>
                <li><a href="<%= ctx %>/admin/services">Manage Services</a></li>
            <% } %>
            <li><a href="<%= ctx %>/admin/reports">Reports</a></li>
            <li><a href="<%= ctx %>/admin/audit">Audit Log</a></li>
            <% if ("System".equals(adminLevel)) { %>
                <li><a href="<%= ctx %>/admin/import">Batch Import</a></li>
            <% } %>
            <li><a href="<%= ctx %>/admin/incidents">Incident Log</a></li>
            <li><a href="<%= ctx %>/admin/patient-behavior">Patient Behavior Log</a></li>
            <% if ("System".equals(adminLevel)) { %>
                <li><a href="<%= ctx %>/admin/settings">Settings</a></li>
            <% } %>
        <% } %>

        <li class="side-menu-section">ACCOUNT</li>
        <li><a href="<%= ctx %>/account/profile">My Profile</a></li>

    </ul>
</nav>
