<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean"%>
<%@page import="hk.edu.hkiit.jakarta.webapp.bean.ServiceBean"%>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clinic Services &mdash; CCHC Clinic System</title>
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
                ArrayList<ClinicServiceBean> clinicServices = (ArrayList<ClinicServiceBean>) request.getAttribute("clinicServices");
                ArrayList<ServiceBean> allServices = (ArrayList<ServiceBean>) request.getAttribute("allServices");
                ClinicBean clinic = (ClinicBean) request.getAttribute("clinic");
                Integer clinicId = (Integer) request.getAttribute("clinicId");
                if (clinics == null) {
                    clinics = new ArrayList<ClinicBean>();
                }
                if (clinicServices == null) {
                    clinicServices = new ArrayList<ClinicServiceBean>();
                }
                if (allServices == null) {
                    allServices = new ArrayList<ServiceBean>();
                }
            %>

            <div class="page-header">
                <h1>Clinic Services<%= clinic != null ? " - " + clinic.getName() : "" %></h1>
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
                <input type="hidden" name="action" value="services" />
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

            <%-- Add new service --%>
            <div class="card" style="margin-bottom:16px">
                <div class="card-header">Add Service to this Clinic</div>
                <div class="card-body">
                    <form id="addClinicServiceForm" method="post" action="<%= ctx %>/admin/clinics">
                        <input type="hidden" name="action" value="addService" />
                        <input type="hidden" name="clinicId" value="<%= clinicId %>" />
                        <div class="filter-bar">
                            <div class="form-group">
                                <label>Service</label>
                                <select name="serviceId" class="form-control" required>
                                    <option value="">Select</option>
                                    <% for (ServiceBean s : allServices) { %>
                                        <option value="<%= s.getServiceId() %>"><%= s.getName() %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Quota / Slot</label>
                                <input type="number" name="quotaPerSlot" class="form-control" required min="1" value="5" />
                            </div>
                            <div class="form-group">
                                <label>Slot Duration (mins)</label>
                                <input type="number" name="slotDurationMins" class="form-control" required min="5" value="30" />
                            </div>
                            <div class="form-group form-group--center">
                                <label class="checkbox-label checkbox-control">
                                    <input type="checkbox" name="requiresApproval" value="true" />
                                    Requires staff approval
                                </label>
                            </div>
                            <button type="button" class="btn btn-primary form-control-button"
                                    data-confirm-submit="addClinicServiceForm"
                                    data-confirm-title="Add Service"
                                    data-confirm-message="Add this service to the selected clinic?">Add</button>
                        </div>
                    </form>
                </div>
            </div>

            <%-- Existing services --%>
            <div class="card">
                <div class="card-header">
                    Mapped Services
                    <span style="float:right; font-size:12px; text-transform:none; letter-spacing:0; font-weight:400; color:var(--gray-500)">
                        <%= clinicServices.size() %> mapping<%= clinicServices.size() == 1 ? "" : "s" %>
                    </span>
                </div>
                <div class="card-body" style="padding:0; overflow-x:auto">
                    <table class="data-table">
                        <thead>
                            <tr><th>Service</th><th>Quota / Slot</th><th>Slot Duration (mins)</th><th>Requires Approval</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                        <% if (clinicServices.isEmpty()) { %>
                            <tr><td colspan="5" style="text-align:center; padding:24px; color:var(--gray-500)">No services mapped to this clinic yet.</td></tr>
                        <% } else for (ClinicServiceBean cs : clinicServices) { %>
                            <tr>
                                <td><%= cs.getServiceName() %></td>
                                <td><input type="number" name="quotaPerSlot" min="1" class="form-control table-number-input"
                                        form="clinicServiceForm<%= cs.getClinicServiceId() %>"
                                        value="<%= cs.getQuotaPerSlot() %>" /></td>
                                <td><input type="number" name="slotDurationMins" min="5" class="form-control table-number-input"
                                        form="clinicServiceForm<%= cs.getClinicServiceId() %>"
                                        value="<%= cs.getSlotDurationMins() %>" /></td>
                                <td>
                                    <label class="checkbox-label">
                                        <input type="checkbox" name="requiresApproval" value="true"
                                            form="clinicServiceForm<%= cs.getClinicServiceId() %>"
                                            <%= cs.isRequiresApproval() ? "checked" : "" %> />
                                        Required
                                    </label>
                                </td>
                                <td class="table-actions">
                                    <form id="clinicServiceForm<%= cs.getClinicServiceId() %>" method="post" action="<%= ctx %>/admin/clinics" style="display:inline">
                                        <input type="hidden" name="clinicId" value="<%= clinicId %>" />
                                        <input type="hidden" name="clinicServiceId" value="<%= cs.getClinicServiceId() %>" />
                                        <input type="hidden" id="clinicServiceAction<%= cs.getClinicServiceId() %>" name="action" />
                                    </form>
                                    <button type="button" class="btn btn-sm btn-primary clinic-action-btn"
                                            data-confirm-field="clinicServiceAction<%= cs.getClinicServiceId() %>"
                                            data-confirm-value="updateService"
                                            data-confirm-submit="clinicServiceForm<%= cs.getClinicServiceId() %>"
                                            data-confirm-title="Save Service"
                                            data-confirm-message="Save changes to <%= cs.getServiceName() %>?">Save</button>
                                    <button type="button" class="btn btn-sm btn-danger clinic-action-btn"
                                            data-confirm-field="clinicServiceAction<%= cs.getClinicServiceId() %>"
                                            data-confirm-value="deleteService"
                                            data-confirm-submit="clinicServiceForm<%= cs.getClinicServiceId() %>"
                                            data-confirm-title="Remove Service"
                                            data-confirm-message="Remove <%= cs.getServiceName() %> from this clinic?"
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
