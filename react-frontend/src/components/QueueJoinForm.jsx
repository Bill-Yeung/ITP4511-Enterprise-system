import { useState, useEffect } from 'react';
import { queueService } from '../services/queueService';
import '../styles/QueueJoinForm.css';

export function QueueJoinForm({ onJoinSuccess }) {
  const [clinics, setClinics] = useState([]);
  const [services, setServices] = useState([]);
  const [selectedClinic, setSelectedClinic] = useState('');
  const [selectedService, setSelectedService] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [popupMessage, setPopupMessage] = useState(null);

  // Load clinics on mount
  useEffect(() => {
    const loadClinics = async () => {
      setLoading(true);
      try {
        const data = await queueService.getClinics();
        setClinics(data);
      } catch {
        setError('Failed to load clinics');
      } finally {
        setLoading(false);
      }
    };
    loadClinics();
  }, []);

  const handleClinicChange = async (e) => {
    const clinicId = e.target.value;
    setSelectedClinic(clinicId);
    setSelectedService('');
    setServices([]);
    setError(null);

    if (!clinicId) return;

    setLoading(true);
    try {
      const data = await queueService.getServices(clinicId);
      setServices(data);
    } catch {
      setError('Failed to load services');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedClinic || !selectedService) {
      setError('Please select clinic and service');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const result = await queueService.joinQueue(selectedClinic, selectedService);
      if (onJoinSuccess) {
        onJoinSuccess(result);
      }
    } catch (err) {
      const serverMsg = err.response?.data?.error;
      if (serverMsg) {
        setPopupMessage(serverMsg);
      } else if (err.response?.status === 409) {
        setPopupMessage('You already have an active ticket for this service today');
      } else {
        setPopupMessage('Failed to join queue. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
    {popupMessage && (
      <div className="queue-popup-overlay" onClick={() => setPopupMessage(null)}>
        <div className="queue-popup" onClick={(e) => e.stopPropagation()}>
          <h3>Notice</h3>
          <p>{popupMessage}</p>
          <button type="button" onClick={() => setPopupMessage(null)}>OK</button>
        </div>
      </div>
    )}
    <form className="queue-join-form" onSubmit={handleSubmit}>
      <h2>Join Queue</h2>

      <div className="queue-join-form-body">
        {error && <div className="error-message">{error}</div>}

        <div className="form-group">
          <label htmlFor="clinic">Clinic</label>
          <select
            id="clinic"
            value={selectedClinic}
            onChange={handleClinicChange}
            disabled={loading}
          >
            <option value="">-- Choose a clinic --</option>
            {clinics.map((clinic) => (
              <option key={clinic.clinic_id} value={clinic.clinic_id}>
                {clinic.name} - {clinic.district}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="service">Service</label>
          <select
            id="service"
            value={selectedService}
            onChange={(e) => setSelectedService(e.target.value)}
            disabled={!selectedClinic || loading}
          >
            <option value="">-- Choose a service --</option>
            {services.map((service) => (
              <option key={service.service_id} value={service.service_id}>
                {service.name} ({service.slotDurationMins} min)
              </option>
            ))}
          </select>
        </div>

        <button type="submit" disabled={loading || !selectedService}>
          {loading ? 'Joining...' : 'Join Queue'}
        </button>
      </div>
    </form>
    </>
  );
}
