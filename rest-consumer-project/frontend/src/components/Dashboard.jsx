import { useState, useEffect } from 'react';
import QuoteCard from './QuoteCard';
import StatusCard from './StatusCard';
import api from '../services/api';
import './Dashboard.css';

function Dashboard() {
  const [consumerQuote, setConsumerQuote] = useState(null);
  const [quotersQuote, setQuotersQuote] = useState(null);
  const [allQuotes, setAllQuotes] = useState([]);
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [newQuoteSource, setNewQuoteSource] = useState(null);
  const [stats, setStats] = useState({
    consumerFetches: 0,
    quotersFetches: 0,
    totalQuotes: 0
  });

  useEffect(() => {
    fetchInitialData();
  }, []);

  const fetchInitialData = async () => {
    try {
      const [statusData, quotesData] = await Promise.all([
        api.consumer.getStatus(),
        api.quoters.getAllQuotes()
      ]);
      
      setStatus(statusData);
      setAllQuotes(quotesData);
      setStats(prev => ({ ...prev, totalQuotes: quotesData.length }));
    } catch (error) {
      console.error('Error fetching initial data:', error);
    }
  };

  const fetchFromConsumer = async () => {
    setLoading(true);
    setNewQuoteSource('consumer');
    try {
      const quote = await api.consumer.getRandomQuote();
      setConsumerQuote(quote);
      setStats(prev => ({ ...prev, consumerFetches: prev.consumerFetches + 1 }));
      setTimeout(() => setNewQuoteSource(null), 1000);
    } catch (error) {
      console.error('Error fetching from consumer:', error);
    }
    setLoading(false);
  };

  const fetchFromQuoters = async () => {
    setLoading(true);
    setNewQuoteSource('quoters');
    try {
      const quote = await api.quoters.getRandomQuote();
      setQuotersQuote(quote);
      setStats(prev => ({ ...prev, quotersFetches: prev.quotersFetches + 1 }));
      setTimeout(() => setNewQuoteSource(null), 1000);
    } catch (error) {
      console.error('Error fetching from quoters:', error);
    }
    setLoading(false);
  };

  const fetchSpecificQuote = async () => {
    const id = prompt('Enter quote ID (1-10):');
    if (id && !isNaN(id)) {
      setLoading(true);
      try {
        const quote = await api.consumer.getQuoteById(parseInt(id));
        setConsumerQuote(quote);
        setStats(prev => ({ ...prev, consumerFetches: prev.consumerFetches + 1 }));
      } catch (error) {
        alert('Quote not found!');
      }
      setLoading(false);
    }
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>🚀 REST Consumer Dashboard</h1>
        <p className="subtitle">Consuming RESTful Web Services with Spring Boot</p>
      </header>

      <div className="status-grid">
        <StatusCard
          title="Consumer Service"
          value={status?.quotersStatus || 'Unknown'}
          icon="🔄"
          color="consumer"
          subtitle="Port 8081"
        />
        <StatusCard
          title="Quoters Service"
          value={status?.quotersStatus === 'connected' ? 'Connected' : 'Disconnected'}
          icon="📡"
          color={status?.quotersStatus === 'connected' ? 'connected' : 'disconnected'}
          subtitle="Port 8080"
        />
        <StatusCard
          title="Consumer Fetches"
          value={stats.consumerFetches}
          icon="📥"
          color="consumer"
        />
        <StatusCard
          title="Direct Fetches"
          value={stats.quotersFetches}
          icon="⚡"
          color="quoters"
        />
      </div>

      <div className="controls">
        <button 
          onClick={fetchFromConsumer} 
          disabled={loading}
          className="btn btn-consumer"
        >
          🔄 Fetch via Consumer (8081)
        </button>
        <button 
          onClick={fetchFromQuoters} 
          disabled={loading}
          className="btn btn-quoters"
        >
          📡 Fetch Direct (8080)
        </button>
        <button 
          onClick={fetchSpecificQuote} 
          disabled={loading}
          className="btn btn-specific"
        >
          🎯 Fetch by ID
        </button>
        <button 
          onClick={fetchInitialData} 
          disabled={loading}
          className="btn btn-refresh"
        >
          🔄 Refresh Status
        </button>
      </div>

      <div className="quotes-section">
        <div className="quotes-grid">
          {consumerQuote && (
            <div className="quote-column">
              <h2>🔄 via Consumer Service</h2>
              <QuoteCard 
                quote={consumerQuote} 
                source="consumer" 
                isNew={newQuoteSource === 'consumer'}
              />
            </div>
          )}
          
          {quotersQuote && (
            <div className="quote-column">
              <h2>📡 Direct from Quoters</h2>
              <QuoteCard 
                quote={quotersQuote} 
                source="quoters" 
                isNew={newQuoteSource === 'quoters'}
              />
            </div>
          )}
        </div>

        {!consumerQuote && !quotersQuote && (
          <div className="empty-state">
            <h2>👆 Click a button above to fetch quotes!</h2>
            <p>Compare fetching quotes through the Consumer service vs directly from Quoters API</p>
          </div>
        )}
      </div>

      <div className="all-quotes-section">
        <h2>📚 All Available Quotes ({allQuotes.length})</h2>
        <div className="quotes-list">
          {allQuotes.map((quote, index) => (
            <div key={index} className="quote-list-item">
              <span className="quote-list-id">#{quote.value.id}</span>
              <span className="quote-list-text">{quote.value.quote}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;