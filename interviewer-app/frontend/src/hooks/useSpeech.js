/**
 * useSpeech
 *
 * Manages the full speech lifecycle:
 * - Web Speech API live transcription
 * - Silence detection via audio level monitoring
 * - Chunk streaming to /processSpeech Lambda
 * - Returns buffered answer, completion state, and interrupt signals
 *
 * Usage:
 *   const {
 *     isListening, transcript, bufferedAnswer, wordCount,
 *     answerComplete, interruptSignal, rambling,
 *     startListening, stopListening, resetBuffer
 *   } = useSpeech({ sessionId, questionId, onAnswerComplete, onInterrupt })
 */

import { useState, useEffect, useRef, useCallback } from "react";
import { processSpeech } from "../utils/api";

const SILENCE_CHECK_INTERVAL_MS = 500;
const SILENCE_COMPLETE_MS       = 3000;
const SILENCE_INTERRUPT_MS      = 8000;

export function useSpeech({ sessionId, questionId, onAnswerComplete, onInterrupt }) {
  const [isListening,     setIsListening]     = useState(false);
  const [transcript,      setTranscript]      = useState("");   // live interim
  const [bufferedAnswer,  setBufferedAnswer]  = useState("");   // server-confirmed full text
  const [wordCount,       setWordCount]       = useState(0);
  const [answerComplete,  setAnswerComplete]  = useState(false);
  const [interruptSignal, setInterruptSignal] = useState(false);
  const [rambling,        setRambling]        = useState(false);
  const [error,           setError]           = useState(null);

  const recognitionRef  = useRef(null);
  const chunkIndexRef   = useRef(0);
  const lastSpeechRef   = useRef(Date.now());
  const silenceTimerRef = useRef(null);
  const audioCtxRef     = useRef(null);
  const analyserRef     = useRef(null);
  const micStreamRef    = useRef(null);

  // ─── Start listening ───────────────────────────────────────────────────────

  const startListening = useCallback(async () => {
    if (!("webkitSpeechRecognition" in window) && !("SpeechRecognition" in window)) {
      setError("Speech recognition not supported in this browser. Use Chrome.");
      return;
    }

    try {
      // Request mic for silence detection
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      micStreamRef.current = stream;
      _setupSilenceDetector(stream);
    } catch (e) {
      setError("Microphone access denied.");
      return;
    }

    const SpeechRecognition =
      window.SpeechRecognition || window.webkitSpeechRecognition;
    const recognition = new SpeechRecognition();

    recognition.continuous      = true;
    recognition.interimResults  = true;
    recognition.lang            = "en-US";
    recognition.maxAlternatives = 1;

    recognition.onstart = () => {
      setIsListening(true);
      setError(null);
      chunkIndexRef.current = 0;
      lastSpeechRef.current = Date.now();
    };

    recognition.onresult = (event) => {
      lastSpeechRef.current = Date.now();
      let interim = "";
      let finalChunk = "";

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i];
        if (result.isFinal) {
          finalChunk += result[0].transcript;
        } else {
          interim += result[0].transcript;
        }
      }

      if (interim) setTranscript(interim);

      if (finalChunk.trim()) {
        _sendChunk(finalChunk.trim(), true, 0);
        setTranscript("");
      }
    };

    recognition.onerror = (e) => {
      if (e.error !== "no-speech") {
        setError(`Speech error: ${e.error}`);
      }
    };

    recognition.onend = () => {
      // Auto-restart if we didn't intentionally stop
      if (isListening && recognitionRef.current) {
        try { recognitionRef.current.start(); } catch (_) {}
      }
    };

    recognitionRef.current = recognition;
    recognition.start();
    _startSilenceTimer();
  }, [sessionId, questionId, isListening]);

  // ─── Stop listening ─────────────────────────────────────────────────────────

  const stopListening = useCallback(() => {
    setIsListening(false);
    if (recognitionRef.current) {
      recognitionRef.current.stop();
      recognitionRef.current = null;
    }
    _cleanupAudio();
    _clearSilenceTimer();
  }, []);

  // ─── Reset buffer ───────────────────────────────────────────────────────────

  const resetBuffer = useCallback(() => {
    setTranscript("");
    setBufferedAnswer("");
    setWordCount(0);
    setAnswerComplete(false);
    setInterruptSignal(false);
    setRambling(false);
    chunkIndexRef.current = 0;
  }, []);

  // ─── Send chunk to Lambda ───────────────────────────────────────────────────

  const _sendChunk = useCallback(async (text, isFinal, silenceMs) => {
    try {
      const result = await processSpeech({
        sessionId,
        questionId,
        transcript:  text,
        isFinal,
        silenceMs,
        chunkIndex:  chunkIndexRef.current++,
      });

      setBufferedAnswer(result.bufferedAnswer);
      setWordCount(result.wordCount);
      setRambling(result.rambling);

      if (result.answerComplete) {
        setAnswerComplete(true);
        stopListening();
        onAnswerComplete?.(result.bufferedAnswer);
      }

      if (result.interruptSignal) {
        setInterruptSignal(true);
        onInterrupt?.(result.interruptReason);
      }
    } catch (e) {
      console.error("processSpeech error:", e);
    }
  }, [sessionId, questionId, onAnswerComplete, onInterrupt, stopListening]);

  // ─── Silence detection via AudioContext analyser ────────────────────────────

  const _setupSilenceDetector = (stream) => {
    try {
      const ctx      = new AudioContext();
      const analyser = ctx.createAnalyser();
      analyser.fftSize = 256;
      const source   = ctx.createMediaStreamSource(stream);
      source.connect(analyser);
      audioCtxRef.current  = ctx;
      analyserRef.current  = analyser;
    } catch (_) {}
  };

  const _startSilenceTimer = () => {
    _clearSilenceTimer();
    silenceTimerRef.current = setInterval(() => {
      const silenceMs = Date.now() - lastSpeechRef.current;

      // Check audio level — if mic is active and quiet, count as silence
      if (analyserRef.current) {
        const data = new Uint8Array(analyserRef.current.frequencyBinCount);
        analyserRef.current.getByteFrequencyData(data);
        const avg = data.reduce((a, b) => a + b, 0) / data.length;
        if (avg > 10) {
          // Audio detected — reset silence clock
          lastSpeechRef.current = Date.now();
          return;
        }
      }

      if (silenceMs >= SILENCE_COMPLETE_MS) {
        // Flush any remaining transcript as final
        if (transcript.trim()) {
          _sendChunk(transcript.trim(), true, silenceMs);
        } else if (bufferedAnswer && !answerComplete) {
          _sendChunk("", true, silenceMs);
        }
      } else if (silenceMs >= SILENCE_INTERRUPT_MS) {
        // Signal interrupt for coding section
        _sendChunk("", false, silenceMs);
      }
    }, SILENCE_CHECK_INTERVAL_MS);
  };

  const _clearSilenceTimer = () => {
    if (silenceTimerRef.current) {
      clearInterval(silenceTimerRef.current);
      silenceTimerRef.current = null;
    }
  };

  const _cleanupAudio = () => {
    if (micStreamRef.current) {
      micStreamRef.current.getTracks().forEach(t => t.stop());
      micStreamRef.current = null;
    }
    if (audioCtxRef.current) {
      audioCtxRef.current.close();
      audioCtxRef.current = null;
    }
    analyserRef.current = null;
  };

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      stopListening();
      _cleanupAudio();
      _clearSilenceTimer();
    };
  }, []);

  return {
    isListening,
    transcript,       // live interim text (not yet confirmed)
    bufferedAnswer,   // server-confirmed full answer
    wordCount,
    answerComplete,
    interruptSignal,
    rambling,
    error,
    startListening,
    stopListening,
    resetBuffer,
  };
}
