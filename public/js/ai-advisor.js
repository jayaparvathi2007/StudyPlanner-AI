/**
 * AI Adaptive Study Planner - AI Study Advisor & Topic Explainer Logic
 */

async function loadAiAdvisorView() {
  populateExplainerSubjects();

  // Load existing recommendations if present
  try {
    const data = await fetchJson('ai');
    if (data && data.content) {
      document.getElementById('aiPlanOutput').innerHTML = parseMarkdown(data.content);
    }
  } catch (err) {
    console.warn('Could not load cached AI recommendation:', err);
  }
}

async function generateAiRecommendations() {
  const outputEl = document.getElementById('aiPlanOutput');
  outputEl.innerHTML = `
    <div style="text-align: center; padding: 48px 24px; color: var(--text-muted);">
      <div style="font-size: 36px; animation: pulse 1.5s infinite; margin-bottom: 12px;">🧠</div>
      <h4>AI Strategist Synthesizing Coursework...</h4>
      <p style="font-size: 13px;">Analyzing exam deadlines, topic difficulties, diagnostic quiz scores, and spaced repetition buffers.</p>
    </div>
  `;

  try {
    const res = await fetchJson('ai/recommendations', { method: 'POST', body: '{}' });
    outputEl.innerHTML = parseMarkdown(res.content);
    showToast('AI Plan Ready', 'Your personalized 7-day study strategy is ready!', 'success');
  } catch (err) {
    outputEl.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h4>Could not generate AI strategy</h4>
        <p>${escapeHtml(err.message)}</p>
        <button class="btn btn-primary btn-sm" onclick="generateAiRecommendations()">Try Again</button>
      </div>
    `;
    showToast('AI Error', err.message, 'danger');
  }
}

function populateExplainerSubjects() {
  const select = document.getElementById('explainerSubjectSelect');
  if (!select) return;

  if (App.subjectsCache && App.subjectsCache.length > 0) {
    select.innerHTML = `<option value="">-- Choose Subject --</option>` +
      App.subjectsCache.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');
  } else {
    fetchJson('subjects').then(subjects => {
      App.subjectsCache = subjects;
      select.innerHTML = `<option value="">-- Choose Subject --</option>` +
        subjects.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');
    });
  }
}

function populateExplainerTopics() {
  const subjectId = parseInt(document.getElementById('explainerSubjectSelect').value);
  const input = document.getElementById('explainerTopicInput');
  if (!subjectId) return;

  const topics = App.topicsCache.filter(t => t.subjectId === subjectId);
  if (topics.length > 0) {
    input.value = topics[0].name;
  }
}

async function requestTopicExplanation(customTopic, customSubject) {
  let topicName = customTopic || document.getElementById('explainerTopicInput').value.trim();
  let subjectName = customSubject;

  if (!subjectName) {
    const subSelect = document.getElementById('explainerSubjectSelect');
    const selectedOption = subSelect.options[subSelect.selectedIndex];
    subjectName = selectedOption && selectedOption.value ? selectedOption.text : 'Computer Science';
  }

  const style = document.getElementById('explainerStyleSelect').value;

  if (!topicName) {
    showToast('Input Required', 'Please enter or select a topic to explain.', 'warning');
    return;
  }

  const outputEl = document.getElementById('aiExplainerOutput');
  outputEl.innerHTML = `
    <div style="text-align: center; padding: 48px 24px; color: var(--text-muted);">
      <div style="font-size: 36px; animation: pulse 1.5s infinite; margin-bottom: 12px;">💡</div>
      <h4>Drafting concept breakdown for "${escapeHtml(topicName)}"...</h4>
      <p style="font-size: 13px;">Formulating intuitive analogies, technical proofs, and exam pitfalls.</p>
    </div>
  `;

  try {
    const res = await fetchJson('ai/explain', {
      method: 'POST',
      body: JSON.stringify({
        topicName,
        subjectName,
        difficulty: 'MEDIUM',
        style
      })
    });
    outputEl.innerHTML = parseMarkdown(res.content);
    showToast('Topic Breakdown Ready', `Explanation for '${topicName}' generated!`, 'success');
  } catch (err) {
    outputEl.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">❌</div>
        <h4>Explanation Failed</h4>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
    showToast('Error', err.message, 'danger');
  }
}
