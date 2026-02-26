import { useNavigate } from "react-router-dom";
import { useSession } from "../context/SessionContext";
import { useEffect } from "react";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import "./Report.css";

const HIRE_COLORS = {
  "Strong Hire": "#00e5a0",
  "Hire":        "#7c6dfa",
  "Lean Hire":   "#ffb740",
  "Lean No Hire":"#ff8040",
  "No Hire":     "#ff4c6a",
};

export default function Report() {
  const nav = useNavigate();
  const { hiringReport: r, session } = useSession();

  useEffect(() => {
    if (!r) nav("/");
  }, [r]);

  if (!r) return null;

  const hireColor = HIRE_COLORS[r.hire_recommendation] ?? "#7c6dfa";

  function downloadPDF() {
    const doc = new jsPDF({ unit: "pt", format: "letter" });
    const W = doc.internal.pageSize.getWidth();

    // Header
    doc.setFillColor(13, 13, 20);
    doc.rect(0, 0, W, 80, "F");
    doc.setTextColor(124, 109, 250);
    doc.setFontSize(22);
    doc.setFont("helvetica", "bold");
    doc.text("InterviewVerse — Hiring Report", 40, 50);

    doc.setTextColor(200, 200, 220);
    doc.setFontSize(11);
    doc.setFont("helvetica", "normal");
    doc.text(`Session: ${session?.sessionId ?? "—"}   Mode: ${session?.mode ?? "—"}`, 40, 68);

    let y = 100;

    // Recommendation
    doc.setFontSize(18);
    doc.setFont("helvetica", "bold");
    doc.setTextColor(...hexToRgb(hireColor));
    doc.text(`${r.hire_recommendation}  ·  ${r.level_projection}`, 40, y);
    y += 30;

    doc.setTextColor(80, 80, 100);
    doc.setFontSize(11);
    doc.setFont("helvetica", "normal");
    doc.text(r.bar_analysis ?? "", 40, y, { maxWidth: W - 80 });
    y += 40;

    // Scores table
    autoTable(doc, {
      startY: y,
      head: [["Category", "Score"]],
      body: [
        ["Overall",             r.final_score],
        ["Behavioral",          r.behavioral_score],
        ["Coding",              r.coding_score],
        ["System Design",       r.system_design_score],
        ["Complexity Awareness",r.complexity_awareness_score],
        ["Communication",       r.communication_score],
        ["Resume Authenticity", r.resume_authenticity_score],
      ],
      styles:     { fillColor: [19, 19, 31], textColor: [232, 232, 240] },
      headStyles: { fillColor: [124, 109, 250] },
      alternateRowStyles: { fillColor: [26, 26, 46] },
    });
    y = doc.lastAutoTable.finalY + 20;

    // Strengths / Weaknesses
    const listSection = (title, items) => {
      if (!items?.length) return;
      doc.setFont("helvetica", "bold");
      doc.setFontSize(13);
      doc.setTextColor(232, 232, 240);
      doc.text(title, 40, y);
      y += 16;
      doc.setFont("helvetica", "normal");
      doc.setFontSize(11);
      doc.setTextColor(160, 160, 190);
      items.forEach((item) => {
        doc.text(`• ${item}`, 48, y, { maxWidth: W - 90 });
        y += 16;
      });
      y += 8;
    };

    listSection("Strengths",              r.strengths_summary);
    listSection("Weaknesses",             r.weaknesses_summary);
    listSection("Missed Depth",           r.missed_depth_opportunities);
    listSection("Coding Improvements",    r.coding_improvements);
    listSection("Architecture Improvements", r.architecture_improvements);

    doc.save(`InterviewVerse_Report_${session?.sessionId ?? "session"}.pdf`);
  }

  const ScoreBar = ({ label, value }) => (
    <div className="score-row">
      <span className="score-label">{label}</span>
      <div className="score-bar-track">
        <div
          className="score-bar-fill"
          style={{
            width: `${value}%`,
            background: value >= 75 ? "var(--success)" : value >= 50 ? "var(--warn)" : "var(--danger)",
          }}
        />
      </div>
      <span className="score-val">{value}</span>
    </div>
  );

  return (
    <div className="report">
      <div className="report-card">
        {/* Header */}
        <div className="report-header">
          <div className="report-logo">InterviewVerse</div>
          <h1 className="report-title">Hiring Evaluation</h1>
        </div>

        {/* Verdict */}
        <div className="verdict-box" style={{ borderColor: hireColor }}>
          <div className="verdict-recommendation" style={{ color: hireColor }}>
            {r.hire_recommendation}
          </div>
          <div className="verdict-level">{r.level_projection}</div>
          <p className="verdict-analysis">{r.bar_analysis}</p>
        </div>

        {/* Scores */}
        <section className="report-section">
          <h2 className="section-title">Score Breakdown</h2>
          <ScoreBar label="Overall"               value={r.final_score} />
          <ScoreBar label="Behavioral"             value={r.behavioral_score} />
          <ScoreBar label="Coding"                 value={r.coding_score} />
          <ScoreBar label="System Design"          value={r.system_design_score} />
          <ScoreBar label="Complexity Awareness"   value={r.complexity_awareness_score} />
          <ScoreBar label="Communication"          value={r.communication_score} />
          <ScoreBar label="Resume Authenticity"    value={r.resume_authenticity_score} />
        </section>

        {/* Lists */}
        {[
          { title: "Strengths",                 items: r.strengths_summary,           color: "var(--success)" },
          { title: "Weaknesses",                items: r.weaknesses_summary,          color: "var(--danger)" },
          { title: "Missed Depth Opportunities",items: r.missed_depth_opportunities,  color: "var(--warn)" },
          { title: "Coding Improvements",       items: r.coding_improvements,         color: "var(--accent)" },
          { title: "Architecture Improvements", items: r.architecture_improvements,   color: "var(--primary)" },
        ].map(({ title, items, color }) =>
          items?.length ? (
            <section key={title} className="report-section">
              <h2 className="section-title" style={{ color }}>{title}</h2>
              <ul className="report-list">
                {items.map((item, i) => <li key={i}>{item}</li>)}
              </ul>
            </section>
          ) : null
        )}

        {/* Behavioral rewrites */}
        {r.behavioral_rewrites?.length > 0 && (
          <section className="report-section">
            <h2 className="section-title" style={{ color: "var(--warn)" }}>Behavioral Rewrites</h2>
            {r.behavioral_rewrites.map((rw, i) => (
              <div key={i} className="rewrite-block">
                <div className="rewrite-label">Original</div>
                <p className="rewrite-text muted">{rw.original}</p>
                <div className="rewrite-label" style={{ color: "var(--success)" }}>Improved</div>
                <p className="rewrite-text">{rw.improved}</p>
              </div>
            ))}
          </section>
        )}

        {/* Actions */}
        <div className="report-actions">
          <button className="btn-primary" onClick={downloadPDF}>
            Download PDF Report
          </button>
          <button className="btn-secondary" onClick={() => nav("/setup")}>
            New Interview
          </button>
        </div>
      </div>
    </div>
  );
}

function hexToRgb(hex) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return [r, g, b];
}
