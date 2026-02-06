import React from 'react';
import Navbar from '../components/Navbar';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AccessDenied = () => {
  const { user } = useAuth();
  const location = useLocation();
  
  const attemptedPage = location.state?.from || 'this page';
  
  const getPageInfo = () => {
    const path = typeof attemptedPage === 'string' ? attemptedPage : attemptedPage.pathname;
    
    if (path?.includes('/admin')) {
      return {
        name: 'Admin Panel',
        icon: '🔐',
        required: ['ROLE_ADMIN'],
        description: 'Manage system users, security settings, and configurations',
        capabilities: ['Create/Delete Users', 'System Settings', 'Security Config', 'View All Logs']
      };
    } else if (path?.includes('/manager')) {
      return {
        name: 'Manager Panel',
        icon: '📊',
        required: ['ROLE_MANAGER', 'ROLE_ADMIN'],
        description: 'View team reports, approve requests, and manage projects',
        capabilities: ['View Team Reports', 'Approve Requests', 'Manage Projects', 'Team Performance']
      };
    }
    
    return {
      name: 'This Page',
      icon: '🔒',
      required: ['Higher Permissions'],
      description: 'This resource requires additional permissions',
      capabilities: []
    };
  };

  const pageInfo = getPageInfo();
  const userRole = user?.role || 'UNKNOWN';

  return (
    <>
      <Navbar />
      <div className="dashboard">
        <div className="container">
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '60px 40px',
            textAlign: 'center',
            maxWidth: '700px',
            margin: '80px auto',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}>
            <div style={{
              width: '100px',
              height: '100px',
              background: '#fee',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 30px',
              fontSize: '48px'
            }}>
              🚫
            </div>

            <h1 style={{fontSize: '32px', fontWeight: 'bold', marginBottom: '15px', color: '#333'}}>
              Access Denied
            </h1>
            <p style={{fontSize: '18px', color: '#666', marginBottom: '10px'}}>
              You attempted to access: <strong style={{color: '#e53e3e'}}>{pageInfo.icon} {pageInfo.name}</strong>
            </p>
            <p style={{fontSize: '16px', color: '#999', marginBottom: '30px'}}>
              {pageInfo.description}
            </p>

            <div style={{
              background: '#fff5f5',
              border: '3px solid #e53e3e',
              padding: '25px',
              borderRadius: '8px',
              marginBottom: '30px',
              textAlign: 'left'
            }}>
              <div style={{display: 'flex', alignItems: 'center', marginBottom: '20px'}}>
                <svg style={{width: '28px', height: '28px', color: '#e53e3e', marginRight: '10px'}} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <h3 style={{margin: 0, fontSize: '20px', color: '#e53e3e'}}>Role Mismatch Detected</h3>
              </div>
              
              <div style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '15px',
                marginBottom: '15px'
              }}>
                <div style={{padding: '15px', background: '#fee', borderRadius: '6px', border: '2px solid #fcc'}}>
                  <div style={{fontSize: '12px', fontWeight: '600', color: '#999', marginBottom: '8px'}}>YOUR ROLE</div>
                  <div style={{fontSize: '18px', fontWeight: 'bold', color: '#e53e3e'}}>
                    {userRole.replace('ROLE_', '')}
                  </div>
                </div>

                <div style={{padding: '15px', background: '#e6ffe6', borderRadius: '6px', border: '2px solid #48bb78'}}>
                  <div style={{fontSize: '12px', fontWeight: '600', color: '#999', marginBottom: '8px'}}>REQUIRED ROLE</div>
                  <div style={{fontSize: '18px', fontWeight: 'bold', color: '#48bb78'}}>
                    {pageInfo.required.map(r => r.replace('ROLE_', '')).join(' or ')}
                  </div>
                </div>
              </div>

              {pageInfo.capabilities.length > 0 && (
                <div style={{padding: '15px', background: 'white', borderRadius: '6px'}}>
                  <strong style={{display: 'block', marginBottom: '8px'}}>📋 What you're missing:</strong>
                  <ul style={{margin: '0', paddingLeft: '20px', lineHeight: '1.8', fontSize: '14px'}}>
                    {pageInfo.capabilities.map((cap, idx) => (
                      <li key={idx}>{cap}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>

            <div style={{
              background: '#e6f0ff',
              padding: '20px',
              borderRadius: '8px',
              marginBottom: '30px',
              textAlign: 'left',
              fontSize: '14px'
            }}>
              <strong>💡 What you can do:</strong>
              <ul style={{margin: '10px 0 0 20px', lineHeight: '1.8'}}>
                <li>Return to your Dashboard to see pages you <strong>can</strong> access</li>
                <li>Register a new account with <strong>{pageInfo.required[0].replace('ROLE_', '')}</strong> role</li>
                <li>Contact an administrator to upgrade your current account</li>
              </ul>
            </div>

            <div style={{display: 'flex', gap: '15px', justifyContent: 'center', flexWrap: 'wrap'}}>
              <Link to="/dashboard" className="btn-primary" style={{width: 'auto', textDecoration: 'none'}}>
                ← Back to Dashboard
              </Link>
              <Link to="/register" style={{
                padding: '12px 24px',
                background: '#48bb78',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                fontSize: '16px',
                fontWeight: '600',
                textDecoration: 'none',
                display: 'inline-block'
              }}>
                Register New Account
              </Link>
              <Link to="/login" style={{
                padding: '12px 24px',
                background: 'white',
                color: '#667eea',
                border: '2px solid #667eea',
                borderRadius: '4px',
                fontSize: '16px',
                fontWeight: '600',
                textDecoration: 'none',
                display: 'inline-block'
              }}>
                Switch Account
              </Link>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">📋 Complete Access Matrix</h2>
            <div style={{overflowX: 'auto'}}>
              <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                  <tr style={{background: '#f5f5f5', borderBottom: '2px solid #ddd'}}>
                    <th style={{padding: '12px', textAlign: 'left'}}>Page / Feature</th>
                    <th style={{padding: '12px', textAlign: 'center', background: userRole.includes('USER') && !userRole.includes('MANAGER') && !userRole.includes('ADMIN') ? '#e6f0ff' : '#f5f5f5'}}>
                      USER
                      {userRole.includes('USER') && !userRole.includes('MANAGER') && !userRole.includes('ADMIN') && <div style={{fontSize: '12px', color: '#667eea', fontWeight: 'bold'}}>← YOU</div>}
                    </th>
                    <th style={{padding: '12px', textAlign: 'center', background: userRole.includes('MANAGER') && !userRole.includes('ADMIN') ? '#e6f0ff' : '#f5f5f5'}}>
                      MANAGER
                      {userRole.includes('MANAGER') && !userRole.includes('ADMIN') && <div style={{fontSize: '12px', color: '#667eea', fontWeight: 'bold'}}>← YOU</div>}
                    </th>
                    <th style={{padding: '12px', textAlign: 'center', background: userRole.includes('ADMIN') ? '#e6f0ff' : '#f5f5f5'}}>
                      ADMIN
                      {userRole.includes('ADMIN') && <div style={{fontSize: '12px', color: '#667eea', fontWeight: 'bold'}}>← YOU</div>}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}><strong>Dashboard</strong></td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                  </tr>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}><strong>Settings</strong></td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                  </tr>
                  <tr style={{borderBottom: '1px solid #eee', background: pageInfo.name === 'Manager Panel' ? '#fff5f5' : 'white'}}>
                    <td style={{padding: '12px'}}><strong>📊 Manager Panel</strong></td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>❌</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                  </tr>
                  <tr style={{background: pageInfo.name === 'Admin Panel' ? '#fff5f5' : 'white'}}>
                    <td style={{padding: '12px'}}><strong>🔐 Admin Panel</strong></td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>❌</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>❌</td>
                    <td style={{padding: '12px', textAlign: 'center', fontSize: '20px'}}>✅</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default AccessDenied;