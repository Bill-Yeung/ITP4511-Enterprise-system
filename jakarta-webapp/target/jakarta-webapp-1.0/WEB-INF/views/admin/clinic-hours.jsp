<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicHoursBean"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clinic Hours &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">

        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <%
                ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
                ArrayList<ClinicHoursBean> hours = (ArrayList<ClinicHoursBean>) request.getAttribute("hours");
                ClinicBean clinic = (ClinicBean) request.getAttribute("clinic");
                Integer clinicId = (Integer) request.getAttribute("clinicId");
                if (clinics == null) {
                    clinics = new ArrayList<ClinicBean>();
                }
                if (hours == null) {
                    hours = new ArrayList<ClinicHoursBean>();
                }
            %>

            <div class="page-header">
                <h1>Clinic Hours<%= clinic != null ? " - " + clinic.getName() : "" %></h1>
                <a class="btn btn-secondary" href="<%= ctx %>/admin/clinics">Back to clinics</a>
            </div>

            <%
                String adminMsg = (String) session.getAttribute("adminMsg");
                String adminError = (String) session.getAttribute("adminError");
                if (adminMsg != null) { session.removeAttribute("adminMsg"); %>
                <div class="alert alert-success"><%= adminMsg %></div>
            <% } if (adminError != null) { session.removeAttribute("adminError"); %>
                <div class="alert alert-error"><%= adminError %></div>
            <% } %>

            <form method="get" action="<%= ctx %>/admin/clinics" class="card" style="margin-bottom:16px">
                <input type="hidden" name="action" value="hours" />
                <div class="card-body">
                    <div class="filter-bar">
                        <div class="form-group">
                            <label>Clinic</label>
                            <select name="clinicId" class="form-control" onchange="this.form.submit()">
                                <% for (ClinicBean cc : clinics) { %>
                                    <option value="<%= cc.getClinicId() %>"
                                        <%= clinicId != null && clinicId == cc.getClinicId() ? "selected" : "" %>>
                                        <%= cc.getName() %>
                                    </option>
                                <% } %>
                            </select>
                        </div>
                    </div>
                </div>
            </form>

            <% if (clinicId != null) { %>

            <%-- Add new hours --%>
            <div class="card" style="margin-bottom:16px">
                <div class="card-header">Add Operating Session</div>
                <div class="card-body">
                    <form id="addClinicHoursForm" method="post" action="<%= ctx %>/admin/clinics">
                        <input type="hidden" name="action" value="addHours" />
                        <input type="hidden" name="clinicId" value="<%= clinicId %>" />
                        <div class="filter-bar">
                            <div class="form-group">
                                <label>Day</label>
                                <select name="dayOfWeek" class="form-control" required>
                                    <option value="Sunday">Sunday</option>
                                    <option value="Monday">Monday</option>
                                    <option value="Tuesday">Tuesday</option>
                                    <option value="Wednesday">Wednesday</option>
                                    <option value="Thursday">Thursday</option>
                                    <option value="Friday">Friday</option>
                                    <option value="Saturday">Saturday</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Open</label>
                                <input type="time" name="openTime" class="form-control" required value="09:00" />
                            </div>
                            <div class="form-group">
                                <label>Close</label>
                                <input type="time" name="closeTime" class="form-control" required value="17:00" />
                            </div>
                            <button type="button" class="btn btn-primary"
                                    data-confirm-submit="addClinicHoursForm"
                                    data-confirm-title="Add Operating Session"
                                    data-confirm-message="Add this operating session?">Add</button>
                        </div>
                        <small style="display:block; margin-top:12px; color:var(--gray-500)">Tip: add two rows per day (e.g. 09:00-13:00 and 14:00-18:00) to model a lunch break.</small>
                    </form>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    Operating Sessions
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= hours.size() %> session<%= hours.size() == 1 ? "" : "s" %>
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <table class="data-table">
                        <thead><tr><th>Day</th><th>Open</th><th>Close</th><th>Actions</th></tr></thead>
                        <tbody>
                        <% if (hours.isEmpty()) { %>
                            <tr><td colspan="4" style="text-align:center; padding:24px; color:var(--gray-500)">No hours defined.</td></tr>
                        <% } else for (ClinicHoursBean h : hours) {
                            String openStr  = h.getOpenTime()  != null ? h.getOpenTime().toString().substring(0,5)  : "";
                            String closeStr = h.getCloseTime() != null ? h.getCloseTime().toString().substring(0,5) : "";
                        %>
                            <tr>
                                <form id="editClinicHoursForm<%= h.getClinicHoursId() %>" method="post" action="<%= ctx %>/admin/clinics">
                                    <input type="hidden" name="action" value="editHours" />
                                    <input type="hidden" name="clinicId" value="<%= clinicId %>" />
                                    <input type="hidden" name="clinicHoursId" value="<%= h.getClinicHoursId() %>" />
                                    <td>
                                        <select name="dayOfWeek" class="form-control" form="editClinicHoursForm<%= h.getClinicHoursId() %>">
                                            <% for (String d : new String[]{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"}) { %>
                                                <option value="<%= d %>" <%= d.equals(h.getDayOfWeek()) ? "selected" : "" %>><%= d %></option>
                                            <% } %>
                                        </select>
                                    </td>
                                    <td><input type="time" name="openTime"  class="form-control" value="<%= openStr %>"  form="editClinicHoursForm<%= h.getClinicHoursId() %>" required /></td>
                                    <td><input type="time" name="closeTime" class="form-control" value="<%= closeStr %>" form="editClinicHoursForm<%= h.getClinicHoursId() %>" required /></td>
                                </form>
                                <td>
                                    <button type="button" class="btn btn-sm btn-primary"
                                            data-confirm-submit="editClinicHoursForm<%= h.getClinicHoursId() %>"
                                            data-confirm-title="Update Operating Session"
                                            data-confirm-message="Save the changes to this operating session?">Save</button>
                                    <form id="deleteClinicHoursForm<%= h.getClinicHoursId() %>" method="post" action="<%= ctx %>/admin/clinics" style="display:inline">
                                        <input type="hidden" name="action" value="deleteHours" />
                                        <input type="hidden" name="clinicId" value="<%= clinicId %>" />
                                        <input type="hidden" name="clinicHoursId" value="<%= h.getClinicHoursId() %>" />
                                    </form>
                                    <button type="button" class="btn btn-sm btn-danger"
                                            data-confirm-submit="deleteClinicHoursForm<%= h.getClinicHoursId() %>"
                                            data-confirm-title="Remove Operating Session"
                                            data-confirm-message="Remove this operating session?"
                                            data-confirm-danger="true">Remove</button>
                                </td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>

            <% } %>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />
    <script src="<%= ctx %>/js/confirm-modal.js"></script>
    
</body>
</html>
