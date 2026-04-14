# NDRCS - Frontend Application 🌐

This folder contains the complete user interface and client-side logic for the National Disaster Response Control System. 

## 🛠️ Tech Stack
- **Structure**: Vanilla HTML5
- **Styling**: Vanilla CSS3 (Custom Design System, Dark/Light modes)
- **Logic**: Vanilla ES6 JavaScript
- **Mapping Engine**: [Leaflet.js](https://leafletjs.com/) mapping with OpenStreetMap

## 📂 Architecture Overview

The frontend is completely decoupled from the backend and communicates exclusively via REST APIs. 
To ensure modularity and simple deployments, all network requests are channeled through a single abstract configuration file:
- `js/api.js`: Holds the `fetchApi` wrapper and the `API_BASE_URL`.

All major user roles have separated JavaScript controllers:
- `citizen-profile.js` & `report-disaster.js` (Public usage)
- `control-dashboard.js` (Command Center dispatching)
- `rescue-dashboard.js` (Live GPS telemetry polling)

## 🚀 Running Locally
Because this project utilizes Vanilla web technologies, there is **no build step** required (No Node.js, Webpack, or Vite needed).

1. Clone the repository.
2. Open the `FRONTEND` folder in VS Code.
3. Start a basic static server (e.g., VS Code "Live Server" extension).
4. By default, ensure `js/api.js` points to your backend URL (e.g., `http://localhost:8080` for local testing or your Railway API for production).
