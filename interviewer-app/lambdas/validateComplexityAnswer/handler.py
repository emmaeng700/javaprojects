"""
POST /validateComplexityAnswer

Body:
{
  "sessionId":    string,
  "submissionId": string,
  "timeComplexity":  string   (e.g. "O(n)", "O(n log n)", "O(n^2)")
  "spaceComplexity": string   (e.g. "O(1)", "O(n)")
}

Called AFTER code passes all test cases (triggerComplexityPopup=true).
Candidate must state their time + space complexity BEFORE seeing evaluation.

Validates their answer against:
1. The actual code they submitted (Gemini analyzes it)
2. Whether they identified the optimal complexity

Determines:
- confetti: true only if code correct + complexity both correct
- If complexity wrong: deduct complexity_awareness score
- If complexity partially right: partial credit

Response:
{
  "sessionId":             string,
  "submissionId":          string,
  "timeComplexityCorrect": bool,
  "spaceComplexityCorrect": bool,
  "actualTimeComplexity":  string,
  "actualSpaceComplexity": string,
  "isOptimal":             bool,
  "confetti":              bool,
  "feedback":              string,
  "critique":              string  (practice mode only),
  "scoreDelta": {
    "coding":               number,
    "complexity_awareness": number
  }
}
"""

import json
import os
import sys
import time

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES
from gemini import call_gemini

# Normalize common complexity notations
COMPLEXITY_ALIASES = {
    "o(1)": "O(1)",
    "o(n)": "O(n)",
    "o(n^2)": "O(n^2)",
    "o(n2)": "O(n^2)",
    "o(n*n)": "O(n^2)",
    "o(n^3)": "O(n^3)",
    "o(n3)": "O(n^3)",
    "o(n*n*n)": "O(n^3)",
    "o(n^2*m)": "O(n^2 * m)",
    "o(log n)": "O(log n)",
    "o(logn)": "O(log n)",
    "o(n log n)": "O(n log n)",
    "o(nlogn)": "O(n log n)",
    "o(n!)": "O(n!)",
    "o(2^n)": "O(2^n)",
    "o(n+m)": "O(n+m)",
    "o(m+n)": "O(n+m)",
    "o(v+e)": "O(V+E)",
    "o(vertices+edges)": "O(V+E)",
    "o(v + e)": "O(V+E)",
    "o(e+v)": "O(V+E)",
    "o(e log v)": "O(E log V)",
    "o(elogv)": "O(E log V)",
    "o(v^2)": "O(V^2)",
    "o(e log e)": "O(E log E)",
}


