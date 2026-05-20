export function AppointmentTabs({ tab, upcomingCount, pastCount, onChange }) {

  return (
    <div className="tabs">
      <button
        className={`tab ${tab === 'upcoming' ? 'active' : ''}`}
        onClick={() => onChange('upcoming')}
      >
        Upcoming ({upcomingCount})
      </button>
      <button
        className={`tab ${tab === 'past' ? 'active' : ''}`}
        onClick={() => onChange('past')}
      >
        Past ({pastCount})
      </button>
    </div>
  );
  
}
