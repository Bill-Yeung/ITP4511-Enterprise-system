<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ServiceBean"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Services &mdash; CCHC Clinic System</title>
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
                <h1>Manage Services</h1>
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
                ArrayList<ServiceBean> services = (ArrayList<ServiceBean>) request.getAttribute("services");
                if (services == null) {
                    services = new ArrayList<ServiceBean>();
                }
            %>

            <div style="margin-bottom:16px">
                <a class="btn btn-success btn-compact" href="<%= ctx %>/admin/services?action=edit">+ New Service</a>
            </div>

            <div class="card">
                <div class="card-header">
                    Services
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= services.size() %> service<%= services.size() == 1 ? "" : "s" %>
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Description</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (services.isEmpty()) { %>
                            <tr><td colspan="4" style="text-align:center; padding:24px; color:var(--gray-500)">No services defined.</td></tr>
                        <% } else for (ServiceBean s : services) { %>
                            <tr>
                                <td><%= s.getServiceId() %></td>
                                <td><%= s.getName() %></td>
                                <td><%= s.getDescription() == null ? "" : s.getDescription() %></td>
                                <td class="table-actions">
                                    <a class="btn btn-sm btn-outline clinic-action-btn"
                                    href="<%= ctx %>/admin/services?action=edit&id=<%= s.getServiceId() %>">Edit</a>
                                    <form id="deleteServiceForm<%= s.getServiceId() %>" method="post" action="<%= ctx %>/admin/services" style="display:inline">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="id" value="<%= s.getServiceId() %>" />
                                    </form>
                                    <button type="button" class="btn btn-sm btn-danger clinic-action-btn"
                                            data-confirm-submit="deleteServiceForm<%= s.getServiceId() %>"
                                            data-confirm-title="Delete Service"
                                            data-confirm-message="Delete service <%= s.getName() %>? Clinic mappings will block this if it is in use."
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
