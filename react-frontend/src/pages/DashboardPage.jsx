import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { appointmentService } from '../services/appointmentService';
import { queueService } from '../services/queueService';
import { formatTicket } from '../utils/clinicCode';

const toDateStr = (d) => d.toISOString().slice(0, 10);

const getWeekDates = () => {

  const today = new Date();
  const daysLeft = 7 - today.getDay();
  const dates = [];

  for (let i = 0; i < daysLeft; i++) {
    const d = new Date(today);
    d.setDate(today.getDate() + i);
    dates.push(toDateStr(d));
  }

  return dates;

};

const formatDay = (d) => {

  const date = new Date(d + 'T00:00:00');
  const todayStr = toDateStr(new Date());
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);

  let label = date.toLocaleDateString('en-US', { weekday: 'long', day: 'numeric', month: 'short' });
  if (d === todayStr) {
    label = 'Today, ' + label;
  } else if (d === toDateStr(tomorrow)) {
    label = 'Tomorrow, ' + label;
  }
  return label;

};

const formatTime = (t) => t ? t.substring(0, 5) : '';

const statusColor = (s) => {
  switch (s) {
    case 'Serving':
      return 'var(--green-600)';
    case 'Waiting':
      return 'var(--primary)';
    case 'Completed':
      return 'var(--gray-400)';
    case 'Cancelled':
      return 'var(--red-600)';
    default:
      return 'var(--gray-600)';
  }
};

export default function DashboardPage() {

  const { user } = useAuth();
  const navigate = useNavigate();

  // Page data
  const [weekDays, setWeekDays] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [stats, setStats] = useState({ total: 0, booked: 0, completed: 0, cancelled: 0 });
  const [loading, setLoading] = useState(true);

  // Initial load

  useEffect(() => {

    if (!user) {
      return;
    }
    let cancelled = false;

    const todayStr = toDateStr(new Date());
    const dates = getWeekDates();

    appointmentService.getAll()
      .then(all => {
        if (cancelled) {
          return;
        }
        setStats({
          total: all.length,
          booked: all.filter(a => a.status === 'Booked' && a.appointmentDate >= todayStr).length,
          completed: all.filter(a => a.status === 'Completed').length,
          cancelled: all.filter(a => a.status === 'Cancelled').length,
        });
        setWeekDays(dates.map(date => ({
          date,
          appointments: all
            .filter(a => a.appointmentDate === date && a.status === 'Booked')
            .sort((a, b) => a.timeSlot.localeCompare(b.timeSlot)),
        })));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    queueService.getMyTickets()
      .then(data => {
        if (!cancelled) {
          setTickets(Array.isArray(data) ? data : []);
        }
      })
      .catch(() => {});

    return () => {
      cancelled = true;
    };

  }, [user]);

  // Derived values

  const todayStr = toDateStr(new Date());
  const hasAnything = weekDays.some(d => d.appointments.length > 0) || tickets.length > 0;

  return (
    <>
      <h2>Welcome back, {user.fullName}</h2>
      <p className="page-subtitle">Here's an overview of your account.</p>

      {/* Stats row */}
      <div className="dashboard-stats">
        <div className="stat-card">
          <div className="stat-value stat-queue">{tickets.length}</div>
          <div className="stat-label">Queue Tickets Today</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{stats.total}</div>
          <div className="stat-label">Total Appointments</div>
        </div>
        <div className="stat-card">
          <div className="stat-value stat-booked">{stats.booked}</div>
          <div className="stat-label">Upcoming</div>
        </div>
        <div className="stat-card">
          <div className="stat-value stat-completed">{stats.completed}</div>
          <div className="stat-label">Completed</div>
        </div>
      </div>

      <div className="dashboard-main-grid">

        <div className="card">
          <div className="card-header">Quick Actions</div>
          <div className="card-body">
            <div className="quick-actions-stack">
              <button className="btn btn-primary btn-block" onClick={() => navigate('/book')}>Book Appointment</button>
              <button className="btn btn-outline btn-block" onClick={() => navigate('/queue')}>Join Queue</button>
              <button className="btn btn-outline btn-block" onClick={() => navigate('/queue-status')}>Queue Status</button>
              <button className="btn btn-outline btn-block" onClick={() => navigate('/appointments')}>All Appointments</button>
              <button className="btn btn-outline btn-block" onClick={() => navigate('/profile')}>Profile</button>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">This Week</div>
          <div className="card-body">
            {loading ? <p>Loading...</p> : !hasAnything ? (
              <p className="empty-inline">Nothing scheduled this week. <a href="/book" onClick={e => { e.preventDefault(); navigate('/book'); }}>Book an appointment</a></p>
            ) : (
              <div className="week-timeline">
                {weekDays.map(day => {
                  const isToday = day.date === todayStr;
                  const todayTickets = isToday ? tickets : [];
                  if (day.appointments.length === 0 && todayTickets.length === 0) return null;

                  return (
                    <div key={day.date} className="week-day">
                      <div className={'week-day-header' + (isToday ? ' week-day-today' : '')}>
                        {formatDay(day.date)}
                      </div>
                      <div className="week-day-items">
                        {todayTickets.map(t => (
                          <div key={'q' + t.ticket_id} className="week-item">
                            <span className="week-item-time">QUEUE</span>
                            <span className="week-item-detail">
                              {t.clinicName} - {t.serviceName} <span className="week-item-doctor">(Ticket {formatTicket(t.clinicName, t.queue_number)})</span>
                            </span>
                            <span className="week-item-badge week-item-badge-queue" style={{ color: statusColor(t.status) }}>{t.status}</span>
                          </div>
                        ))}
                        {day.appointments.map(a => (
                          <div key={'a' + a.appointmentId} className="week-item">
                            <span className="week-item-time">{formatTime(a.timeSlot)}</span>
                            <span className="week-item-detail">
                              {a.clinicName} - {a.serviceName}
                              {a.doctorName ? <span className="week-item-doctor"> - {a.doctorName}</span> : null}
                            </span>
                            <span className="week-item-badge week-item-badge-booked">BOOKED</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

      </div>
    </>
  );
  
}
