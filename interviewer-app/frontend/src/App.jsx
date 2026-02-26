import { Routes, Route, Navigate } from "react-router-dom";
import { SessionProvider } from "./context/SessionContext";
import Home from "./pages/Home";
import Setup from "./pages/Setup";
import Interview from "./pages/Interview";
import Report from "./pages/Report";

export default function App() {
  return (
    <SessionProvider>
      <Routes>
        <Route path="/"          element={<Home />} />
        <Route path="/setup"     element={<Setup />} />
        <Route path="/interview" element={<Interview />} />
        <Route path="/report"    element={<Report />} />
        <Route path="*"          element={<Navigate to="/" replace />} />
      </Routes>
    </SessionProvider>
  );
}
