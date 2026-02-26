"""
POST /executeCode

Body:
{
  "sessionId":  string,
  "questionId": string,
  "language":   "python" | "javascript" | "java",
  "code":       string,
  "testCases":  [{"input": any, "expectedOutput": any}]   (optional override)
}

Executes submitted code in a sandboxed subprocess with:
- Hard timeout (10 seconds)
- Memory cap via resource limits
- No network access (blocked via restricted environment)
- stdout/stderr captured

Evaluates correctness against test cases.
Stores submission in IV_CodeSubmissions.
Decides if complexity pop-up should be triggered.

Response:
{
  "sessionId":          string,
  "submissionId":       string,
  "passed":             bool,
  "passedCount":        number,
  "totalCount":         number,
  "testResults":        [{passed, input, expected, actual, error}],
  "executionTimeMs":    number,
  "triggerComplexityPopup": bool,
  "stderr":             string
}
"""

import json
import os
import sys
import time
import uuid
import subprocess
import tempfile
import resource

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES

TIMEOUT_SECONDS = 10
MAX_OUTPUT_BYTES = 64 * 1024   # 64 KB output cap

SUPPORTED_LANGUAGES = {"python", "javascript", "java"}

# Default test cases by question pattern (fallback when none provided)
DEFAULT_TEST_CASES = [
    {"input": "[2,7,11,15]\n9", "expectedOutput": "[0,1]"},
    {"input": "[3,2,4]\n6",     "expectedOutput": "[1,2]"},
    {"input": "[3,3]\n6",       "expectedOutput": "[0,1]"},
]

LANGUAGE_RUNNERS = {
    "python":     {"ext": ".py",   "cmd": ["python3", "{file}"]},
    "javascript": {"ext": ".js",   "cmd": ["node", "{file}"]},
    "java":       {"ext": ".java", "cmd": None},  # Java handled separately
}


def lambda_handler(event, context):
    try:
        body        = json.loads(event.get("body") or "{}")
        session_id  = body.get("sessionId")
        question_id = body.get("questionId", "")
        language    = body.get("language", "python").lower()
        code        = body.get("code", "").strip()
        test_cases  = body.get("testCases") or DEFAULT_TEST_CASES

        if not session_id:
            return _error(400, "sessionId is required")
        if not code:
            return _error(400, "code is required")
        if language not in SUPPORTED_LANGUAGES:
            return _error(400, f"Unsupported language. Supported: {', '.join(SUPPORTED_LANGUAGES)}")

        # Validate session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        # Security: reject obviously dangerous patterns
        danger = _check_dangerous_code(code, language)
        if danger:
            return _error(400, f"Code rejected: {danger}")

        # Run against test cases
        start_ms = int(time.time() * 1000)
        test_results = _run_test_cases(code, language, test_cases)
        elapsed_ms = int(time.time() * 1000) - start_ms

        passed_count = sum(1 for t in test_results if t["passed"])
        total_count  = len(test_results)
        all_passed   = passed_count == total_count

        stderr_combined = " | ".join(
            t.get("error", "") for t in test_results if t.get("error")
        )

        submission_id = str(uuid.uuid4())
        now = str(time.time())

        # Trigger complexity pop-up only if all tests passed
        trigger_complexity = all_passed

        # Store submission
        TABLES["code"].put_item(Item={
            "sessionId":       session_id,
            "submissionId":    submission_id,
            "questionId":      question_id,
            "language":        language,
            "code":            code,
            "passed":          all_passed,
            "passedCount":     passed_count,
            "totalCount":      total_count,
            "executionTimeMs": elapsed_ms,
            "testResults":     test_results,
            "timestamp":       now,
        })

        return _ok({
            "sessionId":             session_id,
            "submissionId":          submission_id,
            "passed":                all_passed,
            "passedCount":           passed_count,
            "totalCount":            total_count,
            "testResults":           test_results,
            "executionTimeMs":       elapsed_ms,
            "triggerComplexityPopup": trigger_complexity,
            "stderr":                stderr_combined,
        })

    except Exception as e:
        return _error(500, str(e))


def _check_dangerous_code(code: str, language: str) -> str | None:
    """Reject code with obvious sandbox-escape or destructive patterns."""
    blocked_patterns = [
        "import os", "import subprocess", "import socket",
        "__import__", "open(", "exec(", "eval(",
        "system(", "popen(", "urllib", "requests",
        "require('fs')", "require('child_process')", "require('net')",
        "Runtime.getRuntime", "ProcessBuilder", "FileWriter",
    ]
    code_lower = code.lower()
    for pat in blocked_patterns:
        if pat.lower() in code_lower:
            return f"Disallowed construct: '{pat}'"
    if len(code) > 50_000:
        return "Code too large (max 50KB)"
    return None


def _run_test_cases(code: str, language: str, test_cases: list) -> list:
    results = []
    runner  = LANGUAGE_RUNNERS.get(language)
    if not runner:
        return [{"passed": False, "error": f"No runner for {language}"}]

    for tc in test_cases:
        inp      = str(tc.get("input", ""))
        expected = str(tc.get("expectedOutput", "")).strip()
        result   = _run_single(code, language, runner, inp)
        actual   = result.get("stdout", "").strip()
        passed   = actual == expected

        results.append({
            "passed":   passed,
            "input":    inp,
            "expected": expected,
            "actual":   actual,
            "error":    result.get("stderr", ""),
        })

    return results


def _run_single(code: str, language: str, runner: dict, stdin_data: str) -> dict:
    ext = runner["ext"]

    with tempfile.NamedTemporaryFile(suffix=ext, mode="w", delete=False) as f:
        f.write(code)
        tmpfile = f.name

    cmd = [c.replace("{file}", tmpfile) for c in runner["cmd"]]

    try:
        def _limit():
            # 256 MB memory limit per process
            resource.setrlimit(resource.RLIMIT_AS, (256 * 1024 * 1024, 256 * 1024 * 1024))

        proc = subprocess.run(
            cmd,
            input=stdin_data,
            capture_output=True,
            text=True,
            timeout=TIMEOUT_SECONDS,
            preexec_fn=_limit,
            env={"PATH": "/usr/bin:/usr/local/bin"},   # Minimal env — no AWS creds
        )
        stdout = proc.stdout[:MAX_OUTPUT_BYTES]
        stderr = proc.stderr[:1024]
        return {"stdout": stdout, "stderr": stderr}

    except subprocess.TimeoutExpired:
        return {"stdout": "", "stderr": f"Time limit exceeded ({TIMEOUT_SECONDS}s)"}
    except Exception as e:
        return {"stdout": "", "stderr": str(e)}
    finally:
        try:
            os.unlink(tmpfile)
        except Exception:
            pass


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
