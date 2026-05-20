export function BookingFilters({
  clinics,
  services,
  selectedClinic,
  selectedService,
  onClinicChange,
  onServiceChange,
}) {

  return (
    <div className="booking-filters">
      <div className="booking-filter-item">
        <label>Clinic</label>
        <select value={selectedClinic?.clinicId || ''} onChange={onClinicChange}>
          <option value="">-- Select Clinic --</option>
          {(Array.isArray(clinics) ? clinics : []).map(c => (
            <option key={c.clinicId} value={c.clinicId}>{c.name}</option>
          ))}
        </select>
      </div>

      <div className="booking-filter-item">
        <label>Service</label>
        <select
          value={selectedService?.clinicServiceId || ''}
          onChange={onServiceChange}
          disabled={!selectedClinic}
        >
          <option value="">-- Select Service --</option>
          {services.map(s => (
            <option key={s.clinicServiceId} value={s.clinicServiceId}>
              {s.serviceName} ({s.slotDurationMins} min)
            </option>
          ))}
        </select>
      </div>
    </div>
  );
  
}
