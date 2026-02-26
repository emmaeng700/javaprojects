"""
GET /getHiringEvaluation?sessionId=xxx

Fetches the stored hiring evaluation for a completed session.
Used by the frontend to render the final report screen.

Response: full evaluation object from IV_HiringEvaluations
"""

import json
import os
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES

# Score band labels for UI display
SCORE_BANDS = [
    (85, "Exceptional"),
    (70, "Strong"),
    (55, "Meets Bar"),
    (40, "Below Bar"),
    (0,  "Significantly Below Bar"),
]

HIRE_COLORS = {
    "Strong Hire":   "green",
    "Hire":          "blue",
    "Lean Hire":     "yellow",
    "Lean No Hire":  "orange",
    "No Hire":       "red",
}


def lambda_handler(event, context):
    try:
        params     = event.get("queryStringParameters") or {}
        session_id = params.get("sessionId")

        if not session_id:
            return _error(400, "sessionId is required")

        # Fetch evaluation
        resp = TABLES["evaluations"].get_item(Key={"sessionId": session_id})
        evaluation = resp.get("Item")

        if not evaluation:
            # Check if session exists and is still in progress
            s_resp  = TABLES["sessions"].get_item(Key={"sessionId": session_id})
            session = s_resp.get("Item")
            if not session:
                return _error(404, "Session not found")
            if session.get("status") != "completed":
                return _error(409, "Session not yet completed — evaluation not available")
            return _error(404, "Evaluation not found")

        # Enrich with UI display metadata
        final_score  = int(evaluation.get("finalScore", 0))
        hire_rec     = evaluation.get("hireRecommendation", "No Hire")
        score_band   = _threshold_label(final_score, SCORE_BANDS)
        hire_color   = HIRE_COLORS.get(hire_rec, "gray")

        # Build per-dimension score cards for UI
        score_cards = _build_score_cards(evaluation)

        enriched = {
            **evaluation,
            "scoreBand":   score_band,
            "hireColor":   hire_color,
            "scoreCards":  score_cards,
        }

        return _ok(enriched)

    except Exception as e:
        return _error(500, str(e))


def _build_score_cards(evaluation: dict) -> list:
    dimensions = [
        ("behavioral",           "Behavioral",           "behavioral"),
        ("coding",               "Coding",               "coding"),
        ("systemDesign",         "System Design",        "system_design"),
        ("complexityAwareness",  "Complexity Awareness", "complexity_awareness"),
        ("communication",        "Communication",        "communication"),
        ("resumeAuthenticity",   "Resume Authenticity",  "resume_authenticity"),
        ("timeManagement",       "Time Management",      "time_management"),
        ("architectureMaturity", "Architecture Maturity","architecture_maturity"),
    ]

    cards = []
    for eval_key, label, _ in dimensions:
        score_key = f"{eval_key}Score"
        score     = int(evaluation.get(score_key, 0))
        band      = _threshold_label(score, SCORE_BANDS)
        cards.append({
            "key":   eval_key,
            "label": label,
            "score": score,
            "band":  band,
        })

    return cards


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
