import React from 'react';
import Navbar from '../components/Navbar';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AccessDenied = () => {
  const { user } = useAuth();
  const location = useLocation();
  
  // Determine what page they tried to access
  const attemptedPage = location.state?.from || 'this page';
  
  const getPageInfo = () => {
    const path = typeof attemptedPage === 'string' ? attemptedPage : attemptedPage.pathname;
    
    if (path?.includes('/admin')) {
      return {
        name: 'Admin Panel',
        icon: '🔐',
        required: 'ADMIN',
        description: 'Manage system users, security settings, and configurations'
      };
    } else if (path?.includes('/manager')) {
      return {
        name: 'Manager Panel',
        icon: '📊',
        required: 'MANAGER or ADMIN',
        description: 'View team reports, approve requests, and manage projects'
      };
    }
    
    return {
      name: 'This Page',
      icon: '🔒',
      required: 'Higher Permissions',
      description: 'This resource requires additional permissions'
    };
  };

  const pageInfo = getPageInfo();

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
            maxWidth: '600px',
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
              border: '2px solid #feb2b2',
              padding: '20px',
              borderRadius: '8px',
              marginBottom: '30px',
              textAlign: 'left'
            }}>
              <div style={{display: 'flex', alignItems: 'center', marginBottom: '15px'}}>
                <svg style={{width: '24px', height: '24px', color: '#e53e3e', marginRight: '10px'}} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <h3 style={{margin: 0, fontSize: '18px', color: '#e53e3e'}}>Permission Required</h3>
              </div>
              
              <div style={{marginBottom: '10px', padding: '10px', background: 'white', borderRadius: '4px'}}>
                <strong>Your Role:</strong> <span className="badge">{user?.role}</span>
              </div>
              <div style={{marginBottom: '10px', padding: '10px', background: 'white', borderRadius: '4px'}}>
                <strong>Required Role:</strong> <span className="badge">{pageInfo.required}</span>
              </div>
              <div style={{padding: '10px', background: 'white', borderRadius: '4px'}}>
                <strong>Your Username:</strong> {user?.username}
              </div>
            </div>

            <div style={{
              background: '#e6f0ff',
              padding: '15px',
              borderRadius: '8px',
              marginBottom: '30px',
              textAlign: 'left',
              fontSize: '14px'
            }}>
              <strong>💡 What you can do:</strong>
              <ul style={{margin: '10px 0 0 20px', lineHeight: '1.8'}}>
                <li>Go back to your Dashboard</li>
                <li>Contact an administrator to request access</li>
                <li>Login with a different account that has the required permissions</li>
              </ul>
            </div>

            <div style={{display: 'flex', gap: '15px', justifyContent: 'center', flexWrap: 'wrap'}}>
              <Link to="/dashboard" className="btn-primary" style={{width: 'auto', textDecoration: 'none'}}>
                ← Back to Dashboard
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
            <h2 className="card-title">📋 Role Access Matrix</h2>
            <div style={{overflowX: 'auto'}}>
              <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                  <tr style={{background: '#f5f5f5', borderBottom: '2px solid #ddd'}}>
                    <th style={{padding: '12px', textAlign: 'left'}}>Page</th>
                    <th style={{padding: '12px', textAlign: 'center'}}>USER (bob, alice)</th>
                    <th style={{padding: '12px', textAlign: 'center'}}>MANAGER (joe)</th>
                    <th style={{padding: '12px', textAlign: 'center'}}>ADMIN (ben)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}><strong>Dashboard</strong></td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                  </tr>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}><strong>Settings</strong></td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                  </tr>
                  <tr style={{borderBottom: '1px solid #eee'}}>
                    <td style={{padding: '12px'}}><strong>Manager Panel</strong></td>
                    <td style={{padding: '12px', textAlign: 'center'}}>❌</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
                  </tr>
                  <tr>
                    <td style={{padding: '12px'}}><strong>Admin Panel</strong></td>
                    <td style={{padding: '12px', textAlign: 'center'}}>❌</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>❌</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>✅</td>
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