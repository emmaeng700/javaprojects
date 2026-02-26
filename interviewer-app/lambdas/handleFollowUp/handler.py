"""
POST /handleFollowUp

Body:
{
  "sessionId":     string,
  "evaluationId":  string,   (from evaluateAnswer response)
  "answer":        string    (candidate's follow-up response)
}

Generates an intensified follow-up challenge based on the evaluation gap,
then re-evaluates the follow-up answer.

Behavioral fall-off detection:
- If candidate still avoids metrics after 2 follow-ups: flag behavioral_falloff
- If candidate ramblings continues: penalize communication score

Response:
{
  "sessionId":         string,
  "followUpQuestion":  string,
  "followUpScore":     number,
  "behavioralFalloff": bool,
  "falloffReason":     string | null,
  "updatedWeaknesses": [string],
  "critique":          string  (practice mode only),
  "scoreDelta":        { ... }
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


def lambda_handler(event, context):
    try:
        body          = json.loads(event.get("body") or "{}")
        session_id    = body.get("sessionId")
        evaluation_id = body.get("evaluationId")
        answer        = body.get("answer", "").strip()

        if not session_id:
            return _error(400, "sessionId is required")
        if not evaluation_id:
            return _error(400, "evaluationId is required")
        if not answer:
            return _error(400, "answer is required")

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        mode = session.get("mode", "practice")

        # Fetch original evaluation record
        eval_resp = TABLES["messages"].get_item(
            Key={"sessionId": session_id, "messageId": evaluation_id}
        )
        evaluation = eval_resp.get("Item")
        if not evaluation:
            return _error(404, "Evaluation not found")

        follow_reason = evaluation.get("followUpReason", "")
        weaknesses    = evaluation.get("weaknesses", [])
        prior_score   = int(float(evaluation.get("score", 5)))
        section       = evaluation.get("section", "behavioral")

        # Count prior follow-ups this session to detect fall-off
        follow_up_count = _count_follow_ups(session_id)
        is_falloff_risk = follow_up_count >= 2

        # Generate intensified follow-up question + re-evaluate answer
        result = _handle_follow_up_with_gemini(
            session=session,
            section=section,
            follow_reason=follow_reason,
            weaknesses=weaknesses,
            prior_score=prior_score,
            candidate_answer=answer,
            is_falloff_risk=is_falloff_risk,
        )

        follow_up_question  = result.get("followUpQuestion", "")
        follow_up_score     = result.get("followUpScore", prior_score)
        behavioral_falloff  = result.get("behavioralFalloff", False)
        falloff_reason      = result.get("falloffReason")
        updated_weaknesses  = result.get("updatedWeaknesses", weaknesses)
        critique            = result.get("critique", "")
        score_delta         = result.get("scoreDelta", {})

        now = str(time.time())

        # Store candidate follow-up answer
        TABLES["messages"].put_item(Item={
            "sessionId":        session_id,
            "messageId":        str(uuid.uuid4()),
            "role":             "candidate",
            "section":          section,
            "content":          answer,
            "linkedEvalId":     evaluation_id,
            "messageType":      "follow_up_answer",
            "timestamp":        now,
        })

        # Store follow-up question
        fu_id = str(uuid.uuid4())
        TABLES["messages"].put_item(Item={
            "sessionId":        session_id,
            "messageId":        fu_id,
            "role":             "interviewer",
            "section":          section,
            "content":          follow_up_question,
            "questionType":     "follow_up",
            "messageType":      "follow_up_question",
            "timestamp":        now,
        })

        # Store follow-up evaluation
        TABLES["messages"].put_item(Item={
            "sessionId":        session_id,
            "messageId":        str(uuid.uuid4()),
            "role":             "evaluation",
            "section":          section,
            "score":            str(follow_up_score),
            "behavioralFalloff": str(behavioral_falloff),
            "falloffReason":    falloff_reason or "",
            "updatedWeaknesses": updated_weaknesses,
            "critique":         critique,
            "scoreDelta":       score_delta,
            "messageType":      "follow_up_evaluation",
            "timestamp":        now,
        })

        # Apply score deltas
        _update_scores(session_id, score_delta, session)

        # Flag behavioral falloff in session if detected
        if behavioral_falloff:
            TABLES["sessions"].update_item(
                Key={"sessionId": session_id},
                UpdateExpression="SET behavioralFalloff = :f, falloffReason = :r",
                ExpressionAttributeValues={
                    ":f": True,
                    ":r": falloff_reason or "Repeated avoidance of specifics",
                },
            )

        response_data = {
            "sessionId":         session_id,
            "followUpQuestionId": fu_id,
            "followUpQuestion":  follow_up_question,
            "followUpScore":     follow_up_score,
            "behavioralFalloff": behavioral_falloff,
            "falloffReason":     falloff_reason,
            "updatedWeaknesses": updated_weaknesses,
            "scoreDelta":        score_delta,
        }

        if mode == "practice":
            response_data["critique"] = critique

        return _ok(response_data)

    except Exception as e:
        return _error(500, str(e))


def _handle_follow_up_with_gemini(
    session, section, follow_reason, weaknesses, prior_score,
    candidate_answer, is_falloff_risk
) -> dict:

    interview_type = session.get("interviewType", "technical")
    mode           = session.get("mode", "practice")

    falloff_instruction = ""
    if is_falloff_risk:
        falloff_instruction = (
            "IMPORTANT: This candidate has already been followed up on multiple times. "
            "Evaluate whether they are still avoiding specifics. "
            "If yes, set behavioralFalloff=true and explain the pattern."
        )

    prompt = f"""You are a FAANG bar-raiser conducting a {interview_type} interview.
