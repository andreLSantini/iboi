import axios from 'axios';
import { clearSession, getToken, setSubscriptionReason } from './session';

const inferredBaseUrl =
  import.meta.env.VITE_API_BASE_URL ||
  (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8080'
    : '');

const api = axios.create({
  baseURL: inferredBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearSession();
      window.location.href = '/login';
    }

    if (error.response?.status === 402) {
      setSubscriptionReason(error.response?.data?.code || 'SUBSCRIPTION_INACTIVE');
      window.location.href = '/app/assinatura';
    }

    return Promise.reject(error);
  }
);

export default api;
