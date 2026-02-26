"""
POST /startSession

Body:
{
  "userId":        string,
  "interviewType": "behavioral" | "technical" | "system_design",
  "mode":          "practice" | "real",
  "resumeS3Key":   string (optional — S3 key of uploaded resume)
}

Response:
{
  "sessionId":    string,
  "interviewType": string,
  "mode":          string,
  "currentSection": string,
  "timerState":   { ... }
}
"""

import json
import uuid
import time
import sys
import os

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES
from timer import get_limits, compute_timer_state

FIRST_SECTION = {
    "behavioral":   "intro",
    "technical":    "resume_drill",
    "system_design": "resume_drill",
}


def lambda_handler(event, context):
    try:
        body = json.loads(event.get("body") or "{}")

        user_id       = body.get("userId") or str(uuid.uuid4())
        interview_type = body.get("interviewType", body.get("mode", "technical"))
        mode          = body.get("practiceMode", False)
        mode          = "practice" if mode else body.get("realMode", "real")
        resume_s3_key = body.get("resumeS3Key", None)

        if interview_type not in ("behavioral", "technical", "system_design"):
            interview_type = "technical"

        if mode not in ("practice", "real"):
            mode = "real"

        session_id    = str(uuid.uuid4())
        now           = time.time()
        first_section = FIRST_SECTION[interview_type]

        session = {
            "sessionId":       session_id,
            "userId":          user_id,
            "interviewType":   interview_type,
            "mode":            mode,
            "status":          "active",
            "currentSection":  first_section,
            "startedAt":       str(now),
            "sectionStartedAt": str(now),
            "resumeS3Key":     resume_s3_key or "",
            "scores": {
                "behavioral":           0,
                "coding":               0,
                "system_design":        0,
                "complexity_awareness": 0,
                "communication":        0,
                "resume_authenticity":  0,
                "time_management":      0,
                "architecture_maturity": 0,
            },
            "sectionHistory":  [],
            "createdAt":       str(now),
        }

        TABLES["sessions"].put_item(Item=session)

        # Build timer state for response
        session_for_timer = {
            **session,
            "startedAt":       now,
            "sectionStartedAt": now,
        }
        timer_state = compute_timer_state(session_for_timer, now)

        return _ok({
            "sessionId":      session_id,
            "interviewType":  interview_type,
            "mode":           mode,
            "currentSection": first_section,
            "timerState":     timer_state,
        })

    except Exception as e:
        return _error(500, str(e))


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
