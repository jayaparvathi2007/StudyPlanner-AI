/**
 * AI Adaptive Study Planner - Core Application & Routing
 */

const App = {
  currentView: 'dashboard',
  apiBase: 'api',
  studentId: 1,
  currentUser: null,
  hasApiKey: false,
  subjectsCache: [],
  topicsCache: [],
  examsCache: []
};

// =========================================================================
// API Helper
// =========================================================================
async function fetchJson(endpoint, options = {}) {
  const url = endpoint.startsWith('http') ? endpoint : `${App.apiBase}/${endpoint.replace(/^\//, '')}`;
  const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...(options.headers || {})
    }
  };

  try {
    const response = await fetch(url, config);
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || `Request failed with status ${response.status}`);
    }
    return data;
  } catch (err) {
    console.error(`API Error on ${url}:`, err);
    throw err;
  }
}

// =========================================================================
// Toast Notification Engine
// =========================================================================
function showToast(title, message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  else if (type === 'warning') icon = '⚠️';
  else if (type === 'danger') icon = '❌';

  toast.innerHTML = `
    <div class="toast-icon">${icon}</div>
    <div class="toast-body">
      <div class="toast-title">${escapeHtml(title)}</div>
      <div class="toast-msg">${escapeHtml(message)}</div>
    </div>
    <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 4500);
}

// =========================================================================
// Navigation & Routing
// =========================================================================
function switchView(viewName) {
  App.currentView = viewName;

  // Update URL hash
  window.location.hash = viewName;

  // Update Nav links
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.view === viewName);
  });

  // Update View sections
  document.querySelectorAll('.view-section').forEach(sec => {
    sec.classList.remove('active');
  });

  const activeSec = document.getElementById(`view-${viewName}`);
  if (activeSec) {
    activeSec.classList.add('active');
  }

  // Update Header title & subtitle
  const titleEl = document.getElementById('viewTitle');
  const subEl = document.getElementById('viewSubtitle');

  switch (viewName) {
    case 'dashboard':
      titleEl.innerText = 'Student Dashboard';
      subEl.innerText = 'Overview of subjects, deadlines, study hours, and adaptive recommendations';
      if (typeof loadDashboard === 'function') loadDashboard();
      break;
    case 'subjects':
      titleEl.innerText = 'Academic Coursework & Syllabus';
      subEl.innerText = 'Add, edit, and organize subjects, syllabus topics, difficulty, and exams';
      if (typeof loadSubjectsView === 'function') loadSubjectsView();
      break;
    case 'timetable':
      titleEl.innerText = 'Adaptive Study Timetable';
      subEl.innerText = 'Daily and weekly schedule optimized by exam proximity and spaced interleaving';
      if (typeof loadTimetableView === 'function') loadTimetableView();
      break;
    case 'ai-advisor':
      titleEl.innerText = 'AI Study Advisor & Concept Coach';
      subEl.innerText = 'Personalized 7-day tactical strategy and intuitive topic breakdowns';
      if (typeof loadAiAdvisorView === 'function') loadAiAdvisorView();
      break;
    case 'quiz':
      titleEl.innerText = 'AI Practice Quizzes & Diagnostic Tests';
      subEl.innerText = 'Self-test your mastery; automatically schedule remedial reviews on weak concepts';
      if (typeof loadQuizView === 'function') loadQuizView();
      break;
    case 'analytics':
      titleEl.innerText = 'Analytics & Study Discipline';
      subEl.innerText = 'Track study streaks, subject progress rates, and persistent weak topics';
      if (typeof loadAnalyticsView === 'function') loadAnalyticsView();
      break;
  }
}

// =========================================================================
// Global Adaptive Actions
// =========================================================================
async function triggerAdaptiveRescheduling() {
  try {
    showToast('Scheduler Working', 'Analyzing missed sessions and pending exam deadlines...', 'info');
    const res = await fetchJson('timetable/reschedule', { method: 'POST', body: '{}' });
    if (res.rescheduledCount > 0) {
      showToast('Adaptive Rescheduling Complete', res.message, 'success');
    } else {
      showToast('Timetable Up to Date', res.message, 'info');
    }

    // Refresh current view
    if (App.currentView === 'dashboard') loadDashboard();
    else if (App.currentView === 'timetable') loadTimetableView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

// =========================================================================
// AI Configuration Modal Handlers
// =========================================================================
async function checkAiConfigStatus() {
  try {
    const res = await fetchJson('ai/config');
    App.hasApiKey = res.hasApiKey;
    const dot = document.getElementById('aiDot');
    const label = document.getElementById('aiStatusLabel');

    if (res.hasApiKey) {
      dot.classList.add('active');
      label.innerText = 'Gemini Active';
      label.title = `Model: ${res.model}`;
    } else {
      dot.classList.remove('active');
      label.innerText = 'AI Settings';
      label.title = 'Click to configure Gemini API Key';
    }
  } catch (err) {
    console.warn('Could not check AI config status:', err);
  }
}

function openConfigModal() {
  document.getElementById('configModal').classList.add('active');
}

function closeConfigModal() {
  document.getElementById('configModal').classList.remove('active');
}

async function saveAiConfig() {
  const apiKey = document.getElementById('modalApiKeyInput').value.trim();
  const model = document.getElementById('modalModelSelect').value;

  try {
    const res = await fetchJson('ai/config', {
      method: 'POST',
      body: JSON.stringify({ apiKey, model })
    });
    showToast('Config Saved', res.message, 'success');
    closeConfigModal();
    checkAiConfigStatus();
    if (App.currentView === 'ai-advisor') {
      loadAiAdvisorView();
    }
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

// =========================================================================
// Utility: Markdown to HTML Parser
// =========================================================================
function parseMarkdown(md) {
  if (!md) return '';
  let html = md
    // Headings
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    // Bold & Italic
    .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/gim, '<em>$1</em>')
    // Blockquotes
    .replace(/^\> (.*$)/gim, '<blockquote>$1</blockquote>')
    // Unordered lists
    .replace(/^\s*\-\s(.*$)/gim, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/gim, '<ul>$1</ul>')
    // Clean up duplicate uls
    .replace(/<\/ul>\s*<ul>/gim, '')
    // Paragraphs / line breaks
    .replace(/\n\n/gim, '<p></p>')
    .replace(/\n/gim, '<br>');

  return html;
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.innerText = text;
  return div.innerHTML;
}

// =========================================================================
// User Authentication System (Login, Register, Logout, Session)
// =========================================================================
async function checkAuthStatus() {
  try {
    const res = await fetchJson('auth/me');
    if (res.authenticated && res.student) {
      App.currentUser = res.student;
      App.studentId = res.student.id;
      updateUserProfileUI(res.student);
      closeAuthModal();
      return true;
    } else {
      App.currentUser = null;
      updateUserProfileUI(null);
      openAuthModal();
      return false;
    }
  } catch (err) {
    console.warn('Auth check error:', err);
    return false;
  }
}

function updateUserProfileUI(student) {
  const nameEl = document.getElementById('userName');
  const targetEl = document.getElementById('userTarget');
  const avatarEl = document.getElementById('userAvatar');
  const actionBtn = document.getElementById('authActionBtn');

  if (student) {
    nameEl.innerText = student.name;
    targetEl.innerText = `Target: ${student.targetDailyHours || 3.0} hrs/day`;
    const initials = student.name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
    avatarEl.innerText = initials || 'ST';
    actionBtn.innerText = '🚪 Sign Out';
    actionBtn.className = 'logout-btn';
    actionBtn.title = 'Sign Out of Account';
  } else {
    nameEl.innerText = 'Guest Student';
    targetEl.innerText = 'Sign in to sync coursework';
    avatarEl.innerText = '👤';
    actionBtn.innerText = '🔑 Sign In';
    actionBtn.className = 'btn btn-primary btn-sm';
    actionBtn.style.padding = '3px 8px';
    actionBtn.title = 'Sign In or Create Account';
  }
}

function openAuthModal() {
  const modal = document.getElementById('authModal');
  if (modal) modal.classList.add('active');
}

function closeAuthModal() {
  const modal = document.getElementById('authModal');
  if (modal) modal.classList.remove('active');
}

function switchAuthTab(tab) {
  const loginTab = document.getElementById('tabBtnLogin');
  const regTab = document.getElementById('tabBtnRegister');
  const loginForm = document.getElementById('loginFormContainer');
  const regForm = document.getElementById('registerFormContainer');

  if (tab === 'login') {
    loginTab.classList.add('active');
    regTab.classList.remove('active');
    loginForm.style.display = 'block';
    regForm.style.display = 'none';
  } else {
    loginTab.classList.remove('active');
    regTab.classList.add('active');
    loginForm.style.display = 'none';
    regForm.style.display = 'block';
  }
}

async function executeLogin() {
  const email = document.getElementById('loginEmailInput').value.trim();
  const password = document.getElementById('loginPasswordInput').value;

  if (!email || !password) {
    showToast('Input Required', 'Please enter your email and password.', 'warning');
    return;
  }

  try {
    const res = await fetchJson('auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });

    App.currentUser = res.student;
    App.studentId = res.student.id;
    updateUserProfileUI(res.student);
    closeAuthModal();
    showToast('Signed In', res.message, 'success');

    // Reload active view with user data
    switchView(App.currentView);
  } catch (err) {
    showToast('Sign In Failed', err.message, 'danger');
  }
}

function fillAndExecuteDemoLogin() {
  document.getElementById('loginEmailInput').value = 'alex.mercer@university.edu';
  document.getElementById('loginPasswordInput').value = 'password123';
  executeLogin();
}

async function executeRegister() {
  const name = document.getElementById('regNameInput').value.trim();
  const email = document.getElementById('regEmailInput').value.trim();
  const password = document.getElementById('regPasswordInput').value;
  const targetDailyHours = parseFloat(document.getElementById('regHoursInput').value) || 3.0;

  if (!name || !email || !password) {
    showToast('Input Required', 'Please fill in all required fields.', 'warning');
    return;
  }

  try {
    const res = await fetchJson('auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password, targetDailyHours })
    });

    App.currentUser = res.student;
    App.studentId = res.student.id;
    updateUserProfileUI(res.student);
    closeAuthModal();
    showToast('Account Created', res.message, 'success');

    // Reload active view
    switchView(App.currentView);
  } catch (err) {
    showToast('Registration Failed', err.message, 'danger');
  }
}

async function handleAuthAction() {
  if (App.currentUser) {
    // Logout
    try {
      await fetchJson('auth/logout', { method: 'POST', body: '{}' });
      App.currentUser = null;
      updateUserProfileUI(null);
      showToast('Logged Out', 'You have been signed out.', 'info');
      openAuthModal();
    } catch (err) {
      showToast('Error', err.message, 'danger');
    }
  } else {
    openAuthModal();
  }
}

// =========================================================================
// App Initialization
// =========================================================================
document.addEventListener('DOMContentLoaded', async () => {
  // Navigation menu click listener
  document.querySelectorAll('.nav-menu .nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const view = item.dataset.view;
      if (view) switchView(view);
    });
  });

  // Check user session
  const isLoggedIn = await checkAuthStatus();

  // Handle URL hash on load
  const hash = window.location.hash.replace('#', '') || 'dashboard';
  if (isLoggedIn) {
    switchView(hash);
  }

  // Check AI status
  checkAiConfigStatus();
});
