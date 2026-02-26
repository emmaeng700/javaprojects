"""
Shared Gemini API helper for all Lambda functions.
"""

import json
import os
import urllib.request

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "")
GEMINI_URL = (
    "https://generativelanguage.googleapis.com/v1beta/models/"
    "gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY
)


def call_gemini(prompt: str, temperature: float = 0.5, max_tokens: int = 1024) -> str:
    """
    Call Gemini and return the raw text response.
    Strips markdown code fences if present.
    Raises on HTTP or parsing errors.
    """
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "temperature": temperature,
            "maxOutputTokens": max_tokens,
        },
    }

    req = urllib.request.Request(
        GEMINI_URL,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    with urllib.request.urlopen(req, timeout=30) as resp:
        result = json.loads(resp.read().decode())

    text = result["candidates"][0]["content"]["parts"][0]["text"].strip()

    # Strip markdown code fences
    if text.startswith("```"):
        parts = text.split("```")
        text = parts[1] if len(parts) > 1 else text
        if text.startswith("json"):
            text = text[4:]

    return text.strip()
