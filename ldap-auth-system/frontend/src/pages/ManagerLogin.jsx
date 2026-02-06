import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ManagerLogin = () => {
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
      if (result.user?.role?.includes('ROLE_MANAGER') || result.user?.role?.includes('ROLE_ADMIN')) {
        navigate('/manager');
      } else {
        setError(`Access Denied! You are registered as ${result.user?.role?.replace('ROLE_', '')} but this portal requires MANAGER or ADMIN role.`);
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
    <div className="login-container" style={{background: 'linear-gradient(135deg, #9f7aea 0%, #553c9a 100%)'}}>
      <div className="login-box">
        <div className="login-icon" style={{background: '#9f7aea'}}>
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
        </div>
        
        <div style={{
          background: '#f3e8ff',
          border: '2px solid #9f7aea',
          padding: '15px',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          <h2 style={{margin: '0 0 5px 0', color: '#9f7aea', fontSize: '20px'}}>
            📊 Manager Portal
          </h2>
          <p style={{margin: 0, fontSize: '14px', color: '#666'}}>
            This portal is for MANAGER and ADMIN roles
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
              placeholder="Manager Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
            />
            <input
              type="password"
              className="form-input"
              placeholder="Manager Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-primary" disabled={loading} style={{background: '#9f7aea'}}>
            {loading ? 'Verifying Manager Access...' : 'Sign in as Manager'}
          </button>
        </form>

        <div style={{marginTop: '20px', textAlign: 'center', fontSize: '14px'}}>
          <Link to="/admin-login" style={{color: '#e53e3e', textDecoration: 'none', fontWeight: '600', marginRight: '15px'}}>
            Admin Portal
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
            <li>Accounts with <strong>MANAGER</strong> or <strong>ADMIN</strong> role can login here</li>
            <li>Wrong portal? <Link to="/admin-login" style={{color: '#e53e3e'}}>Try Admin</Link> or <Link to="/login" style={{color: '#667eea'}}>User</Link></li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default ManagerLogin;