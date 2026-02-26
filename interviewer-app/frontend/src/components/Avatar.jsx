import "./Avatar.css";

const STATES = {
  neutral:    { emoji: "🧑‍💼", label: "Listening",      color: "#7c6dfa" },
  nod:        { emoji: "👍", label: "Strong answer",   color: "#00e5a0" },
  concerned:  { emoji: "🤨", label: "Probing deeper",  color: "#ffb740" },
  analytical: { emoji: "🔍", label: "Analyzing code",  color: "#00d4ff" },
  interrupt:  { emoji: "✋", label: "Interrupting",    color: "#ff4c6a" },
  confetti:   { emoji: "🎉", label: "Optimal!",        color: "#00e5a0" },
};

export default function Avatar({ state = "neutral" }) {
  const s = STATES[state] || STATES.neutral;
  return (
    <div className="avatar" style={{ "--avatar-color": s.color }}>
      <div className="avatar-circle">
        <span className="avatar-emoji">{s.emoji}</span>
      </div>
      <div className="avatar-label">{s.label}</div>
      <div className="avatar-pulse" />
    </div>
  );
}
