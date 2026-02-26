"""
POST /generateQuestion

Body:
{
  "sessionId": string,
  "section":   string   ("resume_drill" | "behavioral" | "coding" | "design" | "intro")
}

Fetches session + parsed resume profile, calls Gemini to generate
the next contextually appropriate question for the current section.

Stores message in IV_SessionMessages.

Response:
{
  "sessionId":    string,
  "section":      string,
  "questionId":   string,
  "question":     string,
  "questionType": "opener" | "drill" | "follow_up" | "challenge" | "wrap_up",
  "context":      string   (internal reason — shown in practice mode only)
}
"""

import json
import os
import sys
import time
import uuid
import urllib.request

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
GEMINI_URL = (
    "https://generativelanguage.googleapis.com/v1beta/models/"
    "gemini-1.5-flash:generateContent?key=" + (GEMINI_API_KEY or "")
)

# ── Question Bank (seed material — AI generates its own variants) ─────────────

QUESTION_BANK = {
    "behavioral": {
        "Self-Introduction": [
            "Tell me about yourself",
            "What's the accomplishment you're most proud of?",
        ],
        "Conflict & Disagreement": [
            "Tell me about a time you had a disagreement with your manager",
            "Tell me about a situation when you had a conflict with a teammate",
            "Tell me about a time you disagreed with a colleague",
            "Tell me about a time when you had a different opinion than the rest of the team",
        ],
        "Failure & Mistakes": [
            "Tell me about a time you failed and how you dealt with it",
            "Tell me about a time when you missed a deadline",
            "Describe a time when you took a big risk and it failed",
        ],
        "Leadership & Motivation": [
            "Describe a time when you led a team and the outcome",
            "Describe a time you needed to motivate individuals or encourage collaboration",
        ],
        "Pressure & Stress": [
            "Tell me about a time you worked well under pressure",
            "Describe a time when your workload was heavy and you had competing deadlines",
        ],
        "Decision-Making": [
            "Provide an example of a difficult decision you made",
            "Tell me about a time when you had multiple possible solutions to a problem",
            "Assume you're tasked to design a system; how would you resolve ambiguity?",
        ],
        "Going Above & Beyond": [
            "Describe a time you went above and beyond project requirements",
            "Describe a situation where you saw a problem and took initiative to correct it",
        ],
        "Feedback & Criticism": [
            "Describe a time you received tough or critical feedback",
            "Describe a time you had to give someone difficult feedback",
        ],
        "Technical Problem-Solving": [
            "Tell me about a time you had to solve a complex technical problem",
            "What's the biggest technical challenge you've worked on?",
            "Give an example of debugging a particularly challenging technical issue",
        ],
        "Teamwork & Collaboration": [
            "Describe a time you worked as part of a team to achieve a common goal",
            "Give an example of working with a team from a different department",
        ],
        "Adaptation & Change": [
            "Tell me about a time you had to adapt to rapidly changing project requirements",
            "Tell me about a time you went out of your comfort zone",
        ],
        "Prioritization & Time Management": [
            "Tell me about a time you had to prioritize tasks quickly",
            "How do you prioritize your workload when everything feels urgent?",
        ],
        "Communication": [
            "Describe a time you had to explain a complex technical concept to a non-technical person",
        ],
        "Handling Uncertainty": [
            "How do you handle a situation where you don't know the answer?",
            "What do you do when assigned a task you have no experience with?",
        ],
    },

    "coding": {
        "Graph": [
            "Number of Islands", "Max Area of Island", "Island Perimeter",
            "Word Ladder", "Redundant Connection", "Find Eventual Safe States",
            "Flood Fill", "Surrounded Regions", "01 Matrix",
            "Sliding Puzzle", "Critical Connections in a Network",
        ],
        "Binary Search": [
            "Find First and Last Position of Element in Sorted Array",
            "Search Insert Position", "Sqrt(x)",
            "Capacity to Ship Packages Within D Days",
            "Peak Index in a Mountain Array",
            "Find Minimum in Rotated Sorted Array",
            "Guess Number Higher or Lower", "First Bad Version",
        ],
        "Dynamic Programming": [
            "Longest Increasing Subsequence", "Longest Common Subsequence",
            "Longest Palindromic Substring", "Edit Distance",
            "Coin Change", "Burst Balloons", "Remove Boxes",
            "Minimum Cost to Merge Stones", "Tallest Billboard",
        ],
        "Binary Tree": [
            "Binary Tree Right Side View", "Vertical Order Traversal",
            "Validate BST", "Balanced Binary Tree",
            "Count Complete Tree Nodes", "Lowest Common Ancestor",
            "Construct Binary Tree from Preorder and Inorder Traversal",
        ],
        "String": [
            "Edit Distance", "Remove Duplicate Letters",
            "Word Ladder II", "Remove Comments",
            "Find Closest Palindrome",
        ],
        "Stack": [
            "Valid Parentheses", "Min Stack",
            "Largest Rectangle in Histogram", "Trapping Rain Water",
        ],
        "Linked List": [
            "Reverse Linked List", "Merge Two Sorted Lists",
            "Detect Cycle in Linked List", "LRU Cache",
        ],
        "Heap/Priority Queue": [
            "Kth Largest Element in an Array", "Top K Frequent Elements",
            "Merge K Sorted Lists", "Find Median from Data Stream",
        ],
        "Trie": [
            "Implement Trie", "Word Search II", "Design Add and Search Words",
        ],
        "Union Find": [
            "Number of Connected Components in an Undirected Graph",
            "Accounts Merge",
        ],
    },

    "system_design": {
        "Data & Storage": [
            "Design a key-value store like Redis",
            "Design a distributed cache",
            "Design a distributed message queue",
            "Design a distributed counter",
            "Design a unique ID generator at scale",
            "Design a distributed hashmap",
            "Design data synchronization like Dropbox",
        ],
        "Large-Scale Infrastructure": [
            "Design a web crawler for 1 billion URLs",
            "Design a health monitoring system for 10,000 nodes",
            "Design a system to distribute TB of data to 10,000 nodes",
            "Design a system to find duplicate files across 1000 servers",
        ],
        "Social & Communication": [
            "Design Twitter's news feed and timeline",
            "Design a real-time chat system like Slack",
            "Design a push notification service",
            "Design Instagram's photo sharing backend",
        ],
        "APIs & Services": [
            "Design an API rate limiter",
            "Design a URL shortener like bit.ly",
            "Design a logging and metrics aggregation system",
            "Design a leaderboard ranking service",
            "Design a delayed task queue",
            "Design a spam filter for a large email platform",
        ],
        "Product Systems": [
            "Design Uber's backend",
            "Design Gmail or Google Docs",
            "Design Yelp's location-based search",
            "Design Amazon's shopping cart and recommendation engine",
            "Design a flight booking service",
            "Design a payment processor",
            "Design Pastebin",
        ],
    },
}

