<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@ taglib prefix="clinic" uri="/WEB-INF/tlds/clinic-taglib.tld" %>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Check-in &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">

        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <div class="page-header">
                <h1>Patient Check-in</h1>
                <span style="color: var(--gray-500); font-size: 14px;">
                    Today &mdash; <%= request.getAttribute("viewDate") %>
                </span>
            </div>

            <%
                String staffMsg = (String) session.getAttribute("staffMsg");
                String staffError = (String) session.getAttribute("staffError");
                if (staffMsg != null) { session.removeAttribute("staffMsg"); %>
                    <div class="alert alert-success"><%= staffMsg %></div>
            <% } if (staffError != null) { session.removeAttribute("staffError"); %>
                    <div class="alert alert-error"><%= staffError %></div>
            <% } %>

            <%-- Admin: clinic picker --%>
            <%
                Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
                Integer viewClinicId = (Integer) request.getAttribute("viewClinicId");
                String checkinActionUrl = ctx + "/staff/checkin";
                if (isAdmin != null && isAdmin && viewClinicId != null) {
                    checkinActionUrl += "?clinic=" + viewClinicId;
                }
            %>

            <% if (isAdmin != null && isAdmin) { %>
                <%
                    ArrayList<ClinicBean> allClinics = (ArrayList<ClinicBean>) request.getAttribute("allClinics");
                %>
                <form class="filter-bar" method="get"
                    action="<%= ctx %>/staff/checkin">
                    <div class="form-group">
                        <label for="clinic">Clinic</label>
                        <select id="clinic" name="clinic" onchange="this.form.submit()">
                            <% if (allClinics != null) {
                                for (ClinicBean c : allClinics) { %>
                                    <option value="<%= c.getClinicId() %>"
                                        <%= (viewClinicId != null && c.getClinicId() == viewClinicId) ? "selected" : "" %>>
                                        <%= c.getName() %>
                                    </option>
                            <%  } } %>
                        </select>
                    </div>
                </form>
            <% } %>

            <%-- Appointment table with check-in actions --%>
            <div class="card">
                <div class="card-header">
                    Today's Appointments
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <%
                        ArrayList<AppointmentBean> appointments = (ArrayList<AppointmentBean>) request.getAttribute("appointments");
                    %>
                    <clinic:appointmentTable appointments="<%= appointments %>"
                        showStaffActions="true"
                        actionUrl="<%= checkinActionUrl %>"
                        modalId="checkinAppointmentModal" />
                </div>
            </div>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />
    <script src="<%= ctx %>/js/appointment-actions.js"></script>

</body>
</html>
