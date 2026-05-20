<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Dashboard &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">

        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <%
                String role = (String) session.getAttribute("role");
                String fullName = (String) session.getAttribute("fullName");
                Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
                Integer viewClinicId = (Integer) request.getAttribute("viewClinicId");
            %>

            <div class="page-header">
                <h1>Staff Dashboard</h1>
                <span class="badge badge-booked"><%= role %></span>
            </div>

            <%
                String staffMsg = (String) session.getAttribute("staffMsg");
                String staffError = (String) session.getAttribute("staffError");
                if (staffMsg != null) {
                    session.removeAttribute("staffMsg");
            %>
                <div class="alert alert-success"><%= staffMsg %></div>
            <% } if (staffError != null) { session.removeAttribute("staffError"); %>
                <div class="alert alert-error"><%= staffError %></div>
            <% } %>

            <div class="alert alert-info">
                Today's overview for <strong><%= fullName %></strong>
                &mdash; Clinic ID: <%= viewClinicId %>
            </div>

            <% if (isAdmin != null && isAdmin) { %>
                <%
                    ArrayList<ClinicBean> allClinics = (ArrayList<ClinicBean>) request.getAttribute("allClinics");
                %>
                <form class="filter-bar" method="get" action="<%= ctx %>/staff/dashboard">
                    <div class="form-group">
                        <label for="clinic">Clinic</label>
                        <select id="clinic" name="clinic" onchange="this.form.submit()">
                            <% if (allClinics != null) {
                                for (ClinicBean c : allClinics) { %>
                                    <option value="<%= c.getClinicId() %>"
                                        <%= (viewClinicId != null && c.getClinicId() == viewClinicId) ? "selected" : "" %>>
                                        <%= c.getName() %>
                                    </option>
                            <%  } } %>
                        </select>
                    </div>
                </form>
            <% } %>

            <jsp:include page="/WEB-INF/views/components/notifications.jsp" />

            <div class="stat-grid">
                <a href="<%= ctx %>/staff/appointments<%= (isAdmin != null && isAdmin && viewClinicId != null) ? "?clinic=" + viewClinicId : "" %>"
                class="stat-card quick-action-card">
                    <div class="quick-action-title">Appointments</div>
                    <div class="quick-action-sub">Review and update clinic appointments.</div>
                </a>
                <a href="<%= ctx %>/staff/checkin<%= (isAdmin != null && isAdmin && viewClinicId != null) ? "?clinic=" + viewClinicId : "" %>"
                class="stat-card quick-action-card">
                    <div class="quick-action-title">Check-in</div>
                    <div class="quick-action-sub">Handle arrivals and appointment attendance.</div>
                </a>
                <a href="<%= ctx %>/staff/queue<%= (isAdmin != null && isAdmin && viewClinicId != null) ? "?clinic=" + viewClinicId : "" %>"
                class="stat-card quick-action-card">
                    <div class="quick-action-title">Queue Board</div>
                    <div class="quick-action-sub">Call, skip, and serve walk-in patients.</div>
                </a>
            </div>

            <%-- Summary statistics --%>
            <div class="stat-grid">
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("totalToday") %></div>
                    <div class="stat-label">Today Appointments</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("booked") %></div>
                    <div class="stat-label">Pending</div>
                </div>
                <div class="stat-card highlight">
                    <div class="stat-number"><%= request.getAttribute("arrived") %></div>
                    <div class="stat-label">Checked In</div>
                </div>
                <div class="stat-card highlight">
                    <div class="stat-number"><%= request.getAttribute("completed") %></div>
                    <div class="stat-label">Completed</div>
                </div>
                <div class="stat-card warning">
                    <div class="stat-number"><%= request.getAttribute("noshow") %></div>
                    <div class="stat-label">No-shows</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= request.getAttribute("queueWaiting") %></div>
                    <div class="stat-label">Queue Active</div>
                </div>
            </div>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
