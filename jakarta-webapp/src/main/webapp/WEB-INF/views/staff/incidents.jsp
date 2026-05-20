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
    <title>Report Incident &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/header.jsp" />
<div id="page-wrapper">
    <jsp:include page="/WEB-INF/views/menu.jsp" />
    <main id="main-content">

        <div class="page-header">
            <h1>Operational Incidents</h1>
        </div>

        <%
            String staffMsg = (String) session.getAttribute("staffMsg");
            String staffError = (String) session.getAttribute("staffError");
            if (staffMsg != null) { session.removeAttribute("staffMsg"); %>
            <div class="alert alert-success"><%= staffMsg %></div>
        <% } if (staffError != null) { session.removeAttribute("staffError"); %>
            <div class="alert alert-error"><%= staffError %></div>
        <% } %>

        <%-- Submission form --%>
        <%
            Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
            Integer viewClinicId = (Integer) request.getAttribute("viewClinicId");
        %>
        <div class="card" style="margin-bottom:16px">
            <div class="card-header">Report a New Incident</div>
            <div class="card-body">
                <form method="post" action="<%= ctx %>/staff/incidents">
                    <div class="filter-bar">
                        <% if (isAdmin != null && isAdmin) { %>
                            <%
                                ArrayList<ClinicBean> allClinics =
                                    (ArrayList<ClinicBean>) request.getAttribute("allClinics");
                            %>
                            <div class="form-group">
                                <label>Clinic</label>
                                <select name="clinic" class="form-control">
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
                            <label>Category</label>
                            <select name="category" class="form-control">
                                <option value="Equipment">Equipment</option>
                                <option value="Patient">Patient</option>
                                <option value="Safety">Safety</option>
                                <option value="IT">IT</option>
                                <option value="Other" selected>Other</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Severity</label>
                            <select name="severity" class="form-control">
                                <option value="Low" selected>Low</option>
                                <option value="Medium">Medium</option>
                                <option value="High">High</option>
                                <option value="Critical">Critical</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Patient ID <small style="color:var(--gray-500)">(optional)</small></label>
                            <input type="number" name="patientId" class="form-control" min="1" placeholder="e.g. 8" />
                        </div>
                        <div class="form-group">
                            <label>Appointment ID <small style="color:var(--gray-500)">(optional)</small></label>
                            <input type="number" name="appointmentId" class="form-control" min="1" placeholder="e.g. 23" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Description *</label>
                        <textarea name="description" class="form-control" rows="3" required
                                  placeholder="Describe the incident..."></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">Submit Incident</button>
                </form>
            </div>
        </div>

        <%-- Listing --%>
        <%
            ArrayList<IncidentBean> incidents = (ArrayList<IncidentBean>) request.getAttribute("incidents");
            if (incidents == null) incidents = new ArrayList<IncidentBean>();
        %>
        <div class="card">
            <div class="card-header">
                Reported Incidents
                <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                    <%= incidents.size() %> report<%= incidents.size() == 1 ? "" : "s" %>
                </span>
            </div>
            <div class="card-body" style="padding:0; overflow-x:auto">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>#</th><th>When</th><th>Category</th><th>Severity</th>
                            <th>Patient</th><th>Description</th><th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (incidents.isEmpty()) { %>
                        <tr><td colspan="7" style="text-align:center; padding:24px; color:var(--gray-500)">
                            No incidents reported yet.
                        </td></tr>
                    <% } else for (IncidentBean inc : incidents) { %>
                        <tr>
                            <td><%= inc.getIncidentId() %></td>
                            <td><%= inc.getCreatedAt() %></td>
                            <td><%= inc.getCategory() %></td>
                            <td><%= inc.getSeverity() %></td>
                            <td><%= inc.getPatientName() == null ? "-" : inc.getPatientName() %></td>
                            <td style="white-space:pre-wrap; max-width:340px"><%= inc.getDescription() %></td>
                            <td>
                                <% if ("Resolved".equals(inc.getStatus())) { %>
                                    <span class="badge badge-completed">Resolved</span>
                                <% } else if ("InProgress".equals(inc.getStatus())) { %>
                                    <span class="badge badge-arrived">In Progress</span>
                                <% } else { %>
                                    <span class="badge badge-cancelled">Open</span>
                                <% } %>
                            </td>
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
