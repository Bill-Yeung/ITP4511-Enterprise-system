<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.WalkinQueueBean"%>
<%@ taglib prefix="clinic" uri="/WEB-INF/tlds/clinic-taglib.tld" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Queue Board &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/header.jsp" />

<div id="page-wrapper">
    <jsp:include page="/WEB-INF/views/menu.jsp" />

    <main id="main-content">

        <div class="page-header">
            <h1>Queue Board</h1>
        </div>

        <%-- Flash messages --%>
        <%
            String staffMsg = (String) session.getAttribute("staffMsg");
            String staffError = (String) session.getAttribute("staffError");
            if (staffMsg != null) { session.removeAttribute("staffMsg"); %>
                <div class="alert alert-success"><%= staffMsg %></div>
        <% } if (staffError != null) { session.removeAttribute("staffError"); %>
                <div class="alert alert-error"><%= staffError %></div>
        <% } %>

        <%-- Service filter accepts user input --%>
        <%
            ArrayList<ClinicServiceBean> services =
                (ArrayList<ClinicServiceBean>) request.getAttribute("services");
            String selectedServiceId = request.getAttribute("selectedServiceId") != null
                ? request.getAttribute("selectedServiceId").toString() : "";
            Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
            Integer viewClinicId = (Integer) request.getAttribute("viewClinicId");
            String queueActionUrl = ctx + "/staff/queue";
            if (isAdmin != null && isAdmin && viewClinicId != null) {
                queueActionUrl += "?clinic=" + viewClinicId;
            }
        %>
        <form class="filter-bar" method="get"
              action="<%= ctx %>/staff/queue">
            <% if (isAdmin != null && isAdmin) { %>
                <%
                    ArrayList<ClinicBean> allClinics =
                        (ArrayList<ClinicBean>) request.getAttribute("allClinics");
                %>
                <div class="form-group">
                    <label for="clinic">Clinic</label>
                    <select id="clinic" name="clinic">
                        <% if (allClinics != null) {
                            for (ClinicBean c : allClinics) { %>
                                <option value="<%= c.getClinicId() %>"
                                    <%= (viewClinicId != null && c.getClinicId() == viewClinicId) ? "selected" : "" %>>
                                    <%= c.getName() %>
                                </option>
                        <%  } } %>
                    </select>
                </div>
            <% } %>
            <div class="form-group">
                <label for="serviceId">Service</label>
                <select id="serviceId" name="serviceId">
                    <option value="">All Services</option>
                    <% if (services != null) {
                        for (ClinicServiceBean svc : services) { %>
                            <option value="<%= svc.getClinicServiceId() %>"
                                <%= String.valueOf(svc.getClinicServiceId()).equals(selectedServiceId) ? "selected" : "" %>>
                                <%= svc.getServiceName() %>
                            </option>
                    <%  }
                    } %>
                </select>
            </div>
            <button type="submit" class="btn btn-primary">Filter</button>

            <% if (!selectedServiceId.isEmpty()) { %>
                <a href="<%= queueActionUrl %><%= queueActionUrl.contains("?") ? "&" : "?" %>action=callNext&csId=<%= selectedServiceId %>&serviceId=<%= selectedServiceId %>"
                   class="btn btn-success"
                   onclick="return confirm('Call next patient in queue?')">
                    Call Next
                </a>
            <% } %>
        </form>

        <%-- Queue Table (Custom Tag with staff actions) --%>
        <div class="card">
            <div class="card-header">Today's Walk-in Queue</div>
            <div class="card-body" style="padding:0; overflow-x:auto">
                <%
                    ArrayList<WalkinQueueBean> queueList =
                        (ArrayList<WalkinQueueBean>) request.getAttribute("queueList");
                %>
                <clinic:queueTable queues="<%= queueList %>" isStaff="true"
                    actionUrl="<%= queueActionUrl %>" />
            </div>
        </div>

    </main>
</div>

<jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
