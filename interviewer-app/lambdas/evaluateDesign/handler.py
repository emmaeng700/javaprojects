"""
POST /evaluateDesign

Body:
{
  "sessionId":   string,
  "questionId":  string,
  "answer":      string,   (candidate's design explanation)
  "diagramData": object    (optional — JSON of any diagram elements submitted)
}

Evaluates a system design answer against FAANG bar expectations.

Detects missing coverage:
- No scaling discussion
- No bottleneck identification
- Shallow trade-off explanation
- No failure-mode discussion
- Overconfidence without numbers
- Missing data model discussion
- No caching strategy
- No load balancing discussion
- No database choice justification

Scores the answer and determines which stress tests to trigger.

Response:
{
  "sessionId":          string,
  "evaluationId":       string,
  "score":              number (0-10),
  "coverageGaps":       [string],
  "strengths":          [string],
  "stressTestsNeeded":  [string],  ("traffic_spike" | "failure_injection" | "multi_region" | "cost_analysis")
  "shouldStressTest":   bool,
  "critique":           string  (practice mode only),
  "scoreDelta": {
    "system_design":        number,
    "architecture_maturity": number,
    "communication":        number
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

STRESS_TEST_TYPES = ["traffic_spike", "failure_injection", "multi_region", "cost_analysis"]

COVERAGE_CHECKLIST = [
    "scaling_discussion",
    "bottleneck_identification",
    "trade_off_explanation",
    "failure_mode_discussion",
    "data_model",
    "caching_strategy",
    "load_balancing",
    "database_choice_justification",
    "numbers_and_estimates",
    "api_design",
]


def lambda_handler(event, context):
    try:
        body        = json.loads(event.get("body") or "{}")
        session_id  = body.get("sessionId")
        question_id = body.get("questionId", "")
        answer      = body.get("answer", "").strip()
        diagram     = body.get("diagramData")

        if not session_id:
            return _error(400, "sessionId is required")
        if not answer:
            return _error(400, "answer is required")

        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        mode = session.get("mode", "practice")

        # Fetch original question
        original_question = ""
        if question_id:
            try:
                q = TABLES["messages"].get_item(
                    Key={"sessionId": session_id, "messageId": question_id}
                )
                original_question = q.get("Item", {}).get("content", "")
            except Exception:
                pass

        # Evaluate with Gemini
        evaluation = _evaluate_design_with_gemini(
            original_question, answer, diagram, mode
        )

        score             = evaluation.get("score", 5)
        coverage_gaps     = evaluation.get("coverageGaps", [])
        strengths         = evaluation.get("strengths", [])
        stress_tests      = evaluation.get("stressTestsNeeded", [])
        critique          = evaluation.get("critique", "")
        score_delta       = evaluation.get("scoreDelta", {})
        should_stress     = len(stress_tests) > 0

        eval_id = str(uuid.uuid4())
        now     = str(time.time())

        # Store candidate answer
        TABLES["messages"].put_item(Item={
            "sessionId":         session_id,
            "messageId":         str(uuid.uuid4()),
            "role":              "candidate",
            "section":           "design",
            "content":           answer,
            "linkedQuestionId":  question_id,
            "timestamp":         now,
        })

        # Store evaluation
        TABLES["messages"].put_item(Item={
            "sessionId":        session_id,
            "messageId":        eval_id,
            "role":             "evaluation",
            "section":          "design",
            "score":            str(score),
            "coverageGaps":     coverage_gaps,
            "strengths":        strengths,
            "stressTestsNeeded": stress_tests,
            "shouldStressTest": str(should_stress),
            "critique":         critique,
            "scoreDelta":       score_delta,
            "messageType":      "design_evaluation",
            "timestamp":        now,
        })

        # Update session scores
        _update_scores(session_id, score_delta, session)

        response_data = {
            "sessionId":         session_id,
            "evaluationId":      eval_id,
            "score":             score,
            "coverageGaps":      coverage_gaps,
            "strengths":         strengths,
            "stressTestsNeeded": stress_tests,
            "shouldStressTest":  should_stress,
            "scoreDelta":        score_delta,
        }

        if mode == "practice":
            response_data["critique"] = critique

        return _ok(response_data)

    except Exception as e:
        return _error(500, str(e))


def _evaluate_design_with_gemini(question, answer, diagram, mode) -> dict:
    diagram_note = ""
    if diagram:
        diagram_note = f"\nDiagram elements provided: {json.dumps(diagram)[:500]}"

    prompt = f"""You are a FAANG staff engineer evaluating a system design answer.

Design question:
{question or "(general system design question)"}

Candidate's answer:
{answer}
{diagram_note}

Evaluate ruthlessly against FAANG L5/L6 expectations.

Check for presence of each:
- Scaling discussion (how does it handle 10x, 100x traffic?)
- Bottleneck identification (what breaks first?)
- Trade-off explanation (why this choice vs alternatives?)
- Failure mode discussion (what happens when X fails?)
- Data model (how is data structured and stored?)
- Caching strategy (what gets cached, where, TTL?)
- Load balancing (how is traffic distributed?)
- Database choice justification (why SQL vs NoSQL vs graph?)
- Numbers and estimates (QPS, storage, latency estimates)
- API design (endpoints, request/response shapes)

Scoring:
0-3:  Missing most fundamentals. No scaling, no failure modes, no trade-offs.
4-5:  Covers basics but shallow. No numbers, vague trade-offs.
6-7:  Solid coverage. Some gaps in depth.
8-9:  Strong. Numbers present, trade-offs justified, failure modes discussed.
10:   Exceptional. Complete depth across all dimensions with numbers.

Stress tests to trigger (select all applicable):
- "traffic_spike"       if no 10x/100x traffic discussion
- "failure_injection"   if no failure modes discussed
- "multi_region"        if no global distribution or data replication discussed
- "cost_analysis"       if no cost/resource estimates given

Return ONLY this JSON:
{{
  "score": <0-10>,
  "coverageGaps": ["<missing area 1>", "<missing area 2>"],
  "strengths": ["<strength 1>", "<strength 2>"],
  "stressTestsNeeded": ["traffic_spike", "failure_injection"],
  "critique": "<3-sentence sharp critique — what was missing and what FAANG expects>",
  "scoreDelta": {{
    "system_design": <-3 to +3>,
    "architecture_maturity": <-2 to +3>,
    "communication": <-1 to +2>
  }}
}}

Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.2, max_tokens=1024)
    try:
        return json.loads(result)
    except Exception:
        return {
            "score": 5,
            "coverageGaps": ["Evaluation failed"],
            "strengths": [],
            "stressTestsNeeded": ["traffic_spike", "failure_injection"],
            "critique": "Could not evaluate design.",
            "scoreDelta": {"system_design": 0, "architecture_maturity": 0, "communication": 0},
        }


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
