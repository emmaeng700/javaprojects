<div className="hero">
  <h1 className="hero-title">Welcome to LDAP Authentication System</h1>
  <p className="hero-subtitle">Secure authentication powered by Spring Boot & React</p>
  
  {!isAuthenticated ? (
    <div>
      <p style={{marginBottom: '30px', fontSize: '18px', opacity: '0.9'}}>
        Select your portal to continue:
      </p>
      <div style={{display: 'flex', gap: '15px', justifyContent: 'center', flexWrap: 'wrap'}}>
        <Link to="/admin-login" className="btn-hero" style={{background: '#e53e3e'}}>
          🔐 Admin Portal
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
          </svg>
        </Link>
        <Link to="/manager-login" className="btn-hero" style={{background: '#9f7aea'}}>
          📊 Manager Portal
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
          </svg>
        </Link>
        <Link to="/login" className="btn-hero">
          👤 User Portal
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
          </svg>
        </Link>
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