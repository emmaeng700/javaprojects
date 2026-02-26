"""
POST /escalateCodingQuestion

Called after a question is solved to determine if a harder question should be given.

Escalation logic (from spec):
- If candidate solves first question quickly (< 15 min) + correct + optimal + complexity correct
  → Generate harder second question
- If second solved quickly + cleanly
  → Optional third high-difficulty variant
- Otherwise: stop at 2 questions max

Body:
{
  "sessionId":    string,
  "submissionId": string,   (the passing submission)
  "questionNumber": number  (1, 2, or 3)
}

Response:
{
  "sessionId":        string,
  "escalate":         bool,
  "escalationReason": string,
  "nextQuestion":     string | null,
  "nextQuestionId":   string | null,
  "difficulty":       "medium" | "hard" | "very_hard" | null,
  "questionNumber":   number
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

MAX_QUESTIONS      = 3
FAST_SOLVE_MINUTES = 15   # Threshold to trigger escalation


def lambda_handler(event, context):
    try:
        body            = json.loads(event.get("body") or "{}")
        session_id      = body.get("sessionId")
        submission_id   = body.get("submissionId")
        question_number = int(body.get("questionNumber", 1))

        if not session_id:
            return _error(400, "sessionId is required")
        if not submission_id:
            return _error(400, "submissionId is required")

        # Hard cap at 3 questions
        if question_number >= MAX_QUESTIONS:
            return _ok({
                "sessionId":        session_id,
                "escalate":         False,
                "escalationReason": "Maximum questions reached (3)",
                "nextQuestion":     None,
                "nextQuestionId":   None,
                "difficulty":       None,
                "questionNumber":   question_number,
            })

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        # Fetch submission
        sub_resp = TABLES["code"].get_item(
            Key={"sessionId": session_id, "submissionId": submission_id}
        )
        submission = sub_resp.get("Item")
        if not submission:
            return _error(404, "Submission not found")

        # Check escalation criteria
        passed              = submission.get("passed", False)
        is_optimal          = submission.get("isOptimal", False)
        time_correct        = submission.get("timeComplexityCorrect", False)
        space_correct       = submission.get("spaceComplexityCorrect", False)
        exec_time_ms        = int(submission.get("executionTimeMs", 99999))
        complexity_correct  = time_correct and space_correct

        # Estimate solve time from session section start
        section_started = float(session.get("sectionStartedAt", time.time()))
        solve_minutes   = (time.time() - section_started) / 60

        # Escalation rules
        if not passed:
            return _ok(_no_escalate(session_id, question_number, "Solution did not pass all tests"))

        if question_number == 1:
            # Escalate Q1 → Q2 if: solved correctly + fast + optimal + complexity right
            if solve_minutes <= FAST_SOLVE_MINUTES and is_optimal and complexity_correct:
                reason = (
                    f"Solved Q1 in {solve_minutes:.1f} min — optimal solution + "
                    f"correct complexity. Escalating to hard question."
                )
                escalate = True
                difficulty = "hard"
            elif passed and is_optimal:
                reason = "Correct and optimal — giving standard second question."
                escalate = True
                difficulty = "medium"
            else:
                reason = "Solution passed but not optimal. Stopping at 1 question."
                escalate = False
                difficulty = None

        elif question_number == 2:
            # Escalate Q2 → Q3 only if: fast + optimal + complexity correct
            if solve_minutes <= FAST_SOLVE_MINUTES and is_optimal and complexity_correct:
                reason = (
                    f"Exceptional performance — solved Q2 in {solve_minutes:.1f} min, "
                    f"optimal + correct complexity. Bonus hard variant."
                )
                escalate = True
                difficulty = "very_hard"
            else:
                reason = "Good performance but not exceptional. Stopping at 2 questions."
                escalate = False
                difficulty = None
        else:
            escalate  = False
            reason    = "Maximum questions reached"
            difficulty = None

        if not escalate:
            return _ok(_no_escalate(session_id, question_number, reason))

        # Fetch prior coding questions to avoid repeats
        prior_questions = _get_prior_questions(session_id)

        # Generate next question via Gemini
        next_question = _generate_escalated_question(
            session, difficulty, question_number, prior_questions
        )

        next_q_id = str(uuid.uuid4())

        # Store in messages
        TABLES["messages"].put_item(Item={
            "sessionId":    session_id,
            "messageId":    next_q_id,
            "role":         "interviewer",
            "section":      "coding",
            "content":      next_question,
            "questionType": "escalation",
            "difficulty":   difficulty,
            "questionNumber": question_number + 1,
            "timestamp":    str(time.time()),
        })

        return _ok({
            "sessionId":        session_id,
            "escalate":         True,
            "escalationReason": reason,
            "nextQuestion":     next_question,
            "nextQuestionId":   next_q_id,
            "difficulty":       difficulty,
            "questionNumber":   question_number + 1,
        })

    except Exception as e:
        return _error(500, str(e))


def _generate_escalated_question(
    session: dict, difficulty: str, prev_question_number: int, prior_questions: list
) -> str:

    interview_type = session.get("interviewType", "technical")
    prior_text = "\n".join(f"- {q}" for q in prior_questions[:3]) if prior_questions else "None"

    difficulty_guide = {
        "medium":    "LeetCode medium — arrays, strings, hash maps, two pointers, sliding window",
        "hard":      "LeetCode hard — DP, graphs, advanced data structures, backtracking",
        "very_hard": "LeetCode hard variant or follow-up — optimized DP, segment trees, advanced graph algorithms",
    }

    prompt = f"""You are a FAANG senior interviewer escalating a coding interview.

