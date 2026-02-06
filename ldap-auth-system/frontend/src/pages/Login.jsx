import React, { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
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
      setError(result.message || 'Login failed');
    }
    
    setLoading(false);
  };

  const demoUsers = [
    { username: 'ben', password: 'benspassword', role: 'Admin' },
    { username: 'bob', password: 'bobspassword', role: 'User' },
    { username: 'alice', password: 'alicespassword', role: 'User' },
    { username: 'joe', password: 'joespassword', role: 'Manager' }
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
        <p className="login-subtitle">LDAP Authentication System</p>

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

        <div className="divider">
          <span>Demo Users</span>
        </div>

        <div className="demo-users">
          {demoUsers.map((demo) => (
            <button
              key={demo.username}
              className="demo-user-btn"
              onClick={() => {
                setUsername(demo.username);
                setPassword(demo.password);
              }}
            >
              <span className="demo-user-name">{demo.username}</span>
              <span className="demo-user-role">{demo.role}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Login;