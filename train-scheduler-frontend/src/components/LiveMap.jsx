import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, GeoJSON, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import tracksData from '../data/tracksData.json';

// Custom Train Icon
const trainIcon = new L.Icon({
  iconUrl: 'https://cdn-icons-png.flaticon.com/512/776/776105.png',
  iconSize: [24, 24],
  iconAnchor: [12, 12],
  popupAnchor: [0, -12],
});

// Fix for default marker icon in react-leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const cityCoordinates = {
  'New Delhi': [28.6139, 77.2090],
  'Kolkata': [22.5726, 88.3639],
  'Mumbai': [19.0760, 72.8777],
  'Kalka': [30.8354, 76.9388],
  'Shimla': [31.1048, 77.1734],
  'Lucknow': [26.8467, 80.9462],
  'Pune': [18.5204, 73.8567],
};

// Map search source-destination pairs to our GeoJSON Track IDs
const searchToTrackMapping = {
  'New Delhi-Kolkata': 'New Delhi - Kolkata',
  'Kolkata-New Delhi': 'New Delhi - Kolkata',
  'New Delhi-Mumbai': 'New Delhi - Mumbai',
  'Mumbai-New Delhi': 'New Delhi - Mumbai',
  'Kalka-Shimla': 'Kalka - Shimla',
  'Shimla-Kalka': 'Kalka - Shimla',
  'Kolkata-Pune': 'Kolkata - Pune',
  'Pune-Kolkata': 'Kolkata - Pune',
};

// Helper: Given a GeoJSON LineString and a progress percentage (0.0 to 1.0), calculate exact lat/lon
const getInterpolatedPosition = (coordinates, progress) => {
  if (!coordinates || coordinates.length === 0) return [0, 0];
  if (coordinates.length === 1) return [coordinates[0][1], coordinates[0][0]];
  if (progress <= 0) return [coordinates[0][1], coordinates[0][0]];
  if (progress >= 1) return [coordinates[coordinates.length - 1][1], coordinates[coordinates.length - 1][0]];

  const totalSegments = coordinates.length - 1;
  const rawIndex = progress * totalSegments;
  const segmentIndex = Math.floor(rawIndex);
  const segmentProgress = rawIndex - segmentIndex;

  const p1 = coordinates[segmentIndex];
  const p2 = coordinates[segmentIndex + 1];

  // GeoJSON is [lon, lat], Leaflet is [lat, lon]
  const lon = p1[0] + (p2[0] - p1[0]) * segmentProgress;
  const lat = p1[1] + (p2[1] - p1[1]) * segmentProgress;

  return [lat, lon];
};

const LiveMap = ({ source, destination, liveUpdates = [] }) => {
  const defaultCenter = [22.9734, 78.6569];
  const indiaBounds = [
    [6.7535, 68.1623],
    [37.0805, 97.3956]
  ];

  const [activeGeoJsonFeature, setActiveGeoJsonFeature] = useState(null);
  const [currentTime, setCurrentTime] = useState(Date.now());
  const mapWrapperRef = useRef(null);

  const toggleFullScreen = () => {
    if (!document.fullscreenElement) {
      mapWrapperRef.current.requestFullscreen().catch(err => {
        console.error(`Error attempting to enable fullscreen: ${err.message}`);
      });
    } else {
      document.exitFullscreen();
    }
  };

  // Timer to drive the train animations
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(Date.now());
    }, 1000); // update every second
    return () => clearInterval(timer);
  }, []);

  // Update highlighted track based on search
  useEffect(() => {
    const routeKey = `${source}-${destination}`;
    const trackId = searchToTrackMapping[routeKey];
    
    if (trackId) {
      const feature = tracksData.features.find(f => f.properties.id === trackId);
      setActiveGeoJsonFeature(feature);
    } else {
      setActiveGeoJsonFeature(null);
    }
  }, [source, destination]);

  const geoJsonStyle = {
    color: "var(--accent-color)",
    weight: 4,
    dashArray: "10, 10",
    className: "animated-route"
  };

  const allTracksStyle = {
    color: "var(--text-secondary)", // subtle gray/faded color for temporary tracks
    weight: 2,
    opacity: 0.5
  };

  return (
    <div ref={mapWrapperRef} style={{ height: '100%', width: '100%', minHeight: '400px', borderRadius: '8px', overflow: 'hidden', position: 'relative' }}>
      
      {/* Fullscreen Toggle Button */}
      <button 
        onClick={toggleFullScreen}
        style={{
          position: 'absolute', top: '10px', right: '10px', zIndex: 1000,
          backgroundColor: 'var(--bg-color-secondary)', color: 'var(--text-color)',
          border: '1px solid var(--border-color)', borderRadius: '4px',
          padding: '5px 10px', cursor: 'pointer', fontWeight: 'bold'
        }}
      >
        ⛶ Fullscreen
      </button>

      <MapContainer 
        bounds={indiaBounds} // Automatically fit India into the viewport
        zoom={5} 
        minZoom={5}
        maxZoom={18}
        maxBounds={indiaBounds}
        maxBoundsViscosity={1.0}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; OpenStreetMap contributors'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />
        <TileLayer
          attribution='&copy; <a href="https://www.openrailwaymap.org/">OpenRailwayMap</a>'
          url="https://{s}.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png"
        />
        
        {['New Delhi', 'Kolkata', 'Mumbai', 'Kalka', 'Shimla', 'Lucknow', 'Pune'].map((city) => (
          <Marker key={city} position={cityCoordinates[city]}>
            <Popup>{city}</Popup>
          </Marker>
        ))}

        {/* Render ALL tracks permanently as 'temporary tracks' so they are always visible */}
        {tracksData.features.map(feature => (
          <GeoJSON 
            key={`base-${feature.properties.id}`}
            data={feature}
            style={allTracksStyle}
          />
        ))}

        {/* Highlight the specific GeoJSON vector track that was searched */}
        {activeGeoJsonFeature && (
          <GeoJSON 
            key={`active-${activeGeoJsonFeature.properties.id}`} 
            data={activeGeoJsonFeature} 
            style={geoJsonStyle} 
          />
        )}

        {/* Render active live trains snapping to the GeoJSON vectors */}
        {liveUpdates.map((train, idx) => {
          // Find the GeoJSON track this train is on
          const trackFeature = tracksData.features.find(f => f.properties.id === train.currentSectionId);
          
          let coords = defaultCenter;
          if (trackFeature) {
            // Calculate progress based on time. 
            // Demo logic: We span from 1 hour ago to 1 hour in future for full trip
            const expectedDep = new Date(train.expectedDeparture).getTime() - 7200000; // rough start
            const expectedArr = new Date(train.expectedArrival).getTime();
            const totalDuration = expectedArr - expectedDep;
            const elapsed = currentTime - expectedDep;
            
            let progress = elapsed / totalDuration;
            if (progress < 0) progress = 0;
            if (progress > 1) progress = 1;

            coords = getInterpolatedPosition(trackFeature.geometry.coordinates, progress);
          }

          return (
            <Marker key={`train-${train.trainId}-${idx}`} position={coords} icon={trainIcon}>
              <Popup>
                <strong>{train.trainId}</strong><br/>
                Status: {train.delayInMinutes > 0 ? `Delayed (${train.delayInMinutes}m)` : 'On Time'}<br/>
                Track: {train.currentSectionId}
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
};

export default LiveMap;
