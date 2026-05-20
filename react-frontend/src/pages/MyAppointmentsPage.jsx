import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AppointmentList } from '../components/appointments/AppointmentList';
import { AppointmentTabs } from '../components/appointments/AppointmentTabs';
import { CancelAppointmentModal } from '../components/appointments/CancelAppointmentModal';
import { appointmentService } from '../services/appointmentService';
import toast from 'react-hot-toast';

const CANCELLATION_CUTOFF_HOURS = 2;
const RESCHEDULE_CUTOFF_HOURS = 24;
const ACTIVE_STATUSES = ['Pending', 'Booked', 'Arrived'];

const compareAppointmentTime = (a, b) => (
  a.appointmentDate.localeCompare(b.appointmentDate) || a.timeSlot.localeCompare(b.timeSlot)
);

const formatDate = (d) => {
  const date = new Date(d + 'T00:00:00');
  return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
};

const formatTime = (t) => {
  const [h, m] = t.split(':');
  const hour = parseInt(h, 10);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const h12 = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
  return `${h12}:${m} ${ampm}`;
};

const getHoursUntilAppointment = (a) => {
  const apptTime = new Date(`${a.appointmentDate}T${a.timeSlot}`);
  const now = new Date();
  return (apptTime - now) / (1000 * 60 * 60);
};

const canCancel = (a) => {
  if (a.status !== 'Pending' && a.status !== 'Booked') {
    return false;
  }
  return getHoursUntilAppointment(a) >= CANCELLATION_CUTOFF_HOURS;
};

const canReschedule = (a) => {
  if (a.status !== 'Booked') {
    return false;
  }
  return getHoursUntilAppointment(a) >= RESCHEDULE_CUTOFF_HOURS;
};

const statusBadge = (status, cancelReason) => {

  if (status === 'Cancelled' && cancelReason && cancelReason.toLowerCase().includes('rescheduled')) {
    return <span className="badge badge-rescheduled">Rescheduled</span>;
  }

  const cls = {
    Pending: 'badge-pending',
    Booked: 'badge-booked',
    Arrived: 'badge-arrived',
    Completed: 'badge-completed',
    Cancelled: 'badge-cancelled',
    'No-show': 'badge-noshow',
  };

  return <span className={`badge ${cls[status] || ''}`}>{status}</span>;

};

export default function MyAppointmentsPage() {

  const { user } = useAuth();
  const navigate = useNavigate();

  // Page data
  const [appointments, setAppointments] = useState([]);
  const [tab, setTab] = useState('upcoming');
  const [error, setError] = useState('');
  const [cancelModal, setCancelModal] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [loading, setLoading] = useState(false);

  // Data loading

  const loadAppointments = useCallback((cancelled = () => false) => {
    appointmentService.getAll()
      .then((data) => {
        if (!cancelled()) {
          setAppointments(data);
        }
      })
      .catch(() => {
        if (!cancelled()) {
          setError('Failed to load appointments');
        }
      });
  }, []);

  // Initial load

  useEffect(() => {

    if (!user) {
      return;
    }
    let cancelled = false;

    loadAppointments(() => cancelled);

    return () => {
      cancelled = true;
    };

  }, [user, loadAppointments]);

  // User actions

  const handleCancel = async () => {

    if (!cancelModal) {
      return;
    }
    setLoading(true);
    setError('');

    try {

      const data = await appointmentService.cancel(cancelModal.appointmentId, cancelReason.trim());
      if (data?.error) {
        toast.error(data.error);
      } else {
        toast.success('Appointment cancelled successfully.');
        setCancelModal(null);
        setCancelReason('');
        loadAppointments();
      }

    } catch {
      toast.error('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleReschedule = (appt) => {
    navigate(`/book?reschedule=${appt.appointmentId}&clinicServiceId=${appt.clinicServiceId}&origDate=${appt.appointmentDate}&origTime=${appt.timeSlot}`);
  };

  // Derived values

  const upcoming = appointments.filter(a => ACTIVE_STATUSES.includes(a.status)).sort(compareAppointmentTime);
  const past = appointments.filter(a => !ACTIVE_STATUSES.includes(a.status)).sort((a, b) => -compareAppointmentTime(a, b));
  const displayed = tab === 'upcoming' ? upcoming : past;

  return (
    <>
      <div className="page-header">
        <h2>My Appointments</h2>
        <button className="btn btn-primary" onClick={() => navigate('/book')}>
          Book New
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <AppointmentTabs
        tab={tab}
        upcomingCount={upcoming.length}
        pastCount={past.length}
        onChange={setTab}
      />

      <AppointmentList
        appointments={displayed}
        tab={tab}
        formatTime={formatTime}
        statusBadge={statusBadge}
        canCancel={canCancel}
        canReschedule={canReschedule}
        onCancel={setCancelModal}
        onReschedule={handleReschedule}
        onBook={() => navigate('/book')}
      />

      <CancelAppointmentModal
        appointment={cancelModal}
        reason={cancelReason}
        loading={loading}
        formatDate={formatDate}
        formatTime={formatTime}
        onReasonChange={setCancelReason}
        onClose={() => setCancelModal(null)}
        onConfirm={handleCancel}
      />
    </>
  );
  
}
