export function OperatingHoursTable({ dayOrder, hours, todayName, formatTime }) {

  return (
    <div className="card" style={{ marginTop: 16 }}>
      <div className="card-header">Operating Hours</div>
      <div className="card-body">
        <table className="dashboard-table">
          <thead>
            <tr>
              <th>Day</th>
              <th>Open</th>
              <th>Close</th>
            </tr>
          </thead>
          <tbody>
            {dayOrder.map(day => {
              const h = hours.find(x => x.dayOfWeek === day);
              const isToday = day === todayName;
              return (
                <tr key={day} style={isToday ? { fontWeight: 600, color: 'var(--primary)' } : {}}>
                  <td>{day}{isToday ? ' (Today)' : ''}</td>
                  <td>{h ? formatTime(h.openTime) : '-'}</td>
                  <td>{h ? formatTime(h.closeTime) : <span style={{ color: 'var(--red-600)' }}>Closed</span>}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
  
}
