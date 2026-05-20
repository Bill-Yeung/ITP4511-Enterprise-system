<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body class="auth-page">

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div class="auth-split">

        <div class="auth-panel">
            <h2 class="auth-brand-title">Community Care Health Consortium</h2>
            <p class="auth-brand-sub">Appointment &amp; Queue Management System</p>
            <ul class="auth-features">
                <li>Book appointments at any of our 5 clinics</li>
                <li>Join and track walk-in queues in real time</li>
                <li>Receive notifications about your visits</li>
                <li>Manage your profile and medical records</li>
            </ul>
        </div>

        <div class="auth-form-panel">
            <div class="auth-form-inner auth-form-inner--narrow">

                <div class="auth-form-header">
                    <h1>Welcome Back</h1>
                    <p>Sign in to your account to continue.</p>
                </div>

                <!-- Display error messages -->
                <% if (request.getAttribute("errorMsg") != null) { %>
                    <div class="alert alert-error">
                        <%= request.getAttribute("errorMsg") %>
                    </div>
                <% } %>

                <!-- Case: Redirect after registration -->
                <% if ("true".equals(request.getParameter("registered"))) { %>
                    <div class="alert alert-success">
                        Account created and email verified. You can now log in.
                    </div>
                <% } %>

                <!-- Case: Redirect after password reset -->
                <% if ("true".equals(request.getParameter("passwordReset"))) { %>
                    <div class="alert alert-success">
                        Password reset successfully. Please log in with your new password.
                    </div>
                <% } %>

                <form action="<%= ctx %>/login" method="post">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username"
                            value="<%= request.getParameter("username") != null
                                        ? request.getParameter("username") : "" %>"
                            required autofocus placeholder="Enter your username">
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password"
                            required placeholder="Enter your password">
                    </div>
                    <div class="form-group" style="margin-top: 24px">
                        <button type="submit" class="btn btn-primary btn-block">Sign In</button>
                    </div>
                </form>

                <div class="auth-footer">
                    <p><a href="<%= ctx %>/forgot-password">Forgot password?</a></p>
                    <p style="margin-top: 8px">
                        New patient?
                        <a href="<%= ctx %>/register">Create an account</a>
                    </p>
                </div>

            </div>
        </div>

    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
