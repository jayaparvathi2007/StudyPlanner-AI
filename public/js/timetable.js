/**
 * AI Adaptive Study Planner - Timetable & Scheduling Logic
 */

let timetableCurrentMode = 'daily'; // 'daily' or 'weekly'
let timetableSelectedDate = new Date();

async function loadTimetableView() {
  const dateStr = timetableSelectedDate.toISOString().split('T')[0];
  const dateInput = document.getElementById('timetableDateInput');
  if (dateInput) dateInput.value = dateStr;

  const container = document.getElementById('timetableRenderContainer');
  container.innerHTML = `<div style="text-align:center; padding: 40px; color: var(--text-muted);">Loading study timetable...</div>`;

  try {
    if (timetableCurrentMode === 'daily') {
      const sessions = await fetchJson(`timetable?view=daily&date=${dateStr}`);
      renderDailyTimetable(sessions, timetableSelectedDate);
    } else {
      const sessions = await fetchJson(`timetable?view=weekly&startDate=${dateStr}`);
      renderWeeklyTimetable(sessions, timetableSelectedDate);
    }
  } catch (err) {
    showToast('Failed to load timetable', err.message, 'danger');
  }
}

function setTimetableView(mode) {
  timetableCurrentMode = mode;
  document.getElementById('btnDailyView').classList.toggle('active', mode === 'daily');
  document.getElementById('btnWeeklyView').classList.toggle('active', mode === 'weekly');
  loadTimetableView();
}

function changeTimetableDate(deltaDays) {
  const multiplier = timetableCurrentMode === 'weekly' ? 7 : 1;
  timetableSelectedDate.setDate(timetableSelectedDate.getDate() + (deltaDays * multiplier));
  loadTimetableView();
}

function onTimetableDatePicked(val) {
  if (val) {
    timetableSelectedDate = new Date(val);
    loadTimetableView();
  }
}

function jumpTimetableToToday() {
  timetableSelectedDate = new Date();
  loadTimetableView();
}

