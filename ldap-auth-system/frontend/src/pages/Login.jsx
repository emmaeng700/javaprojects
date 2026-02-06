import React, { useState } from 'react';
import { useNavigate, Navigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showDemoUsers, setShowDemoUsers] = useState(false);
  
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!username || !password) {
      setError('Please enter both username and password');
      setLoading(false);
      return;
    }

    const result = await login(username, password);
    
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError('Invalid username or password');
    }
    
    setLoading(false);
  };

  return (
    <div className="login-container" style={{background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'}}>
      <div className="login-box">
        <div className="login-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </div>
        
        <div style={{
          background: '#e6f0ff',
          border: '2px solid #667eea',
          padding: '15px',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          <h2 style={{margin: '0 0 5px 0', color: '#667eea', fontSize: '20px'}}>
            👤 User Portal
          </h2>
          <p style={{margin: 0, fontSize: '14px', color: '#666'}}>
            Standard user access - All registered users can login here
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="error-message">{error}</div>
          )}

          <div className="form-group">
            <input
              type="text"
              className="form-input"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
            />
            <input
              type="password"
              className="form-input"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div style={{marginTop: '20px', textAlign: 'center', fontSize: '14px'}}>
          Don't have an account?{' '}
          <Link to="/register" style={{color: '#667eea', textDecoration: 'none', fontWeight: '600'}}>
            Register here
          </Link>
        </div>

        <div style={{marginTop: '15px', textAlign: 'center', fontSize: '14px'}}>
          <Link to="/admin-login" style={{color: '#e53e3e', textDecoration: 'none', fontWeight: '600', marginRight: '15px'}}>
            Admin Portal
          </Link>
          <Link to="/manager-login" style={{color: '#9f7aea', textDecoration: 'none', fontWeight: '600'}}>
            Manager Portal
          </Link>
        </div>

        <div style={{marginTop: '20px', textAlign: 'center'}}>
          <button 
            onClick={() => setShowDemoUsers(!showDemoUsers)}
            style={{
              background: 'none',
              border: 'none',
              color: '#667eea',
              cursor: 'pointer',
              fontSize: '14px',
              textDecoration: 'underline'
            }}
          >
            {showDemoUsers ? '✕ Hide' : '👁️ Show'} Test Users (if any exist)
          </button>
        </div>

        {showDemoUsers && (
          <div style={{
            marginTop: '15px',
            padding: '12px',
            background: '#e6f0ff',
            borderRadius: '8px',
            fontSize: '12px',
            color: '#333'
          }}>
            <strong>💡 Try this:</strong>
            <ul style={{margin: '8px 0 0 20px', lineHeight: '1.6'}}>
              <li>Register as <strong>USER</strong> and try accessing Admin Portal</li>
              <li>Register as <strong>MANAGER</strong> and try accessing Admin Portal</li>
              <li>Register as <strong>ADMIN</strong> to see full access</li>
            </ul>
          </div>
        )}
      </div>
    </div>
  );
};

export default Login;