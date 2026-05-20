import apiClient, { toErrorData } from './apiClient';

export const patientService = {
  getProfile: async () => {
    const response = await apiClient.get('/api/patient/profile');
    return response.data;
  },

  updateProfile: async (formData) => {
    try {
      const response = await apiClient.post('/api/patient/profile', formData);
      return response.data;
    } catch (error) {
      return toErrorData(error);
    }
  },

  changePassword: async (currentPassword, newPassword, confirmPassword) => {
    try {
      const response = await apiClient.post('/api/patient/change-password', new URLSearchParams({
        currentPassword,
        newPassword,
        confirmPassword,
      }));
      return response.data;
    } catch (error) {
      return toErrorData(error);
    }
  },
};
