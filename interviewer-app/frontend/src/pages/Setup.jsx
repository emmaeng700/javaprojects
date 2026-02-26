import { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useSession } from "../context/SessionContext";
import { startSession, parseResume } from "../utils/api";
import "./Setup.css";

const MODES = [
  { id: "behavioral",    label: "Behavioral",     icon: "🎯", time: "25–35 min", desc: "STAR stories, ownership, impact metrics. Expect interruptions." },
  { id: "technical",     label: "Technical",      icon: "💻", time: "60–75 min", desc: "Resume drill → coding questions → complexity interrogation." },
  { id: "system_design", label: "System Design",  icon: "🏗️", time: "60–75 min", desc: "Resume drill → architecture → failure injection + stress test." },
];

export default function Setup() {
  const nav = useNavigate();
  const { setSession } = useSession();

  const [mode,        setMode]        = useState("technical");
  const [practice,    setPractice]    = useState(false);
  const [resumeFile,  setResumeFile]  = useState(null);
  const [cheatFile,   setCheatFile]   = useState(null);
  const [loading,     setLoading]     = useState(false);
  const [error,       setError]       = useState(null);

  const resumeRef = useRef();
  const cheatRef  = useRef();

  async function handleStart() {
    if (!resumeFile) { setError("Please upload your resume."); return; }
    setError(null);
    setLoading(true);
    try {
      // Upload resume as base64
      const resumeB64  = await toBase64(resumeFile);
      const cheatB64   = cheatFile ? await toBase64(cheatFile) : null;

      const { sessionId } = await startSession({
        interviewType: mode,
        practiceMode:  practice,
        resumeBase64:  resumeB64,
        resumeFilename: resumeFile.name,
        cheatSheetBase64: cheatB64,
        cheatSheetFilename: cheatFile?.name ?? null,
      });

      setSession({ sessionId, mode, practiceMode: practice });
      nav("/interview");
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="setup">
      <div className="setup-card">
        <h2 className="setup-title">Configure Your Interview</h2>

        {/* Mode selector */}
        <section className="setup-section">
          <label className="setup-label">Interview Type</label>
          <div className="mode-grid">
            {MODES.map((m) => (
              <button
                key={m.id}
                className={`mode-btn ${mode === m.id ? "active" : ""}`}
                onClick={() => setMode(m.id)}
              >
                <span className="mode-btn-icon">{m.icon}</span>
                <span className="mode-btn-label">{m.label}</span>
                <span className="mode-btn-time">{m.time}</span>
                <span className="mode-btn-desc">{m.desc}</span>
              </button>
            ))}
          </div>
        </section>

        {/* Practice vs Real */}
        <section className="setup-section">
          <label className="setup-label">Mode</label>
          <div className="toggle-row">
            <button
              className={`toggle-btn ${!practice ? "active" : ""}`}
              onClick={() => setPractice(false)}
            >
              <span className="toggle-dot" />
              Real Mode
              <span className="toggle-note">No hints. Hiring bar revealed at end.</span>
            </button>
            <button
              className={`toggle-btn ${practice ? "active" : ""}`}
              onClick={() => setPractice(true)}
            >
              <span className="toggle-dot" />
              Practice Mode
              <span className="toggle-note">Immediate feedback. Corrections shown.</span>
            </button>
          </div>
        </section>

        {/* File uploads */}
        <section className="setup-section">
          <label className="setup-label">Resume <span className="required">*</span></label>
          <div
            className={`drop-zone ${resumeFile ? "has-file" : ""}`}
            onClick={() => resumeRef.current.click()}
            onDragOver={(e) => e.preventDefault()}
            onDrop={(e) => { e.preventDefault(); setResumeFile(e.dataTransfer.files[0]); }}
          >
            {resumeFile
              ? <><span className="drop-icon">📄</span> {resumeFile.name}</>
              : <><span className="drop-icon">⬆️</span> Drop PDF / DOCX or click to browse</>
            }
          </div>
          <input ref={resumeRef} type="file" accept=".pdf,.doc,.docx" hidden
            onChange={(e) => setResumeFile(e.target.files[0])} />
        </section>

        <section className="setup-section">
          <label className="setup-label">Cheat Sheet <span className="optional">(optional)</span></label>
          <div
            className={`drop-zone ${cheatFile ? "has-file" : ""}`}
            onClick={() => cheatRef.current.click()}
            onDragOver={(e) => e.preventDefault()}
            onDrop={(e) => { e.preventDefault(); setCheatFile(e.dataTransfer.files[0]); }}
          >
            {cheatFile
              ? <><span className="drop-icon">📋</span> {cheatFile.name}</>
              : <><span className="drop-icon">⬆️</span> Notes, projects, or context doc</>
            }
          </div>
          <input ref={cheatRef} type="file" accept=".pdf,.doc,.docx,.txt" hidden
            onChange={(e) => setCheatFile(e.target.files[0])} />
        </section>

        {error && <div className="setup-error">{error}</div>}

        <button
          className="btn-primary setup-start"
          onClick={handleStart}
          disabled={loading}
        >
          {loading ? "Starting session…" : "Begin Interview"}
        </button>
      </div>
    </div>
  );
}

function toBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload  = () => resolve(reader.result.split(",")[1]);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}
