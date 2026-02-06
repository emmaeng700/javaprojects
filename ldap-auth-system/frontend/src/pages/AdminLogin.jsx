import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AdminLogin = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const result = await login(username, password);
    
    if (result.success) {
      // Check if user has ADMIN role
      if (result.user?.role?.includes('ROLE_ADMIN')) {
        navigate('/admin');
      } else {
        setError(`Access Denied! You are registered as ${result.user?.role?.replace('ROLE_', '')} but this portal requires ADMIN role.`);
        // Auto logout the non-admin user
        setTimeout(() => {
          window.location.href = '/';
        }, 3000);
      }
    } else {
      setError('Invalid username or password');
    }
    
    setLoading(false);
  };

  return (
    <div className="login-container" style={{background: 'linear-gradient(135deg, #e53e3e 0%, #7c2d12 100%)'}}>
      <div className="login-box">
        <div className="login-icon" style={{background: '#e53e3e'}}>
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        </div>
        
        <div style={{
          background: '#fee',
          border: '2px solid #e53e3e',
          padding: '15px',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          <h2 style={{margin: '0 0 5px 0', color: '#e53e3e', fontSize: '20px'}}>
            🔐 Administrator Portal
          </h2>
          <p style={{margin: 0, fontSize: '14px', color: '#666'}}>
            This portal is restricted to ADMIN role only
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="error-message" style={{whiteSpace: 'pre-wrap'}}>
              {error}
            </div>
          )}

          <div className="form-group">
            <input
              type="text"
              className="form-input"
              placeholder="Admin Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
            />
            <input
              type="password"
              className="form-input"
              placeholder="Admin Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-primary" disabled={loading} style={{background: '#e53e3e'}}>
            {loading ? 'Verifying Admin Access...' : 'Sign in as Admin'}
          </button>
        </form>

        <div style={{marginTop: '20px', textAlign: 'center', fontSize: '14px'}}>
          <Link to="/manager-login" style={{color: '#667eea', textDecoration: 'none', fontWeight: '600', marginRight: '15px'}}>
            Manager Portal
          </Link>
          <Link to="/login" style={{color: '#667eea', textDecoration: 'none', fontWeight: '600'}}>
            User Portal
          </Link>
        </div>

        <div style={{
          marginTop: '15px',
          padding: '12px',
          background: '#f5f5f5',
          borderRadius: '8px',
          fontSize: '13px',
          textAlign: 'left'
        }}>
          <strong>ℹ️ Portal Access:</strong>
          <ul style={{margin: '8px 0 0 20px', lineHeight: '1.6'}}>
            <li>Only accounts with <strong>ADMIN</strong> role can login here</li>
            <li>Wrong portal? <Link to="/manager-login" style={{color: '#667eea'}}>Try Manager</Link> or <Link to="/login" style={{color: '#667eea'}}>User</Link></li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default AdminLogin;