Interview type: {interview_type}
This is question #{prev_question_number + 1}.
Difficulty: {difficulty} — {difficulty_guide.get(difficulty, '')}

Prior questions asked (do NOT repeat these topics):
{prior_text}

Generate a NEW coding problem. Requirements:
- Appropriate for {difficulty} difficulty
- Different category from prior questions
- Include clear problem statement
- Include 2-3 input/output examples
- Include constraints (array size, value range, etc.)
- Do NOT include hints or solutions

Format:
Problem statement first.
Then Examples section.
Then Constraints section.

Return ONLY the problem text, no extra commentary.
"""

    result = call_gemini(prompt, temperature=0.7, max_tokens=800)
    return result.strip() if result else _fallback_question(difficulty)


def _get_prior_questions(session_id: str) -> list:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            FilterExpression="#s = :sec AND #r = :role",
            ExpressionAttributeNames={"#s": "section", "#r": "role"},
            ExpressionAttributeValues={
                ":sid":  session_id,
                ":sec":  "coding",
                ":role": "interviewer",
            },
        )
        return [item.get("content", "")[:120] for item in resp.get("Items", [])]
    except Exception:
        return []


def _fallback_question(difficulty: str) -> str:
    fallbacks = {
        "medium": (
            "Given a string s, find the length of the longest substring without repeating characters.\n\n"
            "Example 1: Input: s = 'abcabcbb' → Output: 3\n"
            "Example 2: Input: s = 'bbbbb' → Output: 1\n\n"
            "Constraints: 0 <= s.length <= 5 * 10^4, s consists of English letters, digits, symbols and spaces."
        ),
        "hard": (
            "Given an array of integers nums and an integer k, return the number of subarrays "
            "where the product of all elements is strictly less than k.\n\n"
            "Example 1: Input: nums = [10,5,2,6], k = 100 → Output: 8\n\n"
            "Constraints: 1 <= nums.length <= 3 * 10^4, 1 <= nums[i] <= 1000, 0 <= k <= 10^6."
        ),
        "very_hard": (
            "Given a string s and a dictionary of strings wordDict, add spaces in s to construct "
            "a sentence where each word is a valid dictionary word. Return all such possible sentences "
            "in any order.\n\n"
            "Example: Input: s = 'catsanddog', wordDict = ['cat','cats','and','sand','dog']\n"
            "Output: ['cats and dog', 'cat sand dog']\n\n"
            "Constraints: 1 <= s.length <= 20, 1 <= wordDict.length <= 1000."
        ),
    }
    return fallbacks.get(difficulty, fallbacks["medium"])


def _no_escalate(session_id, question_number, reason):
    return {
        "sessionId":        session_id,
        "escalate":         False,
        "escalationReason": reason,
        "nextQuestion":     None,
        "nextQuestionId":   None,
        "difficulty":       None,
        "questionNumber":   question_number,
    }


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
