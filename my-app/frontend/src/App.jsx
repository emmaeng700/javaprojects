import UserList from './components/UserList';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <h1>🚀 Spring Boot + React Full-Stack App</h1>
        <p>Backend: Java 21 + Spring Boot 3.5.10 | Frontend: React + Vite</p>
      </header>
      <main>
        <UserList />
      </main>
    </div>
  );
}

export default App;