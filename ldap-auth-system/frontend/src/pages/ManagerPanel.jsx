import React from 'react';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

const ManagerPanel = () => {
  const { user } = useAuth();

  // Check if user has MANAGER role
  if (!user?.role?.includes('ROLE_MANAGER') && !user?.role?.includes('ROLE_ADMIN')) {
    return <Navigate to="/access-denied" replace />;
  }

  return (
    <>
      <Navbar />
      <div className="dashboard">
        <div className="container">
          <div className="welcome-card">
            <h1 className="welcome-title">📊 Manager Panel</h1>
            <p className="welcome-subtitle">
              This page is accessible to MANAGER and ADMIN roles
            </p>
          </div>

          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon blue">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div>
                <div className="stat-label">Team Members</div>
                <div className="stat-value">12</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon green">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                </svg>
              </div>
              <div>
                <div className="stat-label">Active Projects</div>
                <div className="stat-value">8</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon purple">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
              </div>
              <div>
                <div className="stat-label">Performance</div>
                <div className="stat-value">94%</div>
              </div>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">Team Reports</h2>
            <div style={{overflowX: 'auto'}}>
              <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                  <tr style={{borderBottom: '2px solid #ddd'}}>
                    <th style={{padding: '12px', textAlign: 'left'}}>Project</th>
                    <th style={{padding: '12px', textAlign: 'left'}}>Team Lead</th>
                    <th style={{padding: '12px', textAlign: 'left'}}>Status</th>
                    <th style={{padding: '12px', textAlign: 'left'}}>Progress</th>
                  </tr>
                </thead>
                <tbody>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}>Website Redesign</td>
                    <td style={{padding: '12px'}}>Bob Hamilton</td>
                    <td style={{padding: '12px'}}><span className="badge">In Progress</span></td>
                    <td style={{padding: '12px'}}>75%</td>
                  </tr>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}>Mobile App</td>
                    <td style={{padding: '12px'}}>Alice Johnson</td>
                    <td style={{padding: '12px'}}><span className="badge">Planning</span></td>
                    <td style={{padding: '12px'}}>20%</td>
                  </tr>
                  <tr>
                    <td style={{padding: '12px'}}>API Integration</td>
                    <td style={{padding: '12px'}}>Bob Hamilton</td>
                    <td style={{padding: '12px'}}><span className="badge">Completed</span></td>
                    <td style={{padding: '12px'}}>100%</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">Manager Actions</h2>
            <div className="actions-grid">
              <button className="action-btn blue">Approve Requests</button>
              <button className="action-btn green">View Reports</button>
              <button className="action-btn purple">Team Performance</button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ManagerPanel;