export function ClinicServiceList({
  services,
  filteredServices,
  filterService,
  onFilterChange,
  onBookService,
}) {

  const uniqueServiceNames = [...new Set(services.map(s => s.serviceName))];

  return (
    <div className="card">
      <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span>Services ({filteredServices.length})</span>
        {uniqueServiceNames.length > 1 && (
          <select
            className="filter-select"
            value={filterService}
            onChange={e => onFilterChange(e.target.value)}
          >
            <option value="">All Services</option>
            {uniqueServiceNames.map(name => (
              <option key={name} value={name}>{name}</option>
            ))}
          </select>
        )}
      </div>
      <div className="card-body">
        {filteredServices.length === 0 ? (
          <p style={{ color: 'var(--gray-500)' }}>No services available.</p>
        ) : (
          <div className="service-list">
            {filteredServices.map(s => (
              <div key={s.clinicServiceId} className="service-item">
                <div className="service-item-info">
                  <div className="service-item-name">{s.serviceName}</div>
                  <div className="service-item-meta">
                    {s.slotDurationMins} min per slot - {s.quotaPerSlot} patient{s.quotaPerSlot > 1 ? 's' : ''} per slot
                  </div>
                </div>
                <button
                  className="btn btn-outline btn-sm"
                  onClick={() => onBookService(s)}
                >
                  Book
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
  
}
