import './StatusCard.css';

function StatusCard({ title, value, icon, color, subtitle }) {
  return (
    <div className={`status-card ${color}`}>
      <div className="status-icon">{icon}</div>
      <div className="status-content">
        <h3>{title}</h3>
        <p className="status-value">{value}</p>
        {subtitle && <p className="status-subtitle">{subtitle}</p>}
      </div>
    </div>
  );
}

export default StatusCard;