package hk.edu.hkiit.jakarta.webapp.tag;

import hk.edu.hkiit.jakarta.webapp.bean.AppointmentBean;
import hk.edu.hkiit.jakarta.webapp.bean.ClinicServiceBean;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.util.ArrayList;

import hk.edu.hkiit.jakarta.webapp.util.HtmlUtil;

// Custom tag for appointments
// Attributes: appointments, showStaffActions, actionUrl, modalId
public class AppointmentTableTag extends SimpleTagSupport {

    private ArrayList<AppointmentBean> appointments;
    private boolean showStaffActions = false;
    private String actionUrl = "";
    private String modalId = "appointmentActionModal";
    private ArrayList<ClinicServiceBean> clinicServices;
    private ArrayList<String[]> doctors;

    // Setter for attributes

    public void setAppointments(ArrayList<AppointmentBean> appointments) {
        this.appointments = appointments;
    }

    public void setShowStaffActions(boolean showStaffActions) {
        this.showStaffActions = showStaffActions;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public void setClinicServices(ArrayList<ClinicServiceBean> clinicServices) {
        this.clinicServices = clinicServices;
    }

    public void setDoctors(ArrayList<String[]> doctors) {
        this.doctors = doctors;
    }

    public void setModalId(String modalId) {
        if (modalId != null && !modalId.trim().isEmpty()) {
            this.modalId = modalId.trim();
        }
    }

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        try {

            if (appointments == null || appointments.isEmpty()) {
                out.println("<p class=\"no-data\">No appointments found.</p>");
                return;
            }

            // Table header
            out.println("<table class=\"data-table\"><thead><tr>"
                      + "<th>ID</th>"
                      + "<th>Patient</th>"
                      + "<th>Clinic</th>"
                      + "<th>Service</th>"
                      + "<th>Date</th>"
                      + "<th>Time</th>"
                      + "<th>Status</th>"
                      + "<th>Actions</th>"
                      + "</tr></thead><tbody>");

            // Render each data
            for (AppointmentBean a : appointments) {

                String badgeClass = getStatusBadgeClass(a.getStatus());

                out.println("<tr><td>" + a.getAppointmentId() + "</td>");
                out.println("<td>" + HtmlUtil.escape(a.getPatientName()) + "</td>"
                          + "<td>" + HtmlUtil.escape(a.getClinicName()) + "</td>"
                          + "<td>" + HtmlUtil.escape(a.getServiceName()) + "</td>"
                          + "<td>" + a.getAppointmentDate() + "</td>"
                          + "<td>" + a.getTimeSlot() + "</td>"
                          + "<td><span class=\"badge " + badgeClass + "\">"
                          + HtmlUtil.escape(a.getStatus()) + "</span></td>");
                out.println("<td>");

                if (showStaffActions) {
                    renderStaffActions(out, a);
                } else {
                    out.println("&mdash;");
                }

                out.println("</td>");
                out.println("</tr>");

            }

            out.println("</tbody></table>");

            // Render modals when staff actions are allowed
            if (showStaffActions) {
                renderModal(out);
                if (canEditAppointment()) {
                    renderEditModal(out);
                }
            }

        } catch (IOException ex) {
            throw new JspException("Error in AppointmentTableTag", ex);
        }

    }

    // Render action buttons
    private void renderStaffActions(JspWriter out, AppointmentBean a) throws IOException {

        int id = a.getAppointmentId();
        String base = actionUrl.isEmpty() ? "" : actionUrl;
        String patient = a.getPatientName() == null ? "" : a.getPatientName();
        String dateStr = a.getAppointmentDate() + " " + a.getTimeSlot();

        out.println("<div class=\"action-group\">");

        if (canEditAppointment()) {
            renderEditButton(out, a, base, id);
        }

        switch (a.getStatus()) {
            case "Pending":
                renderActionButton(out, "btn-success", "Approve", "Approve Booking",
                        "Approve booking request for " + patient + " (" + dateStr + ")?",
                        base, "approve", id, false);
                renderActionButton(out, "btn-danger", "Reject", "Reject Booking",
                        "Reject booking request for " + patient + " (" + dateStr + ")?",
                        base, "reject", id, true);
                break;

            case "Booked":
                renderActionButton(out, "btn-success", "Check In", "Check In",
                        "Confirm check-in for " + patient + " (" + dateStr + ")?",
                        base, "checkin", id, false);
                renderActionButton(out, "btn-warning", "No-show", "No-show",
                        "Mark " + patient + " (" + dateStr + ") as No-show?",
                        base, "noshow", id, false);
                renderActionButton(out, "btn-danger", "Cancel", "Cancel Appointment",
                        "Cancel appointment for " + patient + " (" + dateStr + ")?",
                        base, "cancel", id, true);
                break;

            case "Arrived":
                renderActionButton(out, "btn-primary", "Complete", "Complete",
                        "Mark appointment for " + patient + " (" + dateStr + ") as Completed?",
                        base, "complete", id, false);
                break;

            default:
                break;
        }

        out.println("</div>");
    }

