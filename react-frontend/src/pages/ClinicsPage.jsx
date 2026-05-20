import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ClinicInfoCard } from '../components/clinics/ClinicInfoCard';
import { ClinicServiceList } from '../components/clinics/ClinicServiceList';
import { ClinicTabs } from '../components/clinics/ClinicTabs';
import { OperatingHoursTable } from '../components/clinics/OperatingHoursTable';
import { clinicService } from '../services/clinicService';

const DAY_ORDER = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const formatTime = (t) => {
  const [h, m] = t.split(':');
  const hour = parseInt(h, 10);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const h12 = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
  return `${h12}:${m} ${ampm}`;
};

export default function ClinicsPage() {

  const navigate = useNavigate();

  // Page data
  const [clinics, setClinics] = useState([]);
  const [selectedClinic, setSelectedClinic] = useState(null);
  const [services, setServices] = useState([]);
  const [hours, setHours] = useState([]);
  const [filterService, setFilterService] = useState('');
  const [loading, setLoading] = useState(true);

  // Initial load

  useEffect(() => {

    let cancelled = false;

    clinicService.getAll()
      .then((list) => {

        if (cancelled) {
          return;
        }
        setClinics(list);

        if (list.length > 0) {
          const firstClinic = list[0];
          setSelectedClinic(firstClinic);
          setFilterService('');
          clinicService.getServices(firstClinic.clinicId)
            .then((data) => { if (!cancelled) {
              setServices(data);
            } });
          clinicService.getHours(firstClinic.clinicId)
            .then((data) => { if (!cancelled) {
              setHours(data); 
            }});
        }

      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // User actions

  const selectClinic = (clinic) => {
    setSelectedClinic(clinic);
    setFilterService('');
    clinicService.getServices(clinic.clinicId)
      .then(setServices);
    clinicService.getHours(clinic.clinicId)
      .then(setHours);
  };

  // Derived values

  const filteredServices = filterService
    ? services.filter(s => s.serviceName === filterService)
    : services;

  const todayName = new Date().toLocaleDateString('en-US', { weekday: 'long' });
  const todayHours = hours.find(h => h.dayOfWeek === todayName);

  if (loading) {
    return <p>Loading clinics...</p>;
  }

  return (
    <>
      <div className="page-header">
        <h2>Clinics & Services</h2>
      </div>

      <ClinicTabs
        clinics={clinics}
        selectedClinic={selectedClinic}
        onSelect={selectClinic}
      />

      {selectedClinic && (
        <div className="clinics-layout-grid">
          <div>
            <ClinicInfoCard
              clinic={selectedClinic}
              todayHours={todayHours}
              formatTime={formatTime}
              onBook={() => navigate(`/book?clinicId=${selectedClinic.clinicId}`)}
              onJoinQueue={() => navigate('/queue')}
            />
            <OperatingHoursTable
              dayOrder={DAY_ORDER}
              hours={hours}
              todayName={todayName}
              formatTime={formatTime}
            />
          </div>

          <div>
            <ClinicServiceList
              services={services}
              filteredServices={filteredServices}
              filterService={filterService}
              onFilterChange={setFilterService}
              onBookService={(service) => navigate(`/book?clinicServiceId=${service.clinicServiceId}`)}
            />
          </div>
        </div>
      )}
    </>
  );
  
}
