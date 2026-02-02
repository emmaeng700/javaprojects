import { useState, useEffect } from 'react';
import TaskCard from './TaskCard';
import websocketService from '../services/websocket';
import './Dashboard.css';

function Dashboard() {
  const [tasks, setTasks] = useState([]);
  const [connected, setConnected] = useState(false);
  const [stats, setStats] = useState({
    total: 0,
    success: 0,
    warning: 0,
    error: 0
  });

  useEffect(() => {
    console.log('🚀 Connecting to WebSocket...');
    
    websocketService.connect((taskExecution) => {
      console.log('📥 Task received:', taskExecution);
      
      setTasks(prevTasks => {
        const newTasks = [taskExecution, ...prevTasks].slice(0, 20);
        return newTasks;
      });

      setStats(prev => ({
        total: prev.total + 1,
        success: prev.success + (taskExecution.status === 'success' ? 1 : 0),
        warning: prev.warning + (taskExecution.status === 'warning' ? 1 : 0),
        error: prev.error + (taskExecution.status === 'error' ? 1 : 0)
      }));
    });

    setConnected(true);

    return () => {
      websocketService.disconnect();
    };
  }, []);

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>🕐 Scheduled Tasks Dashboard</h1>
        <div className="connection-status">
          <span className={`status-dot ${connected ? 'connected' : 'disconnected'}`}></span>
          <span>{connected ? 'Connected' : 'Disconnected'}</span>
        </div>
      </header>

      <div className="stats-grid">
        <div className="stat-card total">
          <h3>Total Executions</h3>
          <p className="stat-number">{stats.total}</p>
        </div>
        <div className="stat-card success">
          <h3>Successful</h3>
          <p className="stat-number">{stats.success}</p>
        </div>
        <div className="stat-card warning">
          <h3>Warnings</h3>
          <p className="stat-number">{stats.warning}</p>
        </div>
        <div className="stat-card error">
          <h3>Errors</h3>
          <p className="stat-number">{stats.error}</p>
        </div>
      </div>

      <div className="tasks-section">
        <h2>Recent Task Executions</h2>
        {tasks.length === 0 ? (
          <div className="no-tasks">
            <p>⏳ Waiting for scheduled tasks to execute...</p>
            <p className="hint">Make sure backend is running on port 8080</p>
            <div className="loading-spinner"></div>
          </div>
        ) : (
          <div className="tasks-grid">
            {tasks.map((task, index) => (
              <TaskCard key={`${task.taskName}-${task.executionTime}-${index}`} task={task} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Dashboard;