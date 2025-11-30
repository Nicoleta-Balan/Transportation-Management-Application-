import StationsPage from './components/stations/StationsPage';
import { ErrorBoundary } from './components/ErrorBoundary';
import './App.css';

function App() {
  return (
    <ErrorBoundary sectionName="Application">
      <StationsPage />
    </ErrorBoundary>
  );
}

export default App;
