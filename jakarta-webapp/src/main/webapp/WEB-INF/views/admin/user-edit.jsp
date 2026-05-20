<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.UserBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.StaffBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.AdminBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.PatientBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit User &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/header.jsp" />

<div id="page-wrapper">
    <jsp:include page="/WEB-INF/views/menu.jsp" />

    <main id="main-content">

        <%
            UserBean u = (UserBean) request.getAttribute("user");
            StaffBean staff = (StaffBean) request.getAttribute("staff");
            AdminBean admin = (AdminBean) request.getAttribute("admin");
            PatientBean patient = (PatientBean) request.getAttribute("patient");
            ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
            if (clinics == null) clinics = new ArrayList<ClinicBean>();
            boolean isEdit = (u != null);
            String role = isEdit ? u.getRole() : "";
            int currentClinic = staff != null ? staff.getClinicId()
                : (admin != null && admin.getClinicId() != null ? admin.getClinicId() : 0);
            String currentPosition = staff != null ? staff.getPosition() : "";
            String currentAdminLevel = admin != null ? admin.getAdminLevel() : "";
        %>

        <div class="page-header">
            <h1><%= isEdit ? "Edit User" : "New User" %></h1>
            <a class="btn btn-secondary" href="<%= ctx %>/admin/users">Back to list</a>
        </div>

        <%
            String adminError = (String) session.getAttribute("adminError");
            if (adminError != null) { session.removeAttribute("adminError");
        %>
            <div class="alert alert-error"><%= adminError %></div>
        <% } %>

        <form method="post" action="<%= ctx %>/admin/users" class="card">
            <input type="hidden" name="action" value="save" />
            <% if (isEdit) { %>
                <input type="hidden" name="id" value="<%= u.getUserId() %>" />
            <% } %>
            <div class="card-body">

                <div class="form-group">
                    <label>Username</label>
                    <input type="text" name="username" class="form-control"
                           value="<%= isEdit ? u.getUsername() : "" %>"
                           <%= isEdit ? "readonly" : "required" %> />
                    <% if (isEdit) { %><small style="color:var(--gray-500)">Username cannot be changed after creation.</small><% } %>
                </div>

                <div class="form-group">
                    <label>Full Name</label>
                    <input type="text" name="fullName" class="form-control" required
                           value="<%= isEdit && u.getFullName() != null ? u.getFullName() : "" %>" />
                </div>

                <div class="form-group">
                    <label>Email</label>
                    <input type="email" name="email" class="form-control"
                           value="<%= isEdit && u.getEmail() != null ? u.getEmail() : "" %>" />
                </div>

                <div class="form-group">
                    <label>Phone</label>
                    <input type="text" name="phone" class="form-control"
                           value="<%= isEdit && u.getPhone() != null ? u.getPhone() : "" %>" />
                </div>

                <div class="form-group">
                    <label>Role</label>
                    <% if (isEdit) { %>
                        <input type="text" class="form-control" value="<%= role %>" readonly />
                        <input type="hidden" name="role" value="<%= role %>" />
                        <small style="color:var(--gray-500)">Role cannot be changed after creation.</small>
                    <% } else { %>
                        <select name="role" id="roleSelect" class="form-control" required onchange="toggleRoleFields()">
                            <option value="">-- Select role --</option>
                            <option value="Patient">Patient</option>
                            <option value="Staff">Staff</option>
                            <option value="Admin">Admin</option>
                        </select>
                    <% } %>
                </div>

                <div class="form-group">
                    <label><%= isEdit ? "New Password (leave blank to keep current)" : "Password" %></label>
                    <input type="password" name="password" class="form-control"
                           <%= isEdit ? "" : "required" %> />
                </div>

                <%-- Staff-only: clinic + position --%>
                <div id="staffFields" style="<%= "Staff".equals(role) ? "" : "display:none" %>">
                    <div class="form-group">
                        <label>Clinic</label>
                        <select name="clinicId" class="form-control">
                            <option value="">-- Select clinic --</option>
                            <% for (ClinicBean c : clinics) { %>
                                <option value="<%= c.getClinicId() %>"
                                    <%= c.getClinicId() == currentClinic ? "selected" : "" %>>
                                    <%= c.getName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Position</label>
                        <select name="position" class="form-control">
                            <option value="Doctor"     <%= "Doctor".equals(currentPosition) ? "selected" : "" %>>Doctor</option>
                            <option value="Nurse"      <%= "Nurse".equals(currentPosition) ? "selected" : "" %>>Nurse</option>
                            <option value="Front Desk" <%= "Front Desk".equals(currentPosition) ? "selected" : "" %>>Front Desk</option>
                        </select>
                    </div>
                </div>

                <%-- Patient-only profile fields --%>
                <%
                    String pIdNumber = patient != null && patient.getIdNumber() != null ? patient.getIdNumber() : "";
                    String pDob = patient != null && patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "";
                    String pGender = patient != null && patient.getGender() != null ? patient.getGender() : "";
                    String pAddress = patient != null && patient.getAddress() != null ? patient.getAddress() : "";
                    String pEmName = patient != null && patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "";
                    String pEmPhone = patient != null && patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "";
                %>
                <div id="patientFields" style="<%= "Patient".equals(role) ? "" : "display:none" %>">
                    <div class="form-group">
                        <label>HKID / ID Number</label>
                        <input type="text" name="idNumber" class="form-control" value="<%= pIdNumber %>" />
                    </div>
                    <div class="form-group">
                        <label>Date of Birth</label>
                        <input type="date" name="dateOfBirth" class="form-control" value="<%= pDob %>" />
                    </div>
                    <div class="form-group">
                        <label>Gender</label>
                        <select name="gender" class="form-control">
                            <option value=""        <%= "".equals(pGender) ? "selected" : "" %>>-- Not specified --</option>
                            <option value="Male"    <%= "Male".equals(pGender) ? "selected" : "" %>>Male</option>
                            <option value="Female"  <%= "Female".equals(pGender) ? "selected" : "" %>>Female</option>
                            <option value="Other"   <%= "Other".equals(pGender) ? "selected" : "" %>>Other</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Address</label>
                        <input type="text" name="address" class="form-control" value="<%= pAddress %>" />
                    </div>
                    <div class="form-group">
                        <label>Emergency Contact Name</label>
                        <input type="text" name="emergencyContactName" class="form-control" value="<%= pEmName %>" />
                    </div>
                    <div class="form-group">
                        <label>Emergency Contact Phone</label>
                        <input type="text" name="emergencyContactPhone" class="form-control" value="<%= pEmPhone %>" />
                    </div>
                </div>

                <%-- Admin-only: clinic (optional) + admin_level --%>
                <div id="adminFields" style="<%= "Admin".equals(role) ? "" : "display:none" %>">
                    <div class="form-group">
                        <label>Admin Level</label>
                        <select name="adminLevel" class="form-control">
                            <option value="System" <%= "System".equals(currentAdminLevel) ? "selected" : "" %>>System (no clinic)</option>
                            <option value="Clinic" <%= "Clinic".equals(currentAdminLevel) ? "selected" : "" %>>Clinic Manager</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Clinic (only for Clinic-level admin)</label>
                        <select name="clinicIdAdmin" class="form-control"
                                onchange="document.querySelector('select[name=clinicId]').value = this.value">
                            <option value="">-- None (System) --</option>
                            <% for (ClinicBean c : clinics) { %>
                                <option value="<%= c.getClinicId() %>"
                                    <%= c.getClinicId() == currentClinic ? "selected" : "" %>>
                                    <%= c.getName() %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                </div>

                <div style="margin-top:16px; display:flex; gap:12px">
                    <button type="submit" class="btn btn-primary"><%= isEdit ? "Save Changes" : "Create User" %></button>
                    <a href="<%= ctx %>/admin/users" class="btn btn-secondary">Cancel</a>
                </div>
            </div>
        </form>

    </main>
</div>

<jsp:include page="/WEB-INF/views/footer.jsp" />

<script>
function toggleRoleFields() {
    var roleSel = document.getElementById('roleSelect');
    if (!roleSel) return;
    var role = roleSel.value;
    document.getElementById('staffFields').style.display = role === 'Staff' ? '' : 'none';
    document.getElementById('adminFields').style.display = role === 'Admin' ? '' : 'none';
    var pf = document.getElementById('patientFields');
    if (pf) pf.style.display = role === 'Patient' ? '' : 'none';
}
</script>

</body>
</html>
