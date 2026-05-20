export function AppointmentCard({
  appointment,
  tab,
  formatTime,
  statusBadge,
  canCancel,
  canReschedule,
  onCancel,
  onReschedule,
}) {
  
  const date = new Date(appointment.appointmentDate + 'T00:00:00');

  return (
    <div className="appt-card">
      <div className="appt-card-left">
        <div className="appt-date-block">
          <span className="appt-day">{date.getDate()}</span>
          <span className="appt-month">
            {date.toLocaleDateString('en', { month: 'short' })}
          </span>
        </div>
      </div>
      <div className="appt-card-center">
        <div className="appt-clinic">{appointment.clinicName}</div>
        <div className="appt-service">{appointment.serviceName}</div>
        <div className="appt-meta">
          {formatTime(appointment.timeSlot)}
          {appointment.doctorName && ` - ${appointment.doctorName}`}
        </div>
        {appointment.cancelReason && (
          <div className="appt-cancel-reason">Reason: {appointment.cancelReason}</div>
        )}
        {appointment.remarks && (
          <div className="appt-remarks">Notes: {appointment.remarks}</div>
        )}
      </div>
      <div className="appt-card-right">
        {statusBadge(appointment.status, appointment.cancelReason)}
        {tab === 'upcoming' && (
          <div className="appt-actions">
            {canReschedule(appointment) && (
              <button className="btn btn-outline btn-sm" onClick={() => onReschedule(appointment)}>
                Reschedule
              </button>
            )}
            {canCancel(appointment) && (
              <button className="btn btn-danger btn-sm" onClick={() => onCancel(appointment)}>
                Cancel
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
