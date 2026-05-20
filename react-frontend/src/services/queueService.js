import apiClient from './apiClient';

export const queueService = {
  getClinics: async () => {
    try {
      const response = await apiClient.get('/api/queue/clinics');
      return response.data;
    } catch (error) {
      console.error('Error fetching clinics:', error);
      throw error;
    }
  },

  getServices: async (clinicId) => {
    try {
      const response = await apiClient.get(`/api/queue/services`, {
        params: { clinicId },
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching services:', error);
      throw error;
    }
  },

  joinQueue: async (clinicId, serviceId) => {
    try {
      const response = await apiClient.post('/api/queue/join', {
        clinicId: parseInt(clinicId),
        serviceId: parseInt(serviceId),
      });
      return response.data;
    } catch (error) {
      console.error('Error joining queue:', error);
      throw error;
    }
  },

  getMyTickets: async () => {
    try {
      const response = await apiClient.get('/api/queue/my-tickets');
      return response.data;
    } catch (error) {
      console.error('Error fetching my tickets:', error);
      throw error;
    }
  },

  getStatus: async (clinicId, serviceId) => {
    try {
      const response = await apiClient.get('/api/queue/status', {
        params: { clinicId, serviceId },
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching queue status:', error);
      throw error;
    }
  },

  cancelTicket: async (ticketId) => {
    try {
      const response = await apiClient.post('/api/queue/cancel', null, {
        params: { ticketId },
      });
      return response.data;
    } catch (error) {
      console.error('Error cancelling ticket:', error);
      throw error;
    }
  },
};
