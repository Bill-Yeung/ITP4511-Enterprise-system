export function CancelAppointmentModal({
  appointment,
  reason,
  loading,
  formatDate,
  formatTime,
  onReasonChange,
  onClose,
  onConfirm,
}) {

  if (!appointment) {
    return null;
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h3>Cancel Appointment</h3>
        <p>
          Are you sure you want to cancel your appointment at{' '}
          <strong>{appointment.clinicName}</strong> on{' '}
          <strong>{formatDate(appointment.appointmentDate)}</strong> at{' '}
          <strong>{formatTime(appointment.timeSlot)}</strong>?
        </p>
        <div className="form-group">
          <label>Reason (optional)</label>
          <textarea
            rows={3}
            value={reason}
            onChange={e => onReasonChange(e.target.value)}
            placeholder="Why are you cancelling?"
          />
        </div>
        <div className="form-actions form-actions--right">
          <button className="btn btn-outline" onClick={onClose}>
            Keep Appointment
          </button>
          <button className="btn btn-danger" disabled={loading} onClick={onConfirm}>
            {loading ? 'Cancelling...' : 'Confirm Cancel'}
          </button>
        </div>
      </div>
    </div>
  );
  
}
