import { useState, useRef, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { clinicService } from '../services/clinicService';
import { appointmentService } from '../services/appointmentService';
import { BookingConfirmModal } from '../components/booking/BookingConfirmModal';
import { BookingFilters } from '../components/booking/BookingFilters';
import { DateStrip } from '../components/booking/DateStrip';
import { SlotList } from '../components/booking/SlotList';
import toast from 'react-hot-toast';

const VISIBLE_DAYS = 7;
const FULL_DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

const toDateStr = (d) => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

const isToday = (d) => {
  const today = new Date();
  return d.getFullYear() === today.getFullYear() && d.getMonth() === today.getMonth() && d.getDate() === today.getDate();
};

const isPastDate = (d) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return d < today;
};

const isPastSlot = (dateStr, timeStr) => {
  const now = new Date();
  const [h, m] = timeStr.split(':').map(Number);
  const slotDate = new Date(dateStr + 'T00:00:00');
  if (!isToday(slotDate)) {
    return false;
  }
  return h < now.getHours() || (h === now.getHours() && m <= now.getMinutes());
};

// List of dates for date selector
const getStripDates = (dateOffset) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Array.from({ length: VISIBLE_DAYS }, (_, i) => {
    const d = new Date(today);
    d.setDate(today.getDate() + dateOffset + i);
    return d;
  });
};

const formatTime = (t) => {
  const [h, m] = t.split(':');
  const hour = parseInt(h, 10);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const h12 = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
  return `${h12}:${m} ${ampm}`;
};

const formatDateLong = (dateStr) => {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-US', { weekday: 'long', day: '2-digit', month: 'short', year: 'numeric' });
};

const getSlotHour = (slot) => parseInt(slot.time.split(':')[0], 10);

