/**
 * AI Adaptive Study Planner - Analytics & Discipline Logic
 */

async function loadAnalyticsView() {
  try {
    const data = await fetchJson('analytics');

    // 1. Study Streak
    document.getElementById('analyticsStreakDays').innerText = `${data.studyStreakDays} Day${data.studyStreakDays === 1 ? '' : 's'}`;
    document.getElementById('analyticsQuizAvg').innerText = `${data.overallQuizAverage}%`;
    document.getElementById('analyticsQuizCount').innerText = data.totalQuizzesTaken || 0;

    // 2. Subject Progress Breakdown
    renderAnalyticsSubjectBars(data.subjects || []);

    // 3. Weak Topics List
    const weakTopics = await fetchJson('topics?weak=true');
    renderAnalyticsWeakTopics(weakTopics || []);

  } catch (err) {
    showToast('Failed to load analytics', err.message, 'danger');
  }
}

function renderAnalyticsSubjectBars(subjects) {
  const container = document.getElementById('analyticsSubjectBars');
  if (!subjects || subjects.length === 0) {
    container.innerHTML = `<p style="color: var(--text-muted); font-size: 13px;">No subjects registered.</p>`;
    return;
  }

  container.innerHTML = subjects.map(s => {
    const pct = s.progressPercentage || 0;
    return `
      <div class="progress-row" style="margin-bottom: 16px;">
        <div class="progress-header">
          <span>
            <strong>${escapeHtml(s.name)}</strong> (${escapeHtml(s.code)})
          </span>
          <span style="font-weight: 700; color: ${s.color || 'var(--primary)'};">
            ${pct}% (${s.completedTopics}/${s.totalTopics} topics)
          </span>
        </div>
        <div class="progress-bar-outer">
          <div class="progress-bar-inner" style="width: ${pct}%; background: ${s.color || 'var(--primary)'};"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderAnalyticsWeakTopics(weakTopics) {
  const container = document.getElementById('analyticsWeakTopicsList');
  const countBadge = document.getElementById('analyticsWeakCountBadge');
  countBadge.innerText = `${weakTopics.length} Flagged`;

  if (!weakTopics || weakTopics.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 24px;">
        <div class="empty-state-icon">🌟</div>
        <h4>No Weak Topics Detected!</h4>
        <p>You have demonstrated strong performance across all tested coursework concepts.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = weakTopics.map(t => {
    const stars = '★'.repeat(t.confidenceLevel || 2) + '☆'.repeat(5 - (t.confidenceLevel || 2));
    return `
      <div class="session-item remedial">
        <div class="session-left">
          <div style="font-size: 24px; color: var(--danger);">⚠️</div>
          <div class="session-details">
            <div class="session-tag" style="color: ${t.subjectColor || '#4f46e5'};">
              <span>●</span> ${escapeHtml(t.subjectName || 'Subject')}
              <span class="badge badge-hard">Confidence: ${t.confidenceLevel}/5</span>
              <span class="confidence-stars">${stars}</span>
            </div>
            <div class="session-name">${escapeHtml(t.name)}</div>
            <div class="session-notes" style="color: #b91c1c;">
              Flagged from diagnostic assessments. Prioritized for spaced revision.
            </div>
          </div>
        </div>

        <div class="session-actions">
          <button class="btn btn-primary btn-sm" onclick="launchQuizForTopic(${t.id}, '${escapeHtml(t.name)}', '${escapeHtml(t.subjectName)}')">
            🎯 Test Mastery
          </button>
          <button class="btn btn-secondary btn-sm" onclick="explainWeakTopic('${escapeHtml(t.name)}', '${escapeHtml(t.subjectName)}')">
            💡 AI Explainer
          </button>
        </div>
      </div>
    `;
  }).join('');
}

function explainWeakTopic(topicName, subjectName) {
  switchView('ai-advisor');
  const input = document.getElementById('explainerTopicInput');
  if (input) input.value = topicName;
  requestTopicExplanation(topicName, subjectName);
}
