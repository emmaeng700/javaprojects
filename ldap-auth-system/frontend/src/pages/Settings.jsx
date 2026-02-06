import React from 'react';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';

const Settings = () => {
  const { user } = useAuth();

  return (
    <>
      <Navbar />
      <div className="dashboard">
        <div className="container">
          <div className="welcome-card">
            <h1 className="welcome-title">⚙️ Settings</h1>
            <p className="welcome-subtitle">
              This page is accessible to all authenticated users
            </p>
          </div>

          <div className="card">
            <h2 className="card-title">Profile Settings</h2>
            <div className="form-group">
              <label style={{display: 'block', marginBottom: '8px', fontWeight: '600'}}>Username</label>
              <input 
                type="text" 
                className="form-input" 
                value={user?.username} 
                disabled 
                style={{background: '#f5f5f5', borderRadius: '4px'}}
              />
            </div>
            <div className="form-group">
              <label style={{display: 'block', marginBottom: '8px', fontWeight: '600'}}>Full Name</label>
              <input 
                type="text" 
                className="form-input" 
                value={user?.fullName} 
                disabled 
                style={{background: '#f5f5f5', borderRadius: '4px'}}
              />
            </div>
            <div className="form-group">
              <label style={{display: 'block', marginBottom: '8px', fontWeight: '600'}}>Email</label>
              <input 
                type="email" 
                className="form-input" 
                value={user?.email} 
                disabled 
                style={{background: '#f5f5f5', borderRadius: '4px'}}
              />
            </div>
            <button className="btn-primary" style={{width: 'auto'}}>Update Profile</button>
          </div>

          <div className="card">
            <h2 className="card-title">Change Password</h2>
            <div className="form-group">
              <label style={{display: 'block', marginBottom: '8px', fontWeight: '600'}}>Current Password</label>
              <input type="password" className="form-input" placeholder="Enter current password" style={{borderRadius: '4px'}} />
            </div>
            <div className="form-group">
              <label style={{display: 'block', marginBottom: '8px', fontWeight: '600'}}>New Password</label>
              <input type="password" className="form-input" placeholder="Enter new password" style={{borderRadius: '4px'}} />
            </div>
            <div className="form-group">
              <label style={{display: 'block', marginBottom: '8px', fontWeight: '600'}}>Confirm New Password</label>
              <input type="password" className="form-input" placeholder="Confirm new password" style={{borderRadius: '4px'}} />
            </div>
            <button className="btn-primary" style={{width: 'auto'}}>Change Password</button>
          </div>
        </div>
      </div>
    </>
  );
};

export default Settings;