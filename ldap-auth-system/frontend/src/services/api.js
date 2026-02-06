import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Auth API
export const authAPI = {
  login: (username, password) => {
    const auth = btoa(`${username}:${password}`);
    return api.get('/api/auth/status', {
      headers: {
        'Authorization': `Basic ${auth}`
      }
    });
  },

  logout: () => api.post('/api/auth/logout'),

  getStatus: (username, password) => {
    const auth = btoa(`${username}:${password}`);
    return api.get('/api/auth/status', {
      headers: {
        'Authorization': `Basic ${auth}`
      }
    });
  },

  getCurrentUser: (username, password) => {
    const auth = btoa(`${username}:${password}`);
    return api.get('/api/user', {
      headers: {
        'Authorization': `Basic ${auth}`
      }
    });
  },

  getDashboard: (username, password) => {
    const auth = btoa(`${username}:${password}`);
    return api.get('/api/dashboard', {
      headers: {
        'Authorization': `Basic ${auth}`
      }
    });
  }
};

// Public API
export const publicAPI = {
  health: () => api.get('/api/public/health')
};

export default api;