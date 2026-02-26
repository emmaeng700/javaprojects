import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import Editor from "@monaco-editor/react";
import confetti from "canvas-confetti";

// ── Text-to-Speech ────────────────────────────────────────────────────────────
function speak(text) {
  if (!window.speechSynthesis) return;
  // Cancel anything already speaking
  window.speechSynthesis.cancel();
  const utt = new SpeechSynthesisUtterance(text);
  utt.rate   = 0.95;
  utt.pitch  = 0.9;
  utt.volume = 1;
  // Pick a deep/professional voice if available
  const voices = window.speechSynthesis.getVoices();
  const preferred = voices.find(v =>
    /google us english|alex|daniel|karen/i.test(v.name)
  ) || voices.find(v => v.lang === "en-US") || voices[0];
  if (preferred) utt.voice = preferred;
  window.speechSynthesis.speak(utt);
}

import { useSession } from "../context/SessionContext";
import { useSpeech } from "../hooks/useSpeech";
import Avatar from "../components/Avatar";
import Timer from "../components/Timer";
import ComplexityModal from "../components/ComplexityModal";

import {
  generateQuestion, evaluateAnswer, handleFollowUp,
  executeCode, validateComplexityAnswer, interrogateCode,
  escalateCodingQuestion, transitionSection, endSession,
  evaluateDesign, stressTestDesign,
} from "../utils/api";

import "./Interview.css";

const SECTION_TIMES = {
  resume:   15 * 60,
  behavioral: 30 * 60,
  coding:   50 * 60,
  design:   50 * 60,
};