# Openers by section
SECTION_OPENERS = {
    "intro":        "Tell me about yourself.",
    "resume_drill": "Tell me about yourself.",
    "behavioral":   "Tell me about a time you dealt with a significant technical challenge.",
    "coding":       None,   # Gemini generates coding questions
    "design":       None,   # Gemini generates design questions
}


def lambda_handler(event, context):
    try:
        body = json.loads(event.get("body") or "{}")
        session_id = body.get("sessionId")
        section    = body.get("section")

        if not session_id:
            return _error(400, "sessionId is required")
        if not section:
            return _error(400, "section is required")

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        interview_type = session.get("interviewType", "technical")
        mode           = session.get("mode", "practice")

        # Fetch parsed resume profile if available
        profile = _get_profile(session_id)

        # Fetch prior messages for context (last 10)
        prior_messages = _get_prior_messages(session_id, limit=10)

        # Determine question type
        question_count = len([m for m in prior_messages if m.get("role") == "interviewer"])
        if question_count == 0:
            question_type = "opener"
        elif question_count >= 8:
            question_type = "wrap_up"
        else:
            question_type = "drill" if section in ("resume_drill", "behavioral") else "challenge"

        # Generate question
        if question_type == "opener" and section in SECTION_OPENERS and SECTION_OPENERS[section]:
            question = SECTION_OPENERS[section]
            ctx = "Standard opener — evaluate clarity, structure, and self-awareness."
        else:
            question, ctx = _generate_with_gemini(
                interview_type, section, mode, profile, prior_messages, question_type
            )

        question_id = str(uuid.uuid4())
        now = str(time.time())

        # Store in IV_SessionMessages
        TABLES["messages"].put_item(Item={
            "sessionId":    session_id,
            "messageId":    question_id,
            "role":         "interviewer",
            "section":      section,
            "questionType": question_type,
            "content":      question,
            "context":      ctx,
            "timestamp":    now,
        })

        response_data = {
            "sessionId":    session_id,
            "section":      section,
            "questionId":   question_id,
            "question":     question,
            "questionType": question_type,
        }

        # Only include internal context in practice mode
        if mode == "practice":
            response_data["context"] = ctx

        return _ok(response_data)

    except Exception as e:
        return _error(500, str(e))


def _get_profile(session_id: str) -> dict:
    try:
        resp = TABLES["knowledge"].get_item(Key={
            "sessionId": session_id,
            "type": "resume_profile",
        })
        item = resp.get("Item")
        return item.get("profile", {}) if item else {}
    except Exception:
        return {}


def _get_prior_messages(session_id: str, limit: int = 10) -> list:
    try:
        resp = TABLES["messages"].query(
            KeyConditionExpression="sessionId = :sid",
            ExpressionAttributeValues={":sid": session_id},
            ScanIndexForward=False,
            Limit=limit,
        )
        items = resp.get("Items", [])
        return list(reversed(items))
    except Exception:
        return []


