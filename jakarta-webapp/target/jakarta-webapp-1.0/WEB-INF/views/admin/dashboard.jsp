<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">
        
        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <jsp:useBean id="profile" class="hk.edu.hkiit.jakarta.webapp.bean.AdminBean" scope="session" />

            <div class="page-header">
                <h1>Admin Dashboard</h1>
                <span class="badge badge-booked">
                    <jsp:getProperty name="profile" property="adminLevel" /> Admin
                </span>
            </div>

            <%
                String adminMsg = (String) session.getAttribute("adminMsg");
                String adminError = (String) session.getAttribute("adminError");
                if (adminMsg != null) { session.removeAttribute("adminMsg");
            %>
                <div class="alert alert-success"><%= adminMsg %></div>
            <% } if (adminError != null) { session.removeAttribute("adminError"); %>
                <div class="alert alert-error"><%= adminError %></div>
            <% } %>

            <div class="alert alert-info">
                Welcome back, <strong><jsp:getProperty name="profile" property="fullName" /></strong>.
                <% if ("System".equals(profile.getAdminLevel())) { %>
                    You have system-wide access.
                <% } else { %>
                    You are managing Clinic ID: <jsp:getProperty name="profile" property="clinicId" />.
                <% } %>
            </div>

            <jsp:include page="/WEB-INF/views/components/notifications.jsp" />

            <%-- Quick Links --%>
            <div class="stat-grid">
                <a href="<%= ctx %>/admin/reports" class="stat-card quick-action-card">
                    <div class="quick-action-title">Reports</div>
                    <div class="quick-action-sub">Review appointment records and clinic activity.</div>
                </a>
                <a href="<%= ctx %>/admin/reports?tab=utilisation" class="stat-card quick-action-card">
                    <div class="quick-action-title">Utilisation Rate</div>
                    <div class="quick-action-sub">Compare booked slots against offered capacity.</div>
                </a>
                <a href="<%= ctx %>/admin/reports?tab=noshow" class="stat-card quick-action-card">
                    <div class="quick-action-title">No-show Summary</div>
                    <div class="quick-action-sub">Track missed appointments by clinic and service.</div>
                </a>
            </div>

            <%-- Summary statistics --%>
            <div class="stat-grid">
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("todayCount") %></div>
                    <div class="stat-label">Today Appointments</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("totalAppointments") %></div>
                    <div class="stat-label">Total Appointments</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("booked") %></div>
                    <div class="stat-label">Total Booked</div>
                </div>
                <div class="stat-card highlight">
                    <div class="stat-number"><%= request.getAttribute("completed") %></div>
                    <div class="stat-label">Total Completed</div>
                </div>
                <div class="stat-card warning">
                    <div class="stat-number"><%= request.getAttribute("noshow") %></div>
                    <div class="stat-label">Total No-shows</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("cancelled") %></div>
                    <div class="stat-label">Total Cancelled</div>
                </div>
            </div>

            <% if ("System".equals(profile.getAdminLevel())) { %>
            <div class="stat-grid">
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("totalPatients") %></div>
                    <div class="stat-label">Patients</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("totalStaff") %></div>
                    <div class="stat-label">Staff</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("totalClinics") %></div>
                    <div class="stat-label">Clinics</div>
                </div>
            </div>
            <% } %>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
