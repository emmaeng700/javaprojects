import { useState } from "react";
import "./ComplexityModal.css";

export default function ComplexityModal({ onSubmit }) {
  const [time,  setTime]  = useState("");
  const [space, setSpace] = useState("");
  const [error, setError] = useState("");

  function handleSubmit() {
    if (!time.trim() || !space.trim()) {
      setError("Both fields are required before evaluation is shown.");
      return;
    }
    onSubmit({ time: time.trim(), space: space.trim() });
  }

  return (
    <div className="modal-overlay">
      <div className="modal-box">
        <div className="modal-icon">⏱️</div>
        <h2 className="modal-title">State Your Complexity</h2>
        <p className="modal-sub">
          Before your solution is evaluated, you must declare your analysis.
        </p>

        <div className="modal-fields">
          <div className="modal-field">
            <label>Time Complexity</label>
            <input
              value={time}
              onChange={(e) => setTime(e.target.value)}
              placeholder="e.g. O(n log n)"
              autoFocus
            />
          </div>
          <div className="modal-field">
            <label>Space Complexity</label>
            <input
              value={space}
              onChange={(e) => setSpace(e.target.value)}
              placeholder="e.g. O(n)"
            />
          </div>
        </div>

        {error && <div className="modal-error">{error}</div>}

        <button className="btn-primary modal-submit" onClick={handleSubmit}>
          Submit &amp; Evaluate
        </button>
      </div>
    </div>
  );
}
