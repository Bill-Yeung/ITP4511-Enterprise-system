<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Clinic &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">

        <jsp:include page="/WEB-INF/views/menu.jsp" />
        
        <main id="main-content">

            <%
                ClinicBean c = (ClinicBean) request.getAttribute("clinic");
                boolean isEdit = (c != null);
            %>

            <div class="page-header">
                <h1><%= isEdit ? "Edit Clinic" : "New Clinic" %></h1>
                <a class="btn btn-secondary" href="<%= ctx %>/admin/clinics">Back</a>
            </div>

            <%
                String adminError = (String) session.getAttribute("adminError");
                if (adminError != null) { session.removeAttribute("adminError"); %>
                <div class="alert alert-error"><%= adminError %></div>
            <% } %>

            <form id="clinicSaveForm" method="post" action="<%= ctx %>/admin/clinics" class="card">
                <input type="hidden" name="action" value="save" />
                <% if (isEdit) { %>
                    <input type="hidden" name="id" value="<%= c.getClinicId() %>" />
                <% } %>
                <div class="card-body">
                    <div class="form-group">
                        <label>Name *</label>
                        <input type="text" name="name" class="form-control" required
                            value="<%= isEdit ? c.getName() : "" %>" />
                    </div>
                    <div class="form-group">
                        <label>Location / District</label>
                        <input type="text" name="location" class="form-control"
                            value="<%= isEdit && c.getLocation() != null ? c.getLocation() : "" %>" />
                    </div>
                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="queueEnabled"
                                <%= isEdit && c.isQueueEnabled() ? "checked" : "" %> />
                            Enable walk-in queue at this clinic
                        </label>
                    </div>
                    <div style="margin-top:16px; display:flex; gap:12px">
                        <button type="button" class="btn btn-primary"
                                data-confirm-submit="clinicSaveForm"
                                data-confirm-title="<%= isEdit ? "Save Clinic" : "Create Clinic" %>"
                                data-confirm-message="<%= isEdit ? "Save changes to this clinic?" : "Create this clinic?" %>">
                            <%= isEdit ? "Save Changes" : "Create Clinic" %>
                        </button>
                        <a href="<%= ctx %>/admin/clinics" class="btn btn-secondary">Cancel</a>
                    </div>
                </div>
            </form>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />
    <script src="<%= ctx %>/js/confirm-modal.js"></script>

</body>
</html>
