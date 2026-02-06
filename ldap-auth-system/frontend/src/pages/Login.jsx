import React, { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

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

  const demoUsers = [
    { username: 'ben', password: 'benspassword', role: 'Admin', access: 'Full Access' },
    { username: 'joe', password: 'joespassword', role: 'Manager', access: 'Manager + User' },
    { username: 'bob', password: 'bobspassword', role: 'User', access: 'User Only' },
    { username: 'alice', password: 'alicespassword', role: 'User', access: 'User Only' }
  ];

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
        </div>
        
        <h2 className="login-title">Sign in to your account</h2>
        <p className="login-subtitle">Enter your credentials to continue</p>

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
            {showDemoUsers ? '✕ Hide' : '👁️ Show'} Test Users
          </button>
        </div>

        {showDemoUsers && (
          <>
            <div className="divider">
              <span>Test Accounts</span>
            </div>

            <div style={{
              background: '#f5f5f5',
              padding: '15px',
              borderRadius: '8px',
              fontSize: '13px'
            }}>
              {demoUsers.map((demo) => (
                <div key={demo.username} style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '10px',
                  background: 'white',
                  marginBottom: '8px',
                  borderRadius: '4px',
                  border: '1px solid #ddd'
                }}>
                  <div>
                    <div style={{fontWeight: 'bold', color: '#333'}}>{demo.username}</div>
                    <div style={{fontSize: '11px', color: '#666'}}>{demo.role} - {demo.access}</div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      setUsername(demo.username);
                      setPassword(demo.password);
                    }}
                    style={{
                      padding: '6px 12px',
                      background: '#667eea',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '12px'
                    }}
                  >
                    Use
                  </button>
                </div>
              ))}
            </div>

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
                <li>Login as <strong>bob</strong> and try accessing Admin Panel</li>
                <li>Login as <strong>joe</strong> and try accessing Admin Panel</li>
                <li>Login as <strong>ben</strong> to see full access</li>
              </ul>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Login;