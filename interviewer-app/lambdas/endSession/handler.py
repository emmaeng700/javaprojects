"""
POST /endSession

Body:
{
  "sessionId": string
}

Finalizes the session:
1. Pulls all scores accumulated during the session
2. Aggregates with weighted formula per interview type
3. Calls Gemini for bar analysis + written summaries
4. Computes level projection (L3-L6) and hire recommendation
5. Stores full evaluation in IV_HiringEvaluations
6. Marks session as completed

Response:
{
  "sessionId":                  string,
  "finalScore":                 number  (0-100),
  "behavioralScore":            number,
  "codingScore":                number,
  "systemDesignScore":          number,
  "complexityAwarenessScore":   number,
  "communicationScore":         number,
  "resumeAuthenticityScore":    number,
  "timeManagementScore":        number,
  "architectureMaturityScore":  number,
  "levelProjection":            "L3" | "L4" | "L5" | "L6",
  "hireRecommendation":         "Strong Hire" | "Hire" | "Lean Hire" | "Lean No Hire" | "No Hire",
  "barComparisonSummary":       string,
  "strengthsSummary":           [string],
  "weaknessesSummary":          [string],
  "missedDepthOpportunities":   [string],
  "evaluationId":               string
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

# Weighted scoring per interview type
# Weights must sum to 1.0
SCORE_WEIGHTS = {
    "behavioral": {
        "behavioral":           0.40,
        "communication":        0.30,
        "resume_authenticity":  0.15,
        "time_management":      0.15,
        "coding":               0.00,
        "complexity_awareness": 0.00,
        "system_design":        0.00,
        "architecture_maturity": 0.00,
    },
    "technical": {
        "behavioral":           0.10,
        "communication":        0.10,
        "resume_authenticity":  0.10,
        "time_management":      0.10,
        "coding":               0.35,
        "complexity_awareness": 0.15,
        "system_design":        0.00,
        "architecture_maturity": 0.00,
    },
    "system_design": {
        "behavioral":           0.05,
        "communication":        0.15,
        "resume_authenticity":  0.10,
        "time_management":      0.10,
        "coding":               0.00,
        "complexity_awareness": 0.10,
        "system_design":        0.30,
        "architecture_maturity": 0.20,
    },
}

# Level projection thresholds (final weighted score 0-100)
LEVEL_THRESHOLDS = [
    (88, "L6"),
    (74, "L5"),
    (58, "L4"),
    (0,  "L3"),
]

# Hire recommendation thresholds
HIRE_THRESHOLDS = [
    (85, "Strong Hire"),
    (72, "Hire"),
    (58, "Lean Hire"),
    (42, "Lean No Hire"),
    (0,  "No Hire"),
]


def lambda_handler(event, context):
    try:
        body       = json.loads(event.get("body") or "{}")
        session_id = body.get("sessionId")

        if not session_id:
            return _error(400, "sessionId is required")

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        if session.get("status") == "completed":
            # Return existing evaluation if already done
            eval_resp = TABLES["evaluations"].get_item(
                Key={"sessionId": session_id}
            )
            existing = eval_resp.get("Item")
            if existing:
                return _ok(existing)

        interview_type = session.get("interviewType", "technical")
        mode           = session.get("mode", "practice")
        raw_scores     = dict(session.get("scores", {}))

        # Clamp all raw scores to 0-100
        clamped = {k: max(0, min(100, int(raw_scores.get(k, 0)))) for k in SCORE_WEIGHTS["technical"]}

        # Compute weighted final score
        weights    = SCORE_WEIGHTS.get(interview_type, SCORE_WEIGHTS["technical"])
        final_score = int(sum(clamped[k] * weights[k] for k in weights))

        # Level projection
        level = _threshold_label(final_score, LEVEL_THRESHOLDS)

        # Hire recommendation
        hire_rec = _threshold_label(final_score, HIRE_THRESHOLDS)

        # Fetch full message history for Gemini analysis
        messages      = _get_all_messages(session_id)
        code_subs     = _get_code_submissions(session_id)
        section_history = list(session.get("sectionHistory", []))
        behavioral_falloff = session.get("behavioralFalloff", False)

        # Call Gemini for written analysis
        analysis = _generate_bar_analysis(
            interview_type, mode, clamped, final_score, level, hire_rec,
            messages, code_subs, section_history, behavioral_falloff
        )

        evaluation_id = str(uuid.uuid4())
        now           = str(time.time())

        evaluation = {
            "sessionId":                 session_id,
            "evaluationId":              evaluation_id,
            "interviewType":             interview_type,
            "mode":                      mode,
            "finalScore":                final_score,
            "behavioralScore":           clamped["behavioral"],
            "codingScore":               clamped["coding"],
            "systemDesignScore":         clamped["system_design"],
            "complexityAwarenessScore":  clamped["complexity_awareness"],
            "communicationScore":        clamped["communication"],
            "resumeAuthenticityScore":   clamped["resume_authenticity"],
            "timeManagementScore":       clamped["time_management"],
            "architectureMaturityScore": clamped["architecture_maturity"],
            "levelProjection":           level,
            "hireRecommendation":        hire_rec,
            "barComparisonSummary":      analysis.get("barComparisonSummary", ""),
            "strengthsSummary":          analysis.get("strengthsSummary", []),
            "weaknessesSummary":         analysis.get("weaknessesSummary", []),
            "missedDepthOpportunities":  analysis.get("missedDepthOpportunities", []),
            "codingImprovements":        analysis.get("codingImprovements", []),
            "architectureImprovements":  analysis.get("architectureImprovements", []),
            "behavioralRewrites":        analysis.get("behavioralRewrites", []),
            "behavioralFalloff":         behavioral_falloff,
            "createdAt":                 now,
        }

        # Store in IV_HiringEvaluations
        TABLES["evaluations"].put_item(Item=evaluation)

        # Mark session as completed
        TABLES["sessions"].update_item(
            Key={"sessionId": session_id},
            UpdateExpression="SET #st = :s, completedAt = :t, evaluationId = :e",
            ExpressionAttributeNames={"#st": "status"},
            ExpressionAttributeValues={
                ":s": "completed",
                ":t": now,
                ":e": evaluation_id,
            },
        )

        return _ok(evaluation)

    except Exception as e:
        return _error(500, str(e))


def _generate_bar_analysis(
    interview_type, mode, scores, final_score, level, hire_rec,
    messages, code_subs, section_history, behavioral_falloff
) -> dict:

    # Summarize messages for context
    msg_summary = _summarize_messages(messages)
    code_summary = _summarize_code(code_subs)
    falloff_note = "BEHAVIORAL FALLOFF DETECTED — candidate repeatedly avoided specifics." if behavioral_falloff else ""

    prompt = f"""You are a FAANG hiring committee member writing the final evaluation for a candidate.

