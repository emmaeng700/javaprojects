"""
POST /interrogateCode

Called when the frontend detects a natural pause in typing (e.g. 8+ seconds idle).
AI analyzes the current code snapshot and decides whether to interrupt
with a reasoning challenge.

Body:
{
  "sessionId":      string,
  "questionId":     string,
  "currentCode":    string,    (full current editor contents)
  "language":       string,
  "pauseSeconds":   number,    (how long the candidate has been idle)
  "interrogationCount": number (how many interrogations already this question)
}

AI detects:
- What approach/pattern is being used
- What data structure was chosen
- Algorithm category (sliding window, DP, BFS, two pointer, etc.)
- Whether a follow-up is warranted right now

Interrogation triggers:
- "Why did you choose this data structure?"
- "What is your expected time complexity so far?"
- "What happens with large inputs?"
- "Is there a more optimal approach?"
- "What edge cases are you handling?"

Response:
{
  "sessionId":      string,
  "shouldInterrupt": bool,
  "interruptReason": string | null,
  "question":        string | null,
  "questionType":    "approach" | "complexity" | "edge_case" | "optimization" | null,
  "detectedPattern": string   (e.g. "hash map", "sliding window", "brute force")
}
"""

import json
import os
import sys
import time
import uuid

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES
from gemini import call_gemini

MAX_INTERROGATIONS_PER_QUESTION = 3
MIN_PAUSE_TO_INTERRUPT = 8   # seconds


def lambda_handler(event, context):
    try:
        body         = json.loads(event.get("body") or "{}")
        session_id   = body.get("sessionId")
        question_id  = body.get("questionId", "")
        current_code = body.get("currentCode", "").strip()
        language     = body.get("language", "python")
        pause_sec    = int(body.get("pauseSeconds", 0))
        interrogation_count = int(body.get("interrogationCount", 0))

        if not session_id:
            return _error(400, "sessionId is required")
        if not current_code:
            return _ok({"sessionId": session_id, "shouldInterrupt": False,
                        "interruptReason": None, "question": None,
                        "questionType": None, "detectedPattern": "none"})

        # Validate session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        # Don't interrupt too many times per question
        if interrogation_count >= MAX_INTERROGATIONS_PER_QUESTION:
            return _ok({"sessionId": session_id, "shouldInterrupt": False,
                        "interruptReason": "Max interrogations reached",
                        "question": None, "questionType": None,
                        "detectedPattern": "unknown"})

        # Don't interrupt if pause is too short
        if pause_sec < MIN_PAUSE_TO_INTERRUPT:
            return _ok({"sessionId": session_id, "shouldInterrupt": False,
                        "interruptReason": "Pause too short",
                        "question": None, "questionType": None,
                        "detectedPattern": "unknown"})

        # Don't interrupt if code is too short to analyze meaningfully
        if len(current_code) < 80:
            return _ok({"sessionId": session_id, "shouldInterrupt": False,
                        "interruptReason": "Code too short to analyze",
                        "question": None, "questionType": None,
                        "detectedPattern": "none"})

        analysis = _analyze_code_with_gemini(
            current_code, language, interrogation_count, pause_sec
        )

        should_interrupt = analysis.get("shouldInterrupt", False)
        question         = analysis.get("question")
        question_type    = analysis.get("questionType")
        interrupt_reason = analysis.get("interruptReason")
        detected_pattern = analysis.get("detectedPattern", "unknown")

        # Store interrogation in messages if interrupting
        if should_interrupt and question:
            TABLES["messages"].put_item(Item={
                "sessionId":      session_id,
                "messageId":      str(uuid.uuid4()),
                "role":           "interviewer",
                "section":        "coding",
                "content":        question,
                "questionType":   "interrogation",
                "messageType":    "code_interrogation",
                "linkedQuestionId": question_id,
                "detectedPattern": detected_pattern,
                "timestamp":      str(time.time()),
            })

        return _ok({
            "sessionId":       session_id,
            "shouldInterrupt": should_interrupt,
            "interruptReason": interrupt_reason,
            "question":        question,
            "questionType":    question_type,
            "detectedPattern": detected_pattern,
        })

    except Exception as e:
        return _error(500, str(e))


def _analyze_code_with_gemini(
    code: str, language: str, interrogation_count: int, pause_sec: int
) -> dict:

    # Vary interrogation focus based on count to avoid repetition
    focus_instructions = [
        "Focus on: WHY they chose this approach. Ask about data structure choice or algorithm category.",
        "Focus on: complexity and scalability. Ask about time/space complexity or large input behavior.",
        "Focus on: edge cases and optimization. Ask what edge cases they've considered or if a better approach exists.",
    ]
    focus = focus_instructions[min(interrogation_count, 2)]

    prompt = f"""You are a FAANG interviewer watching a candidate code in real time.
Language: {language}
Candidate has been idle for {pause_sec} seconds.
This is interrogation #{interrogation_count + 1} for this question.

Current code:
```
{code[:2500]}
```

Analyze what the candidate is doing:
1. What pattern or approach are they using? (hash map, two pointer, sliding window, DFS/BFS, DP, brute force, etc.)
2. Is the approach correct so far?
3. Is there an obvious suboptimal choice being made?

{focus}

Decide: should you interrupt right now?
- Interrupt if: you can see a meaningful pattern worth questioning
- Do NOT interrupt if: code is too early stage, or the approach looks correct and no useful question exists

If you interrupt, the question must be SHARP and SPECIFIC to what you see in this code.
Do NOT ask generic questions. Reference what you see.

Example good questions:
- "I see you're using a nested loop here — what's the time complexity of this approach?"
- "You chose a dictionary — why not a set for this lookup?"
- "What happens to your sliding window if there are duplicate characters?"

Return ONLY this JSON:
{{
  "shouldInterrupt": <true/false>,
  "interruptReason": "<why interrupt or why not>",
  "question": "<exact sharp question to ask, or null>",
  "questionType": "approach" | "complexity" | "edge_case" | "optimization" | null,
  "detectedPattern": "<e.g. 'brute force O(n^2)', 'hash map O(n)', 'sliding window', 'DFS recursion'>"
}}

Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.4, max_tokens=512)

    try:
        return json.loads(result)
    except Exception:
        return {
            "shouldInterrupt": False,
            "interruptReason": "Analysis failed",
            "question": None,
            "questionType": None,
            "detectedPattern": "unknown",
        }


def _ok(data):
    return {
        "statusCode": 200,
        "headers": {"Content-Type": "application/json", "Access-Control-Allow-Origin": "*"},
        "body": json.dumps(data),
    }


def _error(code, msg):
    return {
        "statusCode": code,
        "headers": {"Content-Type": "application/json", "Access-Control-Allow-Origin": "*"},
        "body": json.dumps({"error": msg}),
    }
