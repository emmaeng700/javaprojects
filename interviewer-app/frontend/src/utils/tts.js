/**
 * Text-to-Speech utility
 *
 * Primary:  ElevenLabs API (high-quality, natural voice)
 * Fallback: Browser SpeechSynthesis (picks best available voice)
 *
 * Set VITE_ELEVENLABS_API_KEY in .env.local to enable ElevenLabs.
 * Set VITE_ELEVENLABS_VOICE_ID to a voice ID (default: Adam — deep, professional).
 */

const ELEVENLABS_KEY     = import.meta.env.VITE_ELEVENLABS_API_KEY;
const ELEVENLABS_VOICE   = import.meta.env.VITE_ELEVENLABS_VOICE_ID || "pNInz6obpgDQGcFmaJgB"; // Adam

let currentAudio = null;

/**
 * Speak text. Returns a promise that resolves when audio finishes (or fails).
 * onStart / onEnd callbacks optional.
 */
export async function speakText(text, { onStart, onEnd } = {}) {
  // Stop anything currently playing
  stopSpeaking();

  if (ELEVENLABS_KEY) {
    try {
      await speakElevenLabs(text, { onStart, onEnd });
      return;
    } catch (e) {
      console.warn("ElevenLabs TTS failed, falling back to browser:", e.message);
    }
  }

  speakBrowser(text, { onStart, onEnd });
}

export function stopSpeaking() {
  if (currentAudio) {
    currentAudio.pause();
    currentAudio.src = "";
    currentAudio = null;
  }
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel();
  }
}

// ── ElevenLabs ────────────────────────────────────────────────────────────────

async function speakElevenLabs(text, { onStart, onEnd } = {}) {
  const res = await fetch(
    `https://api.elevenlabs.io/v1/text-to-speech/${ELEVENLABS_VOICE}/stream`,
    {
      method: "POST",
      headers: {
        "xi-api-key":   ELEVENLABS_KEY,
        "Content-Type": "application/json",
        "Accept":       "audio/mpeg",
      },
      body: JSON.stringify({
        text,
        model_id: "eleven_turbo_v2",
        voice_settings: {
          stability:        0.55,
          similarity_boost: 0.80,
          style:            0.20,
          use_speaker_boost: true,
        },
      }),
    }
  );

  if (!res.ok) throw new Error(`ElevenLabs ${res.status}`);

  const blob = await res.blob();
  const url  = URL.createObjectURL(blob);
  const audio = new Audio(url);
  currentAudio = audio;

  return new Promise((resolve) => {
    audio.onplay  = () => onStart?.();
    audio.onended = () => { URL.revokeObjectURL(url); onEnd?.(); resolve(); };
    audio.onerror = () => { URL.revokeObjectURL(url); onEnd?.(); resolve(); };
    audio.play().catch(resolve);
  });
}

// ── Browser fallback ──────────────────────────────────────────────────────────

function speakBrowser(text, { onStart, onEnd } = {}) {
  if (!window.speechSynthesis) { onEnd?.(); return; }

  const utt = new SpeechSynthesisUtterance(text);
  utt.rate   = 0.92;
  utt.pitch  = 0.85;
  utt.volume = 1;

  const pickVoice = () => {
    const voices = window.speechSynthesis.getVoices();
    // Priority order: Google US English > Alex (macOS) > Daniel (UK) > any en-US
    return (
      voices.find(v => v.name === "Google US English") ||
      voices.find(v => v.name === "Alex") ||
      voices.find(v => v.name === "Daniel") ||
      voices.find(v => v.lang === "en-US" && !v.localService) ||
      voices.find(v => v.lang === "en-US") ||
      voices[0]
    );
  };

  const doSpeak = () => {
    const voice = pickVoice();
    if (voice) utt.voice = voice;
    utt.onstart = () => onStart?.();
    utt.onend   = () => onEnd?.();
    utt.onerror = () => onEnd?.();
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utt);
  };

  if (window.speechSynthesis.getVoices().length === 0) {
    window.speechSynthesis.addEventListener("voiceschanged", doSpeak, { once: true });
  } else {
    doSpeak();
  }
}
