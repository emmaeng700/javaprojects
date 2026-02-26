"""
Server-side timer authority.
Frontend ONLY reads elapsed/remaining — never sets time.
"""

# Time limits in seconds (server enforced)
SECTION_LIMITS = {
    "behavioral": {
        "total": 35 * 60,          # 35 min max
        "sections": {
            "intro":       5 * 60,
            "behavioral": 30 * 60,
        },
    },
    "technical": {
        "total": 75 * 60,          # 75 min max
        "sections": {
            "resume_drill": 15 * 60,
            "coding":       60 * 60,
        },
    },
    "system_design": {
        "total": 75 * 60,
        "sections": {
            "resume_drill": 15 * 60,
            "design":       60 * 60,
        },
    },
}

# Warn frontend at these thresholds (seconds remaining)
WARNING_THRESHOLDS = [300, 120, 60]  # 5 min, 2 min, 1 min


def get_limits(interview_type: str) -> dict:
    return SECTION_LIMITS.get(interview_type, SECTION_LIMITS["technical"])


def compute_timer_state(session: dict, now_ts: float) -> dict:
    """
    Given a session record and current timestamp, return
    the authoritative timer state to send to the frontend.
    """
    interview_type = session["interviewType"]
    limits = get_limits(interview_type)
    current_section = session["currentSection"]

    section_limits = limits["sections"]
    section_start_ts = session["sectionStartedAt"]
    session_start_ts = session["startedAt"]

    section_elapsed = now_ts - section_start_ts
    session_elapsed = now_ts - session_start_ts

    section_limit = section_limits.get(current_section, 60 * 60)
    section_remaining = max(0, section_limit - section_elapsed)
    total_remaining = max(0, limits["total"] - session_elapsed)

    warnings = [t for t in WARNING_THRESHOLDS if section_remaining <= t]
    active_warning = warnings[0] if warnings else None

    force_transition = section_remaining <= 0

    return {
        "currentSection":    current_section,
        "sectionElapsed":    int(section_elapsed),
        "sectionRemaining":  int(section_remaining),
        "totalElapsed":      int(session_elapsed),
        "totalRemaining":    int(total_remaining),
        "activeWarning":     active_warning,
        "forceTransition":   force_transition,
    }
