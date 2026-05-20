<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.UserBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Users &mdash; CCHC Clinic System</title>
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
            <h1>Manage Users</h1>
            <span class="badge badge-booked">
                <jsp:getProperty name="profile" property="adminLevel" /> Admin
            </span>
        </div>

        <%-- Flash messages --%>
        <%
            String adminMsg = (String) session.getAttribute("adminMsg");
            String adminError = (String) session.getAttribute("adminError");
            if (adminMsg != null) { session.removeAttribute("adminMsg");
        %>
            <div class="alert alert-success"><%= adminMsg %></div>
        <% } if (adminError != null) { session.removeAttribute("adminError"); %>
            <div class="alert alert-error"><%= adminError %></div>
        <% } %>

        <%
            ArrayList<UserBean> users = (ArrayList<UserBean>) request.getAttribute("users");
            ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
            String filterRole = (String) request.getAttribute("filterRole");
            String filterClinicId = (String) request.getAttribute("filterClinicId");
            String filterSearch = (String) request.getAttribute("filterSearch");
            if (users == null) users = new ArrayList<UserBean>();
            if (clinics == null) clinics = new ArrayList<ClinicBean>();
        %>

        <%-- Filter bar --%>
        <form method="get" action="<%= ctx %>/admin/users" class="card" style="margin-bottom:16px">
            <div class="card-body">
                <div class="filter-bar">
                    <div class="form-group">
                        <label>Role</label>
                        <select name="role" class="form-control">
                            <option value="" <%= filterRole.isEmpty() ? "selected" : "" %>>All</option>
                            <option value="Patient" <%= "Patient".equals(filterRole) ? "selected" : "" %>>Patient</option>
                            <option value="Staff"   <%= "Staff".equals(filterRole) ? "selected" : "" %>>Staff</option>
                            <option value="Admin"   <%= "Admin".equals(filterRole) ? "selected" : "" %>>Admin</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Clinic</label>
                        <select name="clinicId" class="form-control">
                            <option value="">All</option>
                            <% for (ClinicBean c : clinics) { %>
                                <option value="<%= c.getClinicId() %>"
                                    <%= String.valueOf(c.getClinicId()).equals(filterClinicId) ? "selected" : "" %>>
                                    <%= c.getName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Search</label>
                        <input type="text" name="search" class="form-control"
                               value="<%= filterSearch %>"
                               placeholder="username, name, or email" />
                    </div>
                    <button type="submit" class="btn btn-primary">Filter</button>
                    <a class="btn btn-success" href="<%= ctx %>/admin/users?action=edit">New User</a>
                </div>
            </div>
        </form>

        <%-- User list --%>
        <div class="card">
            <div class="card-header">
                Users
                <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                    <%= users.size() %> result<%= users.size() == 1 ? "" : "s" %>
                </span>
            </div>
            <div class="card-body" style="padding:0; overflow-x:auto">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Username</th>
                            <th>Full Name</th>
                            <th>Role</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Created</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (users.isEmpty()) { %>
                            <tr><td colspan="8" style="text-align:center; padding:24px; color:var(--gray-500)">No users found.</td></tr>
                        <% } else for (UserBean u : users) { %>
                            <tr>
                                <td><%= u.getUserId() %></td>
                                <td><%= u.getUsername() %></td>
                                <td><%= u.getFullName() %></td>
                                <td>
                                    <% if ("Patient".equals(u.getRole())) { %>
                                        <span class="badge badge-info">Patient</span>
                                    <% } else if ("Staff".equals(u.getRole())) { %>
                                        <span class="badge badge-booked">Staff</span>
                                    <% } else { %>
                                        <span class="badge badge-completed">Admin</span>
                                    <% } %>
                                </td>
                                <td><%= u.getEmail() == null ? "" : u.getEmail() %></td>
                                <td><%= u.getPhone() == null ? "" : u.getPhone() %></td>
                                <td><%= u.getCreatedAt() == null ? "" : u.getCreatedAt() %></td>
                                <td>
                                    <a class="btn btn-sm btn-outline"
                                       href="<%= ctx %>/admin/users?action=edit&id=<%= u.getUserId() %>">Edit</a>
                                    <form method="post" action="<%= ctx %>/admin/users"
                                          style="display:inline"
                                          onsubmit="return confirm('Delete user <%= u.getUsername() %>? This cannot be undone.');">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="id" value="<%= u.getUserId() %>" />
                                        <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                                    </form>
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
