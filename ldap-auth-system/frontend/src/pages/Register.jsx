import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    email: '',
    role: 'USER'
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters');
      setLoading(false);
      return;
    }

    try {
      const response = await axios.post('http://localhost:8080/api/auth/register', {
        username: formData.username,
        password: formData.password,
        fullName: formData.fullName,
        email: formData.email,
        role: formData.role
      });

      if (response.data.success) {
        alert('Registration successful! Please login.');
        navigate('/login');
      } else {
        setError(response.data.message || 'Registration failed');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box" style={{maxWidth: '500px'}}>
        <div className="login-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
        </div>
        
        <h2 className="login-title">Create your account</h2>
        <p className="login-subtitle">Fill in your details to register</p>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="error-message">{error}</div>
          )}

          <div style={{marginBottom: '15px'}}>
            <input
              type="text"
              name="username"
              className="form-input"
              placeholder="Username"
              value={formData.username}
              onChange={handleChange}
              required
              style={{borderRadius: '4px', marginBottom: '10px'}}
            />
            
            <input
              type="text"
              name="fullName"
              className="form-input"
              placeholder="Full Name"
              value={formData.fullName}
              onChange={handleChange}
              required
              style={{borderRadius: '4px', marginBottom: '10px'}}
            />
            
            <input
              type="email"
              name="email"
              className="form-input"
              placeholder="Email"
              value={formData.email}
              onChange={handleChange}
              required
              style={{borderRadius: '4px', marginBottom: '10px'}}
            />
            
            <input
              type="password"
              name="password"
              className="form-input"
              placeholder="Password (min 6 characters)"
              value={formData.password}
              onChange={handleChange}
              required
              style={{borderRadius: '4px', marginBottom: '10px'}}
            />
            
            <input
              type="password"
              name="confirmPassword"
              className="form-input"
              placeholder="Confirm Password"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              style={{borderRadius: '4px', marginBottom: '10px'}}
            />

            <div style={{marginBottom: '10px'}}>
              <label style={{display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333', textAlign: 'left'}}>
                Select Role
              </label>
              <select
                name="role"
                value={formData.role}
                onChange={handleChange}
                className="form-input"
                style={{borderRadius: '4px'}}
              >
                <option value="USER">User - Basic Access (Dashboard, Settings)</option>
                <option value="MANAGER">Manager - Can access Manager Panel</option>
                <option value="ADMIN">Admin - Full System Access</option>
              </select>
            </div>
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Creating Account...' : 'Register'}
          </button>
        </form>

        <div style={{marginTop: '20px', textAlign: 'center', fontSize: '14px'}}>
          Already have an account?{' '}
          <Link to="/login" style={{color: '#667eea', textDecoration: 'none', fontWeight: '600'}}>
            Sign in here
          </Link>
        </div>

        <div style={{
          marginTop: '20px',
          padding: '15px',
          background: '#e6f0ff',
          borderRadius: '8px',
          fontSize: '13px',
          textAlign: 'left'
        }}>
          <strong>💡 Role Access Levels:</strong>
          <ul style={{margin: '10px 0 0 20px', lineHeight: '1.8'}}>
            <li><strong>USER:</strong> Dashboard, Settings</li>
            <li><strong>MANAGER:</strong> USER access + Manager Panel</li>
            <li><strong>ADMIN:</strong> MANAGER access + Admin Panel</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Register;