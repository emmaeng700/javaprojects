"""
POST /processSpeech

Receives a finalized speech transcript chunk from the frontend
(after Web Speech API or Whisper produces a final result).

Handles:
- Appending transcript to the current answer buffer in DynamoDB
- Silence detection: if gap > threshold, marks answer as complete
- Multi-turn context tracking: links transcript chunks to current question
- Returns whether the answer is ready for evaluation

Body:
{
  "sessionId":       string,
  "questionId":      string,
  "transcript":      string,    (finalized speech chunk)
  "isFinal":         bool,      (true = end of utterance from speech API)
  "silenceMs":       number,    (milliseconds of silence detected before this chunk)
  "chunkIndex":      number     (0-based chunk counter for this answer)
}

Response:
{
  "sessionId":        string,
  "questionId":       string,
  "bufferedAnswer":   string,    (full accumulated answer so far)
  "wordCount":        number,
  "answerComplete":   bool,      (true when silence threshold hit or isFinal=true)
  "silenceDetected":  bool,
  "shouldEvaluate":   bool,      (true = frontend should call evaluateAnswer now)
  "interruptSignal":  bool,      (true = AI wants to interrupt right now)
  "interruptReason":  string | null
}
"""

import json
import os
import sys
import time

sys.path.append(os.path.join(os.path.dirname(__file__), "../shared"))
from db import TABLES

# Silence thresholds
SILENCE_ANSWER_COMPLETE_MS  = 3000   # 3s silence = answer is done
SILENCE_INTERRUPT_MS        = 8000   # 8s silence = AI may interrupt (coding only)
MIN_WORDS_TO_EVALUATE       = 10     # Don't evaluate one-word answers

# Ramble detection
RAMBLE_WORD_THRESHOLD = 250   # Flag if answer exceeds this without a clear point


def lambda_handler(event, context):
    try:
        body        = json.loads(event.get("body") or "{}")
        session_id  = body.get("sessionId")
        question_id = body.get("questionId", "")
        transcript  = body.get("transcript", "").strip()
        is_final    = bool(body.get("isFinal", False))
        silence_ms  = int(body.get("silenceMs", 0))
        chunk_index = int(body.get("chunkIndex", 0))

        if not session_id:
            return _error(400, "sessionId is required")
        if not transcript:
            return _ok(_empty_response(session_id, question_id))

        # Fetch session
        resp = TABLES["sessions"].get_item(Key={"sessionId": session_id})
        session = resp.get("Item")
        if not session:
            return _error(404, "Session not found")

        current_section = session.get("currentSection", "")

        # Fetch or create speech buffer for this question
        buffer_key = f"speech_buffer_{question_id or 'current'}"
        existing_buffer = _get_speech_buffer(session_id, buffer_key)
        prior_text      = existing_buffer.get("text", "")

        # Append new transcript chunk
        separator    = " " if prior_text and not prior_text.endswith(" ") else ""
        updated_text = (prior_text + separator + transcript).strip()
        word_count   = len(updated_text.split())

        # Detect silence thresholds
        silence_complete  = silence_ms >= SILENCE_ANSWER_COMPLETE_MS
        silence_interrupt = silence_ms >= SILENCE_INTERRUPT_MS and current_section == "coding"

        answer_complete = is_final or silence_complete
        should_evaluate = answer_complete and word_count >= MIN_WORDS_TO_EVALUATE

        # Ramble detection
        rambling = word_count > RAMBLE_WORD_THRESHOLD

        # Interrupt signal (coding section only — links to interrogateCode)
        interrupt_signal = silence_interrupt and not answer_complete
        interrupt_reason = None
        if interrupt_signal:
            interrupt_reason = f"Candidate paused for {silence_ms}ms during coding"

        # Store updated buffer
        _save_speech_buffer(session_id, buffer_key, {
            "text":         updated_text,
            "wordCount":    word_count,
            "questionId":   question_id,
            "chunkCount":   chunk_index + 1,
            "complete":     answer_complete,
            "rambling":     rambling,
            "updatedAt":    str(time.time()),
        })

        # If answer complete, also store the finalized transcript as a speech record
        if answer_complete and updated_text:
            _store_speech_record(session_id, question_id, updated_text,
                                 current_section, word_count, rambling)

        return _ok({
            "sessionId":       session_id,
            "questionId":      question_id,
            "bufferedAnswer":  updated_text,
            "wordCount":       word_count,
            "answerComplete":  answer_complete,
            "silenceDetected": silence_complete,
            "shouldEvaluate":  should_evaluate,
            "interruptSignal": interrupt_signal,
            "interruptReason": interrupt_reason,
            "rambling":        rambling,
        })

    except Exception as e:
        return _error(500, str(e))


def _get_speech_buffer(session_id: str, buffer_key: str) -> dict:
    try:
        resp = TABLES["knowledge"].get_item(
            Key={"sessionId": session_id, "type": buffer_key}
        )
        return resp.get("Item", {})
    except Exception:
        return {}


def _save_speech_buffer(session_id: str, buffer_key: str, data: dict):
    try:
        TABLES["knowledge"].put_item(Item={
            "sessionId": session_id,
            "type":      buffer_key,
            **data,
        })
    except Exception:
        pass


def _store_speech_record(session_id, question_id, text, section, word_count, rambling):
    try:
        import uuid
        TABLES["messages"].put_item(Item={
            "sessionId":        session_id,
            "messageId":        str(uuid.uuid4()),
            "role":             "candidate",
            "section":          section,
            "content":          text,
            "wordCount":        word_count,
            "rambling":         rambling,
            "linkedQuestionId": question_id,
            "messageType":      "speech_transcript",
            "timestamp":        str(time.time()),
        })
    except Exception:
        pass


def _empty_response(session_id, question_id):
    return {
        "sessionId":       session_id,
        "questionId":      question_id,
        "bufferedAnswer":  "",
        "wordCount":       0,
        "answerComplete":  False,
        "silenceDetected": False,
        "shouldEvaluate":  False,
        "interruptSignal": False,
        "interruptReason": None,
        "rambling":        False,
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
