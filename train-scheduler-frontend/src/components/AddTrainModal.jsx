import React, { useState } from 'react';

const AddTrainModal = ({ isOpen, onClose }) => {
  const [trainIdPrefix, setTrainIdPrefix] = useState('TRN');
  const [numberOfTrains, setNumberOfTrains] = useState(1);
  const [selectedTracks, setSelectedTracks] = useState(['New Delhi - Mumbai']);

  const availableTracks = [
    'New Delhi - Mumbai',
    'Kalka - Shimla',
    'New Delhi - Lucknow',
    'Kolkata - Pune'
  ];

  if (!isOpen) return null;

  const handleTrackSelection = (e) => {
    const value = Array.from(e.target.selectedOptions, option => option.value);
    setSelectedTracks(value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const promises = [];
      
      for (let i = 0; i < numberOfTrains; i++) {
        // Distribute trains across selected tracks
        const track = selectedTracks[i % selectedTracks.length];
        const uniqueId = `${trainIdPrefix}-${Math.floor(Math.random() * 10000)}`;

        promises.push(
          fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/livestate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              trainId: uniqueId,
              currentSectionId: track, 
              expectedArrival: new Date(Date.now() + 3600000 + (i * 600000)).toISOString(),
              expectedDeparture: new Date(Date.now() + 3700000 + (i * 600000)).toISOString(),
              delayInMinutes: Math.floor(Math.random() * 30) // random delay for demo
            })
          })
        );
      }
      
      await Promise.all(promises);
      onClose(); // Close modal on success
    } catch (err) {
      console.error('Error adding trains:', err);
    }
  };

  return (
    <div className="modal-overlay" style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.7)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
    }}>
      <div className="glass-panel" style={{ width: '400px', backgroundColor: 'var(--bg-color-secondary)' }}>
        <div className="flex justify-between items-center mb-4">
          <h3>Add Trains to Tracks</h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '1.2rem' }}>✕</button>
        </div>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Train ID Prefix</label>
            <input 
              type="text" 
              className="form-select" 
              placeholder="e.g. TRN"
              value={trainIdPrefix}
              onChange={(e) => setTrainIdPrefix(e.target.value)}
              style={{ backgroundColor: 'var(--bg-color)' }}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Number of Trains to Deploy</label>
            <input 
              type="number" 
              className="form-select" 
              min="1"
              max="50"
              value={numberOfTrains}
              onChange={(e) => setNumberOfTrains(Number(e.target.value))}
              style={{ backgroundColor: 'var(--bg-color)' }}
            />
          </div>

          <div className="form-group mb-4">
            <label className="form-label">Select Tracks (Ctrl/Cmd + Click for multiple)</label>
            <select 
              className="form-select" 
              multiple
              value={selectedTracks} 
              onChange={handleTrackSelection}
              style={{ backgroundColor: 'var(--bg-color)', height: '100px' }}
            >
              {availableTracks.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
            <small style={{ color: 'var(--text-secondary)', display: 'block', marginTop: '5px' }}>
              Trains will be distributed evenly across selected tracks.
            </small>
          </div>

          <div className="flex" style={{ gap: '1rem' }}>
            <button type="button" className="btn btn-secondary w-full" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary w-full">Deploy {numberOfTrains} Train(s)</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddTrainModal;
