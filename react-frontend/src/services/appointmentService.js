import apiClient, { toArray, toErrorData } from './apiClient';

export const appointmentService = {

  getAll: async () => {
    const response = await apiClient.get('/api/appointments');
    return toArray(response.data);
  },

  book: async (clinicServiceId, date, time, doctorId) => {
    try {
      const response = await apiClient.post('/api/appointments', new URLSearchParams({
        action: 'book',
        clinicServiceId,
        date,
        time,
        doctorId: doctorId || '',
      }));
      return response.data;
    } catch (error) {
      return toErrorData(error);
    }
  },

  cancel: async (appointmentId, reason) => {
    try {
      const response = await apiClient.post('/api/appointments', new URLSearchParams({
        action: 'cancel',
        appointmentId,
        reason,
      }));
      return response.data;
    } catch (error) {
      return toErrorData(error);
    }
  },

  reschedule: async (appointmentId, clinicServiceId, date, time, doctorId) => {
    try {
      const response = await apiClient.post('/api/appointments', new URLSearchParams({
        action: 'reschedule',
        appointmentId,
        clinicServiceId,
        date,
        time,
        doctorId: doctorId || '',
      }));
      return response.data;
    } catch (error) {
      return toErrorData(error);
    }
  }
  
};
