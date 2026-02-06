import React from 'react';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

const AdminPanel = () => {
  const { user } = useAuth();

  // Check if user has ADMIN role
  if (!user?.role?.includes('ROLE_ADMIN')) {
    return <Navigate to="/access-denied" replace />;
  }

  return (
    <>
      <Navbar />
      <div className="dashboard">
        <div className="container">
          <div className="welcome-card">
            <h1 className="welcome-title">🔐 Admin Panel</h1>
            <p className="welcome-subtitle">
              This page is only accessible to users with ADMIN role
            </p>
          </div>

          <div className="card">
            <h2 className="card-title">
              <svg className="card-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              System Users
            </h2>
            <div style={{overflowX: 'auto'}}>
              <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                  <tr style={{borderBottom: '2px solid #ddd'}}>
                    <th style={{padding: '12px', textAlign: 'left'}}>Username</th>
                    <th style={{padding: '12px', textAlign: 'left'}}>Role</th>
                    <th style={{padding: '12px', textAlign: 'left'}}>Status</th>
                    <th style={{padding: '12px', textAlign: 'left'}}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}>ben</td>
                    <td style={{padding: '12px'}}><span className="badge">ADMIN</span></td>
                    <td style={{padding: '12px'}}>Active</td>
                    <td style={{padding: '12px'}}>
                      <button style={{padding: '6px 12px', background: '#667eea', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '5px'}}>Edit</button>
                      <button style={{padding: '6px 12px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer'}}>Delete</button>
                    </td>
                  </tr>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}>bob</td>
                    <td style={{padding: '12px'}}><span className="badge">USER</span></td>
                    <td style={{padding: '12px'}}>Active</td>
                    <td style={{padding: '12px'}}>
                      <button style={{padding: '6px 12px', background: '#667eea', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '5px'}}>Edit</button>
                      <button style={{padding: '6px 12px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer'}}>Delete</button>
                    </td>
                  </tr>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}>alice</td>
                    <td style={{padding: '12px'}}><span className="badge">USER</span></td>
                    <td style={{padding: '12px'}}>Active</td>
                    <td style={{padding: '12px'}}>
                      <button style={{padding: '6px 12px', background: '#667eea', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '5px'}}>Edit</button>
                      <button style={{padding: '6px 12px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer'}}>Delete</button>
                    </td>
                  </tr>
                  <tr>
                    <td style={{padding: '12px'}}>joe</td>
                    <td style={{padding: '12px'}}><span className="badge">MANAGER</span></td>
                    <td style={{padding: '12px'}}>Active</td>
                    <td style={{padding: '12px'}}>
                      <button style={{padding: '6px 12px', background: '#667eea', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '5px'}}>Edit</button>
                      <button style={{padding: '6px 12px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer'}}>Delete</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">Admin Controls</h2>
            <div className="actions-grid">
              <button className="action-btn blue">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                </svg>
                Add User
              </button>
              <button className="action-btn green">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                Security Settings
              </button>
              <button className="action-btn purple">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                System Logs
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default AdminPanel;