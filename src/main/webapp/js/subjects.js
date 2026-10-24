/**
 * AI Adaptive Study Planner - Subjects, Topics, and Exams Management
 */

async function loadSubjectsView() {
  try {
    const subjects = await fetchJson('subjects');
    const topics = await fetchJson('topics');
    const exams = await fetchJson('exams');

    App.subjectsCache = subjects;
    App.topicsCache = topics;
    App.examsCache = exams;

    renderSubjectsGrid(subjects, topics);
    populateSelectDropdowns(subjects);
  } catch (err) {
    showToast('Failed to load coursework', err.message, 'danger');
  }
}

function renderSubjectsGrid(subjects, topics) {
  const container = document.getElementById('subjectsCardsContainer');
  if (!subjects || subjects.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <div class="empty-state-icon">📚</div>
        <h4>No subjects created yet</h4>
        <p>Start by adding your coursework subjects, credit goals, and syllabus topics.</p>
        <button class="btn btn-primary" onclick="openAddSubjectModal()">+ Add Subject</button>
      </div>
    `;
    return;
  }

  // Group topics by subject
  const topicsBySubject = {};
  topics.forEach(t => {
    if (!topicsBySubject[t.subjectId]) topicsBySubject[t.subjectId] = [];
    topicsBySubject[t.subjectId].push(t);
  });

  container.innerHTML = subjects.map(sub => {
    const subTopics = topicsBySubject[sub.id] || [];
    const completed = subTopics.filter(t => t.status === 'COMPLETED').length;
    const total = subTopics.length;
    const pct = total > 0 ? Math.round((completed / total) * 100) : 0;

    return `
      <div class="subject-card">
        <div class="subject-card-header" style="background: ${sub.color || '#4f46e5'};">
          <div>
            <h3>${escapeHtml(sub.name)}</h3>
            <span>${escapeHtml(sub.code)} • Priority: ${sub.priority}</span>
          </div>
          <div style="display: flex; gap: 6px;">
            <button class="btn btn-secondary btn-sm" style="padding: 4px 8px; font-size: 11px;" onclick="openEditSubjectModal(${sub.id})">✏️</button>
            <button class="btn btn-danger btn-sm" style="padding: 4px 8px; font-size: 11px;" onclick="deleteSubject(${sub.id}, '${escapeHtml(sub.name)}')">🗑️</button>
          </div>
        </div>

        <div class="subject-card-body">
          <div style="display: flex; justify-content: space-between; font-size: 13px; font-weight: 600;">
            <span>Syllabus Progress</span>
            <span>${completed}/${total} Topics (${pct}%)</span>
          </div>
          <div class="subject-progress-bar">
            <div class="subject-progress-fill" style="width: ${pct}%; background: ${sub.color || 'var(--primary)'};"></div>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <h5 style="font-size: 12px; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">
              Syllabus Topics
            </h5>
            <button class="btn btn-secondary btn-sm" style="padding: 3px 8px; font-size: 11px;" onclick="openAddTopicForSubject(${sub.id})">
              + Add Topic
            </button>
          </div>

          <div class="topic-list">
            ${subTopics.length > 0 ? subTopics.map(t => {
              const diffClass = `badge-${(t.difficulty || 'medium').toLowerCase()}`;
              const isDone = t.status === 'COMPLETED';
              const stars = '★'.repeat(t.confidenceLevel || 3) + '☆'.repeat(5 - (t.confidenceLevel || 3));

              return `
                <div class="topic-row ${t.weak ? 'weak-topic' : ''}">
                  <div class="topic-info-left">
                    <input type="checkbox" ${isDone ? 'checked' : ''} 
                           onchange="quickToggleTopicStatus(${t.id}, this.checked)" title="Mark completed">
                    <span class="topic-name" title="${escapeHtml(t.name)}">${escapeHtml(t.name)}</span>
                  </div>
                  <div class="topic-meta">
                    <span class="badge ${diffClass}">${t.difficulty}</span>
                    <span class="confidence-stars" title="Confidence: ${t.confidenceLevel}/5">${stars}</span>
                    <button class="btn btn-secondary btn-sm" style="padding: 2px 6px; font-size: 10px;" onclick="openEditTopicModal(${t.id})">✏️</button>
                    <button class="btn btn-secondary btn-sm" style="padding: 2px 6px; font-size: 10px;" onclick="launchQuizForTopic(${t.id}, '${escapeHtml(t.name)}', '${escapeHtml(sub.name)}')">🎯</button>
                    <button class="btn btn-danger btn-sm" style="padding: 2px 6px; font-size: 10px;" onclick="deleteTopic(${t.id})">✕</button>
                  </div>
                </div>
              `;
            }).join('') : `
              <p style="font-size: 12px; color: var(--text-muted); text-align: center; padding: 12px 0;">
                No topics added yet. Click '+ Add Topic' above.
              </p>
            `}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function populateSelectDropdowns(subjects) {
  const selects = ['topicSubjectSelect', 'examSubjectSelect', 'quizSubjectSelect', 'explainerSubjectSelect'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    const curVal = el.value;
    el.innerHTML = `<option value="">-- Choose Subject --</option>` +
      subjects.map(s => `<option value="${s.id}">${escapeHtml(s.name)} (${escapeHtml(s.code)})</option>`).join('');
    if (curVal) el.value = curVal;
  });
}

// =========================================================================
// Subject Modal CRUD
// =========================================================================
function openAddSubjectModal() {
  document.getElementById('subjectModalTitle').innerHTML = `<span>📚</span> Add New Subject`;
  document.getElementById('subjectIdInput').value = '';
  document.getElementById('subjectNameInput').value = '';
  document.getElementById('subjectCodeInput').value = '';
  document.getElementById('subjectColorInput').value = '#4f46e5';
  document.getElementById('subjectPrioritySelect').value = 'MEDIUM';
  document.getElementById('subjectDescInput').value = '';
  document.getElementById('subjectModal').classList.add('active');
}

function openEditSubjectModal(id) {
  const sub = App.subjectsCache.find(s => s.id === id);
  if (!sub) return;

  document.getElementById('subjectModalTitle').innerHTML = `<span>📚</span> Edit Subject`;
  document.getElementById('subjectIdInput').value = sub.id;
  document.getElementById('subjectNameInput').value = sub.name;
  document.getElementById('subjectCodeInput').value = sub.code;
  document.getElementById('subjectColorInput').value = sub.color || '#4f46e5';
  document.getElementById('subjectPrioritySelect').value = sub.priority || 'MEDIUM';
  document.getElementById('subjectDescInput').value = sub.description || '';
  document.getElementById('subjectModal').classList.add('active');
}

function closeSubjectModal() {
  document.getElementById('subjectModal').classList.remove('active');
}

async function saveSubject() {
  const id = document.getElementById('subjectIdInput').value;
  const name = document.getElementById('subjectNameInput').value.trim();
  const code = document.getElementById('subjectCodeInput').value.trim();
  const color = document.getElementById('subjectColorInput').value;
  const priority = document.getElementById('subjectPrioritySelect').value;
  const description = document.getElementById('subjectDescInput').value.trim();

  if (!name) {
    showToast('Validation Error', 'Subject name is required', 'warning');
    return;
  }

  const payload = { name, code, color, priority, description };
  const method = id ? 'PUT' : 'POST';
  const endpoint = id ? `subjects/${id}` : 'subjects';

  try {
    await fetchJson(endpoint, {
      method,
      body: JSON.stringify(payload)
    });
    showToast('Success', `Subject ${id ? 'updated' : 'created'} successfully!`, 'success');
    closeSubjectModal();
    loadSubjectsView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

async function deleteSubject(id, name) {
  if (!confirm(`Are you sure you want to delete '${name}' and all its topics and exams?`)) {
    return;
  }

  try {
    await fetchJson(`subjects/${id}`, { method: 'DELETE' });
    showToast('Deleted', `Subject '${name}' removed.`, 'info');
    loadSubjectsView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

// =========================================================================
// Topic Modal CRUD
// =========================================================================
function openAddTopicModal() {
  openAddTopicForSubject(null);
}

function openAddTopicForSubject(subjectId) {
  document.getElementById('topicModalTitle').innerHTML = `<span>📖</span> Add Topic`;
  document.getElementById('topicIdInput').value = '';
  if (subjectId) {
    document.getElementById('topicSubjectSelect').value = subjectId;
  }
  document.getElementById('topicNameInput').value = '';
  document.getElementById('topicDifficultySelect').value = 'MEDIUM';
  document.getElementById('topicHoursInput').value = 2.5;
  document.getElementById('topicStatusSelect').value = 'NOT_STARTED';
  document.getElementById('topicConfidenceSelect').value = 3;
  document.getElementById('topicModal').classList.add('active');
}

function openEditTopicModal(id) {
  const top = App.topicsCache.find(t => t.id === id);
  if (!top) return;

  document.getElementById('topicModalTitle').innerHTML = `<span>📖</span> Edit Topic`;
  document.getElementById('topicIdInput').value = top.id;
  document.getElementById('topicSubjectSelect').value = top.subjectId;
  document.getElementById('topicNameInput').value = top.name;
  document.getElementById('topicDifficultySelect').value = top.difficulty || 'MEDIUM';
  document.getElementById('topicHoursInput').value = top.estimatedHours || 2.0;
  document.getElementById('topicStatusSelect').value = top.status || 'NOT_STARTED';
  document.getElementById('topicConfidenceSelect').value = top.confidenceLevel || 3;
  document.getElementById('topicModal').classList.add('active');
}

function closeTopicModal() {
  document.getElementById('topicModal').classList.remove('active');
}

async function saveTopic() {
  const id = document.getElementById('topicIdInput').value;
  const subjectId = parseInt(document.getElementById('topicSubjectSelect').value);
  const name = document.getElementById('topicNameInput').value.trim();
  const difficulty = document.getElementById('topicDifficultySelect').value;
  const estimatedHours = parseFloat(document.getElementById('topicHoursInput').value) || 2.0;
  const status = document.getElementById('topicStatusSelect').value;
  const confidenceLevel = parseInt(document.getElementById('topicConfidenceSelect').value) || 3;

  if (!subjectId || isNaN(subjectId)) {
    showToast('Validation Error', 'Please select a subject.', 'warning');
    return;
  }
  if (!name) {
    showToast('Validation Error', 'Topic name is required.', 'warning');
    return;
  }

  const payload = { subjectId, name, difficulty, estimatedHours, status, confidenceLevel };
  const method = id ? 'PUT' : 'POST';
  const endpoint = id ? `topics/${id}` : 'topics';

  try {
    await fetchJson(endpoint, {
      method,
      body: JSON.stringify(payload)
    });
    showToast('Success', `Topic ${id ? 'updated' : 'added'} successfully!`, 'success');
    closeTopicModal();
    loadSubjectsView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

async function quickToggleTopicStatus(topicId, isCompleted) {
  try {
    const status = isCompleted ? 'COMPLETED' : 'IN_PROGRESS';
    const confidenceLevel = isCompleted ? 4 : 2;
    await fetchJson(`topics/${topicId}/progress`, {
      method: 'PUT',
      body: JSON.stringify({ status, confidenceLevel })
    });
    showToast('Topic Updated', `Status changed to ${status}`, 'success');
    loadSubjectsView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

async function deleteTopic(id) {
  if (!confirm('Are you sure you want to delete this topic?')) return;
  try {
    await fetchJson(`topics/${id}`, { method: 'DELETE' });
    showToast('Deleted', 'Topic removed.', 'info');
    loadSubjectsView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}

// =========================================================================
// Exam Modal CRUD
// =========================================================================
function openAddExamModal() {
  document.getElementById('examModalTitle').innerHTML = `<span>🎯</span> Add Upcoming Exam`;
  document.getElementById('examIdInput').value = '';
  document.getElementById('examTitleInput').value = '';
  // Default date 7 days from now
  const nextWeek = new Date();
  nextWeek.setDate(nextWeek.getDate() + 7);
  document.getElementById('examDateInput').value = nextWeek.toISOString().split('T')[0];
  document.getElementById('examTargetScoreInput').value = 90;
  document.getElementById('examPrioritySelect').value = 'HIGH';
  document.getElementById('examNotesInput').value = '';
  document.getElementById('examModal').classList.add('active');
}

function closeExamModal() {
  document.getElementById('examModal').classList.remove('active');
}

async function saveExam() {
  const id = document.getElementById('examIdInput').value;
  const subjectId = parseInt(document.getElementById('examSubjectSelect').value);
  const title = document.getElementById('examTitleInput').value.trim();
  const examDate = document.getElementById('examDateInput').value;
  const targetScore = parseInt(document.getElementById('examTargetScoreInput').value) || 90;
  const priority = document.getElementById('examPrioritySelect').value;
  const notes = document.getElementById('examNotesInput').value.trim();

  if (!subjectId || isNaN(subjectId)) {
    showToast('Validation Error', 'Please select a subject.', 'warning');
    return;
  }
  if (!title) {
    showToast('Validation Error', 'Exam title is required.', 'warning');
    return;
  }
  if (!examDate) {
    showToast('Validation Error', 'Exam date is required.', 'warning');
    return;
  }

  const payload = { subjectId, title, examDate, targetScore, priority, notes };
  const method = id ? 'PUT' : 'POST';
  const endpoint = id ? `exams/${id}` : 'exams';

  try {
    await fetchJson(endpoint, {
      method,
      body: JSON.stringify(payload)
    });
    showToast('Success', `Exam ${id ? 'updated' : 'scheduled'} successfully!`, 'success');
    closeExamModal();
    loadSubjectsView();
  } catch (err) {
    showToast('Error', err.message, 'danger');
  }
}
