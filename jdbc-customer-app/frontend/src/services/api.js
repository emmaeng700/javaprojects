import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/customers';

const api = {
  // Get all customers
  getAllCustomers: async () => {
    const response = await axios.get(API_BASE_URL);
    return response.data;
  },

  // Search by first name
  searchByFirstName: async (firstName) => {
    const response = await axios.get(`${API_BASE_URL}/search`, {
      params: { firstName }
    });
    return response.data;
  },

  // Get customer by ID
  getCustomerById: async (id) => {
    const response = await axios.get(`${API_BASE_URL}/${id}`);
    return response.data;
  },

  // Create customer
  createCustomer: async (firstName, lastName) => {
    const response = await axios.post(API_BASE_URL, {
      firstName,
      lastName
    });
    return response.data;
  },

  // Update customer
  updateCustomer: async (id, firstName, lastName) => {
    const response = await axios.put(`${API_BASE_URL}/${id}`, {
      firstName,
      lastName
    });
    return response.data;
  },

  // Delete customer
  deleteCustomer: async (id) => {
    await axios.delete(`${API_BASE_URL}/${id}`);
  },

  // Get count
  getCount: async () => {
    const response = await axios.get(`${API_BASE_URL}/count`);
    return response.data.count;
  }
};

export default api;