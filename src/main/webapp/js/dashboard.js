/**
 * AI Adaptive Study Planner - Dashboard Logic
 */

async function loadDashboard() {
  try {
    const data = await fetchJson('dashboard');

    // Update user info
    if (data.student) {
      document.getElementById('userName').innerText = data.student.name;
      document.getElementById('userTarget').innerText = `Target: ${data.student.targetDailyHours} hrs/day`;
      const initials = data.student.name.split(' ').map(n => n[0]).join('').toUpperCase();
      document.getElementById('userAvatar').innerText = initials || 'ST';
    }

    // 1. KPI Cards
    document.getElementById('statStudyHours').innerText = `${data.totalStudyHours} hrs`;
    document.getElementById('statProgress').innerText = `${data.overallProgressPercentage}%`;
    document.getElementById('statTopicsFraction').innerText = `${data.completedTopics} of ${data.totalTopics} topics completed`;

    // Health Score
    const healthEl = document.getElementById('statHealthScore');
    const healthLabel = document.getElementById('statHealthLabel');
    healthEl.innerText = `${data.healthScore}/100`;
    if (data.healthScore >= 75) {
      healthLabel.innerText = '🛡️ High Mastery Pace';
      healthLabel.className = 'stat-subtext highlight';
    } else if (data.healthScore >= 50) {
      healthLabel.innerText = '⚠️ Moderate (Catch up needed)';
      healthLabel.className = 'stat-subtext';
    } else {
      healthLabel.innerText = '🚨 Critical (Remedial alert)';
      healthLabel.className = 'stat-subtext alert';
    }

    // Weak Badge
    const weakBadge = document.getElementById('weakBadge');
    if (weakBadge) {
      if (data.weakTopicsCount > 0) {
        weakBadge.style.display = 'inline-block';
        weakBadge.innerText = data.weakTopicsCount;
      } else {
        weakBadge.style.display = 'none';
      }
    }

    // Nearest Exam
    const nextExamDaysEl = document.getElementById('statNextExamDays');
    const nextExamTitleEl = document.getElementById('statNextExamTitle');
    if (data.upcomingExams && data.upcomingExams.length > 0) {
      const nearest = data.upcomingExams[0];
      const days = nearest.daysRemaining;
      nextExamDaysEl.innerText = days <= 0 ? 'Today!' : `${days}d`;
      nextExamTitleEl.innerText = `${nearest.subjectName} (${nearest.title})`;
      if (days <= 7) {
        nextExamDaysEl.style.color = '#ef4444';
      } else {
        nextExamDaysEl.style.color = '';
      }
    } else {
      nextExamDaysEl.innerText = '--';
      nextExamTitleEl.innerText = 'No upcoming exams';
    }

    // Adaptive Notification Banner text
    const bannerText = document.getElementById('adaptiveBannerText');
    if (data.missedSessionsCount > 0) {
      bannerText.innerHTML = `⚠️ <strong>Adaptive Alert:</strong> You have <strong>${data.missedSessionsCount} missed or overdue session(s)</strong>. Click 'Reschedule Unfinished' to immediately recover lost hours before your exam deadlines.`;
    } else if (data.upcomingExams && data.upcomingExams.length > 0) {
      const nearest = data.upcomingExams[0];
      bannerText.innerHTML = `Upcoming milestone: <strong>${escapeHtml(nearest.title)}</strong> in <strong>${nearest.daysRemaining} days</strong>. Current syllabus completion: <strong>${data.overallProgressPercentage}%</strong>.`;
    }

    // 2. Today's Sessions
    renderTodaySessions(data.todaySessions || []);

    // 3. Coursework Progress in Left Column
    renderDashboardSubjectProgress(data.subjects || []);

    // 4. Upcoming Exams Strip
    renderUpcomingExams(data.upcomingExams || []);

    // 5. AI Strategy Snippet
    renderAiSnippet(data.latestAiRecommendation);

  } catch (err) {
    showToast('Failed to load dashboard', err.message, 'danger');
  }
}

