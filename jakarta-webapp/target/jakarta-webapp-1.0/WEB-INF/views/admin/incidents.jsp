<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.IncidentBean"%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Incident Log &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/header.jsp" />
<div id="page-wrapper">
    <jsp:include page="/WEB-INF/views/menu.jsp" />
    <main id="main-content">

        <%
            ArrayList<IncidentBean> incidents = (ArrayList<IncidentBean>) request.getAttribute("incidents");
            ArrayList<String[]> summary = (ArrayList<String[]>) request.getAttribute("summary");
            ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
            Integer filterClinicId = (Integer) request.getAttribute("filterClinicId");
            String filterStatus    = (String) request.getAttribute("filterStatus");
            String filterCategory  = (String) request.getAttribute("filterCategory");
            Boolean isClinicAdmin = (Boolean) request.getAttribute("isClinicAdmin");
            if (incidents == null) incidents = new ArrayList<IncidentBean>();
            if (summary == null) summary = new ArrayList<String[]>();
            if (clinics == null) clinics = new ArrayList<ClinicBean>();
        %>

        <div class="page-header">
            <h1>Incident Log</h1>
        </div>

        <%
            String adminMsg = (String) session.getAttribute("adminMsg");
            String adminError = (String) session.getAttribute("adminError");
            if (adminMsg != null) { session.removeAttribute("adminMsg"); %>
            <div class="alert alert-success"><%= adminMsg %></div>
        <% } if (adminError != null) { session.removeAttribute("adminError"); %>
            <div class="alert alert-error"><%= adminError %></div>
        <% } %>

        <%-- Filter bar --%>
        <form class="card" method="get" action="<%= ctx %>/admin/incidents"
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
                        <label>Status</label>
                        <select name="status" class="form-control">
                            <option value="">All</option>
                            <option value="Open"        <%= "Open".equals(filterStatus) ? "selected" : "" %>>Open</option>
                            <option value="InProgress"  <%= "InProgress".equals(filterStatus) ? "selected" : "" %>>In Progress</option>
                            <option value="Resolved"    <%= "Resolved".equals(filterStatus) ? "selected" : "" %>>Resolved</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Category</label>
                        <select name="category" class="form-control">
                            <option value="">All</option>
                            <% String[] cats = {"Equipment","Patient","Safety","IT","Other"};
                               for (String cc : cats) { %>
                                <option value="<%= cc %>" <%= cc.equals(filterCategory) ? "selected" : "" %>><%= cc %></option>
                            <% } %>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary">Filter</button>
                    <a href="<%= ctx %>/admin/incidents" class="btn btn-outline">Reset</a>
                </div>
            </div>
        </form>

        <div class="card" style="margin-bottom:16px">
            <div class="card-header">
                Patient Incident Summary
                <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                    <%= summary.size() %> patient<%= summary.size() == 1 ? "" : "s" %>
                </span>
            </div>
            <div class="card-body" style="padding:0; overflow-x:auto">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Patient</th>
                            <th>Total Incidents</th>
                            <th>Open</th>
                            <th>Resolved</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (summary.isEmpty()) { %>
                        <tr><td colspan="4" style="text-align:center; padding:24px; color:var(--gray-500)">
                            No patient-linked incidents recorded yet.
                        </td></tr>
                    <% } else for (String[] row : summary) { %>
                        <tr>
                            <td><%= row[1] %> <small style="color:var(--gray-500)">#<%= row[0] %></small></td>
                            <td><%= row[2] %></td>
                            <td><%= row[3] %></td>
                            <td><%= row[4] %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                Incidents
                <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                    <%= incidents.size() %> result<%= incidents.size() == 1 ? "" : "s" %>
                </span>
            </div>
            <div class="card-body" style="padding:0; overflow-x:auto">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>#</th><th>When</th><th>Clinic</th><th>Reporter</th>
                            <th>Category</th><th>Severity</th><th>Patient</th>
                            <th>Description</th><th>Status / Resolution</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (incidents.isEmpty()) { %>
                        <tr><td colspan="9" style="text-align:center; padding:24px; color:var(--gray-500)">
                            No incidents match the current filter.
                        </td></tr>
                    <% } else for (IncidentBean inc : incidents) { %>
                        <tr>
                            <form method="post" action="<%= ctx %>/admin/incidents">
                                <input type="hidden" name="incidentId" value="<%= inc.getIncidentId() %>" />
                                <td><%= inc.getIncidentId() %></td>
                                <td><%= inc.getCreatedAt() %></td>
                                <td><%= inc.getClinicName() %></td>
                                <td><%= inc.getReportedByName() %></td>
                                <td><%= inc.getCategory() %></td>
                                <td><%= inc.getSeverity() %></td>
                                <td>
                                    <%= inc.getPatientName() == null ? "-" : inc.getPatientName() %>
                                    <% if (inc.getAppointmentId() != null) { %>
                                        <br><small style="color:var(--gray-500)">Appt #<%= inc.getAppointmentId() %></small>
                                    <% } %>
                                </td>
                                <td style="white-space:pre-wrap; max-width:280px"><%= inc.getDescription() %></td>
                                <td style="min-width:280px">
                                    <select name="status" class="form-control" style="margin-bottom:6px">
                                        <option value="Open"       <%= "Open".equals(inc.getStatus()) ? "selected" : "" %>>Open</option>
                                        <option value="InProgress" <%= "InProgress".equals(inc.getStatus()) ? "selected" : "" %>>In Progress</option>
                                        <option value="Resolved"   <%= "Resolved".equals(inc.getStatus()) ? "selected" : "" %>>Resolved</option>
                                    </select>
                                    <textarea name="resolutionNotes" class="form-control" rows="2"
                                              placeholder="Resolution notes..."><%= inc.getResolutionNotes() == null ? "" : inc.getResolutionNotes() %></textarea>
                                    <button type="submit" class="btn btn-sm btn-primary" style="margin-top:6px">Save</button>
                                </td>
                            </form>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>

    </main>
</div>
<jsp:include page="/WEB-INF/views/footer.jsp" />
</body>
</html>
