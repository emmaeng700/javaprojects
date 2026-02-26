"""
POST /transitionSection

Body:
{
  "sessionId": string,
  "requestedSection": string  (optional — server validates and may override)
}

Server checks:
- Is the current section time expired? Force transition if so.
- Is the requested section valid for this interview type?
- Log section history with time spent.

Response:
{
  "sessionId":        string,
  "previousSection":  string,
  "currentSection":   string,
  "timerState":       { ... },
  "forced":           bool   (true if server forced it due to timeout)
}
"""

import json
import time
import sys
import os
from decimal import Decimal

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES
from timer import compute_timer_state, get_limits

SECTION_ORDER = {
    "behavioral":    ["intro", "behavioral", "done"],
    "technical":     ["resume_drill", "coding", "done"],
    "system_design": ["resume_drill", "design", "done"],
}


def lambda_handler(event, context):
    try:
        body = json.loads(event.get("body") or "{}")
        session_id         = body.get("sessionId")
        requested_section  = body.get("requestedSection")

        if not session_id:
            return _error(400, "sessionId is required")

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        if session["status"] == "completed":
            return _error(400, "Session already completed")

        now               = time.time()
        interview_type    = session["interviewType"]
        current_section   = session["currentSection"]
        section_started   = float(session["sectionStartedAt"])
        session_started   = float(session["startedAt"])

        # Rehydrate for timer
        session_for_timer = {
            **session,
            "startedAt":        session_started,
            "sectionStartedAt": section_started,
        }
        timer_state = compute_timer_state(session_for_timer, now)

        # Determine next section
        order = SECTION_ORDER.get(interview_type, [])
        try:
            current_idx = order.index(current_section)
        except ValueError:
            return _error(400, f"Unknown section: {current_section}")

        forced = timer_state["forceTransition"]

        if forced or not requested_section:
            # Server forces next section
            next_section = order[current_idx + 1] if current_idx + 1 < len(order) else "done"
        else:
            # Validate requested section is the legitimate next one
            expected_next = order[current_idx + 1] if current_idx + 1 < len(order) else "done"
            if requested_section != expected_next:
                return _error(400, f"Invalid transition. Expected: {expected_next}")
            next_section = expected_next

        time_spent = int(now - section_started)

        # Log section history
        history_entry = {
            "section":   current_section,
            "timeSpent": time_spent,
            "endedAt":   str(now),
        }

        new_status = "completed" if next_section == "done" else "active"

        # Update session
        TABLES["sessions"].update_item(
            Key={"sessionId": session_id},
            UpdateExpression=(
                "SET currentSection = :ns, "
                "sectionStartedAt = :ss, "
                "#st = :status, "
                "sectionHistory = list_append(sectionHistory, :h)"
            ),
            ExpressionAttributeNames={"#st": "status"},
            ExpressionAttributeValues={
                ":ns":     next_section,
                ":ss":     str(now),
                ":status": new_status,
                ":h":      [history_entry],
            },
        )

        # New timer state for next section
        new_session_for_timer = {
            **session,
            "currentSection":   next_section,
            "startedAt":        session_started,
            "sectionStartedAt": now,
        }
        new_timer_state = compute_timer_state(new_session_for_timer, now)

        return _ok({
            "sessionId":       session_id,
            "previousSection": current_section,
            "currentSection":  next_section,
            "timeSpentSeconds": time_spent,
            "timerState":      new_timer_state,
            "forced":          forced,
            "sessionStatus":   new_status,
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
