/**
 * AI Adaptive Study Planner - AI Practice Quiz & Adaptive Assessment Logic
 */

let currentQuizState = {
  topicId: null,
  topicName: '',
  subjectName: '',
  questions: [],
  currentIndex: 0,
  userAnswers: [],
  selectedOptionIndex: null,
  correctCount: 0
};

async function loadQuizView() {
  populateQuizDropdowns();
  loadQuizHistory();
}

function populateQuizDropdowns() {
  const subSelect = document.getElementById('quizSubjectSelect');
  if (!subSelect) return;

  if (App.subjectsCache && App.subjectsCache.length > 0) {
    subSelect.innerHTML = `<option value="">-- Choose Subject --</option>` +
      App.subjectsCache.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');
  } else {
    fetchJson('subjects').then(subjects => {
      App.subjectsCache = subjects;
      subSelect.innerHTML = `<option value="">-- Choose Subject --</option>` +
        subjects.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');
    });
  }
}

function populateQuizTopics() {
  const subjectId = parseInt(document.getElementById('quizSubjectSelect').value);
  const topSelect = document.getElementById('quizTopicSelect');
  if (!topSelect) return;

  if (!subjectId) {
    topSelect.innerHTML = `<option value="">-- Choose Topic --</option>`;
    return;
  }

  fetchJson(`topics?subjectId=${subjectId}`).then(topics => {
    topSelect.innerHTML = `<option value="">-- Choose Topic --</option>` +
      topics.map(t => `<option value="${t.id}" data-name="${escapeHtml(t.name)}" data-diff="${t.difficulty}">${escapeHtml(t.name)} (${t.difficulty})</option>`).join('');
  });
}

function launchQuizForTopic(topicId, topicName, subjectName) {
  switchView('quiz');
  // Preselect or directly generate
  currentQuizState.topicId = topicId;
  currentQuizState.topicName = topicName;
  currentQuizState.subjectName = subjectName;

  startDirectQuizGeneration(topicId, topicName, subjectName, 'MEDIUM');
}

async function startQuizGeneration() {
  const topSelect = document.getElementById('quizTopicSelect');
  const selectedOption = topSelect.options[topSelect.selectedIndex];

  if (!selectedOption || !selectedOption.value) {
    showToast('Selection Required', 'Please select a topic for the quiz.', 'warning');
    return;
  }

  const topicId = parseInt(selectedOption.value);
  const topicName = selectedOption.dataset.name || selectedOption.text;
  const subSelect = document.getElementById('quizSubjectSelect');
  const subjectName = subSelect.options[subSelect.selectedIndex].text;
  const difficulty = document.getElementById('quizDifficultySelect').value;

  startDirectQuizGeneration(topicId, topicName, subjectName, difficulty);
}

async function startDirectQuizGeneration(topicId, topicName, subjectName, difficulty) {
  showToast('Generating AI Quiz', `Synthesizing 5 questions for ${topicName}...`, 'info');

  document.getElementById('quizSetupCard').style.display = 'none';
  document.getElementById('quizResultCard').style.display = 'none';
  document.getElementById('quizPlayerCard').style.display = 'block';

  document.getElementById('quizTopicHeader').innerText = topicName;
  document.getElementById('quizTopicBadge').innerText = difficulty;
  document.getElementById('quizQuestionText').innerHTML = `
    <div style="text-align: center; padding: 32px 0;">
      <div style="font-size: 32px; animation: pulse 1s infinite; margin-bottom: 8px;">🎯</div>
      <p style="color: var(--text-muted); font-size: 14px;">Generating practice questions with AI model...</p>
    </div>
  `;
  document.getElementById('quizOptionsList').innerHTML = '';
  document.getElementById('quizExplanationBox').style.display = 'none';
  document.getElementById('btnSubmitAnswer').style.display = 'none';
  document.getElementById('btnNextQuestion').style.display = 'none';

  try {
    const res = await fetchJson('quiz/generate', {
      method: 'POST',
      body: JSON.stringify({ topicId, topicName, subjectName, difficulty })
    });

    currentQuizState = {
      topicId,
      topicName,
      subjectName,
      questions: res.questions || [],
      currentIndex: 0,
      userAnswers: [],
      selectedOptionIndex: null,
      correctCount: 0
    };

    renderCurrentQuestion();
  } catch (err) {
    showToast('Quiz Generation Error', err.message, 'danger');
    resetQuizView();
  }
}

