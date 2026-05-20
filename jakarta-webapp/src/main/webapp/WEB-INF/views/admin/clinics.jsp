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
    <title>Manage Clinics &mdash; CCHC Clinic System</title>
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
                <h1>Manage Clinics</h1>
                <span class="badge badge-booked">
                    <jsp:getProperty name="profile" property="adminLevel" /> Admin
                </span>
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
                ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
                if (clinics == null) {
                    clinics = new ArrayList<ClinicBean>();
                }
            %>

            <div style="margin-bottom:16px">
                <a class="btn btn-success btn-compact" href="<%= ctx %>/admin/clinics?action=edit">+ New Clinic</a>
            </div>

            <div class="card">
                <div class="card-header">
                    Clinics
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= clinics.size() %> clinic<%= clinics.size() == 1 ? "" : "s" %>
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Location</th>
                                <th>Walk-in Queue</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (clinics.isEmpty()) { %>
                            <tr><td colspan="5" style="text-align:center; padding:24px; color:var(--gray-500)">No clinics defined.</td></tr>
                        <% } else for (ClinicBean c : clinics) { %>
                            <tr>
                                <td><%= c.getClinicId() %></td>
                                <td><%= c.getName() %></td>
                                <td><%= c.getLocation() == null ? "" : c.getLocation() %></td>
                                <td>
                                    <% if (c.isQueueEnabled()) { %>
                                        <span class="badge badge-completed">Enabled</span>
                                    <% } else { %>
                                        <span class="badge badge-cancelled">Disabled</span>
                                    <% } %>
                                    <form id="queueToggleForm<%= c.getClinicId() %>" method="post" action="<%= ctx %>/admin/clinics" style="display:inline">
                                        <input type="hidden" name="action" value="toggleQueue" />
                                        <input type="hidden" name="id" value="<%= c.getClinicId() %>" />
                                        <input type="hidden" name="enabled" value="<%= !c.isQueueEnabled() %>" />
                                    </form>
                                    <button type="button" class="btn btn-sm btn-outline clinic-action-btn"
                                            data-confirm-submit="queueToggleForm<%= c.getClinicId() %>"
                                            data-confirm-title="<%= c.isQueueEnabled() ? "Disable" : "Enable" %> Walk-in Queue"
                                            data-confirm-message="<%= c.isQueueEnabled() ? "Disable" : "Enable" %> walk-in queue for <%= c.getName() %>?"
                                            data-confirm-danger="<%= c.isQueueEnabled() %>">
                                            <%= c.isQueueEnabled() ? "Disable" : "Enable" %>
                                    </button>
                                </td>
                                <td>
                                    <a class="btn btn-sm btn-outline clinic-action-btn"
                                    href="<%= ctx %>/admin/clinics?action=edit&id=<%= c.getClinicId() %>">Edit</a>
                                    <a class="btn btn-sm btn-outline clinic-action-btn"
                                    href="<%= ctx %>/admin/clinics?action=services&clinicId=<%= c.getClinicId() %>">Services</a>
                                    <a class="btn btn-sm btn-outline clinic-action-btn"
                                    href="<%= ctx %>/admin/clinics?action=hours&clinicId=<%= c.getClinicId() %>">Hours</a>
                                    <form id="deleteClinicForm<%= c.getClinicId() %>" method="post" action="<%= ctx %>/admin/clinics" style="display:inline">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="id" value="<%= c.getClinicId() %>" />
                                    </form>
                                    <button type="button" class="btn btn-sm btn-danger clinic-action-btn"
                                            data-confirm-submit="deleteClinicForm<%= c.getClinicId() %>"
                                            data-confirm-title="Delete Clinic"
                                            data-confirm-message="Delete clinic <%= c.getName() %>? Related rows will block this if any exist."
                                            data-confirm-danger="true">Delete</button>
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
    <script src="<%= ctx %>/js/confirm-modal.js"></script>
    
</body>
</html>
