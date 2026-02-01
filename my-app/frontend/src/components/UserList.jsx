import { useState, useEffect } from 'react';
import { getUsers } from '../services/api';
import './UserList.css';

function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // ⭐ THIS RUNS WHEN COMPONENT LOADS - FETCHES DATA FROM SPRING BOOT
  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const data = await getUsers(); // ⭐ CALLS SPRING BOOT API
      setUsers(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch users. Make sure the backend is running!');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading users...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="user-list">
      <h2>Users from Spring Boot Backend</h2>
      <div className="users-grid">
        {users.map((user) => (
          <div key={user.id} className="user-card">
            <h3>{user.name}</h3>
            <p>{user.email}</p>
            <span className="user-id">ID: {user.id}</span>
          </div>
        ))}
      </div>
      <button onClick={fetchUsers} className="refresh-btn">
        Refresh Users
      </button>
    </div>
  );
}

export default UserList;