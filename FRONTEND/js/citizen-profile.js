// FRONTEND/js/citizen-profile.js

document.addEventListener('DOMContentLoaded', async () => {

  if (!AuthManager.isLoggedIn()) {
    window.location.href = 'login.html';
    return;
  }

  const user = AuthManager.getUserData();
  const alertBox = document.getElementById('alertBox');
  const historyContainer = document.getElementById('historyContainer');
  const emptyHistory = document.getElementById('emptyHistory');

  // Mobile Menu
  document.getElementById('mobileMenuBtn').addEventListener('click', () => {
    document.getElementById('navLinks').classList.toggle('open');
  });

  document.getElementById('logoutBtn').addEventListener('click', () => {
    AuthManager.logout();
  });

  function showAlert(msg, isError=true) {
    alertBox.textContent = msg;
    alertBox.className = `alert ${isError ? 'error' : 'success'}`;
    alertBox.classList.remove('hidden');
  }

  // --- Fetch Real Profile Data ---
  let currentFoundCitizenId = null;
  try {
    let profileData = null;
    
    // Attempt 1: Brute force IDs 1-20 to find a matching full name or email 
    // (since backend doesn't return citizenId on login and CitizenController requires Long id)
    for(let i=1; i<=20; i++) {
        try {
            const { data } = await api.get(`/api/citizen/${i}/profile`);
            if (data && (data.fullName === user.name || data.userId === user.userId)) {
                profileData = data;
                currentFoundCitizenId = i;
                break;
            }
        } catch(e) { } // Ignore 400s or not found strings
    }

    // Attempt 2: If we still can't find it, check if they have incidents and extract from there
    if (!profileData) {
        try {
           const { data: incidents } = await api.get('/api/incidents');
           if (Array.isArray(incidents)) {
               const myIncident = incidents.find(inc => inc.citizen && inc.citizen.authUser && inc.citizen.authUser.userId === user.userId);
               if (myIncident) {
                   const { data } = await api.get(`/api/citizen/${myIncident.citizen.id}/profile`);
                   profileData = data;
                   currentFoundCitizenId = myIncident.citizen.id;
               }
           }
        } catch(e) {}
    }

    if (!profileData) {
        throw new Error("Could not map session to backend Citizen ID.");
    }
    
    const data = profileData;

    document.getElementById('profName').textContent = data.fullName || user.name || 'N/A';
    document.getElementById('profUserId').textContent = data.userId || user.userId || 'N/A';
    document.getElementById('profEmail').textContent = data.email || 'N/A';
    document.getElementById('profPhone').textContent = data.phoneNumber || 'N/A';
    document.getElementById('profAddress').textContent = data.address || 'N/A';

    const reports = data.reports || [];
    document.getElementById('totalReports').textContent = reports.length;

    if (reports.length === 0) {
      emptyHistory.style.display = 'block';
    } else {
      emptyHistory.style.display = 'none';
      
      reports.forEach(rep => {
        const item = document.createElement('div');
        item.className = 'history-item';
        
        item.innerHTML = `
          <div class="history-item-left">
            <h4>${rep.incident?.incidentType || 'Disaster Report'}</h4>
            <div class="history-meta">
              <span>📍 ${rep.incident?.locationName || 'Unknown Location'}</span>
              <span>📅 ${rep.reportedAt ? new Date(rep.reportedAt).toLocaleDateString() : 'Recent'}</span>
            </div>
          </div>
          <div class="history-status status-Reported">Reported</div>
        `;
        historyContainer.appendChild(item);
      });
    }

    // --- Profile Mutability Logic ---
    const editBtn = document.getElementById('editProfileBtn');
    const saveBtn = document.getElementById('saveProfileBtn');
    
    if (editBtn && saveBtn) {
      editBtn.addEventListener('click', () => {
        document.getElementById('profNameInput').classList.remove('hidden');
        document.getElementById('profNameInput').value = document.getElementById('profName').innerText;
        document.getElementById('profName').classList.add('hidden');

        document.getElementById('profPhoneInput').classList.remove('hidden');
        document.getElementById('profPhoneInput').value = document.getElementById('profPhone').innerText;
        document.getElementById('profPhone').classList.add('hidden');

        document.getElementById('profAddressInput').classList.remove('hidden');
        document.getElementById('profAddressInput').value = document.getElementById('profAddress').innerText;
        document.getElementById('profAddress').classList.add('hidden');

        editBtn.classList.add('hidden');
        saveBtn.classList.remove('hidden');
      });

      saveBtn.addEventListener('click', async () => {
        if (!currentFoundCitizenId) return showAlert('Cannot update profile without verified DB linkage.', true);

        const newName = document.getElementById('profNameInput').value.trim();
        const newPhone = document.getElementById('profPhoneInput').value.trim();
        const newAddress = document.getElementById('profAddressInput').value.trim();

        if (!newName || !newPhone || !newAddress) return showAlert('All editable fields are required.', true);
        
        const pwd = prompt("Security check: Please enter your current profile password to authorize changes:");
        if (!pwd) return showAlert("Profile update cancelled.", true);

        try {
          await api.post('/api/auth/login', { userId: user.userId, password: pwd });
        } catch(e) {
          return showAlert("Authentication failed. Profile save rejected.", true);
        }

        try {
          editBtn.disabled = true;
          saveBtn.textContent = 'Saving...';
          await api.put(`/api/citizen/${currentFoundCitizenId}/profile`, { fullName: newName, phoneNumber: newPhone, address: newAddress });
          
          showAlert('Profile updated successfully! Reloading...', false);
          
          setTimeout(() => {
              window.location.reload();
          }, 1500);
          
        } catch (err) {
          showAlert(err.message || 'Failed to update profile', true);
          saveBtn.textContent = 'Save';
          editBtn.disabled = false;
        }
      });
    }

  } catch (error) {
    console.error("Profile Fetch Error:", error);
    showAlert("Could not retrieve full profile data from server. Displaying cached session details.");
    
    document.getElementById('profName').textContent = user.name || 'N/A';
    document.getElementById('profUserId').textContent = user.userId || 'N/A';
    document.getElementById('profEmail').textContent = 'N/A';
    document.getElementById('profPhone').textContent = 'N/A';
    document.getElementById('profAddress').textContent = 'N/A';
    document.getElementById('totalReports').textContent = '0';
    emptyHistory.style.display = 'block';
  }

});
