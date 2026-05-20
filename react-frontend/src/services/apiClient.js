import axios from 'axios';

const apiClient = axios.create({
  baseURL: '',
  withCredentials: true,
});

export const toArray = (data) => Array.isArray(data) ? data : [];

export const toErrorData = (error) => error.response?.data || { error: 'Request failed' };

export default apiClient;
