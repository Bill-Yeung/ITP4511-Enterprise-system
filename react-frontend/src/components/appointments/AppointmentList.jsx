import { AppointmentCard } from './AppointmentCard';

export function AppointmentList({
  appointments,
  tab,
  formatTime,
  statusBadge,
  canCancel,
  canReschedule,
  onCancel,
  onReschedule,
  onBook,
}) {

  if (appointments.length === 0) {
    return (
      <div className="empty-state">
        <p>{tab === 'upcoming' ? 'No upcoming appointments.' : 'No past appointments.'}</p>
        {tab === 'upcoming' && (
          <button className="btn btn-primary" onClick={onBook}>
            Book an Appointment
          </button>
        )}
      </div>
    );
  }

  return (
    <div className="appt-list">
      {appointments.map(a => (
        <AppointmentCard
          key={a.appointmentId}
          appointment={a}
          tab={tab}
          formatTime={formatTime}
          statusBadge={statusBadge}
          canCancel={canCancel}
          canReschedule={canReschedule}
          onCancel={onCancel}
          onReschedule={onReschedule}
        />
      ))}
    </div>
  );
  
}
