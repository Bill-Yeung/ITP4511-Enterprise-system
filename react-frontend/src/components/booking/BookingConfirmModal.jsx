export function BookingConfirmModal({
  rescheduleId,
  originalAppt,
  selectedClinic,
  selectedService,
  confirmSlot,
  doctors,
  selectedDoctor,
  loading,
  formatDateLong,
  formatTime,
  onDoctorChange,
  onCancel,
  onConfirm,
}) {

  if (!confirmSlot) {
    return null;
  }

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h3>{rescheduleId ? 'Confirm Reschedule' : 'Confirm Appointment'}</h3>

        {rescheduleId && originalAppt ? (
          <table className="confirm-table">
            <thead>
              <tr>
                <td className="confirm-label"></td>
                <td className="confirm-label" style={{ color: 'var(--red-600)' }}>Original</td>
                <td className="confirm-label" style={{ color: 'var(--green-600)' }}>New</td>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="confirm-label">Clinic</td>
                <td style={{ color: 'var(--gray-500)' }}>{originalAppt.clinicName}</td>
                <td>{selectedClinic.name}</td>
              </tr>
              <tr>
                <td className="confirm-label">Service</td>
                <td style={{ color: 'var(--gray-500)' }}>{originalAppt.serviceName}</td>
                <td>{selectedService.serviceName}</td>
              </tr>
              <tr>
                <td className="confirm-label">Date</td>
                <td style={{ color: 'var(--gray-500)' }}>{formatDateLong(originalAppt.appointmentDate)}</td>
                <td>{formatDateLong(confirmSlot.date)}</td>
              </tr>
              <tr>
                <td className="confirm-label">Time</td>
                <td style={{ color: 'var(--gray-500)' }}>{formatTime(originalAppt.timeSlot)}</td>
                <td>{formatTime(confirmSlot.time)}</td>
              </tr>
            </tbody>
          </table>
        ) : (
          <table className="confirm-table">
            <tbody>
              <tr><td className="confirm-label">Clinic</td><td>{selectedClinic.name}</td></tr>
              <tr><td className="confirm-label">Service</td><td>{selectedService.serviceName}</td></tr>
              <tr><td className="confirm-label">Date</td><td>{formatDateLong(confirmSlot.date)}</td></tr>
              <tr><td className="confirm-label">Time</td><td>{formatTime(confirmSlot.time)}</td></tr>
            </tbody>
          </table>
        )}

        {doctors.length > 0 && (
          <div className="form-group" style={{ marginTop: 16 }}>
            <label>Preferred Doctor (Optional)</label>
            <select
              value={selectedDoctor?.doctorId || ''}
              onChange={onDoctorChange}
            >
              <option value="">No Preference</option>
              {doctors.map(d => (
                <option key={d.doctorId} value={d.doctorId}>{d.doctorName}</option>
              ))}
            </select>
          </div>
        )}

        <div className="form-actions form-actions--right" style={{ marginTop: 20 }}>
          <button className="btn btn-outline" onClick={onCancel}>Cancel</button>
          <button className="btn btn-primary" disabled={loading} onClick={onConfirm}>
            {loading
              ? (rescheduleId ? 'Rescheduling...' : 'Booking...')
              : (rescheduleId ? 'Confirm Reschedule' : 'Confirm Booking')}
          </button>
        </div>
      </div>
    </div>
  );
  
}
