"""
POST /stressTestDesign

Called when evaluateDesign returns shouldStressTest=true.
Fires one stress scenario at a time based on stressTestsNeeded list.

Stress test types:
- traffic_spike:      "How does this handle 10x traffic overnight?"
- failure_injection:  "Your primary DB just went down. Walk me through what happens."
- multi_region:       "How do you serve users in Asia with <100ms latency?"
- cost_analysis:      "Estimate your monthly AWS bill at 1M daily active users."

Body:
{
  "sessionId":     string,
  "evaluationId":  string,   (from evaluateDesign)
  "stressType":    "traffic_spike" | "failure_injection" | "multi_region" | "cost_analysis",
  "answer":        string    (candidate's response to the stress scenario)
}

If answer not provided: returns the stress question to ask.
If answer provided: evaluates the response and returns next stress test or done.

Response:
{
  "sessionId":         string,
  "stressType":        string,
  "question":          string,      (the stress scenario question)
  "answerEvaluated":   bool,
  "score":             number | null,
  "passed":            bool | null,  (did they handle it adequately?)
  "feedback":          string | null,
  "nextStressType":    string | null, (next stress test to run, or null if done)
  "allStressComplete": bool,
  "scoreDelta": { ... }
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

STRESS_QUESTIONS = {
    "traffic_spike": (
        "Your system is running fine at current load. "
        "You wake up to find traffic has spiked 10x overnight — "
        "a viral event is sending massive unexpected load. "
        "Walk me through exactly what breaks first in your design and how you handle it."
    ),
    "failure_injection": (
        "Your primary database just went down completely. "
        "No warning, no graceful shutdown. "
        "Walk me through what happens to your system second by second, "
        "and what your recovery strategy is."
    ),
    "multi_region": (
        "You now need to serve users in Southeast Asia with under 100ms latency. "
        "Your entire system currently runs in us-east-1. "
        "Walk me through your multi-region architecture changes, "
        "including data replication, consistency trade-offs, and failover."
    ),
    "cost_analysis": (
        "Estimate the monthly AWS infrastructure cost for your design "
        "at 1 million daily active users. "
        "Break down the major cost components — compute, storage, data transfer, caching. "
        "What would you optimize first to reduce costs by 30%?"
    ),
}

STRESS_ORDER = ["traffic_spike", "failure_injection", "multi_region", "cost_analysis"]


def lambda_handler(event, context):
    try:
        body          = json.loads(event.get("body") or "{}")
        session_id    = body.get("sessionId")
        evaluation_id = body.get("evaluationId")
        stress_type   = body.get("stressType")
        answer        = body.get("answer", "").strip()

        if not session_id:
            return _error(400, "sessionId is required")
        if not stress_type or stress_type not in STRESS_QUESTIONS:
            return _error(400, f"stressType must be one of: {', '.join(STRESS_QUESTIONS)}")

        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        mode     = session.get("mode", "practice")
        question = STRESS_QUESTIONS[stress_type]

        # If no answer yet — just return the question
        if not answer:
            return _ok({
                "sessionId":        session_id,
                "stressType":       stress_type,
                "question":         question,
                "answerEvaluated":  False,
                "score":            None,
                "passed":           None,
                "feedback":         None,
                "nextStressType":   None,
                "allStressComplete": False,
                "scoreDelta":       {},
            })

        # Evaluate the answer
        evaluation = _evaluate_stress_answer(stress_type, question, answer, mode)

        score       = evaluation.get("score", 5)
        passed      = evaluation.get("passed", False)
        feedback    = evaluation.get("feedback", "")
        score_delta = evaluation.get("scoreDelta", {})

        now = str(time.time())

        # Store stress Q in messages
        TABLES["messages"].put_item(Item={
            "sessionId":   session_id,
            "messageId":   str(uuid.uuid4()),
            "role":        "interviewer",
            "section":     "design",
            "content":     question,
            "questionType": "stress_test",
            "stressType":  stress_type,
            "messageType": "stress_question",
            "timestamp":   now,
        })

        # Store candidate answer
        TABLES["messages"].put_item(Item={
            "sessionId":   session_id,
            "messageId":   str(uuid.uuid4()),
            "role":        "candidate",
            "section":     "design",
            "content":     answer,
            "stressType":  stress_type,
            "messageType": "stress_answer",
            "timestamp":   now,
        })

        # Store stress evaluation
        TABLES["messages"].put_item(Item={
            "sessionId":      session_id,
            "messageId":      str(uuid.uuid4()),
            "role":           "evaluation",
            "section":        "design",
            "score":          str(score),
            "passed":         str(passed),
            "feedback":       feedback,
            "stressType":     stress_type,
            "scoreDelta":     score_delta,
            "messageType":    "stress_evaluation",
            "linkedEvalId":   evaluation_id or "",
            "timestamp":      now,
        })

        # Apply score deltas
        _update_scores(session_id, score_delta, session)

        # Determine next stress test from evaluation record
        next_stress   = _get_next_stress(session_id, evaluation_id, stress_type)
        all_complete  = next_stress is None

        response_data = {
            "sessionId":         session_id,
            "stressType":        stress_type,
            "question":          question,
            "answerEvaluated":   True,
            "score":             score,
            "passed":            passed,
            "nextStressType":    next_stress,
            "allStressComplete": all_complete,
            "scoreDelta":        score_delta,
        }

        if mode == "practice":
            response_data["feedback"] = feedback

        return _ok(response_data)

    except Exception as e:
        return _error(500, str(e))


def _evaluate_stress_answer(stress_type: str, question: str, answer: str, mode: str) -> dict:
    stress_criteria = {
        "traffic_spike": (
            "Did they identify what breaks first? "
            "Did they mention horizontal scaling, auto-scaling, queue-based load shedding, "
            "or CDN offloading? Did they give specific strategies, not just 'scale up'?"
        ),
        "failure_injection": (
            "Did they describe the failure cascade? "
            "Did they mention circuit breakers, retries with backoff, read replicas, "
            "write-ahead logs, or eventual consistency fallback? "
            "Did they have a concrete recovery path?"
        ),
        "multi_region": (
            "Did they discuss data replication strategy (active-active vs active-passive)? "
            "Did they mention consistency trade-offs (CAP theorem)? "
            "Did they discuss latency routing (Route53, GeoDNS)? "
            "Did they mention cross-region data sync challenges?"
        ),
        "cost_analysis": (
            "Did they actually estimate numbers? "
            "Did they break down compute, storage, transfer costs? "
            "Did they identify the largest cost driver? "
            "Did they propose specific optimizations (reserved instances, spot, tiered storage, caching)?"
        ),
    }

    criteria = stress_criteria.get(stress_type, "Evaluate the response quality.")

    prompt = f"""You are a FAANG staff engineer evaluating a candidate's response to a stress test scenario.

