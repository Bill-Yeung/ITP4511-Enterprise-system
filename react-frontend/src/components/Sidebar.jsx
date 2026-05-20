import { useNavigate, useLocation } from 'react-router-dom';

export default function Sidebar() {

  const navigate = useNavigate();
  const location = useLocation();

  const nav = (e, path) => {
    e.preventDefault();
    navigate(path);
  };

  const cls = (path) => location.pathname === path ? 'sidebar-link active' : 'sidebar-link';

  return (
    <aside className="sidebar">
      <nav className="sidebar-nav">
        <div className="sidebar-section">Overview</div>
        <a href="/dashboard" className={cls('/dashboard')} onClick={e => nav(e, '/dashboard')}>
          Dashboard
        </a>
        <a href="/clinics" className={cls('/clinics')} onClick={e => nav(e, '/clinics')}>
          Clinics &amp; Services
        </a>

        <div className="sidebar-section">Appointments</div>
        <a href="/book" className={cls('/book')} onClick={e => nav(e, '/book')}>
          Book Appointment
        </a>
        <a href="/appointments" className={cls('/appointments')} onClick={e => nav(e, '/appointments')}>
          My Appointments
        </a>

        <div className="sidebar-section">Queue</div>
        <a href="/queue" className={cls('/queue')} onClick={e => nav(e, '/queue')}>
          Walk-in Queue
        </a>
        <a href="/queue-status" className={cls('/queue-status')} onClick={e => nav(e, '/queue-status')}>
          Queue Status
        </a>

        <div className="sidebar-section">Account</div>
        <a href="/notifications" className={cls('/notifications')} onClick={e => nav(e, '/notifications')}>
          Notifications
        </a>
        <a href="/profile" className={cls('/profile')} onClick={e => nav(e, '/profile')}>
          Profile
        </a>
        <a href="/change-password" className={cls('/change-password')} onClick={e => nav(e, '/change-password')}>
          Change Password
        </a>
      </nav>
    </aside>
  );
  
}
