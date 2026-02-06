import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';

const Home = () => {
  const { isAuthenticated } = useAuth();

  return (
    <>
      <Navbar />
      <div className="home">
        <div className="hero">
          <h1 className="hero-title">Welcome to LDAP Authentication System</h1>
          <p className="hero-subtitle">Secure authentication powered by Spring Boot & React</p>
          
          {!isAuthenticated ? (
            <div>
              <p style={{marginBottom: '30px', fontSize: '18px', opacity: '0.9'}}>
                Select your portal to continue:
              </p>
              <div style={{
                display: 'flex', 
                gap: '15px', 
                justifyContent: 'center', 
                flexWrap: 'wrap',
                maxWidth: '900px',
                margin: '0 auto'
              }}>
                <Link 
                  to="/admin-login" 
                  className="btn-hero" 
                  style={{
                    background: '#e53e3e',
                    flex: '1 1 250px'
                  }}
                >
                  <div style={{display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%'}}>
                    🔐 Admin Portal
                    <svg style={{marginLeft: '8px', width: '20px', height: '20px'}} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                    </svg>
                  </div>
                </Link>
                
                <Link 
                  to="/manager-login" 
                  className="btn-hero" 
                  style={{
                    background: '#9f7aea',
                    flex: '1 1 250px'
                  }}
                >
                  <div style={{display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%'}}>
                    📊 Manager Portal
                    <svg style={{marginLeft: '8px', width: '20px', height: '20px'}} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                    </svg>
                  </div>
                </Link>
                
                <Link 
                  to="/login" 
                  className="btn-hero" 
                  style={{
                    background: '#667eea',
                    flex: '1 1 250px'
                  }}
                >
                  <div style={{display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%'}}>
                    👤 User Portal
                    <svg style={{marginLeft: '8px', width: '20px', height: '20px'}} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                    </svg>
                  </div>
                </Link>
              </div>
              
              <div style={{
                marginTop: '30px',
                padding: '20px',
                background: 'rgba(255,255,255,0.1)',
                borderRadius: '10px',
                maxWidth: '600px',
                margin: '30px auto 0'
              }}>
                <p style={{fontSize: '14px', lineHeight: '1.8', margin: 0}}>
                  <strong>New here?</strong> <Link to="/register" style={{color: 'white', textDecoration: 'underline'}}>Register an account</Link> with your desired role and try accessing different portals!
                </p>
              </div>
            </div>
          ) : (
            <Link to="/dashboard" className="btn-hero">
              Go to Dashboard
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </Link>
          )}
        </div>

        <div className="features">
          <div className="feature-card">
            <div className="feature-icon blue">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <h3 className="feature-title">Secure Authentication</h3>
            <p className="feature-description">
              Spring Security with role-based access control for enterprise authentication.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon purple">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h3 className="feature-title">Modern UI</h3>
            <p className="feature-description">
              Beautiful React interface with clean CSS for responsive design.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon pink">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <h3 className="feature-title">Role-Based Access</h3>
            <p className="feature-description">
              Control access with role-based permissions and protected routes.
            </p>
          </div>
        </div>
      </div>
    </>
  );
};

export default Home;