<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.time.format.TextStyle"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Locale"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicHoursBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean"%>
<%
    String ctx = request.getContextPath();
    ArrayList<ClinicBean> clinics = (ArrayList<ClinicBean>) request.getAttribute("clinics");
    Map<Integer, ArrayList<ClinicServiceBean>> servicesByClinic = (Map<Integer, ArrayList<ClinicServiceBean>>) request.getAttribute("servicesByClinic");
    Map<Integer, ArrayList<ClinicHoursBean>> hoursByClinic = (Map<Integer, ArrayList<ClinicHoursBean>>) request.getAttribute("hoursByClinic");
    String[] dayOrder = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    String todayName = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clinics &amp; Services &mdash; CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <div id="page-wrapper">

        <jsp:include page="/WEB-INF/views/menu.jsp" />

        <main id="main-content">

            <div class="page-header">
                <h1>Clinics &amp; Services</h1>
            </div>

            <% if (clinics == null || clinics.isEmpty()) { %>
                <p class="no-data">No clinics found.</p>
            <% } else { %>
                <div class="clinic-tabs">
                    <% for (int i = 0; i < clinics.size(); i++) {
                        ClinicBean clinic = clinics.get(i);
                    %>
                        <button type="button"
                                class="clinic-tab<%= i == 0 ? " active" : "" %>"
                                data-clinic-tab="<%= clinic.getClinicId() %>">
                            <%= clinic.getName() %>
                        </button>
                    <% } %>
                </div>

                <% for (int i = 0; i < clinics.size(); i++) {
                    ClinicBean clinic = clinics.get(i);
                    ArrayList<ClinicServiceBean> services = servicesByClinic.get(clinic.getClinicId());
                    ArrayList<ClinicHoursBean> hours = hoursByClinic.get(clinic.getClinicId());
                    ClinicHoursBean todayHours = null;
                    if (hours != null) {
                        for (ClinicHoursBean h : hours) {
                            if (todayName.equals(h.getDayOfWeek())) {
                                todayHours = h;
                                break;
                            }
                        }
                    }
                %>
                    <div class="clinics-layout-grid clinic-panel<%= i == 0 ? "" : " is-hidden" %>"
                        data-clinic-panel="<%= clinic.getClinicId() %>">
                        <div>
                            <div class="card">
                                <div class="card-header"><%= clinic.getName() %></div>
                                <div class="card-body">
                                    <p style="color:var(--gray-600); margin:0 0 12px"><%= clinic.getLocation() %></p>
                                    <% if (todayHours != null) { %>
                                        <p style="margin:0">
                                            <strong>Open today:</strong> <%= timeFmt.format(todayHours.getOpenTime()) %> - <%= timeFmt.format(todayHours.getCloseTime()) %>
                                        </p>
                                    <% } else { %>
                                        <p style="color:var(--red-600); margin:0"><strong>Closed today</strong></p>
                                    <% } %>
                                </div>
                            </div>

                            <div class="card" style="margin-top:16px">
                                <div class="card-header">Operating Hours</div>
                                <div class="card-body">
                                    <table class="dashboard-table">
                                        <thead>
                                            <tr>
                                                <th>Day</th>
                                                <th>Open</th>
                                                <th>Close</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <% for (String day : dayOrder) {
                                                ClinicHoursBean match = null;
                                                if (hours != null) {
                                                    for (ClinicHoursBean h : hours) {
                                                        if (day.equals(h.getDayOfWeek())) {
                                                            match = h;
                                                            break;
                                                        }
                                                    }
                                                }
                                                boolean isToday = day.equals(todayName);
                                            %>
                                                <tr<%= isToday ? " style=\"font-weight:600; color:var(--primary)\"" : "" %>>
                                                    <td><%= day %><%= isToday ? " (Today)" : "" %></td>
                                                    <td><%= match == null ? "-" : timeFmt.format(match.getOpenTime()) %></td>
                                                    <td>
                                                        <% if (match == null) { %>
                                                            <span style="color:var(--red-600)">Closed</span>
                                                        <% } else { %>
                                                            <%= timeFmt.format(match.getCloseTime()) %>
                                                        <% } %>
                                                    </td>
                                                </tr>
                                            <% } %>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div>
                            <div class="card">
                                <div class="card-header" style="display:flex; justify-content:space-between; align-items:center">
                                    <span>Services (<%= services == null ? 0 : services.size() %>)</span>
                                    <% if (services != null && services.size() > 1) { %>
                                        <select class="filter-select" data-service-filter="<%= clinic.getClinicId() %>">
                                            <option value="">All Services</option>
                                            <% for (ClinicServiceBean service : services) { %>
                                                <option value="<%= service.getServiceName() %>"><%= service.getServiceName() %></option>
                                            <% } %>
                                        </select>
                                    <% } %>
                                </div>
                                <div class="card-body">
                                    <% if (services == null || services.isEmpty()) { %>
                                        <p style="color:var(--gray-500)">No services available.</p>
                                    <% } else { %>
                                        <div class="service-list">
                                            <% for (ClinicServiceBean service : services) { %>
                                                <div class="service-item" data-service-item="<%= service.getServiceName() %>">
                                                    <div class="service-item-info">
                                                        <div class="service-item-name"><%= service.getServiceName() %></div>
                                                        <div class="service-item-meta">
                                                            <%= service.getSlotDurationMins() %> min per slot -
                                                            <%= service.getQuotaPerSlot() %> patient<%= service.getQuotaPerSlot() == 1 ? "" : "s" %> per slot
                                                            <% if (service.isRequiresApproval()) { %>
                                                                - requires approval
                                                            <% } %>
                                                        </div>
                                                    </div>
                                                </div>
                                            <% } %>
                                        </div>
                                    <% } %>
                                </div>
                            </div>
                        </div>
                    </div>
                <% } %>
            <% } %>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

    <script>

        document.addEventListener('click', function (event) {
            var tab = event.target.closest('[data-clinic-tab]');
            if (!tab) {
                return;
            }

            var clinicId = tab.getAttribute('data-clinic-tab');
            document.querySelectorAll('[data-clinic-tab]').forEach(function (item) {
                item.classList.toggle('active', item === tab);
            });
            document.querySelectorAll('[data-clinic-panel]').forEach(function (panel) {
                panel.classList.toggle('is-hidden', panel.getAttribute('data-clinic-panel') !== clinicId);
            });
        });

        document.addEventListener('change', function (event) {
            var filter = event.target.closest('[data-service-filter]');
            if (!filter) {
                return;
            }

            var panel = filter.closest('[data-clinic-panel]');
            var value = filter.value;
            panel.querySelectorAll('[data-service-item]').forEach(function (item) {
                item.classList.toggle('is-hidden', value && item.getAttribute('data-service-item') !== value);
            });
        });

    </script>

</body>
</html>