    private boolean canEditAppointment() {
        return clinicServices != null && !clinicServices.isEmpty();
    }

    private void renderEditButton(JspWriter out, AppointmentBean a, String url, int id) throws IOException {
        
        String remarks = a.getRemarks() == null ? "" : a.getRemarks();
        String doctorId = a.getDoctorId() == null ? "" : String.valueOf(a.getDoctorId());
        
        out.println("<button type=\"button\" class=\"btn btn-xs btn-outline js-appointment-edit\""
                  + " data-edit-modal-id=\"" + modalId + "Edit\""
                  + " data-url=\"" + url + "\""
                  + " data-id=\"" + id + "\""
                  + " data-clinic-service-id=\"" + a.getClinicServiceId() + "\""
                  + " data-appointment-date=\"" + a.getAppointmentDate() + "\""
                  + " data-time-slot=\"" + a.getTimeSlot() + "\""
                  + " data-doctor-id=\"" + doctorId + "\""
                  + " data-status=\"" + HtmlUtil.escape(a.getStatus()) + "\""
                  + " data-remarks=\"" + HtmlUtil.escape(remarks) + "\">Edit</button>");

    }

    private void renderActionButton(JspWriter out, String styleClass, String label,
                                    String title, String message, String url,
                                    String action, int id, boolean showReason) throws IOException {

        out.println("<button type=\"button\" class=\"btn btn-xs " + styleClass + " js-appointment-action\""
                  + " data-modal-id=\"" + modalId + "\""
                  + " data-title=\"" + HtmlUtil.escape(title) + "\""
                  + " data-message=\"" + HtmlUtil.escape(message) + "\""
                  + " data-url=\"" + url + "\""
                  + " data-action=\"" + action + "\""
                  + " data-id=\"" + id + "\""
                  + " data-show-reason=\"" + showReason + "\">"
                  + HtmlUtil.escape(label) + "</button>");

    }

    // Confirmation modal
    private void renderModal(JspWriter out) throws IOException {

        String modal = modalId;
        String titleId = modalId + "Title";
        String messageId = modalId + "Message";
        String reasonGroupId = modalId + "ReasonGroup";
        String reasonId = modalId + "Reason";
        String confirmBtnId = modalId + "ConfirmBtn";
        String formId = modalId + "Form";
        String actionId = modalId + "Action";
        String appointmentId = modalId + "AppointmentId";

        out.println("<div id=\"" + modal + "\" class=\"modal-overlay is-hidden\" data-appointment-action-modal>"
                  + "<div class=\"modal\">"
                  + "<form id=\"" + formId + "\" method=\"post\">"
                  + "<input type=\"hidden\" id=\"" + actionId + "\" name=\"action\">"
                  + "<input type=\"hidden\" id=\"" + appointmentId + "\" name=\"id\">"
                  + "<h3 id=\"" + titleId + "\"></h3>"
                  + "<p id=\"" + messageId + "\"></p>"
                  + "<div id=\"" + reasonGroupId + "\" class=\"form-group is-hidden\">"
                  + "<label for=\"" + reasonId + "\">Reason</label>"
                  + "<textarea id=\"" + reasonId + "\" name=\"reason\" rows=\"3\" placeholder=\"Enter reason...\"></textarea>"
                  + "</div>"
                  + "<div class=\"form-actions form-actions--right\">"
                  + "<button type=\"button\" class=\"btn btn-outline\" data-appointment-action-close>Go Back</button>"
                  + "<button type=\"submit\" id=\"" + confirmBtnId + "\" class=\"btn btn-primary\">Confirm</button>"
                  + "</div></form></div></div>");

    }

