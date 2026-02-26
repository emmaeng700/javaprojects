/**
 * InterviewVerse API client
 *
 * All Lambda endpoints go through API Gateway.
 * Base URL is set via VITE_API_BASE_URL environment variable.
 */

const BASE = import.meta.env.VITE_API_BASE_URL || "";

async function parseResponse(res) {
  const text = await res.text();
  if (!text) throw new Error(`Empty response from server (HTTP ${res.status})`);
  let data;
  try {
    data = JSON.parse(text);
  } catch {
    throw new Error(`Server returned non-JSON (HTTP ${res.status}): ${text.slice(0, 120)}`);
  }
  if (!res.ok) throw new Error(data.error || data.message || `HTTP ${res.status}`);
  return data;
}

async function post(path, body) {
  const res = await fetch(`${BASE}${path}`, {
    method:  "POST",
    headers: { "Content-Type": "application/json" },
    body:    JSON.stringify(body),
  });
  return parseResponse(res);
}

async function get(path, params = {}) {
  const qs  = new URLSearchParams(params).toString();
  const url = `${BASE}${path}${qs ? "?" + qs : ""}`;
  const res = await fetch(url);
  return parseResponse(res);
}

// ─── Session ──────────────────────────────────────────────────────────────────

export const startSession       = (body) => post("/startSession", body);
export const transitionSection  = (body) => post("/transitionSection", body);
export const endSession         = (body) => post("/endSession", body);

// ─── Resume ───────────────────────────────────────────────────────────────────

export const parseResume        = (body) => post("/parseResume", body);

// ─── Questions ────────────────────────────────────────────────────────────────

export const generateQuestion   = (body) => post("/generateQuestion", body);
export const evaluateAnswer     = (body) => post("/evaluateAnswer", body);
export const handleFollowUp     = (body) => post("/handleFollowUp", body);

// ─── Coding ───────────────────────────────────────────────────────────────────

export const executeCode              = (body) => post("/executeCode", body);
export const validateComplexityAnswer = (body) => post("/validateComplexityAnswer", body);
export const interrogateCode          = (body) => post("/interrogateCode", body);
export const escalateCodingQuestion   = (body) => post("/escalateCodingQuestion", body);

// ─── System Design ────────────────────────────────────────────────────────────

export const evaluateDesign     = (body) => post("/evaluateDesign", body);
export const stressTestDesign   = (body) => post("/stressTestDesign", body);

// ─── Mode & Cheat Sheet ───────────────────────────────────────────────────────

export const getModeContext     = (params) => get("/getModeContext", params);
export const parseCheatSheet    = (body)   => post("/parseCheatSheet", body);

// ─── Speech ───────────────────────────────────────────────────────────────────

export const processSpeech      = (body) => post("/processSpeech", body);

// ─── Evaluation ───────────────────────────────────────────────────────────────

export const getHiringEvaluation = (params) => get("/getHiringEvaluation", params);
