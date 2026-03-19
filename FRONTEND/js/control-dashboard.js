// FRONTEND/js/control-dashboard.js

document.addEventListener('DOMContentLoaded', () => {

  if (!AuthManager.isLoggedIn()) {
    window.location.href = 'login.html';
    return;
  }

  const user = AuthManager.getUserData();

  if (user.role !== 'CONTROL_ROOM') {
    alert("CRITICAL ERROR: Unauthorized Account Level. Returning to portal.");
    window.location.href = 'citizen-home.html';
    return;
  }

  document.getElementById('logoutBtn').addEventListener('click', () => AuthManager.logout());

  const navSyncTime = document.getElementById('navSyncTime');
  const metricTotal = document.getElementById('metricTotal');
  const metricAssigned = document.getElementById('metricAssigned');
  const metricTeams = document.getElementById('metricTeams');
  const metricResolved = document.getElementById('metricResolved');

  const incidentListTarget = document.getElementById('incidentListTarget');
  const inboxTarget = document.getElementById('inboxTarget');

  const assignIncidentId = document.getElementById('assignIncidentId');
  const assignTeamId = document.getElementById('assignTeamId');

  const dispatchAlert = document.getElementById('dispatchAlert');
  const transmitAlert = document.getElementById('transmitAlert');

  function showSmallAlert(el, msg, isError = false) {
    el.textContent = msg;
    el.className = `alert ${isError ? 'error' : 'success'}`;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 5000);
  }

  const mumbaiCenter = [19.0760, 72.8777];
  const map = L.map('controlMap', {
    center: mumbaiCenter,
    zoom: 12,
    zoomControl: true
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  const incidentIcon = L.divIcon({
    className: 'incident-marker',
    iconSize: [16, 16]
  });

  const rescueIcon = L.divIcon({
    className: 'rescue-marker',
    iconSize: [16, 16]
  });

  const incidentLayer = L.layerGroup().addTo(map);
  const rescueLayer = L.layerGroup().addTo(map);

  let cachedIncidents = [];
  let cachedTeams = [];
  const ackedMessages = new Set();

  async function performSync() {
    try {
      navSyncTime.style.color = "#ffeb3b";

      const summaryRes = await api.get('/api/dashboard/summary').catch(() => ({ data: {} }));
      const summary = summaryRes.data || {};

      const incRes = await api.get('/api/incidents').catch(() => ({ data: [] }));
      cachedIncidents = Array.isArray(incRes.data) ? incRes.data : [];

      const teamRes = await api.get('/api/rescue/live-status').catch(() => ({ data: [] }));
      cachedTeams = Array.isArray(teamRes.data) ? teamRes.data : [];

      const myProfileId = 1;
      const inboxRes = await api.get(`/api/messages/inbox?role=CONTROL_ROOM&profileId=${myProfileId}`).catch(() => ({ data: [] }));
      const messages = Array.isArray(inboxRes.data) ? inboxRes.data : [];

      metricTotal.textContent = summary.totalIncidents ?? cachedIncidents.length;
      metricAssigned.textContent = summary.activeAssignments ?? 0;
      metricTeams.textContent = summary.liveTrackedRescueTeams ?? cachedTeams.length;
      metricResolved.textContent = summary.resolvedIncidents ?? cachedIncidents.filter(i => i.status === 'RESOLVED').length;

      incidentLayer.clearLayers();
      cachedIncidents.forEach(inc => {
        const isInactive = ['RESOLVED', 'COMPLETED', 'CLOSED'].includes(String(inc.status || '').toUpperCase());
        if (!isInactive && inc.latitude != null && inc.longitude != null) {
          L.marker([inc.latitude, inc.longitude], { icon: incidentIcon })
            .addTo(incidentLayer)
            .bindPopup(`
              <b>INCIDENT #${inc.id}</b><br>
              Type: ${inc.incidentType || 'Unknown'}<br>
              Location: ${inc.locationName || 'Unknown'}<br>
              Status: ${inc.status || 'REPORTED'}<br>
              Urgency: ${inc.urgencyLevel || 'NORMAL'}
            `);
        }
      });

      rescueLayer.clearLayers();
      cachedTeams.forEach(team => {
        const isOffline = (team.statusMessage || '').toUpperCase().includes('OFFLINE');
        if (!isOffline && team.latitude != null && team.longitude != null) {
          L.marker([team.latitude, team.longitude], { icon: rescueIcon })
            .addTo(rescueLayer)
            .bindPopup(`
              <b>${team.rescueTeam?.teamName || 'Rescue Team'}</b><br>
              Code: ${team.rescueTeam?.teamCode || 'Unknown'}<br>
              Location: ${team.locationName || 'Unknown'}<br>
              Status: ${team.statusMessage || 'Active'}<br>
              Urgency: ${team.urgencyLevel || 'NORMAL'}
            `);
        }
      });

      if (cachedIncidents.length === 0) {
        incidentListTarget.innerHTML = `<div class="loader-text">Zero Active Incidents in Sector.</div>`;
      } else {
        incidentListTarget.innerHTML = cachedIncidents.map(i => `
          <div class="list-item">
            <div class="list-item-header">
              <span class="item-id">ID: ${i.id}</span>
              <span class="item-status status-${(i.status || '').toLowerCase()}">${i.status || 'REPORTED'}</span>
            </div>
            <div class="item-title">${i.incidentType || 'Unknown'} - ${i.urgencyLevel || 'NORMAL'} Priority</div>
            <div class="item-desc">📍 ${i.locationName || `${i.latitude}, ${i.longitude}`}</div>
          </div>
        `).join('');
      }

      const currIncSelection = assignIncidentId.value;
      const currTeamSelection = assignTeamId.value;

      assignIncidentId.innerHTML =
        `<option value="">Select Incident from Queue</option>` +
        cachedIncidents.map(i => `<option value="${i.id}">#${i.id} - ${i.incidentType}</option>`).join('');

      if (currIncSelection) assignIncidentId.value = currIncSelection;

      assignTeamId.innerHTML =
        `<option value="">Select Available Squad</option>` +
        cachedTeams.map(t => `
          <option value="${t.rescueTeam?.id || ''}">
            ${t.rescueTeam?.teamName || 'Team'} (${t.statusMessage || 'Ready'})
          </option>
        `).join('');

      if (currTeamSelection) assignTeamId.value = currTeamSelection;

      const transmitTarget = document.getElementById('transmitTarget');
      const currTransmitSelection = transmitTarget.value;
      transmitTarget.innerHTML =
        `<option value="">Select Rescue Squad</option>` +
        cachedTeams.map(t => `
          <option value="${t.rescueTeam?.id || ''}">
            ${t.rescueTeam?.teamName || 'Team'} (Code: ${t.rescueTeam?.teamCode || 'Unknown'})
          </option>
        `).join('');

      if (currTransmitSelection) transmitTarget.value = currTransmitSelection;

      if (messages.length === 0) {
        inboxTarget.innerHTML = `<div class="loader-text">Inbox Empty.</div>`;
      } else {
        inboxTarget.innerHTML = messages.map(m => {
          const isAcked = ackedMessages.has(m.id);
          return `
          <div class="list-item">
            <div class="list-item-header">
              <span class="item-id">MSG ID: ${m.id}</span>
              <span class="item-id">${m.createdAt ? new Date(m.createdAt).toLocaleTimeString() : ''}</span>
            </div>
            <div class="item-title" style="font-size: 0.8rem;">${m.messageText || 'No Body'}</div>
            <div class="msg-actions" style="display:flex; justify-content:space-between; align-items:center; margin-top:5px;">
              ${isAcked ? 
                `<span style="color:#69f0ae; font-size: 0.70rem; font-weight: bold;">✓ ACKED</span>` : 
                `<button class="btn-micro" onclick="window.handleMsgAction('${m.id}', 'ACKNOWLEDGED')">ACK</button>`
              }
              <button class="btn-micro" style="margin-left: auto;" onclick="window.handleMsgAction('${m.id}', 'DELETED')">CLR</button>
            </div>
          </div>
        `}).join('');
      }

      const now = new Date();
      navSyncTime.textContent = `SYNC: ${now.toLocaleTimeString('en-US', { hour12: false })}`;
      navSyncTime.style.color = "#69f0ae";

    } catch (err) {
      console.error("Dashboard Sync Error:", err);
      navSyncTime.textContent = `ERROR: SYNC FAILED`;
      navSyncTime.style.color = "#ef5350";
    }
  }

  document.getElementById('dispatchForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const incId = assignIncidentId.value;
    const teamId = assignTeamId.value;

    if (!incId || !teamId) {
      showSmallAlert(dispatchAlert, 'Please select both incident and rescue team.', true);
      return;
    }

    try {
      await api.post('/api/assignments', {
        controlUserId: user.userId,
        incidentId: parseInt(incId),
        rescueTeamId: parseInt(teamId)
      });

      showSmallAlert(dispatchAlert, 'Directive Broadcasted Successfully.', false);
      assignIncidentId.value = '';
      assignTeamId.value = '';
      performSync();
    } catch (err) {
      showSmallAlert(dispatchAlert, err.message || 'Dispatch failed.', true);
    }
  });

  document.getElementById('transmitForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const targetId = document.getElementById('transmitTarget').value.trim();
    const content = document.getElementById('transmitContent').value.trim();

    if (!targetId || !content) {
      showSmallAlert(transmitAlert, 'Target ID and message are required.', true);
      return;
    }

    try {
      const payload = {
        senderUserId: user.userId,
        receiverRole: "RESCUE_TEAM",
        receiverId: parseInt(targetId),
        messageText: content,
        messageType: "CUSTOM"
      };

      await api.post('/api/messages/send', payload);
      showSmallAlert(transmitAlert, 'Transmission Successful.', false);
      document.getElementById('transmitContent').value = '';
      performSync();
    } catch (err) {
      showSmallAlert(transmitAlert, err.message || 'Transmission Failed.', true);
    }
  });

  window.handleMsgAction = async (msgId, actionStr) => {
    if (!confirm(`Execute action: ${actionStr} on Msg ${msgId}?`)) return;

    try {
      await api.post('/api/messages/action', {
        messageId: parseInt(msgId),
        actionType: actionStr,
        userId: user.userId
      });

      if (actionStr === 'ACKNOWLEDGED') {
        ackedMessages.add(parseInt(msgId));
      }

      performSync();
    } catch (err) {
      alert("Action Failed: " + err.message);
    }
  };

  setTimeout(() => map.invalidateSize(), 300);
  performSync();
  setInterval(performSync, 5000);

});
