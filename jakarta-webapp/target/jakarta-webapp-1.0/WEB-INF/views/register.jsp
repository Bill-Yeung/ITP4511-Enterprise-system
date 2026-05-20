<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register &mdash; CCHC Clinic System</title>
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
            </ul>
        </div>

        <div class="auth-form-panel">
            <div class="auth-form-inner">

                <div class="auth-form-header">
                    <h1>Create Patient Account</h1>
                    <p>Fill in your details to register.</p>
                </div>

                <% if (request.getAttribute("errorMsg") != null) { %>
                    <div class="alert alert-error"><%= request.getAttribute("errorMsg") %></div>
                <% } %>

                <form action="<%= ctx %>/register" method="post" novalidate>

                    <fieldset class="form-fieldset">
                        <legend>Account Details</legend>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="username">Username <span class="req">*</span></label>
                                <input type="text" id="username" name="username" required
                                    minlength="4" maxlength="30" autocomplete="username"
                                    placeholder="min. 4 characters">
                            </div>
                            <div class="form-group">
                                <label for="email">Email Address <span class="req">*</span></label>
                                <input type="email" id="email" name="email" required
                                    maxlength="100" autocomplete="email"
                                    placeholder="you@example.com">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="password">Password <span class="req">*</span></label>
                                <input type="password" id="password" name="password" required
                                    minlength="8" autocomplete="new-password"
                                    placeholder="min. 8 characters">
                            </div>
                            <div class="form-group">
                                <label for="confirmPassword">Confirm Password <span class="req">*</span></label>
                                <input type="password" id="confirmPassword" name="confirmPassword" required
                                    minlength="8" autocomplete="new-password"
                                    placeholder="repeat password">
                            </div>
                        </div>
                    </fieldset>

                    <fieldset class="form-fieldset">
                        <legend>Personal Information</legend>
                        <div class="form-group">
                            <label for="fullName">Full Name <span class="req">*</span></label>
                            <input type="text" id="fullName" name="fullName" required
                                maxlength="100" autocomplete="name"
                                placeholder="As shown on your ID document">
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="phone">Phone Number</label>
                                <input type="tel" id="phone" name="phone" maxlength="20"
                                    autocomplete="tel" placeholder="e.g. 9123 4567">
                            </div>
                            <div class="form-group">
                                <label for="idNumber">HKID / Passport No.</label>
                                <input type="text" id="idNumber" name="idNumber" maxlength="20"
                                    placeholder="e.g. A123456(7)">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="dateOfBirth">Date of Birth</label>
                                <input type="date" id="dateOfBirth" name="dateOfBirth">
                            </div>
                            <div class="form-group">
                                <label for="gender">Gender</label>
                                <select id="gender" name="gender">
                                    <option value="">-- Select --</option>
                                    <option value="Male">Male</option>
                                    <option value="Female">Female</option>
                                    <option value="Other">Other</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="address">Address</label>
                            <input type="text" id="address" name="address" maxlength="200"
                                autocomplete="street-address" placeholder="Flat/Floor, Block, Street, District">
                        </div>
                    </fieldset>

                    <fieldset class="form-fieldset">
                        <legend>Emergency Contact</legend>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="emergencyContactName">Contact Name</label>
                                <input type="text" id="emergencyContactName" name="emergencyContactName"
                                    maxlength="100" placeholder="Name of emergency contact">
                            </div>
                            <div class="form-group">
                                <label for="emergencyContactPhone">Contact Phone</label>
                                <input type="tel" id="emergencyContactPhone" name="emergencyContactPhone"
                                    maxlength="20" placeholder="e.g. 9876 5432">
                            </div>
                        </div>
                    </fieldset>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary btn-block">
                            Create Account &amp; Verify Email
                        </button>
                    </div>

                </form>

                <p class="auth-footer">
                    Already have an account?
                    <a href="<%= ctx %>/login">Sign in here</a>
                </p>

            </div>
        </div>
        
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
