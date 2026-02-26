import { useNavigate } from "react-router-dom";
import "./Home.css";

export default function Home() {
  const nav = useNavigate();

  return (
    <div className="home">
      <div className="home-hero">
        <div className="home-badge">FAANG-Level Onsite Simulator</div>
        <h1 className="home-title">
          Interview<span className="accent">Verse</span>
        </h1>
        <p className="home-sub">
          High-bar AI interviewer. Behavioral. Technical. System Design.<br />
          No hand-holding. Real hiring calibration.
        </p>
        <button className="btn-primary home-cta" onClick={() => nav("/setup")}>
          Start Interview
        </button>
      </div>

      <div className="home-features">
        {FEATURES.map((f) => (
          <div key={f.title} className="feature-card">
            <div className="feature-icon">{f.icon}</div>
            <h3>{f.title}</h3>
            <p>{f.desc}</p>
          </div>
        ))}
      </div>

      <div className="home-modes">
        <div className="mode-pill behavioral">Behavioral &nbsp;25–35 min</div>
        <div className="mode-pill technical">Technical &nbsp;60–75 min</div>
        <div className="mode-pill design">System Design &nbsp;60–75 min</div>
      </div>
    </div>
  );
}

const FEATURES = [
  { icon: "🎯", title: "Resume Drilling",      desc: "AI parses your resume and probes every claim for metrics and impact." },
  { icon: "💻", title: "Code Interrogation",   desc: "Interrupted mid-solution with complexity and trade-off challenges." },
  { icon: "🏗️", title: "Design Stress Tests",  desc: "Failure injection, traffic spikes, and cost analysis on your architecture." },
  { icon: "📊", title: "Hiring Bar Report",     desc: "L3–L6 projection with Strong Hire / No Hire calibration." },
  { icon: "🎙️", title: "Voice Input",           desc: "Answer with your voice. Silence detection tracks rambling." },
  { icon: "⏱️", title: "Server-Enforced Timers", desc: "Sections auto-transition. No extensions. Just like a real onsite." },
];
