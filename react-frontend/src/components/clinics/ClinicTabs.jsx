export function ClinicTabs({ clinics, selectedClinic, onSelect }) {

  return (
    <div className="clinic-tabs">
      {clinics.map(c => (
        <button
          key={c.clinicId}
          className={'clinic-tab' + (selectedClinic?.clinicId === c.clinicId ? ' active' : '')}
          onClick={() => onSelect(c)}
        >
          {c.name}
        </button>
      ))}
    </div>
  );
  
}
