"""
GET /getModeContext?sessionId=xxx

Returns the mode-specific context rules and any live feedback
visible to the candidate based on practice vs real mode.

Practice Mode:
- Immediate evaluation scores shown
- Critique shown after each answer
- Structure improvement suggestions shown
- Complexity corrections shown
- Encouraging, coaching tone from AI
- Retry allowed (tracked but no penalty)
- Cheat sheet access allowed

Real Mode:
- No scores shown during interview
- No critique during interview
- High-pressure tone
- Internal critiques stored silently
- No retry
- Final report only at end
- Hiring bar classification revealed only at end

Response:
{
  "sessionId":      string,
  "mode":           "practice" | "real",
  "rules": {
    "showScores":          bool,
    "showCritique":        bool,
    "showComplexityHints": bool,
    "allowRetry":          bool,
    "allowCheatSheet":     bool,
    "toneProfile":         "coaching" | "high_pressure",
    "revealOnEnd":         [string]   (what gets revealed at session end)
  },
  "liveFeedback":   [FeedbackItem] | null,   (practice only — recent critiques)
  "sessionProgress": {
    "currentSection":   string,
    "questionsAnswered": number,
    "timeElapsed":       number,
    "sectionScore":      number | null   (practice only)
  }
}
"""

import json
import os
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES
from timer import compute_timer_state
import time

MODE_RULES = {
    "practice": {
        "showScores":          True,
        "showCritique":        True,
        "showComplexityHints": True,
        "allowRetry":          True,
        "allowCheatSheet":     True,
        "toneProfile":         "coaching",
        "revealOnEnd": [
            "full_transcript",
            "all_scores",
            "behavioral_rewrites",
            "coding_improvements",
            "architecture_improvements",
            "hiring_recommendation",
            "level_projection",
        ],
    },
    "real": {
        "showScores":          False,
        "showCritique":        False,
        "showComplexityHints": False,
        "allowRetry":          False,
        "allowCheatSheet":     False,
        "toneProfile":         "high_pressure",
        "revealOnEnd": [
            "final_score",
            "level_projection",
            "hire_recommendation",
            "bar_comparison_summary",
            "strengths_summary",
            "weaknesses_summary",
            "missed_depth_opportunities",
            "full_transcript",
        ],
    },
}

# AI tone system prompts injected into question generation per mode
TONE_PROMPTS = {
    "coaching": (
        "You are a supportive but rigorous FAANG interviewer in a practice session. "
        "After each answer, give direct, actionable feedback. "
        "Point out exactly what was missing and how to improve it. "
        "Be encouraging but honest — do not sugarcoat weaknesses."
    ),
    "high_pressure": (
        "You are a demanding FAANG bar-raiser in a real interview. "
        "Do not give feedback or hints during the interview. "
        "Maintain professional but high-pressure tone. "
        "Challenge weak answers immediately with sharp follow-ups. "
        "Do not reveal how the candidate is doing at any point."
    ),
}


def lambda_handler(event, context):
    try:
        params     = event.get("queryStringParameters") or {}
        session_id = params.get("sessionId")

        if not session_id:
            return _error(400, "sessionId is required")

        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        mode           = session.get("mode", "practice")
        current_section = session.get("currentSection", "")
        interview_type  = session.get("interviewType", "technical")
        scores          = dict(session.get("scores", {}))

        rules = MODE_RULES.get(mode, MODE_RULES["practice"])

        # Compute timer state
        now = time.time()
        session_for_timer = {
            **session,
            "startedAt":        float(session.get("startedAt", now)),
            "sectionStartedAt": float(session.get("sectionStartedAt", now)),
        }
        timer_state = compute_timer_state(session_for_timer, now)

        # Count questions answered this section
        questions_answered = _count_answers(session_id, current_section)

        # Section score (practice only — weighted avg of evaluations)
        section_score = None
        if mode == "practice":
            section_score = _get_section_score(session_id, current_section)

        # Live feedback (practice only — last 3 critiques)
        live_feedback = None
        if mode == "practice":
            live_feedback = _get_live_feedback(session_id, current_section)

        return _ok({
            "sessionId": session_id,
            "mode":      mode,
            "rules":     rules,
            "tonePrompt": TONE_PROMPTS.get(rules["toneProfile"], ""),
            "liveFeedback": live_feedback,
            "sessionProgress": {
                "currentSection":    current_section,
                "interviewType":     interview_type,
                "questionsAnswered": questions_answered,
                "timerState":        timer_state,
                "sectionScore":      section_score,
            },
        })

    except Exception as e:
        return _error(500, str(e))


def _count_answers(session_id: str, section: str) -> int:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            FilterExpression="#r = :role AND #s = :sec",
            ExpressionAttributeNames={"#r": "role", "#s": "section"},
            ExpressionAttributeValues={
                ":sid":  session_id,
                ":role": "candidate",
                ":sec":  section,
            },
        )
        return len(resp.get("Items", []))
    except Exception:
        return 0


def _get_section_score(session_id: str, section: str) -> int | None:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            FilterExpression="#r = :role AND #s = :sec",
            ExpressionAttributeNames={"#r": "role", "#s": "section"},
            ExpressionAttributeValues={
                ":sid":  session_id,
                ":role": "evaluation",
                ":sec":  section,
            },
        )
        evals = resp.get("Items", [])
        scores = [int(float(e.get("score", 0))) for e in evals if e.get("score")]
        if not scores:
            return None
        return int(sum(scores) / len(scores))
    except Exception:
        return None


def _get_live_feedback(session_id: str, section: str) -> list:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            FilterExpression="#r = :role AND #s = :sec",
            ExpressionAttributeNames={"#r": "role", "#s": "section"},
            ExpressionAttributeValues={
                ":sid":  session_id,
                ":role": "evaluation",
                ":sec":  section,
            },
            ScanIndexForward=False,
            Limit=3,
        )
        items = resp.get("Items", [])
        return [
            {
                "score":      int(float(item.get("score", 0))),
                "critique":   item.get("critique", ""),
                "weaknesses": item.get("weaknesses", []),
                "strengths":  item.get("strengths", []),
                "timestamp":  item.get("timestamp", ""),
            }
            for item in items
        ]
    except Exception:
        return []


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
