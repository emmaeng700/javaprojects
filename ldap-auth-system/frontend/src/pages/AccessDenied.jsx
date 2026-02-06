import React from 'react';
import Navbar from '../components/Navbar';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AccessDenied = () => {
  const { user } = useAuth();

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
            margin: '80px auto'
          }}>
            <div style={{
              width: '80px',
              height: '80px',
              background: '#fee',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 30px'
            }}>
              <svg style={{width: '40px', height: '40px', color: '#e53e3e'}} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>

            <h1 style={{fontSize: '32px', fontWeight: 'bold', marginBottom: '15px', color: '#333'}}>
              Access Denied
            </h1>
            <p style={{fontSize: '18px', color: '#666', marginBottom: '30px'}}>
              You don't have permission to access this page.
            </p>

            <div style={{
              background: '#f5f5f5',
              padding: '20px',
              borderRadius: '8px',
              marginBottom: '30px',
              textAlign: 'left'
            }}>
              <p style={{marginBottom: '10px'}}><strong>Your current role:</strong> {user?.role}</p>
              <p style={{marginBottom: '10px'}}><strong>Username:</strong> {user?.username}</p>
              <p style={{marginBottom: '0', color: '#666', fontSize: '14px'}}>
                This page requires different permissions than your current role allows.
              </p>
            </div>

            <div style={{display: 'flex', gap: '15px', justifyContent: 'center', flexWrap: 'wrap'}}>
              <Link to="/dashboard" className="btn-primary" style={{width: 'auto', textDecoration: 'none'}}>
                Go to Dashboard
              </Link>
              <Link to="/" style={{
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
                Go Home
              </Link>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">Role Requirements</h2>
            <div style={{display: 'grid', gap: '20px'}}>
              <div style={{padding: '15px', background: '#f5f5f5', borderRadius: '8px'}}>
                <h3 style={{marginBottom: '10px', color: '#333'}}>🔐 Admin Panel</h3>
                <p style={{marginBottom: '5px', color: '#666'}}>Required Role: <span className="badge">ADMIN</span></p>
                <p style={{color: '#666', fontSize: '14px'}}>Manage users, system settings, and security configurations</p>
              </div>

              <div style={{padding: '15px', background: '#f5f5f5', borderRadius: '8px'}}>
                <h3 style={{marginBottom: '10px', color: '#333'}}>📊 Manager Panel</h3>
                <p style={{marginBottom: '5px', color: '#666'}}>Required Role: <span className="badge">MANAGER</span> or <span className="badge">ADMIN</span></p>
                <p style={{color: '#666', fontSize: '14px'}}>View team reports, approve requests, and manage projects</p>
              </div>

              <div style={{padding: '15px', background: '#f5f5f5', borderRadius: '8px'}}>
                <h3 style={{marginBottom: '10px', color: '#333'}}>⚙️ Settings</h3>
                <p style={{marginBottom: '5px', color: '#666'}}>Required Role: <span className="badge">Any authenticated user</span></p>
                <p style={{color: '#666', fontSize: '14px'}}>Update profile and change password</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default AccessDenied;