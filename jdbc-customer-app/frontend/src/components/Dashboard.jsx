import { useState, useEffect } from 'react';
import CustomerCard from './CustomerCard';
import api from '../services/api';
import './Dashboard.css';

function Dashboard() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [newFirstName, setNewFirstName] = useState('');
  const [newLastName, setNewLastName] = useState('');
  const [totalCount, setTotalCount] = useState(0);
  const [showAddForm, setShowAddForm] = useState(false);

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const data = await api.getAllCustomers();
      setCustomers(data);
      const count = await api.getCount();
      setTotalCount(count);
      setError(null);
    } catch (err) {
      setError('Failed to load customers. Make sure backend is running on port 8080.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      loadCustomers();
      return;
    }

    try {
      setLoading(true);
      const data = await api.searchByFirstName(searchTerm);
      setCustomers(data);
      setError(null);
    } catch (err) {
      setError('Search failed');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    
    if (!newFirstName.trim() || !newLastName.trim()) {
      alert('Please fill in both fields');
      return;
    }

    try {
      await api.createCustomer(newFirstName, newLastName);
      setNewFirstName('');
      setNewLastName('');
      setShowAddForm(false);
      loadCustomers();
    } catch (err) {
      setError('Failed to create customer');
      console.error(err);
    }
  };

  const handleUpdate = async (id, firstName, lastName) => {
    try {
      await api.updateCustomer(id, firstName, lastName);
      loadCustomers();
    } catch (err) {
      setError('Failed to update customer');
      console.error(err);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this customer?')) {
      return;
    }

    try {
      await api.deleteCustomer(id);
      loadCustomers();
    } catch (err) {
      setError('Failed to delete customer');
      console.error(err);
    }
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>🗄️ JDBC Customer Management</h1>
        <div className="header-stats">
          <div className="stat-badge">
            <span className="stat-label">Total Customers</span>
            <span className="stat-value">{totalCount}</span>
          </div>
        </div>
      </header>

      {error && (
        <div className="error-banner">
          ⚠️ {error}
        </div>
      )}

      <div className="controls-section">
        <div className="search-bar">
          <input
            type="text"
            placeholder="Search by first name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          />
          <button onClick={handleSearch} className="btn-search">
            🔍 Search
          </button>
          <button onClick={loadCustomers} className="btn-reset">
            🔄 Show All
          </button>
        </div>

        <button 
          onClick={() => setShowAddForm(!showAddForm)} 
          className="btn-add"
        >
          {showAddForm ? '❌ Cancel' : '➕ Add Customer'}
        </button>
      </div>

      {showAddForm && (
        <div className="add-form-section">
          <form onSubmit={handleCreate} className="add-form">
            <h3>Add New Customer</h3>
            <div className="form-row">
              <input
                type="text"
                placeholder="First Name"
                value={newFirstName}
                onChange={(e) => setNewFirstName(e.target.value)}
                required
              />
              <input
                type="text"
                placeholder="Last Name"
                value={newLastName}
                onChange={(e) => setNewLastName(e.target.value)}
                required
              />
              <button type="submit" className="btn-submit">
                💾 Create
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="customers-section">
        <h2>Customers ({customers.length})</h2>
        
        {loading ? (
          <div className="loading">
            <div className="spinner"></div>
            <p>Loading customers...</p>
          </div>
        ) : customers.length === 0 ? (
          <div className="no-customers">
            <p>📭 No customers found</p>
            <p className="hint">Click "Add Customer" to create one!</p>
          </div>
        ) : (
          <div className="customers-grid">
            {customers.map((customer) => (
              <CustomerCard
                key={customer.id}
                customer={customer}
                onUpdate={handleUpdate}
                onDelete={handleDelete}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Dashboard;