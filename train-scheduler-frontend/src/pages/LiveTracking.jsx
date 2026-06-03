import React from 'react';

const LiveTracking = ({ liveUpdates }) => {
  return (
    <div className="main-content">
      <div className="flex justify-between items-center mb-4">
        <h1>Live Tracking</h1>
        <div className="flex items-center">
          <span className="status-indicator status-live"></span>
          <span className="text-muted">Listening for updates...</span>
        </div>
      </div>

      <div className="glass-panel">
        <h3 className="mb-2">Recent Movements</h3>
        
        {liveUpdates.length === 0 ? (
          <div style={{ padding: '4rem 0', textAlign: 'center' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem', opacity: 0.5 }}>📡</div>
            <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.5rem' }}>No Live Data Available</h4>
            <p className="text-muted">
              The system is waiting for telemetry from the railway network.
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--glass-border)' }}>
                  <th style={{ padding: '1rem' }}>Train ID</th>
                  <th style={{ padding: '1rem' }}>Section</th>
                  <th style={{ padding: '1rem' }}>Expected Arrival</th>
                  <th style={{ padding: '1rem' }}>Delay (min)</th>
                  <th style={{ padding: '1rem' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {liveUpdates.map((update, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                    <td style={{ padding: '1rem', fontWeight: '500' }}>{update.trainId}</td>
                    <td style={{ padding: '1rem' }}>{update.currentSectionId}</td>
                    <td style={{ padding: '1rem', color: 'var(--text-secondary)' }}>
                      {new Date(update.expectedArrival).toLocaleTimeString()}
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <span style={{ 
                        color: update.delayInMinutes > 0 ? 'var(--warning-color)' : 'var(--success-color)' 
                      }}>
                        {update.delayInMinutes}
                      </span>
                    </td>
                    <td style={{ padding: '1rem' }}>
                      {update.delayInMinutes > 15 ? (
                        <span style={{ color: 'var(--danger-color)', fontSize: '0.875rem' }}>Severely Delayed</span>
                      ) : update.delayInMinutes > 0 ? (
                        <span style={{ color: 'var(--warning-color)', fontSize: '0.875rem' }}>Delayed</span>
                      ) : (
                        <span style={{ color: 'var(--success-color)', fontSize: '0.875rem' }}>On Time</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default LiveTracking;
