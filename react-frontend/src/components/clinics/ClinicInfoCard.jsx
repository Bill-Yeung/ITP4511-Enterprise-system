export function ClinicInfoCard({ clinic, todayHours, formatTime, onBook, onJoinQueue }) {

  return (
    <div className="card">
      <div className="card-header">{clinic.name}</div>
      <div className="card-body">
        <p style={{ color: 'var(--gray-600)', margin: '0 0 12px' }}>
          {clinic.location}
        </p>
        {todayHours ? (
          <p style={{ margin: 0 }}>
            <strong>Open today:</strong> {formatTime(todayHours.openTime)} - {formatTime(todayHours.closeTime)}
          </p>
        ) : (
          <p style={{ color: 'var(--red-600)', margin: 0 }}><strong>Closed today</strong></p>
        )}
        <div style={{ marginTop: 12 }}>
          <button className="btn btn-primary btn-sm" onClick={onBook}>
            Book at this clinic
          </button>
          {todayHours && clinic.queueEnabled && (
            <button className="btn btn-outline btn-sm" style={{ marginLeft: 8 }} onClick={onJoinQueue}>
              Join Queue
            </button>
          )}
        </div>
      </div>
    </div>
  );
  
}
