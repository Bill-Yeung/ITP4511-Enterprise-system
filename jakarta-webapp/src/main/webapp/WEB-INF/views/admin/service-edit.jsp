<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ServiceBean"%>
<%
    String ctx = request.getContextPath();
    ServiceBean service = (ServiceBean) request.getAttribute("service");
    boolean isEdit = service != null;
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isEdit ? "Edit Service" : "New Service" %> &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">

        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <div class="page-header">
                <h1><%= isEdit ? "Edit Service" : "New Service" %></h1>
                <a class="btn btn-secondary" href="<%= ctx %>/admin/services">Back</a>
            </div>

            <%
                String adminError = (String) session.getAttribute("adminError");
                if (adminError != null) { session.removeAttribute("adminError"); %>
                <div class="alert alert-error"><%= adminError %></div>
            <% } %>

            <form id="serviceSaveForm" method="post" action="<%= ctx %>/admin/services" class="card">
                <input type="hidden" name="action" value="save" />
                <% if (isEdit) { %>
                    <input type="hidden" name="id" value="<%= service.getServiceId() %>" />
                <% } %>
                <div class="card-body">
                    <div class="form-group">
                        <label>Name *</label>
                        <input type="text" name="name" class="form-control" required
                            value="<%= isEdit ? service.getName() : "" %>" />
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" class="form-control" rows="4"><%= isEdit && service.getDescription() != null ? service.getDescription() : "" %></textarea>
                    </div>
                    <div style="margin-top:16px; display:flex; gap:12px">
                        <button type="button" class="btn btn-primary"
                                data-confirm-submit="serviceSaveForm"
                                data-confirm-title="<%= isEdit ? "Save Service" : "Create Service" %>"
                                data-confirm-message="<%= isEdit ? "Save changes to this service?" : "Create this service?" %>">
                            <%= isEdit ? "Save Changes" : "Create Service" %>
                        </button>
                        <a href="<%= ctx %>/admin/services" class="btn btn-secondary">Cancel</a>
                    </div>
                </div>
            </form>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />
    <script src="<%= ctx %>/js/confirm-modal.js"></script>
    
</body>
</html>
