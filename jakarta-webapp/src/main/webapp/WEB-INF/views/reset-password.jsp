<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body class="auth-page">

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div class="auth-split">

        <div class="auth-panel">
            <div class="auth-brand-logo">CCHC</div>
            <h2 class="auth-brand-title">Set New<br>Password</h2>
            <p class="auth-brand-sub">Choose a strong password for your account.</p>
            <ul class="auth-features">
                <li>At least 8 characters long</li>
                <li>Both passwords must match</li>
                <li>You will be redirected to login after reset</li>
            </ul>
        </div>

        <div class="auth-form-panel">
            <div class="auth-form-inner auth-form-inner--narrow">

                <div class="auth-form-header">
                    <h1>Set New Password</h1>
                    <p>Enter your new password below.</p>
                </div>

                <% if (request.getAttribute("errorMsg") != null) { %>
                    <div class="alert alert-error"><%= request.getAttribute("errorMsg") %></div>
                <% } %>

                <form action="<%= ctx %>/reset-password" method="post">
                    <div class="form-group">
                        <label for="newPassword">New Password</label>
                        <input type="password" id="newPassword" name="newPassword"
                            required autofocus
                            minlength="8"
                            placeholder="Enter new password (min. 8 characters)">
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">Confirm New Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword"
                            required
                            minlength="8"
                            placeholder="Re-enter new password">
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary btn-block">Reset Password</button>
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
