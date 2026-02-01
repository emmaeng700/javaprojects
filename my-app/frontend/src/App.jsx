import UserList from './components/UserList';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <h1>🚀 Spring Boot + React App</h1>
        <p>Full-stack application demo</p>
      </header>
      <main>
        <UserList />
      </main>
    </div>
  );
}

export default App;