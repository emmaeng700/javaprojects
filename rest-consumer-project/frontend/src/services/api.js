import axios from 'axios';

const CONSUMER_API = 'http://localhost:8081/api';
const QUOTERS_API = 'http://localhost:8080/api';

const api = {
  // Consumer endpoints
  consumer: {
    getRandomQuote: async () => {
      const response = await axios.get(`${CONSUMER_API}/quote/random`);
      return response.data;
    },
    getAllQuotes: async () => {
      const response = await axios.get(`${CONSUMER_API}/quote/all`);
      return response.data;
    },
    getQuoteById: async (id) => {
      const response = await axios.get(`${CONSUMER_API}/quote/${id}`);
      return response.data;
    },
    getStatus: async () => {
      const response = await axios.get(`${CONSUMER_API}/status`);
      return response.data;
    }
  },

  // Direct quoters endpoints (for comparison)
  quoters: {
    getRandomQuote: async () => {
      const response = await axios.get(`${QUOTERS_API}/random`);
      return response.data;
    },
    getAllQuotes: async () => {
      const response = await axios.get(`${QUOTERS_API}/`);
      return response.data;
    },
    getCount: async () => {
      const response = await axios.get(`${QUOTERS_API}/count`);
      return response.data;
    }
  }
};

export default api;