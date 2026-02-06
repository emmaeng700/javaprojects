import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const hasRole = (role) => {
    return user?.role?.includes(role);
  };

  return (
    <nav className="navbar">
      <div className="navbar-content">
        <Link to="/" className="navbar-brand">
          <svg className="navbar-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
          LDAP Auth System
        </Link>

        {isAuthenticated && (
          <div className="navbar-menu">
            <Link to="/dashboard" className="navbar-link">Dashboard</Link>
            
            {hasRole('ROLE_ADMIN') && (
              <Link to="/admin" className="navbar-link">🔐 Admin Panel</Link>
            )}
            
            {(hasRole('ROLE_MANAGER') || hasRole('ROLE_ADMIN')) && (
              <Link to="/manager" className="navbar-link">📊 Manager Panel</Link>
            )}
            
            <Link to="/settings" className="navbar-link">Settings</Link>
            
            <div className="navbar-user">
              <div className="navbar-avatar">
                {user?.username?.charAt(0).toUpperCase()}
              </div>
              <span>{user?.username}</span>
            </div>
            <button onClick={handleLogout} className="btn-logout">
              Logout
            </button>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;