function renderCurrentQuestion() {
  const { questions, currentIndex } = currentQuizState;
  if (!questions || questions.length === 0 || currentIndex >= questions.length) {
    finishQuiz();
    return;
  }

  const q = questions[currentIndex];
  currentQuizState.selectedOptionIndex = null;

  document.getElementById('quizQuestionCounter').innerText = `Question ${currentIndex + 1} / ${questions.length}`;
  document.getElementById('quizQuestionText').innerText = q.question;
  document.getElementById('quizExplanationBox').style.display = 'none';
  document.getElementById('quizExplanationBox').innerHTML = '';

  const optsContainer = document.getElementById('quizOptionsList');
  const letters = ['A', 'B', 'C', 'D'];

  optsContainer.innerHTML = (q.options || []).map((opt, idx) => `
    <div class="quiz-option" id="quizOpt_${idx}" onclick="selectQuizOption(${idx})">
      <div class="option-badge">${letters[idx]}</div>
      <div style="flex:1;">${escapeHtml(opt)}</div>
    </div>
  `).join('');

  document.getElementById('btnSubmitAnswer').style.display = 'inline-flex';
  document.getElementById('btnSubmitAnswer').disabled = false;
  document.getElementById('btnNextQuestion').style.display = 'none';
}

function selectQuizOption(idx) {
  // If already confirmed, ignore clicks
  if (document.getElementById('btnSubmitAnswer').style.display === 'none') return;

  currentQuizState.selectedOptionIndex = idx;
  document.querySelectorAll('.quiz-option').forEach((el, i) => {
    el.classList.toggle('selected', i === idx);
  });
}

function submitCurrentAnswer() {
  const { selectedOptionIndex, questions, currentIndex } = currentQuizState;
  if (selectedOptionIndex === null) {
    showToast('Select an Option', 'Please select one of the four options to answer.', 'warning');
    return;
  }

  const q = questions[currentIndex];
  const isCorrect = selectedOptionIndex === q.correctIndex;

  if (isCorrect) {
    currentQuizState.correctCount++;
  }
  currentQuizState.userAnswers.push({
    questionId: q.id,
    selectedIndex: selectedOptionIndex,
    correctIndex: q.correctIndex,
    isCorrect
  });

  // Reveal correct and wrong styling
  document.querySelectorAll('.quiz-option').forEach((el, i) => {
    if (i === q.correctIndex) {
      el.classList.add('correct');
    } else if (i === selectedOptionIndex) {
      el.classList.add('wrong');
    }
  });

  // Reveal Explanation
  const expBox = document.getElementById('quizExplanationBox');
  expBox.style.display = 'block';
  expBox.className = `quiz-explanation-box ${isCorrect ? 'badge-easy' : 'badge-hard'}`;
  expBox.innerHTML = `
    <strong>${isCorrect ? '✅ Correct Answer!' : '❌ Incorrect.'}</strong><br>
    ${escapeHtml(q.explanation)}
  `;

  document.getElementById('btnSubmitAnswer').style.display = 'none';
  const btnNext = document.getElementById('btnNextQuestion');
  btnNext.style.display = 'inline-flex';
  btnNext.innerText = currentIndex + 1 < questions.length ? 'Next Question →' : 'View Results 🎉';
}

function goToNextQuestion() {
  currentQuizState.currentIndex++;
  if (currentQuizState.currentIndex < currentQuizState.questions.length) {
    renderCurrentQuestion();
  } else {
    finishQuiz();
  }
}

