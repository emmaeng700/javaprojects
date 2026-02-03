import { useState } from 'react';
import './CustomerCard.css';

function CustomerCard({ customer, onUpdate, onDelete }) {
  const [isEditing, setIsEditing] = useState(false);
  const [firstName, setFirstName] = useState(customer.firstName);
  const [lastName, setLastName] = useState(customer.lastName);

  const handleSave = async () => {
    await onUpdate(customer.id, firstName, lastName);
    setIsEditing(false);
  };

  const handleCancel = () => {
    setFirstName(customer.firstName);
    setLastName(customer.lastName);
    setIsEditing(false);
  };

  return (
    <div className="customer-card">
      <div className="customer-id">ID: {customer.id}</div>
      
      {isEditing ? (
        <div className="edit-form">
          <input
            type="text"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            placeholder="First Name"
          />
          <input
            type="text"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            placeholder="Last Name"
          />
          <div className="button-group">
            <button className="btn-save" onClick={handleSave}>💾 Save</button>
            <button className="btn-cancel" onClick={handleCancel}>❌ Cancel</button>
          </div>
        </div>
      ) : (
        <div className="customer-info">
          <h3>{customer.firstName} {customer.lastName}</h3>
          <div className="button-group">
            <button className="btn-edit" onClick={() => setIsEditing(true)}>✏️ Edit</button>
            <button className="btn-delete" onClick={() => onDelete(customer.id)}>🗑️ Delete</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default CustomerCard;