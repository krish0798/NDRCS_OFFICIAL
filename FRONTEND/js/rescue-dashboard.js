// FRONTEND/js/rescue-dashboard.js

document.addEventListener('DOMContentLoaded', () => {
  if (!AuthManager.isLoggedIn()) {
    window.location.href = 'login.html';
    return;
  }

  const user = AuthManager.getUserData();

  if (user.role !== 'RESCUE_TEAM') {
    alert("CRITICAL ERROR: Unauthorized Account Level.");
    window.location.href = 'login.html';
    return;
  }

  document.getElementById('logoutBtn').addEventListener('click', () => AuthManager.logout());

  document.getElementById('sessionEndBtn')?.addEventListener('click', async () => {
    if (!confirm("Are you sure you want to Go Offline? Your location will be hidden from operational maps.")) return;
    
    stopAutoTracking();
    document.getElementById('sessionEndBtn').style.display = 'none';
    
    try {
      await api.post('/api/rescue/update-location', {
        userId: user.userId,
        latitude: currentLat, // Retain physical coordinates suppressing backend constraints
        longitude: currentLng,
        locationName: "OFFLINE",
        statusMessage: "OFFLINE",
        urgencyLevel: "NORMAL"
      });
      
      showAlert("You are now OFFLINE. Public/Control tracking disengaged.");
      performFieldSync();
    } catch (err) {
      showAlert(err.message || "Failed to go offline.", true);
    }
  });

  const alertBox = document.getElementById('alertBox');
  const syncStamp = document.getElementById('syncStamp');
  const inboxList = document.getElementById('inboxList');
  const missionText = document.getElementById('missionText');
  const statusDot = document.getElementById('statusDot');
  const updateGeoBtn = document.getElementById('updateLocationBtn');

  function showAlert(msg, isError = false) {
    alertBox.textContent = msg;
    alertBox.className = `alert ${isError ? 'error' : 'success'}`;
    alertBox.classList.remove('hidden');
    setTimeout(() => alertBox.classList.add('hidden'), 5000);
  }

  const initialCenter = [19.0760, 72.8777];
  const map = L.map('rescueMap', {
    center: initialCenter,
    zoom: 12,
    zoomControl: false
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  L.control.zoom({ position: 'topright' }).addTo(map);

  const teamIcon = L.divIcon({
    className: 'rescue-marker',
    iconSize: [18, 18]
  });

  const incidentIcon = L.divIcon({
    className: 'incident-marker',
    iconSize: [18, 18]
  });

  let myMarker = null;
  let targetMarker = null;

  let currentLat = initialCenter[0];
  let currentLng = initialCenter[1];
  let myProfileId = 1;
  let autoTrackingStarted = false;
  let firstMapLoad = true;
  const ackedMessages = new Set();

  function updateMapCenter(forceRecenter = false) {
    if (myMarker) {
      myMarker.setLatLng([currentLat, currentLng]);
    } else {
      myMarker = L.marker([currentLat, currentLng], { icon: teamIcon }).addTo(map);
    }

    if (firstMapLoad || forceRecenter) {
      if (myMarker && targetMarker) {
        const bounds = L.latLngBounds([myMarker.getLatLng(), targetMarker.getLatLng()]);
        map.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 });
      } else {
        map.setView([currentLat, currentLng], 14);
      }
      firstMapLoad = false;
    }
  }

  async function transmitLocation(statusMessage = "Active tracking") {
    try {
      const payload = {
        userId: user.userId,
        latitude: currentLat,
        longitude: currentLng,
        locationName: "Field GPS Position",
        statusMessage,
        urgencyLevel: "NORMAL"
      };

      await api.post('/api/rescue/update-location', payload);

      updateGeoBtn.textContent = '✓ Live Tracking Active';
      updateGeoBtn.disabled = false;
    } catch (err) {
      updateGeoBtn.disabled = false;
      updateGeoBtn.textContent = '📍 Live Position Update';
      showAlert(err.message || "Location update failed.", true);
    }
  }

  let trackingInterval = null;

  function startAutoTracking() {
    if (autoTrackingStarted) return;
    autoTrackingStarted = true;

    if (!("geolocation" in navigator)) {
      showAlert("Geolocation is not supported in this browser.", true);
      updateMapCenter();
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        currentLat = pos.coords.latitude;
        currentLng = pos.coords.longitude;
        updateMapCenter();
        await transmitLocation("Live tracking started");
      },
      () => {
        showAlert("GPS permission denied. Using fallback map center.", true);
        updateMapCenter();
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );

    trackingInterval = setInterval(() => {
      navigator.geolocation.getCurrentPosition(
        async (pos) => {
          currentLat = pos.coords.latitude;
          currentLng = pos.coords.longitude;
          updateMapCenter();
          await transmitLocation("Active tracking");
        },
        () => {
          console.warn("GPS fetch skipped this cycle.");
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
      );
    }, 5000);
  }

  function stopAutoTracking() {
    if (!autoTrackingStarted) return;
    autoTrackingStarted = false;
    if (trackingInterval) {
      clearInterval(trackingInterval);
      trackingInterval = null;
    }
  }

  const toggleTrackingBtn = document.getElementById('toggleTrackingBtn');
  if (toggleTrackingBtn) {
    toggleTrackingBtn.addEventListener('click', () => {
      if (autoTrackingStarted) {
        stopAutoTracking();
        toggleTrackingBtn.innerHTML = '▶ Start Live Tracking';
        toggleTrackingBtn.style.backgroundColor = '#1976d2'; // blue
        showAlert("Live tracking paused.", false);
      } else {
        startAutoTracking();
        toggleTrackingBtn.innerHTML = '⏹ Stop Live Tracking';
        toggleTrackingBtn.style.backgroundColor = '#ef5350'; // red
        showAlert("Live tracking resumed.", false);
      }
    });
  }

  updateGeoBtn.addEventListener('click', async () => {
    updateGeoBtn.disabled = true;
    updateGeoBtn.textContent = 'Acquiring GPS...';

    if (!("geolocation" in navigator)) {
      updateGeoBtn.disabled = false;
      updateGeoBtn.textContent = '📍 Live Position Update';
      showAlert("Geolocation is not supported in this browser.", true);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        currentLat = pos.coords.latitude;
        currentLng = pos.coords.longitude;
        updateMapCenter();
        await transmitLocation("Manual live update");
      },
      async () => {
        updateGeoBtn.disabled = false;
        updateGeoBtn.textContent = '📍 Live Position Update';
        showAlert("Could not fetch current GPS location.", true);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  });

  async function performFieldSync() {
    try {
      const teamRes = await api.get('/api/rescue/live-status').catch(() => ({ data: [] }));
      const teams = Array.isArray(teamRes.data) ? teamRes.data : [];

      const myLiveStatus = teams.find(t =>
        String(t?.rescueTeam?.authUser?.userId || '').toLowerCase() === 
        String(user?.userId || '').toLowerCase()
      );

      if (myLiveStatus) {
        myProfileId = myLiveStatus.rescueTeam.id;
        missionText.textContent = `STATUS: ${myLiveStatus.rescueTeam.currentStatus || 'ACTIVE DISPATCH'}`;

        const stat = myLiveStatus.rescueTeam.currentStatus;
        if (stat === 'ASSIGNED' || stat === 'IN_PROGRESS') {
          statusDot.className = 'pulse-dot blue';
        } else if (stat === 'OFFLINE') {
          statusDot.className = 'pulse-dot grey';
        } else {
          statusDot.className = 'pulse-dot green';
        }

        if (myLiveStatus.latitude != null && myLiveStatus.longitude != null) {
          currentLat = myLiveStatus.latitude;
          currentLng = myLiveStatus.longitude;
          updateMapCenter();
        }

        if (myMarker) {
          myMarker.bindPopup(`
            <b>${myLiveStatus.rescueTeam?.teamName || 'Rescue Team'}</b><br>
            Code: ${myLiveStatus.rescueTeam?.teamCode || 'Unknown'}<br>
            Location: ${myLiveStatus.locationName || 'Unknown'}<br>
            Status: ${myLiveStatus.statusMessage || 'Active'}<br>
            Urgency: ${myLiveStatus.urgencyLevel || 'NORMAL'}
          `);
        }

        console.log("DEBUG: Rescue Live Status Payload:", myLiveStatus);

        const inc = myLiveStatus.assignedIncident;
        const isInactive = inc ? ['RESOLVED', 'COMPLETED', 'CLOSED'].includes(String(inc.status || '').toUpperCase()) : false;

        if (inc && !isInactive && inc.latitude != null && inc.longitude != null) {
          let needsReframing = false;

          if (targetMarker) {
            const oldLat = targetMarker.getLatLng().lat;
            const oldLng = targetMarker.getLatLng().lng;
            if (oldLat !== inc.latitude || oldLng !== inc.longitude) {
              targetMarker.setLatLng([inc.latitude, inc.longitude]);
              needsReframing = true;
            }
          } else {
            targetMarker = L.marker([inc.latitude, inc.longitude], { icon: incidentIcon }).addTo(map);
            needsReframing = true;
          }

          targetMarker.bindPopup(`
            <b>INCIDENT #${inc.id}</b><br>
            Type: ${inc.incidentType || 'Unknown'}<br>
            Location: ${inc.locationName || 'Unknown'}<br>
            Status: ${inc.status || 'ASSIGNED'}<br>
            Urgency: ${inc.urgencyLevel || 'NORMAL'}
          `);
          
          if (needsReframing) {
            updateMapCenter(true);
          }
        } else {
          if (inc && isInactive) {
            console.log("DEBUG: Assigned incident is resolved/completed. Removing marker.");
          } else {
            console.warn("DEBUG: Assigned incident is missing or lacks coordinates completely:", inc);
          }
          if (targetMarker) {
            map.removeLayer(targetMarker);
            targetMarker = null;
            updateMapCenter(true); // Refit bounds back to solo rescue marker
          }
        }
      }

      const inboxRes = await api.get(`/api/messages/inbox?role=RESCUE_TEAM&profileId=${myProfileId}`).catch(() => ({ data: [] }));
      const messages = Array.isArray(inboxRes.data) ? inboxRes.data : [];

      if (messages.length === 0) {
        inboxList.innerHTML = `<div class="empty-inbox">No active directives.</div>`;
      } else {
        inboxList.innerHTML = messages.map(m => {
          const isCritical =
            (m.messageText || '').toUpperCase().includes('URGENT') ||
            m.messageType === 'EMERGENCY';
          
          const isAcked = ackedMessages.has(m.id);

          return `
            <div class="msg-card ${isCritical ? 'critical' : ''}">
              <div class="msg-header">
                <span>CONTROL ➜ DEPLOYMENT</span>
                <span>${m.createdAt ? new Date(m.createdAt).toLocaleTimeString() : ''}</span>
              </div>
              <div class="msg-body">${m.messageText || ''}</div>
              <div class="msg-action-row" style="display:flex; justify-content:space-between; align-items:center;">
                ${isAcked ? 
                  `<span style="color:#69f0ae; font-size:0.75rem; font-weight:bold; letter-spacing:0.5px;">✓ ACKNOWLEDGED</span>` : 
                  `<button class="btn-micro" onclick="window.sendMsgAction(${m.id}, 'ACKNOWLEDGED')">ACKNOWLEDGE</button>`
                }
                <button class="btn-micro" style="margin-left: auto;" onclick="window.sendMsgAction(${m.id}, 'DELETED')">CLEAR</button>
              </div>
            </div>
          `;
        }).join('');
      }

      syncStamp.textContent = new Date().toLocaleTimeString('en-US', { hour12: false });

    } catch (err) {
      console.error("Rescue Sync Err", err);
      syncStamp.textContent = "SYNC ERR";
      statusDot.className = 'pulse-dot red';
    }
  }

  document.getElementById('rescueTransmitForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const content = document.getElementById('transmissionBody').value.trim();
    if (!content) {
      showAlert('Message content is required.', true);
      return;
    }

    try {
      await api.post('/api/messages/send', {
        senderUserId: user.userId,
        receiverRole: 'CONTROL_ROOM',
        receiverId: 1,
        messageText: content,
        messageType: 'CUSTOM'
      });

      showAlert('Transmission Successful.');
      document.getElementById('transmissionBody').value = '';
      performFieldSync();
    } catch (err) {
      showAlert(err.message || 'Transmission Failed.', true);
    }
  });

  window.sendQuickStatus = async (statusLabel) => {
    try {
      await api.post('/api/messages/send', {
        senderUserId: user.userId,
        receiverRole: 'CONTROL_ROOM',
        receiverId: 1,
        messageText: `STATUS UPDATE: ${statusLabel}. L/L: ${currentLat.toFixed(4)}, ${currentLng.toFixed(4)}`,
        messageType: 'STATUS_UPDATE'
      });

      showAlert(`Broadcasted: ${statusLabel}`);
      performFieldSync();
    } catch (err) {
      showAlert(err.message || 'Quick Action Failed.', true);
    }
  };

  window.completeMission = async () => {
    try {
      if (!confirm("Confirm Mission Completed? This will resolve the incident and clear your active assignment.")) return;

      await api.post('/api/assignments/complete', { userId: user.userId });
      
      // Keep emitting the message so control room sees the log
      await api.post('/api/messages/send', {
        senderUserId: user.userId,
        receiverRole: 'CONTROL_ROOM',
        receiverId: 1,
        messageText: `STATUS UPDATE: Mission Completed. L/L: ${currentLat.toFixed(4)}, ${currentLng.toFixed(4)}`,
        messageType: 'STATUS_UPDATE'
      });

      showAlert(`Mission Completed! Operations cleared.`);
      performFieldSync();
    } catch (err) {
      showAlert(err.message || 'Mission completion failed.', true);
    }
  };

  window.sendMsgAction = async (msgId, actionStr) => {
    try {
      await api.post('/api/messages/action', {
        messageId: parseInt(msgId),
        actionType: actionStr,
        userId: user.userId
      });

      if (actionStr === 'ACKNOWLEDGED') {
        ackedMessages.add(parseInt(msgId));
      }

      performFieldSync();
    } catch (err) {
      showAlert(err.message || "Action Failed.", true);
    }
  };

  setTimeout(() => map.invalidateSize(), 300);
  updateMapCenter();
  performFieldSync();
  startAutoTracking();
  setInterval(performFieldSync, 5000);
});
