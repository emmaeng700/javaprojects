import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';

const Dashboard = () => {
  const { user, credentials } = useAuth();
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const response = await authAPI.getDashboard(
        credentials.username,
        credentials.password
      );
      setDashboardData(response.data);
    } catch (err) {
      setError('Failed to load dashboard data');
      console.error('Dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="loading">
          <div className="spinner"></div>
        </div>
      </>
    );
  }

  if (error) {
    return (
      <>
        <Navbar />
        <div className="dashboard">
          <div className="container">
            <div className="error-message">{error}</div>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="dashboard">
        <div className="container">
          <div className="welcome-card">
            <h1 className="welcome-title">
              {dashboardData?.message || `Welcome, ${user?.fullName}!`}
            </h1>
            <p className="welcome-subtitle">
              You are logged in as <strong>{user?.username}</strong>
            </p>
          </div>

          <div className="card">
            <h2 className="card-title">
              <svg className="card-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              User Information
            </h2>
            <div className="info-grid">
              <div className="info-item">
                <label>Username</label>
                <p>{user?.username}</p>
              </div>
              <div className="info-item">
                <label>Full Name</label>
                <p>{user?.fullName}</p>
              </div>
              <div className="info-item">
                <label>Email</label>
                <p>{user?.email}</p>
              </div>
              <div className="info-item">
                <label>Role</label>
                <p><span className="badge">{user?.role}</span></p>
              </div>
            </div>
          </div>

          {dashboardData?.stats && (
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon blue">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                <div>
                  <div className="stat-label">Total Users</div>
                  <div className="stat-value">{dashboardData.stats.totalUsers}</div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon green">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <div>
                  <div className="stat-label">Active Groups</div>
                  <div className="stat-value">{dashboardData.stats.activeGroups}</div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon purple">
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <div className="stat-label">Login Time</div>
                  <div className="stat-value" style={{fontSize: '18px'}}>
                    {new Date(dashboardData.stats.loginTime).toLocaleTimeString()}
                  </div>
                </div>
              </div>
            </div>
          )}

          <div className="card">
            <h2 className="card-title">🧪 Test Your Access</h2>
            <p style={{marginBottom: '20px', color: '#666'}}>
              Try accessing these pages to see role-based restrictions in action:
            </p>
            
            <div style={{display: 'grid', gap: '15px'}}>
              <div style={{
                padding: '15px',
                background: user?.role?.includes('ROLE_ADMIN') ? '#e6ffe6' : '#fff5f5',
                border: user?.role?.includes('ROLE_ADMIN') ? '2px solid #48bb78' : '2px solid #feb2b2',
                borderRadius: '8px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div>
                  <h3 style={{margin: '0 0 8px 0'}}>🔐 Admin Panel</h3>
                  <p style={{margin: '0', fontSize: '14px', color: '#666'}}>
                    Required: <span className="badge">ADMIN</span> | 
                    Your Role: <span className="badge">{user?.role}</span>
                  </p>
                  <p style={{margin: '8px 0 0 0', fontSize: '13px', color: user?.role?.includes('ROLE_ADMIN') ? '#48bb78' : '#e53e3e', fontWeight: 'bold'}}>
                    {user?.role?.includes('ROLE_ADMIN') ? '✅ You have access' : '❌ Access will be denied'}
                  </p>
                </div>
                <Link 
                  to="/admin" 
                  style={{
                    padding: '10px 20px',
                    background: user?.role?.includes('ROLE_ADMIN') ? '#48bb78' : '#e53e3e',
                    color: 'white',
                    textDecoration: 'none',
                    borderRadius: '4px',
                    fontWeight: '600'
                  }}
                >
                  Try Access
                </Link>
              </div>

              <div style={{
                padding: '15px',
                background: (user?.role?.includes('ROLE_MANAGER') || user?.role?.includes('ROLE_ADMIN')) ? '#e6ffe6' : '#fff5f5',
                border: (user?.role?.includes('ROLE_MANAGER') || user?.role?.includes('ROLE_ADMIN')) ? '2px solid #48bb78' : '2px solid #feb2b2',
                borderRadius: '8px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div>
                  <h3 style={{margin: '0 0 8px 0'}}>📊 Manager Panel</h3>
                  <p style={{margin: '0', fontSize: '14px', color: '#666'}}>
                    Required: <span className="badge">MANAGER</span> or <span className="badge">ADMIN</span> | 
                    Your Role: <span className="badge">{user?.role}</span>
                  </p>
                  <p style={{margin: '8px 0 0 0', fontSize: '13px', color: (user?.role?.includes('ROLE_MANAGER') || user?.role?.includes('ROLE_ADMIN')) ? '#48bb78' : '#e53e3e', fontWeight: 'bold'}}>
                    {(user?.role?.includes('ROLE_MANAGER') || user?.role?.includes('ROLE_ADMIN')) ? '✅ You have access' : '❌ Access will be denied'}
                  </p>
                </div>
                <Link 
                  to="/manager" 
                  style={{
                    padding: '10px 20px',
                    background: (user?.role?.includes('ROLE_MANAGER') || user?.role?.includes('ROLE_ADMIN')) ? '#48bb78' : '#e53e3e',
                    color: 'white',
                    textDecoration: 'none',
                    borderRadius: '4px',
                    fontWeight: '600'
                  }}
                >
                  Try Access
                </Link>
              </div>

              <div style={{
                padding: '15px',
                background: '#e6ffe6',
                border: '2px solid #48bb78',
                borderRadius: '8px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div>
                  <h3 style={{margin: '0 0 8px 0'}}>⚙️ Settings</h3>
                  <p style={{margin: '0', fontSize: '14px', color: '#666'}}>
                    Required: <span className="badge">Any authenticated user</span> | 
                    Your Role: <span className="badge">{user?.role}</span>
                  </p>
                  <p style={{margin: '8px 0 0 0', fontSize: '13px', color: '#48bb78', fontWeight: 'bold'}}>
                    ✅ You have access
                  </p>
                </div>
                <Link 
                  to="/settings" 
                  style={{
                    padding: '10px 20px',
                    background: '#48bb78',
                    color: 'white',
                    textDecoration: 'none',
                    borderRadius: '4px',
                    fontWeight: '600'
                  }}
                >
                  Try Access
                </Link>
              </div>
            </div>

            <div style={{
              marginTop: '20px',
              padding: '15px',
              background: '#e6f0ff',
              borderRadius: '8px',
              fontSize: '14px'
            }}>
              <strong>💡 Try this:</strong>
              <ol style={{margin: '10px 0 0 20px', lineHeight: '1.8'}}>
                <li>Click "Try Access" on pages marked ❌ to see the access denied screen</li>
                <li>Register a new account with a different role</li>
                <li>Compare what pages you can access with each role</li>
              </ol>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">
              <svg className="card-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              Quick Actions
            </h2>
            <div className="actions-grid">
              <button className="action-btn blue">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                </svg>
                View Settings
              </button>
              <button className="action-btn green">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                View Reports
              </button>
              <button className="action-btn purple">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                Manage Users
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Dashboard;