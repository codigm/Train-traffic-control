import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import AddTrainModal from './AddTrainModal';

const Navbar = ({ connected }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <nav className="navbar">
      <div className="nav-brand">
        Train Traffic Control
        {!connected && <span style={{ marginLeft: '10px', color: 'var(--danger-color)', fontSize: '0.8rem' }}> (Disconnected)</span>}
      </div>
      <div className="nav-links">
        <NavLink to="/" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>Dashboard</NavLink>
        <NavLink to="/live" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>Live Tracking</NavLink>
      </div>
      <div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          + Add Trains
        </button>
      </div>
      <AddTrainModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />
    </nav>
  );
};

export default Navbar;
