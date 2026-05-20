import { NotificationList } from '../components/NotificationList';
import '../styles/NotificationsPage.css';

export function NotificationsPage() {
  return (
    <div className="notifications-page">
      <div className="notifications-container">
        <h1>Notifications</h1>
        <NotificationList />
      </div>
    </div>
  );
}
