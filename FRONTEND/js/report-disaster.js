// FRONTEND/js/report-disaster.js

document.addEventListener('DOMContentLoaded', () => {

  if (!AuthManager.isLoggedIn()) {
    window.location.href = 'login.html';
    return;
  }

  const user = AuthManager.getUserData();
  const alertBox = document.getElementById('alertBox');
  
  // Mobile Menu
  document.getElementById('mobileMenuBtn').addEventListener('click', () => {
    document.getElementById('navLinks').classList.toggle('open');
  });

  document.getElementById('logoutBtn').addEventListener('click', () => {
    AuthManager.logout();
  });

  function showAlert(msg, isError=true) {
    alertBox.textContent = msg;
    alertBox.className = `alert ${isError ? 'error' : 'success'} text-center`;
    alertBox.classList.remove('hidden');
    // Scroll to top to see alert
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function hideAlert() {
    alertBox.classList.add('hidden');
  }

  // Pre-fill userId
  document.getElementById('userId').value = user.userId;

  // --- Map Coordinate Picker Logic ---
  
  // Start at Mumbai
  const mumbaiCenter = [19.0760, 72.8777];
  
  const mapOptions = {
    center: mumbaiCenter,
    zoom: 12
  };

  const map = L.map('locationPickerMap', mapOptions);
  
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
  }).addTo(map);

  // Fix map size loading inside grids
  setTimeout(() => map.invalidateSize(), 200);

  // Picker marker
  const pickerIcon = L.divIcon({
    className: 'incident-picker-marker',
    iconSize: [24, 24],
    iconAnchor: [12, 24] // Tip touching point
  });

  const marker = L.marker(mumbaiCenter, {
    icon: pickerIcon,
    draggable: true 
  }).addTo(map);

  const latInput = document.getElementById('latitude');
  const lngInput = document.getElementById('longitude');

  // Init input values
  latInput.value = mumbaiCenter[0];
  lngInput.value = mumbaiCenter[1];
  
  marker.bindPopup(`
    <b>Selected Incident Location</b><br>
    Lat: ${mumbaiCenter[0]}<br>
    Lng: ${mumbaiCenter[1]}
  `).openPopup();

  // Update inputs when dragged
  marker.on('dragend', function(e) {
    const latlng = marker.getLatLng();
    const latStr = latlng.lat.toFixed(6);
    const lngStr = latlng.lng.toFixed(6);
    latInput.value = latStr;
    lngInput.value = lngStr;
    marker.getPopup().setContent(`
      <b>Selected Incident Location</b><br>
      Lat: ${latStr}<br>
      Lng: ${lngStr}
    `);
    marker.openPopup();
  });

  // Update marker when map is clicked
  map.on('click', function(e) {
    const latStr = e.latlng.lat.toFixed(6);
    const lngStr = e.latlng.lng.toFixed(6);
    marker.setLatLng(e.latlng);
    latInput.value = latStr;
    lngInput.value = lngStr;
    marker.getPopup().setContent(`
      <b>Selected Incident Location</b><br>
      Lat: ${latStr}<br>
      Lng: ${lngStr}
    `);
    marker.openPopup();
  });

  // --- Geolocation API Logic ---
  const geoBtn = document.getElementById('geoBtn');
  geoBtn.addEventListener('click', () => {
    if ("geolocation" in navigator) {
      geoBtn.textContent = 'Locating...';
      geoBtn.disabled = true;
      navigator.geolocation.getCurrentPosition(
        (pos) => {
           const lat = pos.coords.latitude;
           const lng = pos.coords.longitude;
           const latStr = lat.toFixed(6);
           const lngStr = lng.toFixed(6);
           latInput.value = latStr;
           lngInput.value = lngStr;
           marker.setLatLng([lat, lng]);
           marker.getPopup().setContent(`
             <b>Selected Incident Location</b><br>
             Lat: ${latStr}<br>
             Lng: ${lngStr}
           `);
           map.setView([lat, lng], 15);
           marker.openPopup();
           geoBtn.textContent = '📍 Location Updated';
           setTimeout(() => { geoBtn.textContent = '📍 Use Current Location'; geoBtn.disabled = false; }, 3000);
        },
        (err) => {
           console.warn("Geolocation error:", err);
           showAlert("Could not fetch location. Please ensure location permissions are granted.", true);
           geoBtn.textContent = '📍 Use Current Location';
           geoBtn.disabled = false;
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
      );
    } else {
      showAlert("Geolocation is not supported by your browser.", true);
    }
  });

  // --- Form Submission Logic ---
  const reportForm = document.getElementById('reportForm');
  const submitBtn = document.getElementById('submitReportBtn');

  reportForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();
    
    // Exact schema matching requirements:
    // userId, password, incidentType, description, latitude, longitude, locationName, urgencyLevel
    
    const payload = {
      userId: document.getElementById('userId').value.trim(),
      password: document.getElementById('password').value,
      incidentType: document.getElementById('incidentType').value,
      description: document.getElementById('description').value.trim(),
      latitude: parseFloat(document.getElementById('latitude').value),
      longitude: parseFloat(document.getElementById('longitude').value),
      locationName: document.getElementById('locationName').value.trim(),
      urgencyLevel: document.getElementById('urgencyLevel').value
    };

    // basic validation incase floats fail
    if(isNaN(payload.latitude) || isNaN(payload.longitude)) {
      formAlert("Invalid Coordinates selected.", true);
      return;
    }

    try {
      submitBtn.disabled = true;
      submitBtn.textContent = 'TRANSMITTING REPORT...';

      // POST /api/incidents/report
      const { data } = await api.post('/api/incidents/report', payload);
      
      showAlert('VITAL: Incident report successfully transmitted to Control Room.', false);
      
      // Reset form but keep userid/coords
      reportForm.reset();
      document.getElementById('userId').value = user.userId;
      latInput.value = payload.latitude;
      lngInput.value = payload.longitude;
      
    } catch (err) {
      showAlert(err.message || 'Report Transmission Failed. Ensure password is correct and retry.');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'SUBMIT INCIDENT REPORT TO CONTROL ROOM';
    }
  });

});
