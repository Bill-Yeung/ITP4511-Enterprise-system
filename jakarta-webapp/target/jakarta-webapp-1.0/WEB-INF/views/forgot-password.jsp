<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body class="auth-page">

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div class="auth-split">

        <div class="auth-panel">
            <h2 class="auth-brand-title">Forgot Password?</h2>
            <p class="auth-brand-sub">We will send a verification code to your email.</p>
            <ul class="auth-features">
                <li>Enter your registered email address</li>
                <li>A 6-digit OTP will be sent to you</li>
                <li>Use the code to set a new password</li>
            </ul>
        </div>

        <div class="auth-form-panel">
            <div class="auth-form-inner auth-form-inner--narrow">

                <div class="auth-form-header">
                    <h1>Reset Password</h1>
                    <p>Enter your email address and we will send you an OTP to reset your password.</p>
                </div>

                <!-- Display error messages -->
                <% if (request.getAttribute("errorMsg") != null) { %>
                    <div class="alert alert-error"><%= request.getAttribute("errorMsg") %></div>
                <% } %>

                <form action="<%= ctx %>/forgot-password" method="post">
                    <div class="form-group">
                        <label for="email">Email Address</label>
                        <input type="email" id="email" name="email"
                            required autofocus
                            placeholder="Enter your registered email"
                            value="<%= request.getParameter("email") != null
                                        ? request.getParameter("email") : "" %>">
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary btn-block">Send OTP</button>
                    </div>
                </form>

                <p class="auth-footer">
                    <a href="<%= ctx %>/login">&larr; Back to Login</a>
                </p>

            </div>
        </div>

    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
