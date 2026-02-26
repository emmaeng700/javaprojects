import { createContext, useContext, useState } from "react";

const SessionContext = createContext(null);

export function SessionProvider({ children }) {
  const [session, setSession] = useState(null); // { sessionId, mode, practiceMode, resumeKey }
  const [hiringReport, setHiringReport] = useState(null);

  return (
    <SessionContext.Provider value={{ session, setSession, hiringReport, setHiringReport }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error("useSession must be used inside SessionProvider");
  return ctx;
}
