import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/startSession":             { target: "http://localhost:4000", changeOrigin: true },
      "/transitionSection":        { target: "http://localhost:4000", changeOrigin: true },
      "/endSession":               { target: "http://localhost:4000", changeOrigin: true },
      "/parseResume":              { target: "http://localhost:4000", changeOrigin: true },
      "/generateQuestion":         { target: "http://localhost:4000", changeOrigin: true },
      "/evaluateAnswer":           { target: "http://localhost:4000", changeOrigin: true },
      "/handleFollowUp":           { target: "http://localhost:4000", changeOrigin: true },
      "/executeCode":              { target: "http://localhost:4000", changeOrigin: true },
      "/validateComplexityAnswer": { target: "http://localhost:4000", changeOrigin: true },
      "/interrogateCode":          { target: "http://localhost:4000", changeOrigin: true },
      "/escalateCodingQuestion":   { target: "http://localhost:4000", changeOrigin: true },
      "/evaluateDesign":           { target: "http://localhost:4000", changeOrigin: true },
      "/stressTestDesign":         { target: "http://localhost:4000", changeOrigin: true },
      "/getModeContext":           { target: "http://localhost:4000", changeOrigin: true },
      "/parseCheatSheet":          { target: "http://localhost:4000", changeOrigin: true },
      "/processSpeech":            { target: "http://localhost:4000", changeOrigin: true },
      "/getHiringEvaluation":      { target: "http://localhost:4000", changeOrigin: true },
    },
  },
});
