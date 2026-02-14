import { useEffect, useState } from "react";
import { API_BASE_URL } from "./config";
import "./App.css";

type ReceivedMessage = {
  body: string;
  receivedAt: string;
};

function App() {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState<ReceivedMessage[]>([]);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  const fetchMessages = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/messages`);
      if (!res.ok) throw new Error("Failed to fetch messages");
      const data = (await res.json()) as ReceivedMessage[];
      setMessages(data);
      setLastUpdated(new Date());
    } catch (e: any) {
      setError(e.message ?? "Error fetching messages");
    }
  };

  useEffect(() => {
    fetchMessages();
    const interval = setInterval(fetchMessages, 3000); // poll every 3s
    return () => clearInterval(interval);
  }, []);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;
    setSending(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE_URL}/api/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: input }),
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Send failed");
      }
      setInput("");
      await fetchMessages();
    } catch (e: any) {
      setError(e.message ?? "Error sending message");
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="app">
      <header className="header">
        <h1>Spring Boot + RabbitMQ + React</h1>
        <p>Send messages through RabbitMQ and see what the backend receives.</p>
      </header>

      <main className="main">
        <section className="card">
          <h2>Send a message</h2>
          <form onSubmit={handleSend} className="form">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type your message..."
              disabled={sending}
            />
            <button type="submit" disabled={sending || !input.trim()}>
              {sending ? "Sending..." : "Send"}
            </button>
          </form>
          {error && <div className="error">Error: {error}</div>}
        </section>

        <section className="card">
          <h2>Received messages</h2>
          {lastUpdated && (
            <div className="meta">
              Last updated: {lastUpdated.toLocaleTimeString()}
            </div>
          )}
          {messages.length === 0 ? (
            <p className="empty">No messages yet. Send one!</p>
          ) : (
            <ul className="messages">
              {messages.slice().reverse().map((m, idx) => (
                <li key={idx} className="message">
                  <div className="body">{m.body}</div>
                  <div className="timestamp">
                    {new Date(m.receivedAt).toLocaleString()}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;