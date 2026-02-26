"""
POST /evaluateAnswer

Body:
{
  "sessionId":  string,
  "questionId": string,
  "answer":     string,
  "section":    string   ("behavioral" | "resume_drill" | "intro")
}

Evaluates a candidate's answer using Gemini.
Detects: vague language, missing metrics, weak ownership, inflated claims,
         rambling, avoidance, no impact stated.

Scores the answer, decides if a follow-up challenge is needed.
Stores evaluation in IV_SessionMessages.
Updates relevant scores in IV_InterviewSessions.

Response:
{
  "sessionId":       string,
  "questionId":      string,
  "score":           number (0-10),
  "needsFollowUp":   bool,
  "followUpReason":  string | null,
  "weaknesses":      [string],
  "strengths":       [string],
  "critique":        string   (practice mode only),
  "scoreDelta": {
    "behavioral":          number,
    "communication":       number,
    "resume_authenticity": number,
    "time_management":     number
  }
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

SCORE_MAP = {
    "behavioral":          "behavioral",
    "resume_drill":        "resume_authenticity",
    "intro":               "communication",
}


def lambda_handler(event, context):
    try:
        body       = json.loads(event.get("body") or "{}")
        session_id = body.get("sessionId")
        question_id = body.get("questionId")
        answer     = body.get("answer", "").strip()
        section    = body.get("section", "behavioral")

        if not session_id:
            return _error(400, "sessionId is required")
        if not answer:
            return _error(400, "answer is required")

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        mode           = session.get("mode", "practice")
        interview_type = session.get("interviewType", "technical")

        # Fetch original question if questionId provided
        original_question = ""
        if question_id:
            try:
                q_resp = TABLES["messages"].get_item(
                    Key={"sessionId": session_id, "messageId": question_id}
                )
                q_item = q_resp.get("Item", {})
                original_question = q_item.get("content", "")
            except Exception:
                pass

        # Fetch resume profile for context
        profile = _get_profile(session_id)

        # Evaluate with Gemini
        evaluation = _evaluate_with_gemini(
            interview_type, section, mode, original_question, answer, profile
        )

        score          = evaluation.get("score", 5)
        needs_follow   = evaluation.get("needsFollowUp", False)
        follow_reason  = evaluation.get("followUpReason")
        weaknesses     = evaluation.get("weaknesses", [])
        strengths      = evaluation.get("strengths", [])
        critique       = evaluation.get("critique", "")
        score_delta    = evaluation.get("scoreDelta", {})

        eval_id = str(uuid.uuid4())
        now     = str(time.time())

        # Store candidate answer
        TABLES["messages"].put_item(Item={
            "sessionId":   session_id,
            "messageId":   str(uuid.uuid4()),
            "role":        "candidate",
            "section":     section,
            "content":     answer,
            "linkedQuestionId": question_id or "",
            "timestamp":   now,
        })

        # Store evaluation record
        TABLES["messages"].put_item(Item={
            "sessionId":      session_id,
            "messageId":      eval_id,
            "role":           "evaluation",
            "section":        section,
            "score":          str(score),
            "needsFollowUp":  str(needs_follow),
            "followUpReason": follow_reason or "",
            "weaknesses":     weaknesses,
            "strengths":      strengths,
            "critique":       critique,
            "scoreDelta":     score_delta,
            "timestamp":      now,
        })

        # Update session scores
        _update_scores(session_id, section, score_delta, session)

        response_data = {
            "sessionId":      session_id,
            "questionId":     question_id,
            "evaluationId":   eval_id,
            "score":          score,
            "needsFollowUp":  needs_follow,
            "followUpReason": follow_reason,
            "weaknesses":     weaknesses,
            "strengths":      strengths,
            "scoreDelta":     score_delta,
        }

        # Only expose full critique in practice mode
        if mode == "practice":
            response_data["critique"] = critique

        return _ok(response_data)

    except Exception as e:
        return _error(500, str(e))


def _evaluate_with_gemini(
    interview_type, section, mode, question, answer, profile
) -> dict:

    profile_context = ""
    if profile:
        inflated = profile.get("inflated_claims", [])
        probes   = profile.get("deep_probe_targets", [])
        profile_context = f"""
Known inflated claims from resume: {json.dumps(inflated[:3])}
Deep probe targets: {json.dumps([p.get('area') for p in probes[:3]])}
"""

    prompt = f"""You are a senior FAANG bar-raiser evaluating a candidate's answer.

Interview type: {interview_type}
Section: {section}
Mode: {mode}

{profile_context}

Question asked:
{question or "(opener/general question)"}

Candidate answer:
{answer}

Evaluate this answer ruthlessly. Detect:
- Vague language (no specifics)
- Missing metrics or numbers
- Weak ownership ("we did" instead of "I did")
- Inflated or unverifiable claims
- Rambling or lack of structure
- No clear impact or outcome stated
- Avoidance of hard details

Scoring guide:
0-3: Very weak. Vague, no metrics, no ownership, no impact.
4-5: Below bar. Some substance but missing key elements.
6-7: At bar. Decent answer with minor gaps.
8-9: Above bar. Specific, metrics-driven, strong ownership.
10:  Exceptional. Rare. Complete STAR with metrics and sharp insight.

Return ONLY a JSON object:
{{
  "score": <0-10>,
  "needsFollowUp": <true if score < 7 or critical gap detected>,
  "followUpReason": "<specific gap to probe, or null>",
  "weaknesses": ["<specific weakness 1>", "<specific weakness 2>"],
  "strengths": ["<specific strength 1>"],
  "critique": "<2-3 sentence direct critique — what was missing and how to fix it>",
  "scoreDelta": {{
    "behavioral": <-2 to +2 integer>,
    "communication": <-2 to +2 integer>,
    "resume_authenticity": <-2 to +2 integer>,
    "time_management": <-1 to +1 integer>
  }}
}}

Be aggressive. A mediocre answer should score 4-5, not 7.
Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.2, max_tokens=1024)

    try:
        return json.loads(result)
    except Exception:
        return {
            "score": 5,
            "needsFollowUp": True,
            "followUpReason": "Could not parse evaluation — defaulting to follow-up.",
            "weaknesses": ["Evaluation parsing failed"],
            "strengths": [],
            "critique": "Evaluation service error.",
            "scoreDelta": {"behavioral": 0, "communication": 0, "resume_authenticity": 0, "time_management": 0},
        }


def _get_profile(session_id: str) -> dict:
    try:
        resp = TABLES["knowledge"].get_item(Key={"sessionId": session_id, "type": "resume_profile"})
        item = resp.get("Item")
        return item.get("profile", {}) if item else {}
    except Exception:
        return {}


def _update_scores(session_id: str, section: str, score_delta: dict, session: dict):
    """Apply score deltas to session scores, clamped to 0-100."""
    if not score_delta:
        return

    current_scores = dict(session.get("scores", {}))

    update_parts = []
    expr_values  = {}

    score_keys = ["behavioral", "communication", "resume_authenticity", "time_management"]
    for key in score_keys:
        delta = int(score_delta.get(key, 0))
        if delta == 0:
            continue
        current = int(current_scores.get(key, 0))
        new_val  = max(0, min(100, current + (delta * 5)))   # delta * 5 scales -2..+2 to -10..+10
        update_parts.append(f"scores.{key} = :s_{key}")
        expr_values[f":s_{key}"] = new_val

    if not update_parts:
        return

    TABLES["sessions"].update_item(
        Key={"sessionId": session_id},
        UpdateExpression="SET " + ", ".join(update_parts),
        ExpressionAttributeValues=expr_values,
    )


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
