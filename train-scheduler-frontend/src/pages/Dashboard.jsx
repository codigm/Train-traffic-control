import React, { useState } from 'react';
import LiveMap from '../components/LiveMap';

const Dashboard = ({ emergencies, liveUpdates }) => {
  const [source, setSource] = useState('New Delhi');
  const [destination, setDestination] = useState('Kolkata');
  const [activeRoute, setActiveRoute] = useState({ source: null, destination: null });

  const handleFindTrains = () => {
    setActiveRoute({ source, destination });
  };

  const handleReRoute = async () => {
    try {
      await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/emergency/report`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          trackId: 'TRK-101',
          eventType: 'RE_ROUTE_REQUIRED',
          description: 'Manual override initiated. Trains re-routing.'
        })
      });
    } catch (err) {
      console.error('Error triggering emergency:', err);
    }
  };

  const handleHaltTraffic = async () => {
    try {
      await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/emergency/report`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          trackId: 'ALL_TRACKS',
          eventType: 'SYSTEM_HALT',
          description: 'Critical: All traffic halted by manual override.'
        })
      });
    } catch (err) {
      console.error('Error halting traffic:', err);
    }
  };

  // Fallback static trains if no live data is available yet
  const trains = liveUpdates && liveUpdates.length > 0 ? liveUpdates.map(update => ({
    name: `Train ${update.trainId}`,
    route: `Current: ${update.currentSectionId}`,
    dep: 'Live',
    arr: new Date(update.expectedArrival).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})
  })) : [
    { name: 'Shatabdi Express', route: 'New Delhi → Lucknow', dep: '06:15', arr: '12:30' },
    { name: 'Rajdhani Express', route: 'Mumbai → New Delhi', dep: '17:00', arr: '08:35' },
    { name: 'Duronto Express', route: 'Kolkata → Pune', dep: '05:45', arr: '10:15' },
    { name: 'Himalayan Queen', route: 'Kalka → Shimla', dep: '12:10', arr: '17:30' },
  ];

  return (
    <div className="main-content">
      <div className="dashboard-grid">
        
        {/* Left Column: System Status & Manual Override */}
        <div className="flex" style={{ flexDirection: 'column', gap: '1.5rem' }}>
          <div className="panel">
            <h3 className="mb-4">AI System Status</h3>
            
            <div className="system-status-item">
              <div className="flex items-center">
                <div className="system-status-icon icon-success">✓</div>
                <span>Conflict Prediction</span>
              </div>
              <span className="status-badge status-active">Active</span>
            </div>

            <div className="system-status-item">
              <div className="flex items-center">
                <div className="system-status-icon icon-info">⚡</div>
                <span>Route Optimization</span>
              </div>
              <span className="status-badge status-active">Active</span>
            </div>

            <div className="system-status-item">
              <div className="flex items-center">
                <div className="system-status-icon icon-warning">!</div>
                <span>Emergency Handling</span>
              </div>
              <span className="status-badge status-standby">Standby</span>
            </div>
          </div>

          <div className="panel">
            <h3 className="mb-4">Manual Override</h3>
            <button className="btn btn-secondary w-full mb-4" onClick={handleReRoute}>
              ↹ Re-Route Train
            </button>
            <button className="btn btn-danger w-full" onClick={handleHaltTraffic} style={{ background: 'var(--danger-color)', color: 'white' }}>
              🛑 Halt All Traffic
            </button>
            <p className="text-center mt-4" style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
              Triggers a backend WebSocket event.
            </p>
          </div>

          {/* Emergencies Column */}
          {emergencies && emergencies.length > 0 && (
            <div className="panel" style={{ background: 'rgba(239, 68, 68, 0.1)', borderColor: 'rgba(239, 68, 68, 0.3)' }}>
               <h3 className="mb-2" style={{ color: 'var(--danger-color)' }}>🚨 Active Emergencies</h3>
               <div className="train-list" style={{ maxHeight: '250px' }}>
                  {emergencies.map((em, idx) => (
                    <div className="train-card" key={idx} style={{ borderColor: 'rgba(239, 68, 68, 0.3)' }}>
                      <div className="train-info">
                        <div className="train-name" style={{ color: 'var(--danger-color)' }}>{em.eventType}</div>
                        <div className="train-route">{em.trackId} - {em.description}</div>
                      </div>
                      <div className="time-block">
                        <span className="time-label">Reported</span>
                        <span className="time-value" style={{ fontSize: '0.85rem' }}>{new Date(em.reportedAt || Date.now()).toLocaleTimeString()}</span>
                      </div>
                    </div>
                  ))}
               </div>
            </div>
          )}
        </div>

        {/* Middle Column: Map */}
        <div className="panel" style={{ padding: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          <LiveMap source={activeRoute.source} destination={activeRoute.destination} liveUpdates={liveUpdates} />
        </div>

        {/* Right Column: Active Train Schedule */}
        <div className="panel">
          <h3 className="mb-2">Active Train Schedule</h3>
          <h2 className="mb-4">Find Trains by Route</h2>
          
          <div className="form-group">
            <label className="form-label">Source</label>
            <select className="form-select" value={source} onChange={(e) => setSource(e.target.value)}>
              <option>Kalka</option>
              <option>New Delhi</option>
              <option>Mumbai</option>
              <option>Kolkata</option>
            </select>
          </div>

          <div className="form-group mb-4">
            <label className="form-label">Destination</label>
            <select className="form-select" value={destination} onChange={(e) => setDestination(e.target.value)}>
              <option>Kolkata</option>
              <option>Shimla</option>
              <option>Lucknow</option>
              <option>Pune</option>
            </select>
          </div>

          <button className="btn btn-primary w-full mb-4" onClick={handleFindTrains}>
            Find Trains
          </button>

          <div className="train-list">
            {trains.map((train, idx) => (
              <div className="train-card" key={idx}>
                <div className="train-icon">🚆</div>
                <div className="train-info">
                  <div className="train-name">{train.name}</div>
                  <div className="train-route">{train.route}</div>
                </div>
                <div className="train-times">
                  <div className="time-block">
                    <span className="time-label">Departure</span>
                    <span className="time-value">{train.dep}</span>
                  </div>
                  <div className="time-block">
                    <span className="time-label">Arrival</span>
                    <span className="time-value">{train.arr}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

        </div>

      </div>
    </div>
  );
};

export default Dashboard;
