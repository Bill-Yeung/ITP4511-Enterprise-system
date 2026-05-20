import apiClient from './apiClient';

export const notificationService = {
  getNotifications: async (page = 1, limit = 20) => {
    try {
      const response = await apiClient.get('/api/notification', {
        params: { page, limit },
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching notifications:', error);
      throw error;
    }
  },

  markAsRead: async (notificationId) => {
    try {
      const response = await apiClient.post('/api/notification', null, {
        params: { action: 'markRead', notificationId },
      });
      return response.data;
    } catch (error) {
      console.error('Error marking notification as read:', error);
      throw error;
    }
  },

  deleteNotification: async (notificationId) => {
    try {
      const response = await apiClient.post('/api/notification', null, {
        params: { action: 'delete', notificationId },
      });
      return response.data;
    } catch (error) {
      console.error('Error deleting notification:', error);
      throw error;
    }
  },

  markAllAsRead: async () => {
    try {
      const response = await apiClient.post('/api/notification', null, {
        params: { action: 'markAllRead' },
      });
      return response.data;
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      throw error;
    }
  },
};