    // Edit appointment modal
    private void renderEditModal(JspWriter out) throws IOException {

        String modal = modalId + "Edit";
        String formId = modalId + "EditForm";
        String appointmentId = modalId + "EditAppointmentId";
        String serviceId = modalId + "EditClinicServiceId";
        String dateId = modalId + "EditDate";
        String timeId = modalId + "EditTime";
        String doctorId = modalId + "EditDoctorId";
        String statusId = modalId + "EditStatus";
        String remarksId = modalId + "EditRemarks";

        out.println("<div id=\"" + modal + "\" class=\"modal-overlay is-hidden\" data-appointment-edit-modal>"
                  + "<div class=\"modal\">"
                  + "<form id=\"" + formId + "\" method=\"post\">"
                  + "<input type=\"hidden\" name=\"action\" value=\"update\">"
                  + "<input type=\"hidden\" id=\"" + appointmentId + "\" name=\"id\">"
                  + "<h3>Edit Appointment</h3>"
                  + "<div class=\"form-group\"><label for=\"" + serviceId + "\">Service</label>"
                  + "<select id=\"" + serviceId + "\" name=\"clinicServiceId\">");

        // Render possible services
        if (clinicServices != null) {
            for (ClinicServiceBean s : clinicServices) {
                out.println("<option value=\"" + s.getClinicServiceId() + "\">" + HtmlUtil.escape(s.getServiceName()) + "</option>");
            }
        }

        out.println("</select></div>"
                  + "<div class=\"form-group\"><label for=\"" + dateId + "\">Date</label>"
                  + "<input type=\"date\" id=\"" + dateId + "\" name=\"appointmentDate\" required></div>"
                  + "<div class=\"form-group\"><label for=\"" + timeId + "\">Time</label>"
                  + "<input type=\"time\" id=\"" + timeId + "\" name=\"timeSlot\" required></div>"
                  + "<div class=\"form-group\"><label for=\"" + doctorId + "\">Doctor</label>"
                  + "<select id=\"" + doctorId + "\" name=\"doctorId\"><option value=\"\">No specific doctor</option>");

        // Render possible doctor list
        if (doctors != null) {
            for (String[] doctor : doctors) {
                out.println("<option value=\"" + doctor[0] + "\">" + HtmlUtil.escape(doctor[1]) + "</option>");
            }
        }

        out.println("</select></div>"
                  + "<div class=\"form-group\"><label for=\"" + statusId + "\">Status</label>"
                  + "<select id=\"" + statusId + "\" name=\"status\">"
                  + "<option value=\"Pending\">Pending</option>"
                  + "<option value=\"Booked\">Booked</option>"
                  + "<option value=\"Arrived\">Arrived</option>"
                  + "<option value=\"Completed\">Completed</option>"
                  + "<option value=\"No-show\">No-show</option>"
                  + "<option value=\"Cancelled\">Cancelled</option>"
                  + "</select></div>"
                  + "<div class=\"form-group\"><label for=\"" + remarksId + "\">Remarks</label>"
                  + "<textarea id=\"" + remarksId + "\" name=\"remarks\" rows=\"3\"></textarea></div>"
                  + "<div class=\"form-actions form-actions--right\">"
                  + "<button type=\"button\" class=\"btn btn-outline\" data-appointment-edit-close>Go Back</button>"
                  + "<button type=\"submit\" class=\"btn btn-primary\">Save Changes</button>"
                  + "</div></form></div></div>");
                  
    }

    private String getStatusBadgeClass(String status) {

        if (status == null) {
            return "";
        }

        switch (status) {
            case "Pending":
                return "badge-pending";
            case "Booked":
                return "badge-booked";
            case "Arrived":
                return "badge-arrived";
            case "Completed":
                return "badge-completed";
            case "No-show":
                return "badge-noshow";
            case "Cancelled":
                return "badge-cancelled";
            default:
                return "";
        }

    }

}
