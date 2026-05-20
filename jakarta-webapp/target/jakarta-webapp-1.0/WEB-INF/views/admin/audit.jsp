<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.AuditLogBean"%>
<%@ taglib prefix="clinic" uri="/WEB-INF/tlds/clinic-taglib.tld" %>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Audit Log &mdash; CCHC Clinic System</title>
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
                <h1>Audit Log</h1>
                <span class="badge badge-booked">
                    <jsp:getProperty name="profile" property="adminLevel" /> Admin
                </span>
            </div>

            <%
                ArrayList<AuditLogBean> logs = (ArrayList<AuditLogBean>) request.getAttribute("auditLogs");
                String selectedRole = (String) request.getAttribute("selectedRole");
                String selectedSort = (String) request.getAttribute("selectedSort");
                Boolean isClinicAdmin = (Boolean) request.getAttribute("isClinicAdmin");
                if (selectedRole == null) {
                    selectedRole = "";
                }
                if (selectedSort == null) {
                    selectedSort = "id_asc";
                }
            %>

            <form class="filter-bar" method="get" action="<%= ctx %>/admin/audit">
                <div class="form-group">
                    <label for="role">Role</label>
                    <select id="role" name="role">
                        <option value="" <%= "".equals(selectedRole) ? "selected" : "" %>>All roles</option>
                        <option value="Admin" <%= "Admin".equals(selectedRole) ? "selected" : "" %>>Admin</option>
                        <option value="Staff" <%= "Staff".equals(selectedRole) ? "selected" : "" %>>Staff</option>
                        <% if (isClinicAdmin == null || !isClinicAdmin) { %>
                            <option value="Patient" <%= "Patient".equals(selectedRole) ? "selected" : "" %>>Patient</option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="sort">Sort By</label>
                    <select id="sort" name="sort">
                        <option value="id_asc" <%= "id_asc".equals(selectedSort) ? "selected" : "" %>>ID low to high</option>
                        <option value="id_desc" <%= "id_desc".equals(selectedSort) ? "selected" : "" %>>ID high to low</option>
                        <option value="time_desc" <%= "time_desc".equals(selectedSort) ? "selected" : "" %>>Newest first</option>
                        <option value="time_asc" <%= "time_asc".equals(selectedSort) ? "selected" : "" %>>Oldest first</option>
                        <option value="user_asc" <%= "user_asc".equals(selectedSort) ? "selected" : "" %>>User name</option>
                        <option value="role_asc" <%= "role_asc".equals(selectedSort) ? "selected" : "" %>>Role</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Apply</button>
                <a href="<%= ctx %>/admin/audit" class="btn btn-outline">Reset</a>
            </form>

            <div class="card">
                <div class="card-header">
                    <%= (isClinicAdmin != null && isClinicAdmin) ? "Clinic Staff & Admin Actions" : "All Staff & Admin Actions" %>
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= logs != null ? logs.size() : 0 %> records
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <clinic:auditLogTable logs="<%= logs %>" />
                </div>
            </div>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