// =========================================================================
// Daily Timetable Rendering
// =========================================================================
function renderDailyTimetable(sessions, dateObj) {
  const container = document.getElementById('timetableRenderContainer');
  const dayName = dateObj.toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric', year: 'numeric' });

  if (!sessions || sessions.length === 0) {
    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <h3>📅 Schedule for ${dayName}</h3>
          </div>
        </div>
        <div class="empty-state">
          <div class="empty-state-icon">🛋️</div>
          <h4>No study sessions scheduled for this day</h4>
          <p>Generate an adaptive schedule or pick a different date.</p>
          <button class="btn btn-primary" onclick="openGenerateTimetableModal()">🗓️ Generate Timetable</button>
        </div>
      </div>
    `;
    return;
  }

  container.innerHTML = `
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <h3>📅 Schedule for ${dayName}</h3>
          <span class="card-badge">${sessions.length} Session${sessions.length === 1 ? '' : 's'}</span>
        </div>
      </div>
      <div class="session-list">
        ${sessions.map(s => {
          const isDone = s.status === 'COMPLETED';
          const isMissed = s.status === 'MISSED';
          const isRescheduled = s.status === 'RESCHEDULED';
          const isRemedial = s.remedial;
          const timeFormatted = (s.startTime || '09:00:00').substring(0, 5);

          let badgeHtml = `<span class="badge badge-in-progress">${s.durationMinutes} min</span>`;
          if (isDone) badgeHtml = `<span class="badge badge-completed">✓ Done (${s.actualDurationMinutes || s.durationMinutes}m)</span>`;
          if (isMissed) badgeHtml = `<span class="badge badge-missed">Missed</span>`;
          if (isRescheduled) badgeHtml = `<span class="badge badge-rescheduled">Rescheduled</span>`;
          if (isRemedial) badgeHtml += ` <span class="badge badge-hard">Remedial Makeup</span>`;

          return `
            <div class="session-item ${isDone ? 'completed' : ''} ${isRemedial ? 'remedial' : ''}">
              <div class="session-left">
                <div class="session-time">${timeFormatted}</div>
                <div class="session-details">
                  <div class="session-tag" style="color: ${s.subjectColor || '#4f46e5'};">
                    <span>●</span> ${escapeHtml(s.subjectName || 'Subject')}
                    ${badgeHtml}
                  </div>
                  <div class="session-name">${escapeHtml(s.topicName)}</div>
                  ${s.notes ? `<div class="session-notes">${escapeHtml(s.notes)}</div>` : ''}
                </div>
              </div>

              <div class="session-actions">
                ${!isDone && !isRescheduled ? `
                  <button class="btn btn-success btn-sm" onclick="openCompleteSessionModal(${s.id}, ${s.topicId}, '${escapeHtml(s.topicName)}', ${s.durationMinutes})">
                    ✓ Complete
                  </button>
                  <button class="btn btn-secondary btn-sm" onclick="markSessionMissed(${s.id})">
                    Mark Missed
                  </button>
                ` : ''}
                <button class="btn btn-secondary btn-sm" onclick="launchQuizForTopic(${s.topicId}, '${escapeHtml(s.topicName)}', '${escapeHtml(s.subjectName)}')">
                  🎯 Quiz
                </button>
                <button class="btn btn-danger btn-sm" style="padding: 6px 10px;" onclick="deleteTimetableSlot(${s.id})">
                  ✕
                </button>
              </div>
            </div>
          `;
        }).join('')}
      </div>
    </div>
  `;
}

// =========================================================================
// Weekly Timetable Rendering
// =========================================================================
function renderWeeklyTimetable(sessions, startDateObj) {
  const container = document.getElementById('timetableRenderContainer');
  const todayStr = new Date().toISOString().split('T')[0];

  // Group sessions by date YYYY-MM-DD
  const sessionsByDate = {};
  sessions.forEach(s => {
    const d = s.scheduledDate;
    if (!sessionsByDate[d]) sessionsByDate[d] = [];
    sessionsByDate[d].push(s);
  });

  const dayColumns = [];
  for (let i = 0; i < 7; i++) {
    const dayDate = new Date(startDateObj);
    dayDate.setDate(dayDate.getDate() + i);
    const dayIso = dayDate.toISOString().split('T')[0];
    const isToday = dayIso === todayStr;

    const dayName = dayDate.toLocaleDateString('en-US', { weekday: 'short' });
    const dayNum = dayDate.toLocaleDateString('en-US', { month: 'numeric', day: 'numeric' });

    const daySessions = sessionsByDate[dayIso] || [];

    dayColumns.push(`
      <div class="day-column">
        <div class="day-header ${isToday ? 'today' : ''}">
          <div class="day-name">${dayName}</div>
          <div class="day-date">${dayNum}</div>
        </div>
        <div class="day-slots">
          ${daySessions.length > 0 ? daySessions.map(s => {
            const timeStr = (s.startTime || '09:00:00').substring(0, 5);
            const isDone = s.status === 'COMPLETED';
            const isMissed = s.status === 'MISSED';
            const isRemedial = s.remedial;

            return `
              <div class="timetable-slot ${isDone ? 'completed' : ''} ${isRemedial ? 'remedial' : ''}" 
                   style="border-left-color: ${s.subjectColor || '#4f46e5'};"
                   onclick="openCompleteSessionModal(${s.id}, ${s.topicId}, '${escapeHtml(s.topicName)}', ${s.durationMinutes})">
                <div class="slot-time">
                  ${timeStr} (${s.durationMinutes}m)
                  ${isDone ? '✓' : ''}
                  ${isMissed ? '❌' : ''}
                  ${isRemedial ? '⚡' : ''}
                </div>
                <div class="slot-topic" title="${escapeHtml(s.topicName)}">
                  ${escapeHtml(s.topicName)}
                </div>
                <div style="font-size: 10px; color: ${s.subjectColor || '#64748b'}; font-weight: 600;">
                  ${escapeHtml(s.subjectName)}
                </div>
              </div>
            `;
          }).join('') : `
            <div style="text-align: center; color: var(--text-light); font-size: 11px; padding: 20px 0;">
              Free day
            </div>
          `}
        </div>
      </div>
    `);
  }

  container.innerHTML = `
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <h3>🗓️ 7-Day Weekly Grid View</h3>
        </div>
      </div>
      <div class="weekly-grid">
        ${dayColumns.join('')}
      </div>
    </div>
  `;
}

// =========================================================================
// Timetable Generation Modal Handlers
// =========================================================================
function openGenerateTimetableModal() {
  document.getElementById('genStartDateInput').value = new Date().toISOString().split('T')[0];
  document.getElementById('genDaysAheadInput').value = 7;
  document.getElementById('genDailyHoursInput').value = 3.5;
  document.getElementById('genClearFutureCheckbox').checked = true;
  document.getElementById('generateTimetableModal').classList.add('active');
}

function closeGenerateTimetableModal() {
  document.getElementById('generateTimetableModal').classList.remove('active');
}

async function executeTimetableGeneration() {
  const startDate = document.getElementById('genStartDateInput').value;
  const daysAhead = parseInt(document.getElementById('genDaysAheadInput').value) || 7;
  const dailyHours = parseFloat(document.getElementById('genDailyHoursInput').value) || 3.0;
  const clearFuture = document.getElementById('genClearFutureCheckbox').checked;

  try {
    showToast('Generating Timetable', 'Synthesizing deadlines, topic difficulties, and spaced interleaving...', 'info');
    const res = await fetchJson('timetable/generate', {
      method: 'POST',
      body: JSON.stringify({ startDate, daysAhead, dailyHours, clearFuture })
    });
    showToast('Success', res.message, 'success');
    closeGenerateTimetableModal();
    loadTimetableView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

// =========================================================================
// Complete Session Modal
// =========================================================================
function openCompleteSessionModal(sessionId, topicId, topicName, durationMinutes) {
  document.getElementById('completeSessionId').value = sessionId;
  document.getElementById('completeTopicId').value = topicId;
  document.getElementById('completeTopicName').innerText = topicName;
  document.getElementById('completeActualMinutesInput').value = durationMinutes || 60;
  document.getElementById('completeNotesInput').value = '';
  document.getElementById('completeMarkTopicCheckbox').checked = true;
  document.getElementById('completeSessionModal').classList.add('active');
}

function closeCompleteSessionModal() {
  document.getElementById('completeSessionModal').classList.remove('active');
}

async function confirmCompleteSession() {
  const sessionId = parseInt(document.getElementById('completeSessionId').value);
  const topicId = parseInt(document.getElementById('completeTopicId').value);
  const actualDurationMinutes = parseInt(document.getElementById('completeActualMinutesInput').value) || 60;
  const notes = document.getElementById('completeNotesInput').value.trim();
  const markTopicCompleted = document.getElementById('completeMarkTopicCheckbox').checked;

  try {
    const res = await fetchJson('timetable/complete', {
      method: 'POST',
      body: JSON.stringify({
        sessionId,
        topicId,
        actualDurationMinutes,
        notes,
        markTopicCompleted,
        confidenceLevel: 4
      })
    });
    showToast('Recorded', res.message, 'success');
    closeCompleteSessionModal();

    if (App.currentView === 'dashboard') loadDashboard();
    else loadTimetableView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

async function markSessionMissed(sessionId) {
  try {
    const res = await fetchJson('timetable/miss', {
      method: 'POST',
      body: JSON.stringify({ sessionId })
    });
    showToast('Marked Missed', res.message, 'warning');
    loadTimetableView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

async function deleteTimetableSlot(id) {
  if (!confirm('Remove this session slot from your timetable?')) return;
  try {
    await fetchJson(`timetable/${id}`, { method: 'DELETE' });
    showToast('Deleted', 'Session slot removed.', 'info');
    loadTimetableView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}
