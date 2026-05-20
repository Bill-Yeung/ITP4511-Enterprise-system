<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String action = (String) session.getAttribute("otpAction");
    boolean isForgotPassword = "forgot-password".equals(action);
    String otpEmail = (String) session.getAttribute("otpEmail");
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OTP Verification &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body class="auth-page">

    <div class="auth-split">

        <div class="auth-panel">
            <div class="auth-brand-logo">CCHC</div>
            <h2 class="auth-brand-title"><%= isForgotPassword ? "Password Reset" : "Email Verification" %></h2>
            <p class="auth-brand-sub">Your account security matters to us.</p>
            <ul class="auth-features">
                <li>A 6-digit code was sent to your email</li>
                <li>The code expires in <strong>5 minutes</strong></li>
                <li>Check your spam folder if not received</li>
            </ul>
        </div>

        <div class="auth-form-panel">
            <div class="auth-form-inner auth-form-inner--narrow">

                <div class="auth-form-header">
                    <h1><%= isForgotPassword ? "Reset Password" : "Email Verification" %></h1>
                    <p>
                        <% if (isForgotPassword) { %>
                            Enter the 6-digit code to verify your identity and reset your password.
                        <% } else { %>
                            Enter the code to verify your email address and activate your account.
                        <% } %>
                    </p>
                </div>

                <div class="otp-email-display">
                    <span class="otp-email-label">Code sent to:</span>
                    <span class="otp-email-value"><%= otpEmail %></span>
                </div>

                <% if (request.getAttribute("errorMsg") != null) { %>
                    <div class="alert alert-error"><%= request.getAttribute("errorMsg") %></div>
                <% } %>

                <% if (request.getParameter("resent") != null) { %>
                    <div class="alert alert-success">A new OTP has been sent to your email.</div>
                <% } %>

                <form action="<%= ctx %>/otp-verify" method="post">
                    <div class="form-group">
                        <label for="otpCode">6-Digit OTP Code</label>
                        <input type="text" id="otpCode" name="otpCode"
                            class="otp-input"
                            maxlength="6" required
                            autocomplete="one-time-code"
                            placeholder="&bull; &bull; &bull; &bull; &bull; &bull;"
                            autofocus>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary btn-block">Verify Code</button>
                    </div>
                </form>

                <p class="auth-footer">
                    <% if (isForgotPassword) { %>
                        <a href="<%= ctx %>/forgot-password">&larr; Back to Forgot Password</a>
                    <% } else { %>
                        <a href="<%= ctx %>/login">&larr; Back to Login</a>
                    <% } %>
                </p>

            </div>
        </div>

    </div>

    <script>
        // Timer for OTP code expiry
        (function () {
            let seconds = 300;
            const el = document.createElement('p');
            el.className = 'otp-timer';
            document.querySelector('.auth-form-inner').appendChild(el);

            const tick = () => {
                if (seconds <= 0) {
                    el.textContent = 'Code expired. Please go back and request a new one.';
                    el.style.color = '#c0392b';
                    return;
                }
                const m = Math.floor(seconds / 60);
                const s = seconds % 60;
                el.textContent = 'Code expires in ' + m + ':' + String(s).padStart(2, '0');
                seconds--;
                setTimeout(tick, 1000);
            };
            tick();
        })();
    </script>

</body>
</html>