function renderTodaySessions(sessions) {
  const container = document.getElementById('todaySessionsContainer');
  const countBadge = document.getElementById('todayCountBadge');
  countBadge.innerText = `${sessions.length} Session${sessions.length === 1 ? '' : 's'}`;

  if (sessions.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 24px;">
        <div class="empty-state-icon">☕</div>
        <h4>No sessions scheduled today</h4>
        <p>Take a break or click 'Generate Timetable' to plan your study blocks.</p>
        <button class="btn btn-primary btn-sm" onclick="openGenerateTimetableModal()">Schedule Study Slots</button>
      </div>
    `;
    return;
  }

  container.innerHTML = sessions.map(s => {
    const isDone = s.status === 'COMPLETED';
    const isMissed = s.status === 'MISSED';
    const isRemedial = s.remedial;
    const timeFormatted = (s.startTime || '09:00:00').substring(0, 5);

    let statusBadge = `<span class="badge badge-in-progress">${s.durationMinutes} min</span>`;
    if (isDone) statusBadge = `<span class="badge badge-completed">✓ Done (${s.actualDurationMinutes || s.durationMinutes}m)</span>`;
    if (isMissed) statusBadge = `<span class="badge badge-missed">Missed</span>`;
    if (isRemedial) statusBadge += ` <span class="badge badge-hard" style="font-size:10px;">Remedial</span>`;

    return `
      <div class="session-item ${isDone ? 'completed' : ''} ${isRemedial ? 'remedial' : ''}">
        <div class="session-left">
          <div class="session-time">${timeFormatted}</div>
          <div class="session-details">
            <div class="session-tag" style="color: ${s.subjectColor || '#4f46e5'}">
              <span>●</span> ${escapeHtml(s.subjectName || 'Subject')}
              ${statusBadge}
            </div>
            <div class="session-name">${escapeHtml(s.topicName)}</div>
            ${s.notes ? `<div class="session-notes">${escapeHtml(s.notes)}</div>` : ''}
          </div>
        </div>

        <div class="session-actions">
          ${!isDone ? `
            <button class="btn btn-success btn-sm" onclick="openCompleteSessionModal(${s.id}, ${s.topicId}, '${escapeHtml(s.topicName)}', ${s.durationMinutes})">
              ✓ Done
            </button>
            <button class="btn btn-secondary btn-sm" onclick="quickMarkMissed(${s.id})">
              Missed
            </button>
          ` : `
            <button class="btn btn-secondary btn-sm" onclick="launchQuizForTopic(${s.topicId}, '${escapeHtml(s.topicName)}', '${escapeHtml(s.subjectName)}')">
              🎯 Quiz
            </button>
          `}
        </div>
      </div>
    `;
  }).join('');
}

function renderDashboardSubjectProgress(subjects) {
  const container = document.getElementById('dashboardSubjectProgressList');
  if (!subjects || subjects.length === 0) {
    container.innerHTML = `<p style="color: var(--text-muted); font-size: 13px;">No subjects registered yet.</p>`;
    return;
  }

  container.innerHTML = subjects.map(sub => {
    const pct = sub.progressPercentage || 0;
    return `
      <div class="progress-row" style="margin-bottom: 12px;">
        <div class="progress-header">
          <span style="display:flex; align-items:center; gap:6px;">
            <span style="display:inline-block; width:10px; height:10px; border-radius:50%; background:${sub.color || '#4f46e5'};"></span>
            <strong>${escapeHtml(sub.name)}</strong> (${escapeHtml(sub.code)})
          </span>
          <span style="color: var(--text-muted); font-size: 12px;">
            ${sub.completedTopics}/${sub.totalTopics} topics (${pct}%)
          </span>
        </div>
        <div class="progress-bar-outer">
          <div class="progress-bar-inner" style="width: ${pct}%; background: ${sub.color || 'var(--primary)'};"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderUpcomingExams(exams) {
  const container = document.getElementById('upcomingExamsContainer');
  if (!exams || exams.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 18px;">
        <p style="font-size: 13px;">No exams added yet.</p>
        <button class="btn btn-secondary btn-sm" onclick="openAddExamModal()">+ Schedule Exam</button>
      </div>
    `;
    return;
  }

  container.innerHTML = exams.map(e => {
    const days = e.daysRemaining;
    const isUrgent = days <= 7;
    return `
      <div class="exam-pill" style="border-left-color: ${e.subjectColor || '#4f46e5'};">
        <div class="exam-main">
          <h4>${escapeHtml(e.title)}</h4>
          <p>${escapeHtml(e.subjectName)} • Target Score: ${e.targetScore}%</p>
        </div>
        <div class="exam-countdown">
          <div class="countdown-number ${isUrgent ? 'urgent' : ''}">
            ${days <= 0 ? 'Today' : `${days}d`}
          </div>
          <span style="font-size: 10px; color: var(--text-muted); text-transform: uppercase;">
            ${days <= 0 ? 'Exam Day' : 'Remaining'}
          </span>
        </div>
      </div>
    `;
  }).join('');
}

function renderAiSnippet(recommendationText) {
  const container = document.getElementById('dashboardAiSnippet');
  if (!recommendationText) {
    container.innerHTML = `
      <p style="color: var(--text-muted); margin-bottom: 12px;">
        No active study plan generated yet. Let the AI synthesize your exam schedule and weak areas into an optimal strategy.
      </p>
      <button class="btn btn-primary btn-sm" onclick="generateAiRecommendations()">
        Generate 7-Day Strategy 🚀
      </button>
    `;
    return;
  }

  // Display first 2 markdown sections
  const html = parseMarkdown(recommendationText);
  container.innerHTML = `
    <div style="max-height: 220px; overflow-y: auto; padding-right: 4px;">
      ${html}
    </div>
    <div style="margin-top: 12px; text-align: right;">
      <button class="btn btn-secondary btn-sm" onclick="switchView('ai-advisor')">Open Full Strategy →</button>
    </div>
  `;
}

async function quickMarkMissed(sessionId) {
  try {
    const res = await fetchJson('timetable/miss', {
      method: 'POST',
      body: JSON.stringify({ sessionId })
    });
    showToast('Session Marked Missed', res.message, 'warning');
    loadDashboard();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}
