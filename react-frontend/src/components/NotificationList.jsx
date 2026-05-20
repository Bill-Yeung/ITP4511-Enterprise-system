import { useNotifications } from '../hooks/useNotifications';
import { formatDistanceToNow } from 'date-fns';
import '../styles/NotificationList.css';

export function NotificationList() {
  const { notifications, loading, error, markAsRead, deleteNotification } =
    useNotifications();

  if (loading && notifications.length === 0) {
    return <div className="loading">Loading notifications...</div>;
  }

  const handleNotificationClick = (notification) => {
    if (!notification.is_read) {
      markAsRead(notification.notification_id);
    }
  };

  return (
    <div className="notification-list">
      <h2>Recent Updates</h2>

      {error && <div className="error-message">{error}</div>}

      {notifications.length === 0 ? (
        <div className="no-notifications">No notifications yet</div>
      ) : (
        <div className="notifications">
          {notifications.map((notification) => (
            <div
              key={notification.notification_id}
              className={`notification-item ${!notification.is_read ? 'unread' : ''}`}
              onClick={() => handleNotificationClick(notification)}
            >
              <div className="notification-header">
                <h4>{notification.title || 'Notification'}</h4>
                <span className={`notification-type type-${notification.type}`}>
                  {notification.type.replace(/_/g, ' ')}
                </span>
              </div>

              <div className="notification-message">{notification.message}</div>

              <div className="notification-footer">
                <span className="notification-time">
                  {formatDistanceToNow(new Date(notification.created_at), {
                    addSuffix: true,
                  })}
                </span>
                <button
                  className="delete-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteNotification(notification.notification_id);
                  }}
                  title="Delete notification"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