def _generate_with_gemini(
    interview_type: str,
    section: str,
    mode: str,
    profile: dict,
    prior_messages: list,
    question_type: str,
) -> tuple:

    profile_summary = ""
    if profile:
        name = profile.get("name", "the candidate")
        probes = profile.get("deep_probe_targets", [])
        inflated = profile.get("inflated_claims", [])
        profile_summary = f"""
Candidate: {name}
Deep probe targets: {json.dumps(probes[:3])}
Inflated claims to challenge: {json.dumps(inflated[:3])}
"""

    history_text = ""
    for m in prior_messages[-6:]:
        role = "Interviewer" if m["role"] == "interviewer" else "Candidate"
        history_text += f"{role}: {m['content']}\n"

    section_instructions = {
        "resume_drill": (
            "You are a FAANG interviewer doing a 15-minute resume drill. "
            "Ask sharp, specific questions about their experience, architecture decisions, "
            "metrics, ownership, and technical depth. "
            "Challenge inflated claims directly. "
            "Do NOT ask generic questions."
        ),
        "behavioral": (
            "You are a FAANG bar-raiser. Ask behavioral questions using STAR format expectations. "
            "After any answer, follow up on missing metrics, vague ownership, or weak impact. "
            "Be aggressive — push for specifics."
        ),
        "coding": (
            "Generate a LeetCode-medium to hard coding question relevant to backend/systems. "
            "Format: problem statement only, no solution. Include input/output examples."
        ),
        "design": (
            "Generate a FAANG-level system design question. "
            "Focus on distributed systems, scalability, and trade-offs. "
            "One sentence prompt only."
        ),
        "intro": (
            "Ask the candidate to introduce themselves. "
            "Use: 'Tell me about yourself and what you've been working on recently.'"
        ),
    }

    instructions = section_instructions.get(section, "Ask a relevant technical question.")

    wrap_up_note = ""
    if question_type == "wrap_up":
        wrap_up_note = "This is the final question before section ends. Make it a synthesis question."

    # Build question bank seed for this section
    bank_hint = ""
    if section == "behavioral":
        bank = QUESTION_BANK["behavioral"]
        sample = []
        import random
        for cat, qs in bank.items():
            sample.append(f"[{cat}] {random.choice(qs)}")
        bank_hint = (
            "Question bank (use as inspiration — generate your OWN variant, do NOT copy verbatim):\n"
            + "\n".join(sample[:8])
        )
    elif section == "coding":
        bank = QUESTION_BANK["coding"]
        import random
        cat = random.choice(list(bank.keys()))
        problem = random.choice(bank[cat])
        bank_hint = (
            f"Seed problem category: {cat} — inspired by '{problem}'. "
            "Generate your own original problem in this category. Do NOT use the exact problem name."
        )
    elif section == "design":
        bank = QUESTION_BANK["system_design"]
        import random
        cat = random.choice(list(bank.keys()))
        topic = random.choice(bank[cat])
        bank_hint = (
            f"Seed topic: {cat} — '{topic}'. "
            "Generate your own system design question inspired by this area. Add scale/constraint context."
        )

    prompt = f"""You are a senior FAANG interviewer conducting a {interview_type} interview.
Section: {section}
Mode: {mode} ({"show real-time critique" if mode == "practice" else "no live feedback, high pressure"})
Question type: {question_type}

{instructions}
{wrap_up_note}

{profile_summary}

{bank_hint}

Conversation so far:
{history_text}

Generate the NEXT interviewer question. Be direct, sharp, and specific.

Return a JSON object:
{{
  "question": "the exact question to ask",
  "context": "1-sentence internal reason — what weakness or area this probes"
}}

Return ONLY the JSON, no markdown.
"""

    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"temperature": 0.7, "maxOutputTokens": 512},
    }

    req = urllib.request.Request(
        GEMINI_URL,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            result = json.loads(resp.read().decode())
            text = result["candidates"][0]["content"]["parts"][0]["text"].strip()
            if text.startswith("```"):
                text = text.split("```")[1]
                if text.startswith("json"):
                    text = text[4:]
            parsed = json.loads(text)
            return parsed["question"], parsed.get("context", "")
    except Exception as e:
        fallback = {
            "resume_drill": "Walk me through the most technically complex project on your resume.",
            "behavioral":   "Tell me about a time you had to make a difficult technical decision under pressure.",
            "coding":       "Given an array of integers, find the two numbers that sum to a target value. Return their indices.",
            "design":       "Design a URL shortening service like bit.ly.",
            "intro":        "Tell me about yourself.",
        }
        return fallback.get(section, "Tell me about a recent technical challenge."), f"Fallback due to: {e}"


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
