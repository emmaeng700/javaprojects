import { useState, useEffect } from 'react';
import './QuoteCard.css';

function QuoteCard({ quote, source, isNew }) {
  const [animate, setAnimate] = useState(false);

  useEffect(() => {
    if (isNew) {
      setAnimate(true);
      const timer = setTimeout(() => setAnimate(false), 1000);
      return () => clearTimeout(timer);
    }
  }, [quote, isNew]);

  return (
    <div className={`quote-card ${animate ? 'new-quote' : ''} ${source}`}>
      <div className="quote-header">
        <span className="quote-id">#{quote.value.id}</span>
        <span className={`quote-source-badge ${source}`}>
          {source === 'consumer' ? '🔄 via Consumer' : '📡 Direct'}
        </span>
      </div>
      <div className="quote-body">
        <blockquote>
          <p className="quote-text">"{quote.value.quote}"</p>
        </blockquote>
      </div>
      <div className="quote-footer">
        <span className={`quote-status ${quote.type}`}>
          {quote.type === 'success' ? '✓' : '✗'} {quote.type}
        </span>
      </div>
    </div>
  );
}

export default QuoteCard;