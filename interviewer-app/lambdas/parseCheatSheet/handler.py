"""
POST /parseCheatSheet

Practice mode only. Allows candidate to upload a cheat sheet / study notes
(algorithms, system design patterns, behavioral frameworks).

Parses the file, stores structured knowledge in IV_ParsedKnowledgeFiles,
and returns a summary of what was indexed.

Body:
{
  "sessionId":   string,
  "s3Key":       string,   (S3 key of uploaded file)
  "fileType":    "algorithms" | "system_design" | "behavioral" | "general"
}

Gemini parses the content and extracts:
- Key concepts covered
- Algorithm patterns listed
- System design patterns listed
- Behavioral frameworks (STAR examples, etc.)

The parsed content is then used by generateQuestion to:
- Tailor questions around the candidate's stated knowledge
- Probe whether they actually understand what they listed
- In real mode: this endpoint is BLOCKED

Response:
{
  "sessionId":    string,
  "fileType":     string,
  "keyConcepts":  [string],
  "algorithms":   [string],
  "designPatterns": [string],
  "behavioralFrameworks": [string],
  "summary":      string,
  "probeTargets": [string]  (areas to test them on from this sheet)
}
"""

import json
import os
import sys
import time

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES
from gemini import call_gemini

S3_BUCKET = "interviewverse-uploads"


def lambda_handler(event, context):
    try:
        body       = json.loads(event.get("body") or "{}")
        session_id = body.get("sessionId")
        s3_key     = body.get("s3Key")
        file_type  = body.get("fileType", "general")

        if not session_id:
            return _error(400, "sessionId is required")
        if not s3_key:
            return _error(400, "s3Key is required")

        # Fetch session and enforce practice-mode-only
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        if session.get("mode") != "practice":
            return _error(403, "Cheat sheet upload is only available in practice mode")

        if session.get("status") == "completed":
            return _error(400, "Session already completed")

        # Fetch file from S3
        from db import session as aws_session
        s3 = aws_session.client("s3")

        try:
            obj      = s3.get_object(Bucket=S3_BUCKET, Key=s3_key)
            content  = obj["Body"].read().decode("utf-8", errors="replace")[:6000]
        except Exception as e:
            return _error(400, f"Could not fetch file from S3: {e}")

        # Parse with Gemini
        parsed = _parse_cheat_sheet_with_gemini(content, file_type)

        key_concepts    = parsed.get("keyConcepts", [])
        algorithms      = parsed.get("algorithms", [])
        design_patterns = parsed.get("designPatterns", [])
        behavioral      = parsed.get("behavioralFrameworks", [])
        summary         = parsed.get("summary", "")
        probe_targets   = parsed.get("probeTargets", [])

        now = str(time.time())

        # Store in IV_ParsedKnowledgeFiles
        TABLES["knowledge"].put_item(Item={
            "sessionId":           session_id,
            "type":                f"cheat_sheet_{file_type}",
            "s3Key":               s3_key,
            "fileType":            file_type,
            "keyConcepts":         key_concepts,
            "algorithms":          algorithms,
            "designPatterns":      design_patterns,
            "behavioralFrameworks": behavioral,
            "summary":             summary,
            "probeTargets":        probe_targets,
            "uploadedAt":          now,
        })

        return _ok({
            "sessionId":            session_id,
            "fileType":             file_type,
            "keyConcepts":          key_concepts,
            "algorithms":           algorithms,
            "designPatterns":       design_patterns,
            "behavioralFrameworks": behavioral,
            "summary":              summary,
            "probeTargets":         probe_targets,
        })

    except Exception as e:
        return _error(500, str(e))


def _parse_cheat_sheet_with_gemini(content: str, file_type: str) -> dict:
    type_instructions = {
        "algorithms": (
            "Focus on: algorithm patterns, data structures, time/space complexities listed, "
            "problem categories (DP, graphs, sorting, etc.)"
        ),
        "system_design": (
            "Focus on: system design patterns, architectural components, scalability techniques, "
            "database choices, caching strategies, consistency models"
        ),
        "behavioral": (
            "Focus on: STAR framework examples, leadership principles, specific stories or examples, "
            "metrics or outcomes mentioned"
        ),
        "general": (
            "Extract all technical concepts, algorithms, patterns, frameworks, and any structured knowledge"
        ),
    }

    instructions = type_instructions.get(file_type, type_instructions["general"])

    prompt = f"""You are analyzing a candidate's study notes / cheat sheet for a FAANG interview.

File type: {file_type}
{instructions}

Content:
{content}

Extract structured knowledge and identify areas to probe the candidate on.
Probe targets should be things they LISTED but might not fully understand in depth.

Return ONLY this JSON:
{{
  "keyConcepts": ["<concept 1>", "<concept 2>"],
  "algorithms": ["<algorithm or pattern 1>", "<algorithm 2>"],
  "designPatterns": ["<design pattern 1>", "<pattern 2>"],
  "behavioralFrameworks": ["<framework or story 1>"],
  "summary": "<2-sentence summary of what this cheat sheet covers>",
  "probeTargets": [
    "<specific thing to probe — e.g. 'They listed LRU cache — ask them to implement it'>",
    "<another probe target>"
  ]
}}

Return ONLY the JSON, no markdown.
"""

    result = call_gemini(prompt, temperature=0.2, max_tokens=1024)
    try:
        return json.loads(result)
    except Exception:
        return {
            "keyConcepts": [],
            "algorithms": [],
            "designPatterns": [],
            "behavioralFrameworks": [],
            "summary": "Could not parse cheat sheet.",
            "probeTargets": [],
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
