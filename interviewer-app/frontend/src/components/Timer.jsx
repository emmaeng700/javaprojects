import { useEffect, useState } from "react";
import "./Timer.css";

export default function Timer({ totalSeconds, onExpire, section }) {
  const [remaining, setRemaining] = useState(totalSeconds);

  useEffect(() => {
    setRemaining(totalSeconds);
  }, [totalSeconds]);

  useEffect(() => {
    if (remaining <= 0) { onExpire?.(); return; }
    const id = setTimeout(() => setRemaining((r) => r - 1), 1000);
    return () => clearTimeout(id);
  }, [remaining]);

  const pct     = remaining / totalSeconds;
  const mins    = String(Math.floor(remaining / 60)).padStart(2, "0");
  const secs    = String(remaining % 60).padStart(2, "0");
  const urgent  = remaining <= 120;
  const warning = remaining <= 300;

  return (
    <div className={`timer ${urgent ? "urgent" : warning ? "warning" : ""}`}>
      <div className="timer-section">{section}</div>
      <div className="timer-display">{mins}:{secs}</div>
      <div className="timer-bar-track">
        <div className="timer-bar-fill" style={{ width: `${pct * 100}%` }} />
      </div>
    </div>
  );
}
