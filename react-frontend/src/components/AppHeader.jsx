import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../hooks/useNotifications';

export default function AppHeader() {

  const { user } = useAuth();
  const navigate = useNavigate();
  const { unreadCount } = useNotifications();

  const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/logout';
  };

  return (
    <header className="dashboard-header">
      <div className="header-brand" style={{ cursor: 'pointer' }} onClick={() => navigate('/dashboard')}>
        <span className="header-logo">CCHC</span>
        <span className="header-brand-text">
          <span className="header-brand-name">CCHC Clinic System</span>
          <span className="header-brand-sub">Appointment &amp; Queue System</span>
        </span>
      </div>
      <div className="header-user">
        <span className="header-user-name">{user?.fullName}</span>
        <span className="header-user-role">PATIENT</span>
        <button
          className="header-notif-btn"
          onClick={() => navigate('/notifications')}
          title="Notifications"
        >
          <svg className="header-notif-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          {unreadCount > 0 && (
            <span className="header-notif-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
          )}
        </button>
        <button onClick={handleLogout} className="header-logout-btn">Logout</button>
      </div>
    </header>
  );
  
}