Stress scenario type: {stress_type}
Question asked:
{question}

Candidate's answer:
{answer}

Evaluation criteria for this scenario:
{criteria}

Score 0-10:
0-3: Completely missed the point. Generic answer, no specifics.
4-5: Partial. Identified some issues but missed key engineering details.
6-7: Solid. Covered main points with reasonable depth.
8-9: Strong. Specific, concrete, showed deep systems knowledge.
10: Exceptional. Complete, numbers-driven, showed staff-level thinking.

Return ONLY this JSON:
{{
  "score": <0-10>,
  "passed": <true if score >= 6>,
  "feedback": "<2-3 sentences: what they got right, what was missing, what a strong answer looks like>",
  "scoreDelta": {{
    "system_design": <-2 to +3>,
    "architecture_maturity": <-2 to +3>,
    "communication": <-1 to +1>
  }}
}}

Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.2, max_tokens=600)
    try:
        return json.loads(result)
    except Exception:
        return {
            "score": 5,
            "passed": False,
            "feedback": "Evaluation failed.",
            "scoreDelta": {"system_design": 0, "architecture_maturity": 0, "communication": 0},
        }


def _get_next_stress(session_id: str, evaluation_id: str, current_stress: str) -> str | None:
    """Find the next pending stress test from the original evaluation record."""
    if not evaluation_id:
        return None

    try:
        eval_resp = TABLES["messages"].get_item(
            Key={"sessionId": session_id, "messageId": evaluation_id}
        )
        evaluation = eval_resp.get("Item", {})
        tests_needed = evaluation.get("stressTestsNeeded", [])
    except Exception:
        return None

    # Find what stress tests have already been run this session
    completed = _get_completed_stress_tests(session_id)

    # Return next test from the needed list that hasn't been run
    for t in STRESS_ORDER:
        if t in tests_needed and t not in completed and t != current_stress:
            return t

    return None


def _get_completed_stress_tests(session_id: str) -> set:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            FilterExpression="messageType = :t",
            ExpressionAttributeValues={
                ":sid": session_id,
                ":t":   "stress_evaluation",
            },
        )
        return {item.get("stressType") for item in resp.get("Items", [])}
    except Exception:
        return set()


def _update_scores(session_id, score_delta, session):
    if not score_delta:
        return
    current = dict(session.get("scores", {}))
    parts, vals = [], {}
    for key in ["system_design", "architecture_maturity", "communication"]:
        delta = int(score_delta.get(key, 0))
        if delta == 0:
            continue
        new_val = max(0, min(100, int(current.get(key, 0)) + delta * 5))
        parts.append(f"scores.{key} = :s_{key}")
        vals[f":s_{key}"] = new_val
    if parts:
        TABLES["sessions"].update_item(
            Key={"sessionId": session_id},
            UpdateExpression="SET " + ", ".join(parts),
            ExpressionAttributeValues=vals,
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
