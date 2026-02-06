import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [credentials, setCredentials] = useState(null);

  useEffect(() => {
    const storedUsername = localStorage.getItem('username');
    const storedPassword = localStorage.getItem('password');

    if (storedUsername && storedPassword) {
      checkAuthStatus(storedUsername, storedPassword);
    } else {
      setLoading(false);
    }
  }, []);

  const checkAuthStatus = async (username, password) => {
    try {
      const response = await authAPI.getStatus(username, password);
      if (response.data.authenticated) {
        setUser(response.data.user);
        setCredentials({ username, password });
      } else {
        logout();
      }
    } catch (error) {
      console.error('Auth check failed:', error);
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (username, password) => {
    try {
      const response = await authAPI.login(username, password);
      
      if (response.data.authenticated) {
        setUser(response.data.user);
        setCredentials({ username, password });
        localStorage.setItem('username', username);
        localStorage.setItem('password', password);
        return { success: true, user: response.data.user };
      } else {
        return { success: false, message: 'Authentication failed' };
      }
    } catch (error) {
      console.error('Login failed:', error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Invalid username or password' 
      };
    }
  };

  const logout = () => {
    setUser(null);
    setCredentials(null);
    localStorage.removeItem('username');
    localStorage.removeItem('password');
  };

  const value = {
    user,
    credentials,
    loading,
    login,
    logout,
    isAuthenticated: !!user
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};