export default function BookAppointmentPage() {
  
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const sseRef = useRef(null);
  const selectedDateRef = useRef(null);

  const rescheduleId = searchParams.get('reschedule');
  const preselectedCsId = searchParams.get('clinicServiceId');
  const preselectedClinicId = searchParams.get('clinicId');
  const origDate = searchParams.get('origDate');
  const origTime = searchParams.get('origTime');

  // Page data
  const [clinics, setClinics] = useState([]);
  const [services, setServices] = useState([]);
  const [slots, setSlots] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [slotsLoading, setSlotsLoading] = useState(false);

  // User selection
  const [selectedClinic, setSelectedClinic] = useState(null);
  const [selectedService, setSelectedService] = useState(null);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null);
  const [dateOffset, setDateOffset] = useState(0);
  const [confirmSlot, setConfirmSlot] = useState(null);
  const [clinicHours, setClinicHours] = useState([]);
  const [originalAppt, setOriginalAppt] = useState(null);

  const openDays = [...new Set(clinicHours.map(h => h.dayOfWeek))];
  const stripDates = getStripDates(dateOffset);
  const morningSlots = slots.filter(s => getSlotHour(s) < 12);
  const afternoonSlots = slots.filter(s => getSlotHour(s) >= 12);

  const isDayClosed = (d) => !openDays.includes(FULL_DAY_NAMES[d.getDay()]);

  // Data loading

  const filterServicesForReschedule = useCallback((list, original = originalAppt) => {
    
    if (!rescheduleId || !original) {
      return list;
    }
    return list.filter(s => s.serviceName === original.serviceName);

  }, [originalAppt, rescheduleId]);

  const fetchServicesAndHours = useCallback((clinic) => {

    clinicService.getServices(clinic.clinicId)
      .then(list => setServices(filterServicesForReschedule(list)))
      .catch(() => setError('Failed to load services'));

    clinicService.getHours(clinic.clinicId)
      .then(setClinicHours)
      .catch(() => {});

  }, [filterServicesForReschedule]);

  const fetchSlots = (service, dateStr) => {

    setSlotsLoading(true);
    setSlots([]);

    clinicService.getSlots(service.clinicServiceId, dateStr)
      .then(setSlots)
      .catch(() => setSlots([]))
      .finally(() => setSlotsLoading(false));

  };

  // Live slot updates

  const disconnectSSE = useCallback(() => {
    if (sseRef.current) {
      sseRef.current.close();
      sseRef.current = null;
    }
  }, []);

  const connectSSE = useCallback((service) => {
    disconnectSSE();
    const source = new EventSource(`/api/sse/slots?clinicServiceId=${service.clinicServiceId}`);
    sseRef.current = source;
    source.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        const currentDate = selectedDateRef.current;
        if (data.date && Array.isArray(data.slots) && currentDate && data.date === toDateStr(currentDate)) {
          setSlots(data.slots);
        }
      } catch { }
    };
    source.onerror = () => {
      source.close();
      sseRef.current = null;
    };
  }, [disconnectSSE]);

  useEffect(() => {
    selectedDateRef.current = selectedDate;
  }, [selectedDate]);

  // Initial load

  useEffect(() => {

    if (!user) {
      return;
    }
    let cancelled = false;

    const loadInitialData = async () => {

      try {

        const clinicList = await clinicService.getAll();
        if (cancelled) {
          return;
        }
        setClinics(clinicList);

        // Load original appointment (if any)

        let original = null;
        if (rescheduleId) {
          const appointments = await appointmentService.getAll();
          if (cancelled) {
            return;
          }
          original = appointments.find(a => a.appointmentId === parseInt(rescheduleId, 10)) || null;
          if (original) {
            setOriginalAppt(original);
          }
        }

        if (preselectedClinicId && !preselectedCsId) {
          const cId = parseInt(preselectedClinicId, 10);
          const clinic = clinicList.find(c => c.clinicId === cId);
          if (clinic) {
            setSelectedClinic(clinic);
            fetchServicesAndHours(clinic);
          }
          return;
        }

        if (preselectedCsId) {
          const csId = parseInt(preselectedCsId, 10);
          for (const clinic of clinicList) {
            try {
              
              const svcList = await clinicService.getServices(clinic.clinicId);
              if (cancelled) {
                return;
              }

              const match = svcList.find(s => s.clinicServiceId === csId);
              if (!match) {
                continue;
              }

              setSelectedClinic(clinic);
              setServices(filterServicesForReschedule(svcList, original));
              setSelectedService(match);
              clinicService.getHours(clinic.clinicId).then(setClinicHours).catch(() => {});
              connectSSE(match);

              if (origDate) {
                const d = new Date(origDate + 'T00:00:00');
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                const daysDiff = Math.floor((d - today) / (1000 * 60 * 60 * 24));
                setDateOffset(Math.max(0, Math.floor(daysDiff / 7) * 7));
                setSelectedDate(d);
                selectedDateRef.current = d;
                clinicService.getDoctors(clinic.clinicId, origDate).then(setDoctors).catch(() => setDoctors([]));
                clinicService.getSlots(match.clinicServiceId, origDate).then(setSlots).catch(() => {});
              }

              break;

            } catch {}
          }
        }
      } catch {
        if (!cancelled) {
          setError('Failed to load clinics');
        }
      }
    };

    loadInitialData();

    return () => {
      cancelled = true;
      disconnectSSE();
    };

  }, [
    user,
    rescheduleId,
    preselectedClinicId,
    preselectedCsId,
    origDate,
    fetchServicesAndHours,
    connectSSE,
    disconnectSSE,
    filterServicesForReschedule,
  ]);

  // User actions

  const handleClinicChange = (e) => {

    disconnectSSE();
    const clinicId = parseInt(e.target.value, 10);
    const clinic = clinics.find(c => c.clinicId === clinicId) || null;
    setSelectedClinic(clinic);
    setSelectedService(null);
    setServices([]);
    setDoctors([]);
    setSlots([]);
    setClinicHours([]);
    setConfirmSlot(null);
    setSelectedDoctor(null);
    setSelectedDate(null);
    selectedDateRef.current = null;
    setDateOffset(0);
    if (clinic) {
      fetchServicesAndHours(clinic);
    }

  };

  const handleServiceChange = (e) => {

    disconnectSSE();
    const csId = parseInt(e.target.value, 10);
    const svc = services.find(s => s.clinicServiceId === csId) || null;
    setSelectedService(svc);
    setSlots([]);
    setConfirmSlot(null);
    setSelectedDate(null);
    selectedDateRef.current = null;
    setDateOffset(0);
    if (svc) {
      connectSSE(svc);
    }

  };

  const handleDateSelect = (d) => {

    if (isPastDate(d) || isDayClosed(d)) {
      return;
    }
    setSelectedDate(d);
    selectedDateRef.current = d;
    setConfirmSlot(null);
    setSelectedDoctor(null);
    if (selectedClinic) {
      clinicService.getDoctors(selectedClinic.clinicId, toDateStr(d))
        .then(setDoctors)
        .catch(() => setDoctors([]));
    }
    if (selectedService) {
      fetchSlots(selectedService, toDateStr(d));
    }
  };

  const handleSlotClick = (time) => {
    setConfirmSlot({ date: toDateStr(selectedDate), time });
  };

  const handleDoctorChange = (e) => {
    const val = e.target.value;
    setSelectedDoctor(val ? (doctors.find(d => d.doctorId === parseInt(val, 10)) || null) : null);
  };

  const handleBook = async () => {

    if (!confirmSlot) {
      return;
    }
    setError('');
    setLoading(true);

    try {

      const data = rescheduleId
        ? await appointmentService.reschedule(rescheduleId, selectedService.clinicServiceId, confirmSlot.date, confirmSlot.time, selectedDoctor?.doctorId)
        : await appointmentService.book(selectedService.clinicServiceId, confirmSlot.date, confirmSlot.time, selectedDoctor?.doctorId);
      
      if (data?.error) {

        toast.error(data.error);
        setConfirmSlot(null);

      } else {

        toast.success(rescheduleId
          ? 'Appointment rescheduled successfully!'
          : 'Appointment booked successfully!');
        setConfirmSlot(null);
        setTimeout(() => navigate('/appointments'), 1500);

      }

    } catch {
      toast.error('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <h2>{rescheduleId ? 'Reschedule Appointment' : 'Book Appointment'}</h2>
      </div>

        {error && <div className="alert alert-error">{error}</div>}

        {/* ===== Filters (clincs and services) ===== */}
        <BookingFilters
          clinics={clinics}
          services={services}
          selectedClinic={selectedClinic}
          selectedService={selectedService}
          onClinicChange={handleClinicChange}
          onServiceChange={handleServiceChange}
        />

        {/* Approval-required service hint */}
        {selectedService?.requiresApproval && (
          <div className="alert" style={{
            background: 'var(--orange-50)',
            color: 'var(--orange-600)',
            border: '1px dashed var(--orange-600)',
            padding: '10px 14px',
            borderRadius: '6px',
            margin: '12px 0',
            fontSize: '14px'
          }}>
            <strong>Note:</strong> This service requires staff approval. Your booking will be marked
            <em> Pending</em> until clinic staff confirm it.
          </div>
        )}

        {/* ===== Date Strip ===== */}
        {selectedService && (
          <DateStrip
            stripDates={stripDates}
            dateOffset={dateOffset}
            selectedDate={selectedDate}
            visibleDays={VISIBLE_DAYS}
            isDayClosed={isDayClosed}
            isPastDate={isPastDate}
            isToday={isToday}
            toDateStr={toDateStr}
            onDateSelect={handleDateSelect}
            onDateOffsetChange={setDateOffset}
            onResetDate={() => {
              setDateOffset(0);
              setSelectedDate(null);
              selectedDateRef.current = null;
              setDoctors([]);
              setSelectedDoctor(null);
              setSlots([]);
              setConfirmSlot(null);
            }}
          />
        )}

        {/* Available Time Slots ===== */}
        {selectedService && !selectedDate && (
          <div className="empty-state">
            {isDayClosed(new Date()) ? (
              <>
                <p style={{ color: 'var(--red-600)', fontWeight: 600 }}>This clinic is closed today.</p>
                <p>Please select another date above to view available time slots.</p>
              </>
            ) : (
              <p>Select a date above to view available time slots.</p>
            )}
          </div>
        )}

        {selectedService && selectedDate && (
          <SlotList
            slots={slots}
            slotsLoading={slotsLoading}
            selectedDate={selectedDate}
            morningSlots={morningSlots}
            afternoonSlots={afternoonSlots}
            origDate={origDate}
            origTime={origTime}
            toDateStr={toDateStr}
            formatDateLong={formatDateLong}
            formatTime={formatTime}
            isPastSlot={isPastSlot}
            onSlotClick={handleSlotClick}
          />
        )}

        {!selectedService && (
          <div className="empty-state">
            <p>{selectedClinic
              ? `Select a service at ${selectedClinic.name} to continue.`
              : 'Select a clinic and service to view available time slots.'
            }</p>
          </div>
        )}

      {/* ===== Confirmation Modal ===== */}
      <BookingConfirmModal
        rescheduleId={rescheduleId}
        originalAppt={originalAppt}
        selectedClinic={selectedClinic}
        selectedService={selectedService}
        confirmSlot={confirmSlot}
        doctors={doctors}
        selectedDoctor={selectedDoctor}
        loading={loading}
        formatDateLong={formatDateLong}
        formatTime={formatTime}
        onDoctorChange={handleDoctorChange}
        onCancel={() => setConfirmSlot(null)}
        onConfirm={handleBook}
      />
    </>
  );

}
