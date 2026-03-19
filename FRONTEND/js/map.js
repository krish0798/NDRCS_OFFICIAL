// FRONTEND/js/map.js

document.addEventListener('DOMContentLoaded', () => {

  // --- Auth UI & Overlays ---
  const userRoleDisplay = document.getElementById('userRoleOverlay');
  const loginBtn = document.getElementById('loginBtn');
  const logoutBtn = document.getElementById('logoutBtn');

  let currentRole = "Public Read-Only";

  if (AuthManager.isLoggedIn()) {
    const user = AuthManager.getUserData();
    currentRole = user.role || 'Citzen Logged In';
    userRoleDisplay.textContent = `${currentRole.replace('_', ' ')} - ${user.name}`;
    loginBtn.classList.add('hidden');
    logoutBtn.classList.remove('hidden');
    
    if(user.role === 'CONTROL_ROOM') {
      userRoleDisplay.style.color = '#ef5350';
      userRoleDisplay.style.borderColor = '#ef5350';
    }
  } else {
    userRoleDisplay.textContent = `PUBLIC MONITORING VIEW`;
    loginBtn.classList.remove('hidden');
    logoutBtn.classList.add('hidden');
  }

  logoutBtn.addEventListener('click', () => {
    AuthManager.logout();
  });

  // --- Mobile Sidebar Toggle ---
  const mobileToggleBtn = document.getElementById('mobileSidebarToggle');
  const mapSidebar = document.querySelector('.map-sidebar');
  
  mobileToggleBtn.addEventListener('click', () => {
    mapSidebar.classList.toggle('open');
    if (mapSidebar.classList.contains('open')) {
      mobileToggleBtn.innerHTML = '🔽 Hide Operations Panel';
    } else {
      mobileToggleBtn.innerHTML = '📊 View Operations Panel';
    }
  });


  // --- Leaflet Map Initialization ---
  const mumbaiCenter = [19.0760, 72.8777];
  const defaultZoom = 11;

  const mapOptions = {
    center: mumbaiCenter,
    zoom: defaultZoom,
    zoomControl: false
  };

  const map = L.map('ndrcs-map', mapOptions);
  
  L.control.zoom({ position: 'topright' }).addTo(map);

  // OpenStreetMap light tiles for clear detailed operational view
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  // --- Custom Icons ---
  const incidentIcon = L.divIcon({
    className: 'incident-marker',
    iconSize: [20, 20],
    iconAnchor: [10, 10],
    popupAnchor: [0, -10]
  });

  const rescueIcon = L.divIcon({
    className: 'rescue-marker',
    iconSize: [20, 20],
    iconAnchor: [10, 10],
    popupAnchor: [0, -10]
  });

  const incidentLayer = L.layerGroup().addTo(map);
  const rescueLayer = L.layerGroup().addTo(map);

  const lastUpdatedOverlay = document.getElementById('lastUpdatedOverlay');
  const incidentsContainer = document.getElementById('incidents-container');
  const teamsContainer = document.getElementById('teams-container');

  // --- Polling Logic ---
  async function fetchAndPlotData() {
    try {
      // 1. Fetch Incidents
      const incidentsRes = await api.get('/api/incidents').catch(() => ({ data: [] }));
      let incidents = Array.isArray(incidentsRes.data) ? incidentsRes.data : [];

      // Intrinsic filter: aggressively remove RESOLVED / COMPLETED / CLOSED from telemetry
      incidents = incidents.filter(inc => !['RESOLVED', 'COMPLETED', 'CLOSED'].includes(String(inc.status || '').toUpperCase()));

      // 2. Fetch Rescue Teams
      const rescueRes = await api.get('/api/rescue/live-status').catch(() => ({ data: [] }));
      let rescueTeamsRaw = Array.isArray(rescueRes.data) ? rescueRes.data : [];

      // Filter offline teams before rendering telemetry counts
      const rescueTeams = rescueTeamsRaw.filter(team => {
        const isOffline = (team.statusMessage || '').toUpperCase().includes('OFFLINE');
        return !isOffline && team.latitude && team.longitude;
      });

      // Update Incidents Panel
      if (incidents.length === 0) {
        incidentsContainer.innerHTML = `
          <div class="empty-state-card">
            <div class="empty-icon">🛡️</div>
            <h4>No Active Incidents</h4>
            <p>Sector is currently clear. Monitoring for citizen distress signals and automated hazard alerts.</p>
          </div>
        `;
      } else {
        incidentsContainer.innerHTML = `
          <div class="stat-box-filled">
            <div class="stat-number-large">${incidents.length}</div>
            <div class="stat-text">
              <h4>Reported Incidents</h4>
              <p>Active hazards in sector requiring immediate intervention.</p>
            </div>
          </div>
        `;
      }

      // Update Teams Panel
      if (rescueTeams.length === 0) {
        teamsContainer.innerHTML = `
          <div class="empty-state-card">
            <div class="empty-icon">🚁</div>
            <h4>No Rescue Units Deployed</h4>
            <p>All emergency response squads are currently on standby at their designated staging areas.</p>
          </div>
        `;
      } else {
        teamsContainer.innerHTML = `
          <div class="stat-box-filled">
            <div class="stat-number-large">${rescueTeams.length}</div>
            <div class="stat-text">
              <h4>Active Rescue Teams</h4>
              <p>Units actively dispatched and responding to sector hazards.</p>
            </div>
          </div>
        `;
      }

      // Clear existing layers
      incidentLayer.clearLayers();
      rescueLayer.clearLayers();

      // Plot Incidents
      incidents.forEach(inc => {
        const isInactive = ['RESOLVED', 'COMPLETED', 'CLOSED'].includes(String(inc.status || '').toUpperCase());
        if (!isInactive && inc.latitude && inc.longitude) {
          const marker = L.marker([inc.latitude, inc.longitude], { icon: incidentIcon });
          marker.bindPopup(`
            <b>INCIDENT #${inc.id}</b><br>
            Type: ${inc.incidentType || 'Unknown'}<br>
            Location: ${inc.locationName || 'Unknown'}<br>
            Status: ${inc.status || 'REPORTED'}<br>
            Urgency: ${inc.urgencyLevel || 'NORMAL'}
          `);
          incidentLayer.addLayer(marker);
        }
      });

      // Plot Rescue Teams
      rescueTeams.forEach(team => {
        const marker = L.marker([team.latitude, team.longitude], { icon: rescueIcon });
        marker.bindPopup(`
          <b>${team.rescueTeam?.teamName || 'Rescue Team'}</b><br>
          Code: ${team.rescueTeam?.teamCode || 'Unknown'}<br>
          Location: ${team.locationName || 'Unknown'}<br>
          Status: ${team.statusMessage || 'Active'}<br>
          Urgency: ${team.urgencyLevel || 'NORMAL'}
        `);
        rescueLayer.addLayer(marker);
      });

      // Update timestamp with formatted string
      const now = new Date();
      const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second:'2-digit' });
      lastUpdatedOverlay.textContent = `LAST SYNC: ${timeStr} IST`;
      lastUpdatedOverlay.style.color = '#64b5f6';

    } catch (error) {
      console.error("Error during map telemetry polling:", error);
      lastUpdatedOverlay.textContent = `CRITICAL: TELEMETRY DISCONNECTED. RETRYING...`;
      lastUpdatedOverlay.style.color = '#ef5350';
    }
  }

  // Initial fetch
  fetchAndPlotData();

  // Poll every 5 seconds
  setInterval(fetchAndPlotData, 5000);

  // Fix map tile loading issues when container resizes or loads
  setTimeout(() => {
    map.invalidateSize();
  }, 100);

  window.addEventListener('resize', () => {
    map.invalidateSize();
  });

});