async function finishQuiz() {
  document.getElementById('quizPlayerCard').style.display = 'none';
  document.getElementById('quizResultCard').style.display = 'block';

  const { correctCount, questions, topicId } = currentQuizState;
  const total = questions.length || 5;
  const score = Math.round((correctCount / total) * 100);
  const passed = score >= 60;

  // Update Score Screen
  const scoreCircle = document.getElementById('scoreCircle');
  scoreCircle.className = `score-circle ${passed ? 'passed' : 'failed'}`;
  document.getElementById('resultScoreNumber').innerText = `${score}%`;

  const resultTitle = document.getElementById('resultTitle');
  const resultFeedback = document.getElementById('resultFeedback');
  const adaptiveAlert = document.getElementById('adaptiveRescheduleAlert');

  if (passed) {
    resultTitle.innerText = score >= 80 ? 'Mastery Achieved! 🏆' : 'Good Comprehension! 👍';
    resultFeedback.innerText = `You scored ${correctCount} out of ${total} correctly. Keep building your spaced repetition review cadence.`;
    adaptiveAlert.style.display = 'none';
  } else {
    resultTitle.innerText = 'Concept Gaps Detected ⚠️';
    resultFeedback.innerText = `You scored ${correctCount} out of ${total} (${score}%). This topic requires immediate remedial review.`;
    adaptiveAlert.style.display = 'block';
  }

  // Submit attempt to server
  try {
    const res = await fetchJson('quiz/submit', {
      method: 'POST',
      body: JSON.stringify({
        topicId,
        score,
        totalQuestions: total,
        correctAnswers: correctCount,
        feedback: resultFeedback.innerText
      })
    });

    if (res.remedialScheduled) {
      showToast('Adaptive Action', 'Remedial study session automatically queued for tomorrow!', 'warning');
    } else {
      showToast('Quiz Recorded', 'Your score has been logged to your academic history.', 'success');
    }

    loadQuizHistory();
  } catch (err) {
    showToast('Failed to record score', err.message, 'danger');
  }
}

function resetQuizView() {
  document.getElementById('quizPlayerCard').style.display = 'none';
  document.getElementById('quizResultCard').style.display = 'none';
  document.getElementById('quizSetupCard').style.display = 'block';
}

async function loadQuizHistory() {
  const container = document.getElementById('quizHistoryContainer');
  if (!container) return;

  try {
    const history = await fetchJson('quiz/history');
    if (!history || history.length === 0) {
      container.innerHTML = `<p style="color: var(--text-muted); font-size: 13px;">No quiz attempts logged yet.</p>`;
      return;
    }

    container.innerHTML = `
      <div style="overflow-x: auto;">
        <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
          <thead>
            <tr style="text-align: left; border-bottom: 2px solid var(--border); color: var(--text-muted);">
              <th style="padding: 10px 8px;">Date</th>
              <th style="padding: 10px 8px;">Subject</th>
              <th style="padding: 10px 8px;">Topic</th>
              <th style="padding: 10px 8px;">Score</th>
              <th style="padding: 10px 8px;">Evaluation</th>
            </tr>
          </thead>
          <tbody>
            ${history.map(h => {
              const passed = h.score >= 60;
              const dateStr = (h.attemptedAt || '').substring(0, 10);
              return `
                <tr style="border-bottom: 1px solid var(--border);">
                  <td style="padding: 10px 8px; color: var(--text-muted);">${dateStr}</td>
                  <td style="padding: 10px 8px; font-weight: 600;">${escapeHtml(h.subjectName || '-')}</td>
                  <td style="padding: 10px 8px;">${escapeHtml(h.topicName || '-')}</td>
                  <td style="padding: 10px 8px;">
                    <span class="badge ${passed ? 'badge-easy' : 'badge-hard'}">
                      ${h.score}% (${h.correctAnswers}/${h.totalQuestions})
                    </span>
                  </td>
                  <td style="padding: 10px 8px; color: var(--text-muted); font-size: 12px;">
                    ${escapeHtml(h.feedback || '-')}
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    console.warn('Could not load quiz history:', err);
  }
}
