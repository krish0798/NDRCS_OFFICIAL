// FRONTEND/js/citizen-home.js

document.addEventListener('DOMContentLoaded', () => {

  // Auth Protection
  if (!AuthManager.isLoggedIn()) {
    window.location.href = 'login.html';
    return;
  }

  const user = AuthManager.getUserData();
  
  // Update Welcome Header
  const welcomeHeader = document.getElementById('welcomeHeader');
  if(user.name) {
    welcomeHeader.textContent = `Welcome, ${user.name}`;
  }

  // Logout Handler
  document.getElementById('logoutBtn').addEventListener('click', () => {
    AuthManager.logout();
  });

  // Mobile Menu Toggle
  const mobileMenuBtn = document.getElementById('mobileMenuBtn');
  const navLinks = document.getElementById('navLinks');
  
  mobileMenuBtn.addEventListener('click', () => {
    navLinks.classList.toggle('open');
  });

});
