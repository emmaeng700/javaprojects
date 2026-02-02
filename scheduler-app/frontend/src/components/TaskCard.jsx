import { useState, useEffect } from 'react';
import './TaskCard.css';

function TaskCard({ task }) {
  const [isNew, setIsNew] = useState(false);

  useEffect(() => {
    setIsNew(true);
    const timer = setTimeout(() => setIsNew(false), 1000);
    return () => clearTimeout(timer);
  }, [task.executionTime]);

  const getStatusColor = () => {
    switch (task.status) {
      case 'success': return '#2ecc71';
      case 'warning': return '#f39c12';
      case 'error': return '#e74c3c';
      default: return '#3498db';
    }
  };

  const getStatusIcon = () => {
    switch (task.status) {
      case 'success': return '✓';
      case 'warning': return '⚠';
      case 'error': return '✗';
      default: return '•';
    }
  };

  return (
    <div className={`task-card ${isNew ? 'new-task' : ''}`}>
      <div className="task-header">
        <span 
          className="task-status-icon" 
          style={{ backgroundColor: getStatusColor() }}
        >
          {getStatusIcon()}
        </span>
        <h3>{task.taskName}</h3>
      </div>
      <p className="task-message">{task.message}</p>
      <div className="task-footer">
        <span className="task-time">
          {new Date(task.executionTime).toLocaleTimeString()}
        </span>
        <span 
          className="task-status-badge" 
          style={{ backgroundColor: getStatusColor() }}
        >
          {task.status}
        </span>
      </div>
    </div>
  );
}

export default TaskCard;