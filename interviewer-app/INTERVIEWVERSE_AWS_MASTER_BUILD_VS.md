# 🚀 INTERVIEWVERSE AI — MASTER BUILD SPEC V5
FAANG-Level Onsite Simulator with Hiring Bar Calibration
Behavioral • Technical • System Design
Resume Drill • Real-Time Code Interrogation • Strength-of-Hire Logic
AWS Backend Architecture

---

# 🎯 OBJECTIVE

Build a production-grade AI interview simulator that:

• Simulates FAANG-level intensity  
• Enforces strict timing  
• Performs resume drilling  
• Interrupts coding with reasoning challenges  
• Judges behavioral fall-offs aggressively  
• Evaluates system design depth critically  
• Assigns final hiring recommendation  
• Classifies candidate strength level  
• Stores all evaluations in AWS  

This must feel judgmental, realistic, and high-bar.

---

# 🏆 HIRING BAR CALIBRATION SYSTEM

All sessions must produce:

• Skill scores  
• Behavioral maturity score  
• Technical depth score  
• Communication score  
• Time management score  
• Complexity awareness score  
• Architecture maturity score  
• Resume authenticity score  

Final classification:

{
  overall_score: number,
  level_projection: "L3 | L4 | L5 | L6",
  hire_recommendation: "Strong Hire | Hire | Lean Hire | Lean No Hire | No Hire",
  bar_comparison_summary: string
}

Evaluation must compare candidate performance against calibrated hiring bars.

---

# 🔥 BEHAVIORAL INTENSITY LOGIC

AI must:

• Detect vague statements  
• Detect missing metrics  
• Detect lack of ownership clarity  
• Detect inflated claims  
• Challenge soft explanations  
• Interrupt weak answers  

Example interruptions:

- "You said you improved performance. By how much exactly?"
- "That sounds collaborative. What was YOUR contribution?"
- "Why was that decision correct?"
- "What trade-off did you accept?"

If answer weak:

Follow-up must intensify.

Behavioral fall-off detection:

If candidate:
• Rambles
• Avoids metrics
• Avoids accountability
• Cannot explain impact

Score penalized.

Practice Mode:
Show critique immediately.

Real Mode:
Store critique silently.

---

# 💻 REAL-TIME CODING INTERROGATION

During coding:

AI must monitor:

• Approach chosen
• Patterns used
• Algorithm category

At natural pauses:

Interrupt with:

- "Why did you choose this data structure?"
- "What is your expected time complexity?"
- "What happens with large inputs?"
- "Is there a more optimal solution?"

Before final evaluation:

SYSTEM MUST SHOW POP-UP:

"State your expected time complexity."
"State your expected space complexity."

User must answer BEFORE evaluation shown.

If:

Code correct + Complexity correct:

🎉 Trigger confetti animation.

If code correct but complexity wrong:

No confetti.
Deduct complexity awareness score.

If code inefficient but correct:

AI must push optimization follow-up.

---

# 🧠 CODING ESCALATION LOGIC

Default:
Max 2 coding questions.

If candidate:

• Solves first quickly (< 15 min)
• Correct + optimal
• Explains complexity correctly

Then:

Generate harder second question.

If second solved quickly + cleanly:

Optional third high-difficulty variant.

Otherwise:
Stop at 2.

---

# 🏗️ SYSTEM DESIGN JUDGMENT LOGIC

AI must:

• Detect missing scaling discussion
• Detect missing bottleneck analysis
• Detect shallow trade-off explanation
• Detect no failure-mode discussion
• Detect overconfidence without numbers

Interrupt with:

- "How does this behave at 10x traffic?"
- "What breaks first?"
- "How do you handle consistency?"
- "Why not use a different storage model?"

If user finishes early:

Add:
• Traffic spike simulation
• Failure injection scenario
• Multi-region question
• Cost analysis question

---

# 🧠 RESUME DRILL (FIRST 15 MIN TECH + DESIGN)

Mandatory first question:

"Tell me about yourself."

AI must:

• Parse resume
• Identify inflated claims
• Identify strongest bullet
• Probe deeply into architecture claims
• Probe metrics
• Probe decision-making

At 13–14 min:
Start wrap-up.

At 15 min:
Force transition.

---

# 🕒 TIME RULES (SERVER-ENFORCED)

Behavioral:
25–35 minutes max.

Technical:
60–75 minutes total.
15 min resume drill.
45–60 min coding.

System Design:
60–75 minutes total.
15 min resume drill.
45–60 min design.

All transitions enforced server-side.

Frontend only displays timer.

---

# 🧠 PRACTICE vs REAL MODE

Practice Mode:

• Immediate evaluation
• Immediate structure improvement suggestions
• Shows complexity corrections
• Allows retry
• Encouraging tone

Real Mode:

• No immediate scoring
• High-pressure tone
• Store internal critique
• Final full evaluation only at end
• Hiring bar classification revealed at end

---

# 📊 FINAL REPORT (REAL MODE)

Must include:

{
  final_score: number,
  behavioral_score: number,
  coding_score: number,
  system_design_score: number,
  complexity_awareness_score: number,
  communication_score: number,
  resume_authenticity_score: number,
  level_projection: "L3 | L4 | L5 | L6",
  hire_recommendation: "Strong Hire | Hire | Lean Hire | Lean No Hire | No Hire",
  bar_analysis: string,
  strengths_summary: [],
  weaknesses_summary: [],
  missed_depth_opportunities: [],
  coding_improvements: [],
  architecture_improvements: [],
  behavioral_rewrites: [],
  full_transcript: []
}

PDF export required.

---

# 🎙️ SPEECH SYSTEM

• Live transcription
• Silence detection
• Interruptions allowed during coding
• Server-side context tracking
• Multi-turn conversational memory

---

# 🤖 AVATAR REACTIONS

States:

• Neutral interviewer
• Slight nod on strong answer
• Concerned look on weak reasoning
• Analytical tilt during coding
• Confetti animation on strong optimal solution

Confetti only if:
Code correct + optimal + complexity correct.

---

# 📂 AWS BACKEND STRUCTURE

Lambda Endpoints:

/startSession
/parseResume
/generateQuestion
/evaluateAnswer
/handleFollowUp
/executeCode
/validateComplexityAnswer
/parseCheatSheet
/transitionSection
/endSession

DynamoDB Tables:

Users
InterviewSessions
SessionMessages
CodeSubmissions
ParsedKnowledgeFiles
HiringEvaluations

S3:

interviewverse-uploads

All timers enforced server-side.

---

# 🧩 BUILD PHASES

Phase 1 – AWS infra  
Phase 2 – Session + timer enforcement  
Phase 3 – Resume drill engine  
Phase 4 – Behavioral interrogation engine  
Phase 5 – Monaco + code sandbox  
Phase 6 – Coding interrogation logic  
Phase 7 – Complexity pop-up logic  
Phase 8 – Hiring bar scoring engine  
Phase 9 – System design stress engine  
Phase 10 – Practice vs Real logic  
Phase 11 – Speech integration  
Phase 12 – Avatar + confetti  
Phase 13 – Final hiring report  

Pause after each phase.

---

# ⚠️ IMPLEMENTATION RULES

• Strict JSON schema validation
• Server-side time authority
• Secure sandbox
• Modular architecture
• High-quality logging
• No frontend secrets
• Production-grade error handling

---

# ROLE

You are a senior FAANG-level interviewer simulator architect.

Build this as a realistic high-bar onsite simulator.

Be judgmental, analytical, and rigorous.

Pause after each phase and wait for confirmation.