Interview type: {interview_type}
Mode: {mode}
{falloff_note}

Scores (0-100):
- Behavioral:           {scores['behavioral']}
- Coding:               {scores['coding']}
- System Design:        {scores['system_design']}
- Complexity Awareness: {scores['complexity_awareness']}
- Communication:        {scores['communication']}
- Resume Authenticity:  {scores['resume_authenticity']}
- Time Management:      {scores['time_management']}
- Architecture Maturity:{scores['architecture_maturity']}

Final weighted score: {final_score}/100
Level projection: {level}
Hire recommendation: {hire_rec}

Interview summary:
{msg_summary}

Code submissions:
{code_summary}

Write a rigorous hiring committee evaluation. Be brutally honest.
Compare against FAANG hiring bar expectations for {level}.

Return ONLY this JSON:
{{
  "barComparisonSummary": "<3-4 sentence bar comparison — how does this candidate compare to the {level} bar? Be specific about gaps and strengths>",
  "strengthsSummary": ["<specific strength 1>", "<specific strength 2>", "<specific strength 3>"],
  "weaknessesSummary": ["<specific weakness 1>", "<specific weakness 2>", "<specific weakness 3>"],
  "missedDepthOpportunities": ["<moment where candidate could have shown more depth>"],
  "codingImprovements": ["<specific coding improvement>"],
  "architectureImprovements": ["<specific architecture/design improvement>"],
  "behavioralRewrites": ["<example of a better answer to a behavioral question they gave>"]
}}

Be specific, not generic. Reference what actually happened in the interview.
Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.3, max_tokens=1500)

    try:
        return json.loads(result)
    except Exception:
        return {
            "barComparisonSummary": f"Final score {final_score}/100 — {hire_rec} at {level} bar.",
            "strengthsSummary": [],
            "weaknessesSummary": [],
            "missedDepthOpportunities": [],
            "codingImprovements": [],
            "architectureImprovements": [],
            "behavioralRewrites": [],
        }


def _summarize_messages(messages: list) -> str:
    lines = []
    for m in messages[:40]:
        role    = m.get("role", "unknown")
        content = m.get("content", "")[:200]
        section = m.get("section", "")
        lines.append(f"[{section}] {role}: {content}")
    return "\n".join(lines) if lines else "No messages recorded."


def _summarize_code(code_subs: list) -> str:
    if not code_subs:
        return "No code submissions."
    lines = []
    for sub in code_subs[:5]:
        passed   = sub.get("passed", False)
        lang     = sub.get("language", "")
        optimal  = sub.get("isOptimal", False)
        tc       = sub.get("timeComplexityAnswer", "not stated")
        atc      = sub.get("actualTimeComplexity", "unknown")
        correct  = sub.get("timeComplexityCorrect", False)
        lines.append(
            f"- {'✓' if passed else '✗'} {lang} | optimal={optimal} | "
            f"stated={tc} actual={atc} correct={correct}"
        )
    return "\n".join(lines)


def _get_all_messages(session_id: str) -> list:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            ExpressionAttributeValues={":sid": session_id},
            ScanIndexForward=True,
        )
        return resp.get("Items", [])
    except Exception:
        return []


def _get_code_submissions(session_id: str) -> list:
    try:
        resp = TABLES["code"].query(
            KeyConditionExpression="sessionId = :sid",
            ExpressionAttributeValues={":sid": session_id},
        )
        return resp.get("Items", [])
    except Exception:
        return []


def _threshold_label(score: int, thresholds: list) -> str:
    for threshold, label in thresholds:
        if score >= threshold:
            return label
    return thresholds[-1][1]


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
