<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.SettingBean"%>
<%@ taglib prefix="clinic" uri="/WEB-INF/tlds/clinic-taglib.tld" %>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Policy Settings &mdash; CCHC Clinic System</title>
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
                <h1>Policy Settings</h1>
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
                Configure system-wide policies below. Changes take effect immediately
                and are logged in the audit trail.
            </div>

            <%
                ArrayList<SettingBean> settings = (ArrayList<SettingBean>) request.getAttribute("settings");
            %>

            <form id="settingsForm" method="post" action="<%= ctx %>/admin/settings">
                <div class="card">
                    <div class="card-header">
                        System Settings
                        <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                            <%= settings != null ? settings.size() : 0 %> settings
                        </span>
                    </div>
                    <div class="card-body" style="padding:0; overflow-x:auto">
                        <%-- Custom Tag: clinic:settingsForm renders editable settings table --%>
                        <clinic:settingsForm settings="<%= settings %>" />
                    </div>
                </div>

                <div style="margin-top:16px; display:flex; gap:12px">
                    <button type="button" class="btn btn-primary"
                            data-confirm-submit="settingsForm"
                            data-confirm-title="Save Settings"
                            data-confirm-message="Save changes to system policy settings?">Save Changes</button>
                    <a href="<%= ctx %>/admin/dashboard" class="btn btn-secondary">Cancel</a>
                </div>
            </form>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />
    <script src="<%= ctx %>/js/confirm-modal.js"></script>

</body>
</html>