def lambda_handler(event, context):
    try:
        body           = json.loads(event.get("body") or "{}")
        session_id     = body.get("sessionId")
        submission_id  = body.get("submissionId")
        time_complexity  = (body.get("timeComplexity") or "").strip()
        space_complexity = (body.get("spaceComplexity") or "").strip()

        if not session_id:
            return _error(400, "sessionId is required")
        if not submission_id:
            return _error(400, "submissionId is required")
        if not time_complexity or not space_complexity:
            return _error(400, "timeComplexity and spaceComplexity are required")

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        mode = session.get("mode", "practice")

        # Fetch code submission
        sub_resp = TABLES["code"].get_item(
            Key={"sessionId": session_id, "submissionId": submission_id}
        )
        submission = sub_resp.get("Item")
        if not submission:
            return _error(404, "Submission not found")

        if not submission.get("passed"):
            return _error(400, "Cannot validate complexity — code did not pass all tests")

        code     = submission.get("code", "")
        language = submission.get("language", "python")

        # Normalize user input
        time_norm  = COMPLEXITY_ALIASES.get(time_complexity.lower(), time_complexity)
        space_norm = COMPLEXITY_ALIASES.get(space_complexity.lower(), space_complexity)

        # Ask Gemini to analyze the code's actual complexity
        analysis = _analyze_complexity_with_gemini(code, language, time_norm, space_norm)

        actual_time  = analysis.get("actualTimeComplexity", "Unknown")
        actual_space = analysis.get("actualSpaceComplexity", "Unknown")
        time_correct  = analysis.get("timeComplexityCorrect", False)
        space_correct = analysis.get("spaceComplexityCorrect", False)
        is_optimal    = analysis.get("isOptimal", False)
        feedback      = analysis.get("feedback", "")
        critique      = analysis.get("critique", "")
        score_delta   = analysis.get("scoreDelta", {
            "coding": 0, "complexity_awareness": 0
        })

        # Confetti only if: code correct (already verified) + both complexities correct
        confetti = time_correct and space_correct

        # Update session scores
        _update_scores(session_id, score_delta, session)

        # Update submission record with complexity validation
        TABLES["code"].update_item(
            Key={"sessionId": session_id, "submissionId": submission_id},
            UpdateExpression=(
                "SET timeComplexityAnswer = :tc, spaceComplexityAnswer = :sc, "
                "timeComplexityCorrect = :tcc, spaceComplexityCorrect = :scc, "
                "actualTimeComplexity = :atc, actualSpaceComplexity = :asc, "
                "isOptimal = :opt, confetti = :cf"
            ),
            ExpressionAttributeValues={
                ":tc":  time_norm,
                ":sc":  space_norm,
                ":tcc": time_correct,
                ":scc": space_correct,
                ":atc": actual_time,
                ":asc": actual_space,
                ":opt": is_optimal,
                ":cf":  confetti,
            },
        )

        response_data = {
            "sessionId":              session_id,
            "submissionId":           submission_id,
            "timeComplexityCorrect":  time_correct,
            "spaceComplexityCorrect": space_correct,
            "actualTimeComplexity":   actual_time,
            "actualSpaceComplexity":  actual_space,
            "isOptimal":              is_optimal,
            "confetti":               confetti,
            "feedback":               feedback,
            "scoreDelta":             score_delta,
        }

        if mode == "practice":
            response_data["critique"] = critique

        return _ok(response_data)

    except Exception as e:
        return _error(500, str(e))


def _analyze_complexity_with_gemini(
    code: str, language: str, stated_time: str, stated_space: str
) -> dict:

    prompt = f"""You are a senior FAANG engineer reviewing submitted code for a technical interview.

Language: {language}

Submitted code:
```
{code[:3000]}
```

Candidate stated:
- Time complexity:  {stated_time}
- Space complexity: {stated_space}

Analyze the code and return ONLY this JSON:
{{
  "actualTimeComplexity":   "<correct Big-O time complexity>",
  "actualSpaceComplexity":  "<correct Big-O space complexity>",
  "timeComplexityCorrect":  <true/false — is stated time complexity correct?>,
  "spaceComplexityCorrect": <true/false — is stated space complexity correct?>,
  "isOptimal": <true if this is the most optimal known solution for this problem type>,
  "feedback": "<1-2 sentences: what they got right or wrong about complexity>",
  "critique": "<sharp 2-sentence critique of code quality and complexity understanding>",
  "scoreDelta": {{
    "coding": <-2 to +3 integer based on code quality + optimality>,
    "complexity_awareness": <-3 to +3 integer based on accuracy of complexity answer>
  }}
}}

Be strict:
- O(n) and O(2n) are the same — accept either.
- O(n^2) and O(n*n) are the same — accept either.
- O(n^3) is cubic time — accept O(n^3), O(n*n*n), O(n3).
- For graph problems: O(V+E) is BFS/DFS traversal. O(E log V) is Dijkstra. O(V^2) is Floyd-Warshall naive.
  Accept all standard graph notations (V+E, E+V, vertices+edges).
- If they state O(n log n) but code is O(n^2), that is WRONG.
- O(n^3) should never be marked as optimal unless the problem has no known better solution.
- If optimal solution exists and they used a worse one, isOptimal=false.

Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.1, max_tokens=768)

    try:
        return json.loads(result)
    except Exception:
        return {
            "actualTimeComplexity":   "Unknown",
            "actualSpaceComplexity":  "Unknown",
            "timeComplexityCorrect":  False,
            "spaceComplexityCorrect": False,
            "isOptimal":              False,
            "feedback":               "Complexity analysis failed.",
            "critique":               "Could not analyze code.",
            "scoreDelta":             {"coding": 0, "complexity_awareness": -1},
        }


def _update_scores(session_id: str, score_delta: dict, session: dict):
    if not score_delta:
        return

    current_scores = dict(session.get("scores", {}))
    update_parts   = []
    expr_values    = {}

    for key in ["coding", "complexity_awareness"]:
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
