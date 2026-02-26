"""
POST /parseResume

Body:
{
  "sessionId": string,
  "resumeS3Key": string   (S3 key of uploaded resume PDF/text)
}

Fetches resume from S3, sends to Gemini to extract structured profile.
Stores parsed profile in IV_ParsedKnowledgeFiles.

Response:
{
  "sessionId": string,
  "profile": {
    "name": string,
    "summary": string,
    "experience": [...],
    "projects": [...],
    "skills": [...],
    "education": [...],
    "inflated_claims": [...],   (Gemini-flagged suspiciously strong claims)
    "deep_probe_targets": [...] (top 3 items to drill hard on)
  }
}
"""

import json
import os
import sys
import boto3
import urllib.request

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES, session as aws_session

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
GEMINI_URL = (
    "https://generativelanguage.googleapis.com/v1beta/models/"
    "gemini-1.5-flash:generateContent?key=" + (GEMINI_API_KEY or "")
)

AWS_REGION = os.environ.get("IV_AWS_REGION", "us-east-1")
IV_ACCESS_KEY = os.environ.get("IV_AWS_ACCESS_KEY_ID")
IV_SECRET_KEY = os.environ.get("IV_AWS_SECRET_ACCESS_KEY")

s3 = aws_session.client("s3")
BUCKET = "interviewverse-uploads"


def lambda_handler(event, context):
    try:
        body = json.loads(event.get("body") or "{}")
        session_id = body.get("sessionId")
        resume_s3_key = body.get("resumeS3Key")

        if not session_id:
            return _error(400, "sessionId is required")
        if not resume_s3_key:
            return _error(400, "resumeS3Key is required")

        # Fetch session to validate
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        # Fetch resume text from S3
        try:
            s3_obj = s3.get_object(Bucket=BUCKET, Key=resume_s3_key)
            resume_text = s3_obj["Body"].read().decode("utf-8", errors="replace")
        except Exception as e:
            return _error(400, f"Could not fetch resume from S3: {e}")

        # Truncate to ~8000 chars to stay within Gemini context
        resume_text = resume_text[:8000]

        interview_type = session.get("interviewType", "technical")

        # Call Gemini to parse and analyze
        profile = _parse_resume_with_gemini(resume_text, interview_type)

        # Store in IV_ParsedKnowledgeFiles
        TABLES["knowledge"].put_item(Item={
            "sessionId": session_id,
            "type": "resume_profile",
            "profile": profile,
            "resumeS3Key": resume_s3_key,
        })

        # Also update the session with resumeS3Key if not set
        TABLES["sessions"].update_item(
            Key={"sessionId": session_id},
            UpdateExpression="SET resumeS3Key = :k",
            ExpressionAttributeValues={":k": resume_s3_key},
        )

        return _ok({"sessionId": session_id, "profile": profile})

    except Exception as e:
        return _error(500, str(e))


def _parse_resume_with_gemini(resume_text: str, interview_type: str) -> dict:
    prompt = f"""You are a FAANG senior technical interviewer analyzing a candidate's resume.

Interview type: {interview_type}

Resume:
{resume_text}

Extract and return a JSON object with these exact fields:
{{
  "name": "candidate full name or Unknown",
  "summary": "2-sentence professional summary",
  "experience": [
    {{
      "company": "...",
      "role": "...",
      "duration": "...",
      "key_claims": ["claim1", "claim2"],
      "metrics_present": true/false
    }}
  ],
  "projects": [
    {{
      "name": "...",
      "tech_stack": ["..."],
      "claim": "one-line description",
      "architecture_depth": "shallow|moderate|deep"
    }}
  ],
  "skills": ["skill1", "skill2"],
  "education": [{{"school": "...", "degree": "...", "year": "..."}}],
  "inflated_claims": ["claim that seems exaggerated or unverifiable"],
  "deep_probe_targets": [
    {{
      "area": "what to probe (e.g. specific project or claim)",
      "reason": "why this deserves deep drilling",
      "suggested_question": "a sharp follow-up question"
    }}
  ]
}}

Be aggressive in identifying inflated claims. Flag anything with no metrics, vague ownership, or buzzword overuse.
Return ONLY the JSON object, no markdown.
"""

    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"temperature": 0.2, "maxOutputTokens": 2048},
    }

    req = urllib.request.Request(
        GEMINI_URL,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            result = json.loads(resp.read().decode())
            text = result["candidates"][0]["content"]["parts"][0]["text"].strip()
            # Strip markdown code fences if present
            if text.startswith("```"):
                text = text.split("```")[1]
                if text.startswith("json"):
                    text = text[4:]
            return json.loads(text)
    except Exception as e:
        # Return minimal profile on Gemini failure
        return {
            "name": "Unknown",
            "summary": "Resume parsing failed.",
            "experience": [],
            "projects": [],
            "skills": [],
            "education": [],
            "inflated_claims": [],
            "deep_probe_targets": [],
            "parse_error": str(e),
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
