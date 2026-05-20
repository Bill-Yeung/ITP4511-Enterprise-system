<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib prefix="clinic" uri="/WEB-INF/tlds/clinic-taglib.tld" %>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Batch Import &mdash; CCHC Clinic System</title>
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
                <h1>Batch Import</h1>
                <span class="badge badge-booked">
                    <jsp:getProperty name="profile" property="adminLevel" /> Admin
                </span>
            </div>

            <%
                String importError = (String) request.getAttribute("importError");
                if (importError != null) {
            %>
                <div class="alert alert-error"><%= importError %></div>
            <% } %>

            <div class="card">
                <div class="card-header">Upload CSV File</div>
                <div class="card-body">
                    <form method="post" action="<%= ctx %>/admin/import"
                        enctype="multipart/form-data">
                        <div class="filter-bar">
                            <div class="form-group">
                                <label>Import Type</label>
                                <select name="importType" id="importType" onchange="showFormat(this.value)">
                                    <option value="">Select</option>
                                    <option value="services">Services</option>
                                    <option value="clinic_services">Clinic-Service Timeslots</option>
                                    <option value="users">Users</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>CSV File</label>
                                <div class="file-upload-control">
                                    <label class="btn btn-secondary file-upload-btn" for="csvFileInput">Choose CSV File</label>
                                    <input type="file" id="csvFileInput" name="csvFile" accept=".csv" class="file-upload-input" />
                                    <span id="csvFileName" class="file-upload-name">No file selected</span>
                                </div>
                            </div>
                            <button type="submit" class="btn btn-primary">Import</button>
                        </div>
                    </form>

                    <%-- CSV format guides with templates download --%>

                    <div id="fmt-services" class="csv-format" style="display:none">
                        <strong>Services CSV Format:</strong>
                        <code>Name, Description <br>General Consultation, Outpatient consultation with a GP <br>Vaccination, Flu and COVID-19 boosters</code>
                        <a href="<%= ctx %>/templates/template_services.csv"
                        download class="btn btn-outline btn-sm" style="margin-top:10px">Download Template</a>
                    </div>
                    <div id="fmt-clinic_services" class="csv-format" style="display:none">
                        <strong>Clinic-Service Timeslots CSV Format:</strong>
                        <code>clinic_id, service_id, quota_per_slot, slot_duration_mins <br>1, 1, 8, 30 <br>1, 2, 5, 20 <br>2, 1, 6, 30</code>
                        <a href="<%= ctx %>/templates/template_clinic_services.csv"
                        download class="btn btn-outline btn-sm" style="margin-top:10px">Download Template</a>
                    </div>
                    <div id="fmt-users" class="csv-format" style="display:none">
                        <strong>Users CSV Format:</strong> (Passwords will be BCrypt-hashed on import)
                        <code>username, password, role, full_name, email,phone <br>jsmith, password123, Staff, John Smith, jsmith@clinic.hk, 91234567 <br>nurse01, password123, Staff, Mary Wong, mwong@clinic.hk, 98765432</code>
                        <a href="<%= ctx %>/templates/template_users.csv"
                        download class="btn btn-outline btn-sm" style="margin-top:10px">Download Template</a>
                    </div>
                </div>
            </div>

            <%-- Import results --%>
            <%
                ArrayList<String> importResults = (ArrayList<String>) request.getAttribute("importResults");
                if (importResults != null && !importResults.isEmpty()) {
                    Integer importSuccess = (Integer) request.getAttribute("importSuccess");
                    Integer importFailed = (Integer) request.getAttribute("importFailed");
            %>
            <div class="card" style="margin-top:20px">
                <div class="card-header">Import Results</div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <%-- Custom Tag: clinic:importResults renders import results table with status badges --%>
                    <clinic:importResults results="<%= importResults %>" success="<%= importSuccess %>" failed="<%= importFailed %>" />
                </div>
            </div>
            <% } %>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

    <script>
        
        function showFormat(type) {
            document.querySelectorAll('.csv-format').forEach(function(el) { el.style.display = 'none'; });
            if (type) {
                var el = document.getElementById('fmt-' + type);
                if (el) {
                    el.style.display = 'block';
                }
            }
        }

        document.getElementById('csvFileInput').addEventListener('change', function () {
            document.getElementById('csvFileName').textContent = this.files.length ? this.files[0].name : 'No file selected';
        });

    </script>

</body>
</html>
