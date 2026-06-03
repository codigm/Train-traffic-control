import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useStompClient } from './api/useStompClient';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import LiveTracking from './pages/LiveTracking';

function App() {
  const { connected, liveStateUpdates, emergencies } = useStompClient();

  return (
    <Router>
      <div className="app-container">
        <Navbar connected={connected} />
        
        <Routes>
          <Route path="/" element={<Dashboard emergencies={emergencies} liveUpdates={liveStateUpdates} />} />
          <Route path="/live" element={<LiveTracking liveUpdates={liveStateUpdates} />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
