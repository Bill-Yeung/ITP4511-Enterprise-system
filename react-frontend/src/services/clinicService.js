import apiClient, { toArray } from './apiClient';

export const clinicService = {
  getAll: async () => {
    const response = await apiClient.get('/api/clinics', {
      params: { action: 'list' },
    });
    return toArray(response.data);
  },

  getServices: async (clinicId) => {
    const response = await apiClient.get('/api/clinics', {
      params: { action: 'services', clinicId },
    });
    return toArray(response.data);
  },

  getHours: async (clinicId) => {
    const response = await apiClient.get('/api/clinics', {
      params: { action: 'hours', clinicId },
    });
    return toArray(response.data);
  },

  getDoctors: async (clinicId, date) => {
    const response = await apiClient.get('/api/clinics', {
      params: { action: 'doctors', clinicId, date },
    });
    return toArray(response.data);
  },

  getSlots: async (clinicServiceId, date) => {
    const response = await apiClient.get('/api/clinics', {
      params: { action: 'slots', clinicServiceId, date },
    });
    return toArray(response.data);
  }
  
};
