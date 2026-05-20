<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.UserBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.util.HtmlUtil"%>
<%
    String ctx = request.getContextPath();
    UserBean accountProfile = (UserBean) request.getAttribute("accountProfile");
    String formAction = ctx + "/account/profile";
    String roleLabel = (String) request.getAttribute("roleLabel");
    String roleValue = (String) request.getAttribute("roleValue");
    Object clinicValue = request.getAttribute("clinicValue");
    String errorMsg = (String) request.getAttribute("errorMsg");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Profile &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">
        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <div class="page-header">
                <h1>My Profile</h1>
            </div>

            <% if ("true".equals(request.getParameter("updated"))) { %>
                <div class="alert alert-success">Profile updated successfully.</div>
            <% } %>
            <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(errorMsg) %></div>
            <% } %>

            <form method="post" action="<%= formAction %>">
                <div class="card">
                    <div class="card-header">Account Information</div>
                    <div class="card-body">
                        <div class="form-row">
                            <div class="form-group">
                                <label>Username</label>
                                <input type="text" value="<%= accountProfile.getUsername() %>"
                                    disabled class="input-readonly" />
                            </div>
                            <div class="form-group">
                                <label><%= roleLabel %></label>
                                <input type="text" value="<%= roleValue %>"
                                    disabled class="input-readonly" />
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>Clinic ID</label>
                                <input type="text" value="<%= clinicValue %>"
                                    disabled class="input-readonly" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">Personal Details</div>
                    <div class="card-body">
                        <div class="form-group">
                            <label for="fullName">Full Name <span class="req">*</span></label>
                            <input type="text" id="fullName" name="fullName" required maxlength="100"
                                value="<%= accountProfile.getFullName() %>" />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="email">Email Address <span class="req">*</span></label>
                                <input type="email" id="email" name="email" required maxlength="100"
                                    value="<%= accountProfile.getEmail() %>" />
                            </div>
                            <div class="form-group">
                                <label for="phone">Phone Number</label>
                                <input type="tel" id="phone" name="phone" maxlength="20"
                                    value="<%= accountProfile.getPhone() == null ? "" : accountProfile.getPhone() %>" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-actions form-actions--right">
                    <button type="submit" class="btn btn-primary">Save Changes</button>
                </div>
            </form>

            <form method="post" action="<%= formAction %>">
                <div class="card" style="margin-top:16px">
                    <div class="card-header">Change Password</div>
                    <div class="card-body">
                        <div class="form-group">
                            <label for="currentPassword">Current Password <span class="req">*</span></label>
                            <input type="password" id="currentPassword" name="currentPassword"
                                class="form-control" required autocomplete="current-password" />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="newPassword">New Password <span class="req">*</span></label>
                                <input type="password" id="newPassword" name="newPassword"
                                    class="form-control" required minlength="8"
                                    autocomplete="new-password" />
                            </div>
                            <div class="form-group">
                                <label for="confirmPassword">Confirm New Password <span class="req">*</span></label>
                                <input type="password" id="confirmPassword" name="confirmPassword"
                                    class="form-control" required autocomplete="new-password" />
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-actions form-actions--right">
                    <button type="submit" class="btn btn-primary">Change Password</button>
                </div>
            </form>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
