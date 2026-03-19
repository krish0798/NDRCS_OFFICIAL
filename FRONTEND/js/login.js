// FRONTEND/js/login.js

document.addEventListener('DOMContentLoaded', () => {

  // --- UI & TABS LOGIC ---
  const tabBtns = document.querySelectorAll('.tab-btn');
  const sections = document.querySelectorAll('.auth-section');
  const alertBox = document.getElementById('alert-box');

  function showAlert(msg, type = 'error') {
    alertBox.textContent = msg;
    alertBox.className = `alert ${type}`;
  }

  function hideAlert() {
    alertBox.className = 'alert hidden';
  }

  function switchTab(targetId) {
    hideAlert();
    // Update buttons
    tabBtns.forEach(btn => {
      if (btn.dataset.target === targetId) {
        btn.classList.add('active');
      } else {
        btn.classList.remove('active');
        // also hide OTP tab button if exist conceptually (we didn't make a button for OTP, just the section)
      }
    });

    // Update sections
    sections.forEach(sec => {
      if (sec.id === targetId) {
        sec.classList.remove('hidden');
        sec.classList.add('active');
      } else {
        sec.classList.add('hidden');
        sec.classList.remove('active');
      }
    });
  }

  tabBtns.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      switchTab(btn.dataset.target);
    });
  });

  // --- PASSWORD VISIBILITY TOGGLE ---
  const togglePasswordBtns = document.querySelectorAll('.toggle-password');
  togglePasswordBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-toggle');
      const input = document.getElementById(targetId);
      if (input.type === 'password') {
        input.type = 'text';
        btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #4b5563;"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`; // Hide icon
      } else {
        input.type = 'password';
        btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #4b5563;"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`; // Show icon
      }
    });
  });

  // --- FORM HANDLERS ---
  
  // Realtime Password Strength logic
  const signupPassword = document.getElementById('signupPassword');
  const pwStrengthText = document.getElementById('pwStrengthText');
  let isPasswordStrong = false;
  
  if (signupPassword && pwStrengthText) {
    signupPassword.addEventListener('input', (e) => {
      const val = e.target.value;
      if (!val) {
        pwStrengthText.textContent = '';
        isPasswordStrong = false;
        return;
      }
      
      const hasUpper = /[A-Z]/.test(val);
      const hasLower = /[a-z]/.test(val);
      const hasNum = /[0-9]/.test(val);
      const hasSpecial = /[\W_]/.test(val);
      const isLong = val.length >= 8;
      
      const rulesMet = [hasUpper, hasLower, hasNum, hasSpecial, isLong].filter(Boolean).length;
      
      if (rulesMet === 5) {
        pwStrengthText.textContent = 'Strength: Strong ✓';
        pwStrengthText.style.color = 'var(--success-green)';
        isPasswordStrong = true;
      } else if (rulesMet >= 3) {
        pwStrengthText.textContent = 'Strength: Medium (Add numbers, symbols & uppercase)';
        pwStrengthText.style.color = 'var(--warning-orange)';
        isPasswordStrong = false;
      } else {
        pwStrengthText.textContent = 'Strength: Weak (Requires 8 chars, A-Z, a-z, 0-9, and a symbol)';
        pwStrengthText.style.color = 'var(--alert-red)';
        isPasswordStrong = false;
      }
    });
  }

  // 1. Citizen Signup
  const formCitizenSignup = document.getElementById('form-citizen-signup');
  formCitizenSignup.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();
    
    // Exact schema matching backend: fullName, userId, password, email, phoneNumber, address
    const payload = {
      fullName: document.getElementById('signupFullName').value.trim(),
      userId: document.getElementById('signupUserId').value.trim(),
      email: document.getElementById('signupEmail').value.trim(),
      phoneNumber: document.getElementById('signupPhone').value.trim(),
      address: document.getElementById('signupAddress').value.trim(),
      password: document.getElementById('signupPassword').value
    };

    // Client-side Data Validation Firewall
    if (!/^[A-Za-z ]+$/.test(payload.fullName)) {
      return showAlert('Full Name must contain only English alphabets and spaces.');
    }

    if (!/^\d{10}$/.test(payload.phoneNumber)) {
      return showAlert('Phone Number must be exactly 10 digits.');
    }
    
    if (/^(\d)\1{9}$/.test(payload.phoneNumber)) {
      return showAlert('Phone Number cannot be repetitive fake digits (e.g., 0000000000).');
    }

    if (payload.address.length < 5) {
      return showAlert('Please provide a reasonable and complete residential address.');
    }

    if (!isPasswordStrong) {
      return showAlert('Please use a Strong password meeting all security requirements.');
    }

    // Cache the full name and email locally since backend doesn't return them on login
    localStorage.setItem(`ndrcs_temp_fullname_${payload.userId}`, payload.fullName);
    localStorage.setItem(`ndrcs_temp_email_${payload.userId}`, payload.email);

    try {
      // POST /api/auth/register
      const { data } = await api.post('/api/auth/register', payload);
      
      showAlert('Signup successful! Check your email for OTP.', 'success');
      
      // Auto fill OTP user ID field and switch to OTP section
      document.getElementById('otpUserId').value = payload.userId;
      
      // We manually show OTP section, remove active from tab buttons
      tabBtns.forEach(b => b.classList.remove('active'));
      sections.forEach(s => s.classList.add('hidden'));
      document.getElementById('section-citizen-otp').classList.remove('hidden');

    } catch (err) {
      showAlert(err.message || 'Signup failed. Please try again.');
    }
  });


  // 2. Citizen OTP Verification
  const formCitizenOtp = document.getElementById('form-citizen-otp');
  formCitizenOtp.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();
    
    const userId = document.getElementById('otpUserId').value.trim();
    const token = document.getElementById('otpCode').value.trim(); 
    const email = localStorage.getItem(`ndrcs_temp_email_${userId}`);

    try {
      // POST /api/auth/verify-email
      const payload = { 
        userId: userId, 
        email: email, 
        otpCode: token 
      }; 
      
      const { data } = await api.post('/api/auth/verify-email', payload);
      
      showAlert('Email verified perfectly! You can now login.', 'success');
      formCitizenOtp.reset();
      switchTab('section-citizen-login');

    } catch (err) {
      showAlert(err.message || 'OTP Verification failed.');
    }
  });


  // 3. Citizen Login
  const formCitizenLogin = document.getElementById('form-citizen-login');
  formCitizenLogin.addEventListener('submit', async (e) => {
    e.preventDefault();
    handleLogin(
      document.getElementById('citizenLoginId').value.trim(),
      document.getElementById('citizenLoginPass').value,
      'CITIZEN'
    );
  });


  // 4. Control Room Login
  const formControlLogin = document.getElementById('form-control-login');
  formControlLogin.addEventListener('submit', async (e) => {
    e.preventDefault();
    handleLogin(
      document.getElementById('controlLoginId').value.trim(),
      document.getElementById('controlLoginPass').value,
      'CONTROL_ROOM'
    );
  });


  // 5. Rescue Team Login
  const formRescueLogin = document.getElementById('form-rescue-login');
  formRescueLogin.addEventListener('submit', async (e) => {
    e.preventDefault();
    handleLogin(
      document.getElementById('rescueLoginId').value.trim(),
      document.getElementById('rescueLoginPass').value,
      'RESCUE_TEAM'
    );
  });

  // General Login Handler
  async function handleLogin(userId, password, expectedRole) {
    hideAlert();
    try {
      // POST /api/auth/login
      const payload = { userId, password };
      const { data } = await api.post('/api/auth/login', payload);
      
      // Extract actual role from response ("Login successful: CITIZEN")
      let actualBackendRole = expectedRole;
      if (typeof data === 'string' && data.includes('Login successful:')) {
        actualBackendRole = data.split(':')[1].trim(); 
      } else if (data && data.role) {
        actualBackendRole = data.role;
      }

      const actualRole = String(actualBackendRole || '').toUpperCase();
      const requiredRole = String(expectedRole || '').toUpperCase();

      if (actualRole !== requiredRole) {
        throw new Error(`This account is not authorized for ${requiredRole.replace('_', ' ')} login.`);
      }
      
      const finalRole = actualRole;
      
      const cachedName = localStorage.getItem(`ndrcs_temp_fullname_${userId}`);
      
      AuthManager.login({
        userId: userId,
        role: finalRole,
        fullName: (data && data.fullName) ? data.fullName : (cachedName || userId)
      });

      showAlert('Login successful! Redirecting...', 'success');
      
      // Redirect based on role
      setTimeout(() => {
        if (finalRole === 'CITIZEN') {
          window.location.href = 'citizen-home.html';
        } else if (finalRole === 'CONTROL_ROOM') {
          window.location.href = 'control-dashboard.html';
        } else if (finalRole === 'RESCUE_TEAM') {
          window.location.href = 'rescue-dashboard.html';
        } else {
          window.location.href = 'map.html'; // Fallback
        }
      }, 1000);

    } catch (err) {
      showAlert(err.message || 'Login failed. Invalid credentials.');
    }
  }

});
