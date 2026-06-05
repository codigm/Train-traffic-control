# Train Traffic Control 🚆

A comprehensive Train Movement Optimization and Traffic Control System designed for railway networks. This project features a full-stack architecture that provides real-time train tracking, automated emergency handling, and conflict prediction using a modern web dashboard and a robust Spring Boot backend.

---

## 🌟 Features

- **Real-Time Live Tracking:** Visualizes train movements across the railway network on an interactive map.
- **Automated Conflict Prediction:** Backend intelligently predicts and resolves pathing conflicts using dynamic scheduling algorithms (Timed Dijkstra).
- **Emergency Handling & Manual Overrides:** Instantly halt all traffic or reroute specific trains during emergencies.
- **Weather Simulations:** Automatically adapts speed limits and restricts tracks based on simulated severe weather events.
- **Premium Dashboard UI:** Government-ready, high-contrast, dark-mode glassmorphism interface built with React.

---

## 🏗️ Architecture & Technologies

### Frontend
The frontend is a modern single-page application focused on high performance and real-time telemetry rendering.
- **Framework:** React 19 + Vite
- **Mapping:** Leaflet & React-Leaflet for interactive railway track visualization.
- **Real-time Communication:** `@stomp/stompjs` & `sockjs-client` for connecting to backend WebSockets.
- **Styling:** Custom Vanilla CSS with a premium dark-mode glassmorphism design.

### Backend
The backend is a robust Java service responsible for heavy computations, state management, and real-time data broadcasting.
- **Framework:** Java 17 + Spring Boot 3.5
- **Database:** MongoDB (Spring Data MongoDB)
- **Real-time Communication:** Spring WebSocket & STOMP for broadcasting live telemetry and emergencies.
- **Algorithms:** Pathfinding and scheduling using optimized graph algorithms.

---

## 🚀 Getting Started

### Prerequisites
- Node.js (v18+)
- Java 17
- Maven
- MongoDB (Local or Atlas)

### 1. Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd train-scheduler-backend
   ```
2. Configure your MongoDB connection in `src/main/resources/application.properties` (or use the provided default Atlas URI for testing).
3. Build and run the Spring Boot application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   *The backend will start on `http://localhost:8080`.*

### 2. Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd train-scheduler-frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Configure Environment Variables:
   Ensure you have a `.env` file in the root of the `train-scheduler-frontend` directory with the following variables:
   ```env
   VITE_API_BASE_URL=http://localhost:8080
   VITE_WS_BASE_URL=http://localhost:8080/ws
   ```
4. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *The frontend will be accessible at `http://localhost:5173`.*

---

## 🛠️ Usage

1. **Dashboard:** Upon launching the frontend, you will see the main Traffic Control Dashboard.
2. **Add Trains:** Click the `+ Add Trains` button in the navigation bar to deploy simulated trains onto various tracks.
3. **Live Map:** The map will automatically update, snapping train markers to real GeoJSON tracks based on their expected departure and arrival schedules.
4. **Emergencies:** 
   - Click `Halt All Traffic` to simulate a system-wide critical halt.
   - The backend's `WeatherService` will periodically simulate weather emergencies, which will automatically reflect on the dashboard.

---

## 📄 License
This project is for educational and simulation purposes.
