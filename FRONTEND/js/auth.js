// FRONTEND/js/auth.js - Auth state management
const AuthManager = {
  login(sessionData) {
    // Store simple user info
    if (sessionData.userId) localStorage.setItem('ndrcs_userId', sessionData.userId);
    if (sessionData.role) localStorage.setItem('ndrcs_role', sessionData.role);
    if (sessionData.fullName) localStorage.setItem('ndrcs_name', sessionData.fullName);
  },

  logout() {
    localStorage.removeItem('ndrcs_userId');
    localStorage.removeItem('ndrcs_role');
    localStorage.removeItem('ndrcs_name');
    window.location.href = 'index.html';
  },

  isLoggedIn() {
    return !!localStorage.getItem('ndrcs_userId');
  },

  getUserData() {
    return {
      userId: localStorage.getItem('ndrcs_userId'),
      role: localStorage.getItem('ndrcs_role'),
      name: localStorage.getItem('ndrcs_name')
    };
  }
};