Section: {section}

The candidate just gave a follow-up answer to a challenge.

Original evaluation gap: {follow_reason}
Previous weaknesses flagged: {json.dumps(weaknesses)}
Previous score: {prior_score}/10

Candidate's follow-up answer:
{candidate_answer}

{falloff_instruction}

Your job:
1. Re-evaluate if they addressed the gap.
2. Generate an intensified follow-up question if gaps remain.
   - Be MORE direct and challenging than before.
   - Do not let vague answers pass.
   - Example intensity: "You still haven't given me a number. What was the actual latency improvement?"
3. Detect behavioral fall-off patterns:
   - Still no metrics after being asked
   - Continued use of "we" instead of "I"
   - Rambling without reaching a point
   - Deflecting responsibility

Return ONLY this JSON:
{{
  "followUpQuestion": "<sharp, intensified follow-up question>",
  "followUpScore": <0-10>,
  "behavioralFalloff": <true if candidate is repeating avoidance patterns>,
  "falloffReason": "<specific pattern detected, or null>",
  "updatedWeaknesses": ["<remaining weakness 1>", "<remaining weakness 2>"],
  "critique": "<2-sentence direct critique of this follow-up answer>",
  "scoreDelta": {{
    "behavioral": <-3 to +2 integer>,
    "communication": <-2 to +2 integer>,
    "resume_authenticity": <-2 to +1 integer>,
    "time_management": <-1 to +1 integer>
  }}
}}

Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.3, max_tokens=1024)

    try:
        return json.loads(result)
    except Exception as e:
        return {
            "followUpQuestion": "Let me be direct — what specific number or outcome can you point to?",
            "followUpScore": max(0, prior_score - 1),
            "behavioralFalloff": False,
            "falloffReason": None,
            "updatedWeaknesses": weaknesses,
            "critique": "Evaluation parsing failed.",
            "scoreDelta": {"behavioral": -1, "communication": 0, "resume_authenticity": 0, "time_management": 0},
        }


def _count_follow_ups(session_id: str) -> int:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            FilterExpression="messageType = :t",
            ExpressionAttributeValues={
                ":sid": session_id,
                ":t":   "follow_up_evaluation",
            },
        )
        return len(resp.get("Items", []))
    except Exception:
        return 0


def _update_scores(session_id: str, score_delta: dict, session: dict):
    if not score_delta:
        return

    current_scores = dict(session.get("scores", {}))
    update_parts   = []
    expr_values    = {}

    for key in ["behavioral", "communication", "resume_authenticity", "time_management"]:
        delta = int(score_delta.get(key, 0))
        if delta == 0:
            continue
        current = int(current_scores.get(key, 0))
        new_val  = max(0, min(100, current + (delta * 5)))
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