export default function Interview() {
  const nav = useNavigate();
  const { session, setHiringReport } = useSession();

  // Guard — if navigated here directly without session
  useEffect(() => {
    if (!session) nav("/");
  }, [session]);

  // ── State ──────────────────────────────────────────────────────────────────

  const [messages,        setMessages]       = useState([]);
  const [userInput,       setUserInput]      = useState("");
  const [section,        setSection]         = useState("resume");  // resume | behavioral | coding | design
  const [avatarState,    setAvatarState]     = useState("neutral");
  const [loading,        setLoading]         = useState(false);
  const [code,           setCode]            = useState("# Write your solution here\n");
  const [codeLanguage,   setCodeLanguage]    = useState("python");
  const [showCode,       setShowCode]        = useState(session?.mode === "technical");
  const [showComplexity, setShowComplexity]  = useState(false);
  const [codeOutput,     setCodeOutput]      = useState(null);
  const [currentQuestion,setCurrentQuestion] = useState(null);
  const [sectionTime,    setSectionTime]     = useState(SECTION_TIMES.resume);

  const chatBottomRef = useRef();
  const inputRef      = useRef();

  // ── Speech ─────────────────────────────────────────────────────────────────

  const { isListening, transcript, bufferedAnswer, answerComplete,
          interruptSignal, rambling, startListening, stopListening,
          resetBuffer } = useSpeech({
    sessionId:  session?.sessionId,
    questionId: currentQuestion?.questionId,
    onAnswerComplete: (text) => submitAnswer(text),
    onInterrupt:      ()     => setAvatarState("interrupt"),
  });

  // ── Bootstrap — generate first question ────────────────────────────────────

  useEffect(() => {
    if (!session) return;
    (async () => {
      setLoading(true);
      addMessage("system", "Session started. Loading your first question…");
      try {
        const q = await generateQuestion({
          sessionId:    session.sessionId,
          section:      "resume",
          practiceMode: session.practiceMode,
        });
        setCurrentQuestion(q);
        addMessage("interviewer", q.question);
        setAvatarState("neutral");
      } catch (e) {
        addMessage("system", `Error: ${e.message}`);
      } finally {
        setLoading(false);
      }
    })();
  }, [session]);

  // ── Rambling detection ──────────────────────────────────────────────────────

  useEffect(() => {
    if (rambling) setAvatarState("interrupt");
  }, [rambling]);

  // ── Helpers ─────────────────────────────────────────────────────────────────

  const [isSpeaking, setIsSpeaking] = useState(false);
  const [ttsEnabled, setTtsEnabled] = useState(true);

  function addMessage(role, text, meta = {}) {
    setMessages((prev) => [...prev, { role, text, meta, id: Date.now() + Math.random() }]);
    setTimeout(() => chatBottomRef.current?.scrollIntoView({ behavior: "smooth" }), 50);
    if (role === "interviewer" && ttsEnabled) {
      // Load voices async on first call (Chrome requires user gesture first)
      const doSpeak = () => {
        setIsSpeaking(true);
        const utt = new SpeechSynthesisUtterance(text);
        utt.rate   = 0.95;
        utt.pitch  = 0.9;
        utt.volume = 1;
        const voices = window.speechSynthesis.getVoices();
        const preferred = voices.find(v =>
          /google us english|alex|daniel|karen/i.test(v.name)
        ) || voices.find(v => v.lang === "en-US") || voices[0];
        if (preferred) utt.voice = preferred;
        utt.onend = () => setIsSpeaking(false);
        utt.onerror = () => setIsSpeaking(false);
        window.speechSynthesis.cancel();
        window.speechSynthesis.speak(utt);
      };
      if (window.speechSynthesis.getVoices().length === 0) {
        window.speechSynthesis.addEventListener("voiceschanged", doSpeak, { once: true });
      } else {
        doSpeak();
      }
    }
  }

  // ── Submit text answer ──────────────────────────────────────────────────────

  async function submitAnswer(text) {
    const answer = text || userInput.trim();
    if (!answer || loading) return;
    setUserInput("");
    resetBuffer?.();
    addMessage("user", answer);
    setLoading(true);
    setAvatarState("analytical");

    try {
      const result = await evaluateAnswer({
        sessionId:    session.sessionId,
        questionId:   currentQuestion?.questionId,
        answer,
        section,
        practiceMode: session.practiceMode,
      });

      // Avatar reaction
      if (result.score >= 75)       setAvatarState("nod");
      else if (result.score < 50)   setAvatarState("concerned");
      else                          setAvatarState("neutral");

      if (result.feedback) addMessage("interviewer", result.feedback, { score: result.score });

      // Follow-up if weak
      if (result.requiresFollowUp) {
        const fu = await handleFollowUp({
          sessionId:  session.sessionId,
          questionId: currentQuestion?.questionId,
          context:    answer,
        });
        addMessage("interviewer", fu.followUp, { isFollowUp: true });
        setAvatarState("concerned");
      } else if (result.nextQuestion) {
        setCurrentQuestion(result.nextQuestion);
        addMessage("interviewer", result.nextQuestion.question);
        setAvatarState("neutral");
        if (result.nextQuestion.type === "coding") {
          setShowCode(true);
          setSection("coding");
          setSectionTime(SECTION_TIMES.coding);
        }
      } else if (result.sectionComplete) {
        await handleSectionTransition();
      }
    } catch (e) {
      addMessage("system", `Error: ${e.message}`);
      setAvatarState("neutral");
    } finally {
      setLoading(false);
    }
  }

  // ── Section transition ──────────────────────────────────────────────────────

  async function handleSectionTransition() {
    setLoading(true);
    try {
      const result = await transitionSection({ sessionId: session.sessionId });
      const next = result.nextSection;

      if (!next || next === "end") {
        await finishSession();
        return;
      }

      setSection(next);
      setSectionTime(SECTION_TIMES[next] ?? 30 * 60);
      addMessage("system", result.transitionMessage);
      if (next === "coding") setShowCode(true);

      const q = await generateQuestion({
        sessionId:    session.sessionId,
        section:      next,
        practiceMode: session.practiceMode,
      });
      setCurrentQuestion(q);
      addMessage("interviewer", q.question);
      setAvatarState("neutral");
    } catch (e) {
      addMessage("system", `Transition error: ${e.message}`);
    } finally {
      setLoading(false);
    }
  }

  // ── Code execution ──────────────────────────────────────────────────────────

  async function runCode() {
    setLoading(true);
    setCodeOutput(null);
    try {
      const result = await executeCode({
        sessionId: session.sessionId,
        code,
        language: codeLanguage,
      });
      setCodeOutput(result);

      // Interrogate mid-solution
      const interrogation = await interrogateCode({
        sessionId:  session.sessionId,
        questionId: currentQuestion?.questionId,
        code,
        output:     result.output,
      });
      if (interrogation.challenge) {
        addMessage("interviewer", interrogation.challenge, { isInterrupt: true });
        setAvatarState("interrupt");
      }
    } catch (e) {
      setCodeOutput({ error: e.message });
    } finally {
      setLoading(false);
    }
  }

  // ── Submit code for evaluation ──────────────────────────────────────────────

  function submitCode() {
    setShowComplexity(true);
  }

  async function handleComplexitySubmit({ time, space }) {
    setShowComplexity(false);
    setLoading(true);
    try {
      const result = await validateComplexityAnswer({
        sessionId:       session.sessionId,
        questionId:      currentQuestion?.questionId,
        code,
        language:        codeLanguage,
        timeClaim:       time,
        spaceClaim:      space,
        practiceMode:    session.practiceMode,
      });

      addMessage("interviewer", result.feedback, {
        complexityCorrect: result.complexityCorrect,
        codeCorrect:       result.codeCorrect,
        score:             result.score,
      });

      if (result.codeCorrect && result.complexityCorrect) {
        setAvatarState("confetti");
        fireConfetti();
      } else if (result.codeCorrect && !result.complexityCorrect) {
        setAvatarState("concerned");
      }

      // Escalate if solved quickly and correctly
      if (result.codeCorrect && result.escalate) {
        const escalated = await escalateCodingQuestion({
          sessionId: session.sessionId,
          previousQuestionId: currentQuestion?.questionId,
        });
        if (escalated.question) {
          setCurrentQuestion(escalated);
          addMessage("interviewer", escalated.question, { isEscalation: true });
          setCode("# Write your solution here\n");
          setCodeOutput(null);
          setAvatarState("analytical");
        }
      } else if (result.sectionComplete) {
        await handleSectionTransition();
      }
    } catch (e) {
      addMessage("system", `Error: ${e.message}`);
    } finally {
      setLoading(false);
    }
  }

  // ── Design answer ───────────────────────────────────────────────────────────

  async function submitDesignAnswer(text) {
    const answer = text || userInput.trim();
    if (!answer || loading) return;
    setUserInput("");
    addMessage("user", answer);
    setLoading(true);

    try {
      const [evalResult, stressResult] = await Promise.all([
        evaluateDesign({ sessionId: session.sessionId, questionId: currentQuestion?.questionId, answer }),
        stressTestDesign({ sessionId: session.sessionId, answer }),
      ]);

      if (evalResult.score >= 70) setAvatarState("nod");
      else setAvatarState("concerned");

      addMessage("interviewer", evalResult.feedback, { score: evalResult.score });
      if (stressResult.stressQuestion) {
        addMessage("interviewer", stressResult.stressQuestion, { isStress: true });
        setAvatarState("interrupt");
      }
    } catch (e) {
      addMessage("system", `Error: ${e.message}`);
    } finally {
      setLoading(false);
    }
  }

  // ── End session ─────────────────────────────────────────────────────────────

  async function finishSession() {
    setLoading(true);
    try {
      const report = await endSession({ sessionId: session.sessionId });
      setHiringReport(report);
      nav("/report");
    } catch (e) {
      addMessage("system", `Error ending session: ${e.message}`);
      setLoading(false);
    }
  }

  // ── Confetti ────────────────────────────────────────────────────────────────

  function fireConfetti() {
    confetti({ particleCount: 180, spread: 90, origin: { y: 0.5 } });
    setTimeout(() => confetti({ particleCount: 80, spread: 120, origin: { y: 0.3 } }), 400);
  }

  // ── Send handler (routes based on section) ──────────────────────────────────

  function handleSend() {
    if (section === "design") submitDesignAnswer();
    else submitAnswer();
  }

  function handleKeyDown(e) {
    if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); handleSend(); }
  }

  if (!session) return null;

  return (
    <div className="iv-layout">
      {/* Sidebar */}
      <aside className="iv-sidebar">
        <div className="iv-logo">InterviewVerse</div>

        <Avatar state={avatarState} />

        <Timer
          totalSeconds={sectionTime}
          section={section.toUpperCase()}
          onExpire={handleSectionTransition}
        />

        <div className="iv-session-info">
          <div className="info-row">
            <span className="info-label">Mode</span>
            <span className={`info-val mode-${session.mode}`}>{session.mode.replace("_", " ")}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Type</span>
            <span className="info-val">{session.practiceMode ? "Practice" : "Real"}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Section</span>
            <span className="info-val">{section}</span>
          </div>
        </div>

        {isListening && (
          <div className="mic-indicator">
            <span className="mic-dot" />
            Listening…
            {transcript && <div className="mic-live">{transcript}</div>}
          </div>
        )}

        {isSpeaking && (
          <div className="speaking-indicator">
            <span className="speaking-bar" /><span className="speaking-bar" />
            <span className="speaking-bar" /><span className="speaking-bar" />
            Interviewer speaking…
          </div>
        )}

        <div className="iv-sidebar-actions">
          <button
            className={`btn-tts ${ttsEnabled ? "active" : ""}`}
            onClick={() => {
              if (ttsEnabled) window.speechSynthesis.cancel();
              setTtsEnabled(v => !v);
            }}
          >
            {ttsEnabled ? "🔊 Voice On" : "🔇 Voice Off"}
          </button>
          <button
            className={`btn-mic ${isListening ? "active" : ""}`}
            onClick={isListening ? stopListening : startListening}
          >
            {isListening ? "🛑 Stop" : "🎙️ Speak"}
          </button>
          {section === "coding" && (
            <button
              className="btn-code-toggle"
              onClick={() => setShowCode((v) => !v)}
            >
              {showCode ? "Hide Editor" : "Show Editor"}
            </button>
          )}
          <button className="btn-end" onClick={finishSession}>
            End Session
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="iv-main">
        {/* Chat */}
        <div className="iv-chat">
          {messages.map((m) => (
            <div key={m.id} className={`msg msg-${m.role}`}>
              {m.role === "interviewer" && <div className="msg-role">Interviewer</div>}
              {m.role === "user"        && <div className="msg-role">You</div>}
              {m.role === "system"      && <div className="msg-system">{m.text}</div>}
              {m.role !== "system"      && <div className="msg-text">{m.text}</div>}
              {m.meta?.score !== undefined && session.practiceMode && (
                <div className={`msg-score ${m.meta.score >= 75 ? "good" : m.meta.score >= 50 ? "ok" : "bad"}`}>
                  Score: {m.meta.score}/100
                </div>
              )}
              {m.meta?.isFollowUp   && <div className="msg-tag tag-followup">Follow-up</div>}
              {m.meta?.isInterrupt  && <div className="msg-tag tag-interrupt">Interruption</div>}
              {m.meta?.isStress     && <div className="msg-tag tag-stress">Stress Test</div>}
              {m.meta?.isEscalation && <div className="msg-tag tag-escalation">Escalation</div>}
            </div>
          ))}
          {loading && (
            <div className="msg msg-interviewer">
              <div className="msg-role">Interviewer</div>
              <div className="msg-typing"><span /><span /><span /></div>
            </div>
          )}
          <div ref={chatBottomRef} />
        </div>

        {/* Code editor */}
        {showCode && (
          <div className="iv-code-panel">
            <div className="code-toolbar">
              <select
                value={codeLanguage}
                onChange={(e) => setCodeLanguage(e.target.value)}
                className="lang-select"
              >
                <option value="python">Python</option>
                <option value="javascript">JavaScript</option>
                <option value="java">Java</option>
                <option value="cpp">C++</option>
                <option value="go">Go</option>
              </select>
              <button className="btn-run" onClick={runCode} disabled={loading}>
                ▶ Run
              </button>
              <button className="btn-submit-code" onClick={submitCode} disabled={loading}>
                Submit Solution
              </button>
            </div>

            <Editor
              height="320px"
              language={codeLanguage === "cpp" ? "cpp" : codeLanguage}
              value={code}
              onChange={(v) => setCode(v ?? "")}
              theme="vs-dark"
              options={{
                fontSize: 14,
                fontFamily: "JetBrains Mono, monospace",
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                lineNumbers: "on",
              }}
            />

            {codeOutput && (
              <div className={`code-output ${codeOutput.error ? "error" : ""}`}>
                <div className="code-output-label">
                  {codeOutput.error ? "Error" : "Output"}
                </div>
                <pre>{codeOutput.error || codeOutput.output}</pre>
              </div>
            )}
          </div>
        )}

        {/* Input */}
        <div className="iv-input-bar">
          <textarea
            ref={inputRef}
            className="iv-textarea"
            value={userInput}
            onChange={(e) => setUserInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={
              isListening
                ? "Speaking… (or type here)"
                : section === "coding"
                ? "Type your explanation or use the editor above…"
                : "Type your answer… (Enter to send, Shift+Enter for newline)"
            }
            rows={3}
            disabled={loading}
          />
          <button
            className="btn-send"
            onClick={handleSend}
            disabled={loading || (!userInput.trim() && !bufferedAnswer)}
          >
            {loading ? "…" : "Send"}
          </button>
        </div>
      </main>

      {/* Complexity modal */}
      {showComplexity && <ComplexityModal onSubmit={handleComplexitySubmit} />}
    </div>
  );
}
