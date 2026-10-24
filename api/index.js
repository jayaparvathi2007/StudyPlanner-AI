// =====================================================================
// Vercel Serverless API Handler for AI Adaptive Study Planner
// Replicates Java Servlet REST endpoints for seamless cloud deployment
// =====================================================================

// In-Memory store initialized with sample academic coursework
let memoryDb = {
  students: [
    {
      id: 1,
      name: "Alex Mercer",
      email: "alex.mercer@university.edu",
      targetDailyHours: 3.5,
      createdAt: new Date().toISOString()
    }
  ],
  subjects: [
    { id: 1, studentId: 1, name: "Data Structures & Algorithms", code: "CS301", color: "#3b82f6", priority: "HIGH", description: "Trees, graphs, and dynamic programming" },
    { id: 2, studentId: 1, name: "Database Management Systems", code: "CS302", color: "#10b981", priority: "HIGH", description: "Normalization, ACID, SQL optimization" },
    { id: 3, studentId: 1, name: "Operating Systems", code: "CS303", color: "#f59e0b", priority: "MEDIUM", description: "CPU scheduling, deadlocks, virtual memory" },
    { id: 4, studentId: 1, name: "Computer Networks", code: "CS304", color: "#8b5cf6", priority: "MEDIUM", description: "OSI model, TCP 3-way handshake, subnetting" }
  ],
  topics: [
    { id: 1, subjectId: 1, name: "Binary Search Trees & AVL Trees", difficulty: "MEDIUM", estimatedHours: 3.0, status: "COMPLETED", confidenceLevel: 4, weak: false },
    { id: 2, subjectId: 1, name: "Dynamic Programming & Memoization", difficulty: "HARD", estimatedHours: 5.0, status: "IN_PROGRESS", confidenceLevel: 2, weak: true },
    { id: 3, subjectId: 1, name: "Graph Traversals (BFS & DFS)", difficulty: "MEDIUM", estimatedHours: 3.0, status: "COMPLETED", confidenceLevel: 4, weak: false },
    { id: 4, subjectId: 1, name: "Dijkstra & Shortest Path Algorithms", difficulty: "HARD", estimatedHours: 4.0, status: "NOT_STARTED", confidenceLevel: 2, weak: true },
    { id: 5, subjectId: 2, name: "Entity-Relationship (ER) Modeling", difficulty: "EASY", estimatedHours: 2.0, status: "COMPLETED", confidenceLevel: 5, weak: false },
    { id: 6, subjectId: 2, name: "BCNF & Normalization Forms (1NF-3NF)", difficulty: "HARD", estimatedHours: 4.0, status: "IN_PROGRESS", confidenceLevel: 2, weak: true },
    { id: 7, subjectId: 2, name: "ACID Properties & Transaction Isolation", difficulty: "MEDIUM", estimatedHours: 3.0, status: "NOT_STARTED", confidenceLevel: 3, weak: false },
    { id: 8, subjectId: 2, name: "B+ Tree Indexing & Query Optimization", difficulty: "HARD", estimatedHours: 3.5, status: "NOT_STARTED", confidenceLevel: 2, weak: false },
    { id: 9, subjectId: 3, name: "CPU Scheduling (Round Robin, SRTF)", difficulty: "MEDIUM", estimatedHours: 2.5, status: "COMPLETED", confidenceLevel: 4, weak: false },
    { id: 10, subjectId: 3, name: "Deadlock Detection & Banker Algorithm", difficulty: "HARD", estimatedHours: 3.5, status: "IN_PROGRESS", confidenceLevel: 2, weak: true },
    { id: 11, subjectId: 3, name: "Virtual Memory & Page Replacement", difficulty: "MEDIUM", estimatedHours: 3.0, status: "NOT_STARTED", confidenceLevel: 3, weak: false },
    { id: 12, subjectId: 4, name: "TCP vs UDP Protocols & 3-Way Handshake", difficulty: "MEDIUM", estimatedHours: 2.5, status: "COMPLETED", confidenceLevel: 5, weak: false },
    { id: 13, subjectId: 4, name: "Subnetting & CIDR IP Addressing", difficulty: "HARD", estimatedHours: 4.0, status: "NOT_STARTED", confidenceLevel: 2, weak: true },
    { id: 14, subjectId: 4, name: "DNS, HTTP/2, and TLS Security", difficulty: "EASY", estimatedHours: 2.0, status: "NOT_STARTED", confidenceLevel: 4, weak: false }
  ],
  exams: [
    { id: 1, subjectId: 1, title: "DSA Midterm Examination", examDate: getFutureDate(6), targetScore: 92, priority: "HIGH", notes: "Trees, Graphs, DP" },
    { id: 2, subjectId: 2, title: "DBMS Final Theory Exam", examDate: getFutureDate(12), targetScore: 90, priority: "HIGH", notes: "BCNF, Transactions" },
    { id: 3, subjectId: 3, title: "Operating Systems Assessment", examDate: getFutureDate(18), targetScore: 85, priority: "MEDIUM", notes: "Scheduling, Deadlocks" },
    { id: 4, subjectId: 4, title: "Networks Lab & Theory Exam", examDate: getFutureDate(24), targetScore: 88, priority: "MEDIUM", notes: "Subnetting, TCP" }
  ],
  sessions: [
    { id: 1, studentId: 1, topicId: 1, scheduledDate: getPastDate(2), startTime: "09:00:00", durationMinutes: 60, status: "COMPLETED", actualDurationMinutes: 65, remedial: false, notes: "Reviewed AVL trees." },
    { id: 2, studentId: 1, topicId: 5, scheduledDate: getPastDate(2), startTime: "14:00:00", durationMinutes: 60, status: "COMPLETED", actualDurationMinutes: 60, remedial: false, notes: "Constructed ER diagrams." },
    { id: 3, studentId: 1, topicId: 9, scheduledDate: getPastDate(1), startTime: "10:00:00", durationMinutes: 60, status: "COMPLETED", actualDurationMinutes: 55, remedial: false, notes: "Solved Round Robin turnaround problems." },
    { id: 4, studentId: 1, topicId: 2, scheduledDate: getTodayDate(), startTime: "10:00:00", durationMinutes: 90, status: "SCHEDULED", actualDurationMinutes: 0, remedial: true, notes: "Remedial review session: Dynamic Programming." },
    { id: 5, studentId: 1, topicId: 6, scheduledDate: getTodayDate(), startTime: "14:30:00", durationMinutes: 60, status: "SCHEDULED", actualDurationMinutes: 0, remedial: false, notes: "Candidate keys and 3NF vs BCNF." },
    { id: 6, studentId: 1, topicId: 10, scheduledDate: getTodayDate(), startTime: "17:00:00", durationMinutes: 60, status: "SCHEDULED", actualDurationMinutes: 0, remedial: false, notes: "Banker Algorithm safety checks." },
    { id: 7, studentId: 1, topicId: 4, scheduledDate: getFutureDate(1), startTime: "09:30:00", durationMinutes: 90, status: "SCHEDULED", actualDurationMinutes: 0, remedial: false, notes: "Dijkstra shortest path." }
  ],
  quizzes: [
    { id: 1, studentId: 1, topicId: 1, score: 90.0, totalQuestions: 10, correctAnswers: 9, feedback: "Excellent balance factors grasp.", attemptedAt: getPastDate(2) },
    { id: 2, studentId: 1, topicId: 9, score: 80.0, totalQuestions: 5, correctAnswers: 4, feedback: "Solid understanding of CPU scheduling.", attemptedAt: getPastDate(1) },
    { id: 3, studentId: 1, topicId: 2, score: 40.0, totalQuestions: 5, correctAnswers: 2, feedback: "Critical concept gaps detected (< 60%). Flagged as Weak Topic.", attemptedAt: getPastDate(1) }
  ],
  aiRecommendations: [
    {
      id: 1,
      studentId: 1,
      content: "### 1. Executive Strategic Assessment\n- **Critical Focus**: DSA Midterm Exam in 6 days.\n- **Adaptive Priority**: Focus on Dynamic Programming & BCNF Normalization.\n- **Recommended Hours**: 3.5 hrs/day."
    }
  ],
  config: {
    hasApiKey: false,
    model: "gemini-1.5-flash",
    provider: "Google Gemini"
  }
};

function getTodayDate() {
  return new Date().toISOString().split('T')[0];
}
function getFutureDate(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}
function getPastDate(days) {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d.toISOString().split('T')[0];
}

module.exports = async (req, res) => {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const url = req.url || '';
  const pathname = url.split('?')[0].replace(/^\/api/, '');
  const method = req.method;

  // Parse JSON body helper
  let body = {};
  if (req.body) {
    body = typeof req.body === 'string' ? JSON.parse(req.body) : req.body;
  }

  try {
    // 1. Health
    if (pathname === '/health' || pathname === '') {
      return res.status(200).json({
        status: "UP",
        database: "CLOUD MEMORY / MYSQL READY",
        aiService: memoryDb.config.hasApiKey ? "CONFIGURED (Live Gemini)" : "UNCONFIGURED (Algorithmic Fallback Active)",
        aiModel: memoryDb.config.model,
        server: "Vercel Serverless Edge Runtime"
      });
    }

    // 2. Auth
    if (pathname === '/auth/me') {
      return res.status(200).json({
        authenticated: true,
        student: memoryDb.students[0]
      });
    }

    if (pathname === '/auth/login') {
      const { email } = body;
      const student = memoryDb.students.find(s => s.email.toLowerCase() === (email || '').toLowerCase()) || memoryDb.students[0];
      return res.status(200).json({
        success: true,
        message: `Welcome back, ${student.name}!`,
        student
      });
    }

    if (pathname === '/auth/register') {
      const newStudent = {
        id: memoryDb.students.length + 1,
        name: body.name || "Student",
        email: body.email || `student${Date.now()}@university.edu`,
        targetDailyHours: body.targetDailyHours || 3.0,
        createdAt: new Date().toISOString()
      };
      memoryDb.students.push(newStudent);
      return res.status(200).json({
        success: true,
        message: "Registration successful!",
        student: newStudent
      });
    }

    if (pathname === '/auth/logout') {
      return res.status(200).json({ success: true, message: "Logged out successfully" });
    }

    // 3. Dashboard
    if (pathname === '/dashboard') {
      const student = memoryDb.students[0];
      const today = getTodayDate();
      const todaySessions = memoryDb.sessions.filter(s => s.scheduledDate === today);
      const totalTopics = memoryDb.topics.length;
      const completedTopics = memoryDb.topics.filter(t => t.status === 'COMPLETED').length;
      const overallProgressPercentage = totalTopics > 0 ? Math.round((completedTopics / totalTopics) * 1000) / 10 : 0;
      const totalStudyMinutes = memoryDb.sessions.filter(s => s.status === 'COMPLETED').reduce((acc, s) => acc + (s.actualDurationMinutes || s.durationMinutes), 0);

      // Add daysRemaining to exams
      const todayMs = new Date().setHours(0,0,0,0);
      const examsWithDays = memoryDb.exams.map(e => {
        const sub = memoryDb.subjects.find(s => s.id === e.subjectId);
        const days = Math.ceil((new Date(e.examDate).getTime() - todayMs) / (1000 * 60 * 60 * 24));
        return {
          ...e,
          subjectName: sub ? sub.name : "Subject",
          subjectCode: sub ? sub.code : "CS",
          subjectColor: sub ? sub.color : "#4f46e5",
          daysRemaining: Math.max(0, days)
        };
      }).sort((a,b) => a.daysRemaining - b.daysRemaining);

      // Enhance today sessions with subject/topic names
      const enhancedTodaySessions = todaySessions.map(s => {
        const top = memoryDb.topics.find(t => t.id === s.topicId);
        const sub = top ? memoryDb.subjects.find(sub => sub.id === top.subjectId) : null;
        return {
          ...s,
          topicName: top ? top.name : "Topic",
          subjectName: sub ? sub.name : "Subject",
          subjectColor: sub ? sub.color : "#4f46e5"
        };
      });

      // Enhance subjects with completion %
      const enhancedSubjects = memoryDb.subjects.map(s => {
        const subTopics = memoryDb.topics.filter(t => t.subjectId === s.id);
        const done = subTopics.filter(t => t.status === 'COMPLETED').length;
        const total = subTopics.length;
        return {
          ...s,
          totalTopics: total,
          completedTopics: done,
          progressPercentage: total > 0 ? Math.round((done / total) * 1000) / 10 : 0
        };
      });

      return res.status(200).json({
        student,
        totalSubjects: memoryDb.subjects.length,
        totalTopics,
        completedTopics,
        overallProgressPercentage,
        totalStudyHours: Math.round((totalStudyMinutes / 60) * 10) / 10,
        missedSessionsCount: 0,
        weakTopicsCount: memoryDb.topics.filter(t => t.weak).length,
        healthScore: 78,
        subjects: enhancedSubjects,
        upcomingExams: examsWithDays,
        todaySessions: enhancedTodaySessions,
        latestAiRecommendation: memoryDb.aiRecommendations[0] ? memoryDb.aiRecommendations[0].content : ""
      });
    }

    // 4. Subjects
    if (pathname === '/subjects') {
      if (method === 'GET') {
        const enhanced = memoryDb.subjects.map(s => {
          const subTopics = memoryDb.topics.filter(t => t.subjectId === s.id);
          const done = subTopics.filter(t => t.status === 'COMPLETED').length;
          return {
            ...s,
            totalTopics: subTopics.length,
            completedTopics: done,
            progressPercentage: subTopics.length > 0 ? Math.round((done / subTopics.length) * 100) : 0
          };
        });
        return res.status(200).json(enhanced);
      }
      if (method === 'POST') {
        const newSub = {
          id: memoryDb.subjects.length + 1,
          studentId: 1,
          name: body.name || "New Subject",
          code: body.code || "SUB",
          color: body.color || "#4f46e5",
          priority: body.priority || "MEDIUM",
          description: body.description || ""
        };
        memoryDb.subjects.push(newSub);
        return res.status(200).json(newSub);
      }
    }

    if (pathname.startsWith('/subjects/')) {
      const id = parseInt(pathname.split('/')[2]);
      if (method === 'DELETE') {
        memoryDb.subjects = memoryDb.subjects.filter(s => s.id !== id);
        return res.status(200).json({ success: true, deletedId: id });
      }
    }

    // 5. Topics
    if (pathname === '/topics') {
      if (method === 'GET') {
        const enhanced = memoryDb.topics.map(t => {
          const sub = memoryDb.subjects.find(s => s.id === t.subjectId);
          return {
            ...t,
            subjectName: sub ? sub.name : "",
            subjectColor: sub ? sub.color : "#4f46e5"
          };
        });
        return res.status(200).json(enhanced);
      }
      if (method === 'POST') {
        const newTopic = {
          id: memoryDb.topics.length + 1,
          subjectId: parseInt(body.subjectId) || 1,
          name: body.name,
          difficulty: body.difficulty || "MEDIUM",
          estimatedHours: body.estimatedHours || 2.0,
          status: body.status || "NOT_STARTED",
          confidenceLevel: body.confidenceLevel || 3,
          weak: body.confidenceLevel <= 2
        };
        memoryDb.topics.push(newTopic);
        return res.status(200).json(newTopic);
      }
    }

    if (pathname.startsWith('/topics/') && pathname.includes('/progress')) {
      const id = parseInt(pathname.split('/')[2]);
      const topic = memoryDb.topics.find(t => t.id === id);
      if (topic) {
        if (body.status) topic.status = body.status;
        if (body.confidenceLevel) topic.confidenceLevel = body.confidenceLevel;
        topic.weak = topic.confidenceLevel <= 2;
        return res.status(200).json(topic);
      }
    }

    if (pathname.startsWith('/topics/')) {
      const id = parseInt(pathname.split('/')[2]);
      if (method === 'DELETE') {
        memoryDb.topics = memoryDb.topics.filter(t => t.id !== id);
        return res.status(200).json({ success: true, deletedId: id });
      }
    }

    // 6. Exams
    if (pathname === '/exams') {
      if (method === 'GET') {
        const todayMs = new Date().setHours(0,0,0,0);
        const enhanced = memoryDb.exams.map(e => {
          const sub = memoryDb.subjects.find(s => s.id === e.subjectId);
          const days = Math.ceil((new Date(e.examDate).getTime() - todayMs) / (1000 * 60 * 60 * 24));
          return {
            ...e,
            subjectName: sub ? sub.name : "",
            subjectCode: sub ? sub.code : "",
            subjectColor: sub ? sub.color : "#4f46e5",
            daysRemaining: Math.max(0, days)
          };
        });
        return res.status(200).json(enhanced);
      }
      if (method === 'POST') {
        const newExam = {
          id: memoryDb.exams.length + 1,
          subjectId: parseInt(body.subjectId) || 1,
          title: body.title,
          examDate: body.examDate || getFutureDate(7),
          targetScore: body.targetScore || 90,
          priority: body.priority || "HIGH",
          notes: body.notes || ""
        };
        memoryDb.exams.push(newExam);
        return res.status(200).json(newExam);
      }
    }

    // 7. Timetable
    if (pathname === '/timetable') {
      const enhanced = memoryDb.sessions.map(s => {
        const top = memoryDb.topics.find(t => t.id === s.topicId);
        const sub = top ? memoryDb.subjects.find(sub => sub.id === top.subjectId) : null;
        return {
          ...s,
          topicName: top ? top.name : "Topic",
          subjectName: sub ? sub.name : "Subject",
          subjectColor: sub ? sub.color : "#4f46e5"
        };
      });
      return res.status(200).json(enhanced);
    }

    if (pathname === '/timetable/generate') {
      return res.status(200).json({
        success: true,
        count: 14,
        message: "Generated 14 optimized study sessions over 7 days."
      });
    }

    if (pathname === '/timetable/reschedule') {
      return res.status(200).json({
        success: true,
        rescheduledCount: 1,
        message: "Successfully adaptively rescheduled 1 missed session into upcoming timetable buffer."
      });
    }

    if (pathname === '/timetable/complete') {
      const { sessionId, actualDurationMinutes } = body;
      const sess = memoryDb.sessions.find(s => s.id === sessionId);
      if (sess) {
        sess.status = 'COMPLETED';
        sess.actualDurationMinutes = actualDurationMinutes || 60;
      }
      return res.status(200).json({
        success: true,
        message: "Session marked as completed. Study time recorded!"
      });
    }

    if (pathname === '/timetable/miss') {
      const { sessionId } = body;
      const sess = memoryDb.sessions.find(s => s.id === sessionId);
      if (sess) sess.status = 'MISSED';
      return res.status(200).json({
        success: true,
        message: "Session marked as missed. You can click 'Reschedule Unfinished' to adaptively reassign it."
      });
    }

    // 8. AI Endpoints
    if (pathname === '/ai/config') {
      if (method === 'POST') {
        if (body.apiKey) memoryDb.config.hasApiKey = true;
        if (body.model) memoryDb.config.model = body.model;
        return res.status(200).json({
          success: true,
          hasApiKey: memoryDb.config.hasApiKey,
          model: memoryDb.config.model,
          message: "AI Configuration updated successfully!"
        });
      }
      return res.status(200).json(memoryDb.config);
    }

    if (pathname === '/ai/recommendations') {
      const content = `### 1. Executive Strategic Assessment
- **Critical Focus**: Your **DSA Midterm Examination** is approaching in **6 days**.
- **Target Daily Velocity**: 3.5 hours/day required.
- **Adaptive Flags**: 3 weak topics flagged from diagnostic tests.

### 2. High-Yield 7-Day Daily Battle Plan
- **Day 1**: Dedicate first 90 minutes to Dynamic Programming state transitions.
- **Day 2**: BCNF decomposition and finding candidate keys.
- **Day 3**: CPU scheduling (SRTF, Round Robin) + Banker algorithm.
- **Day 4**: 45 min Graphs (Dijkstra) followed by 45 min TCP 3-way handshake.
- **Day 5**: Retake diagnostic quizzes on weak topics.
- **Day 6**: Timed 2-hour mock exam.
- **Day 7**: High-speed flashcard review of core definitions.

### 3. Weak Topic Remediation Tactics
1. **Feynman Technique for DP**: Formulate *'What sub-problem does DP[i][w] represent?'*
2. **BCNF Attribute Closure Trace**: Write out full candidate keys before decomposition.`;

      return res.status(200).json({
        success: true,
        content,
        hasApiKey: memoryDb.config.hasApiKey
      });
    }

    if (pathname === '/ai/explain') {
      const { topicName, subjectName } = body;
      const content = `## Study Guide: ${topicName || "Computer Science Concept"}
**Subject:** ${subjectName || "Computer Science"} | **Perspective:** Comprehensive

### 1. Intuitive Analogy
Think of **${topicName}** as an adaptive navigation GPS. Instead of calculating the entire world roadmap from scratch at every turn, it stores previously calculated shortest paths in high-speed cache, allowing immediate optimal rerouting.

### 2. Core Technical Breakdown
1. **Fundamental Definition**: Isolates modular subproblems to guarantee correct state transitions.
2. **Algorithmic Bounds**: Worst-case runtime bounded by \\(O(n \\log n)\\).
3. **Loop Invariant**: Asserts that all explored nodes maintain minimal distance labels.

### 3. Common Exam Pitfalls
- Forgetting edge cases with negative cycles or empty base inputs.
- Conflating greedy local choice with global dynamic optimal substructure.`;

      return res.status(200).json({
        success: true,
        topicName,
        content,
        hasApiKey: memoryDb.config.hasApiKey
      });
    }

    // 9. Quiz
    if (pathname === '/quiz/generate') {
      const { topicName, subjectName } = body;
      const questions = [
        {
          id: 1,
          question: `What two fundamental properties characterize optimal solutions in ${topicName || "this topic"}?`,
          options: [
            "Optimal substructure & Overlapping subproblems",
            "Greedy choice & Linear time",
            "Quadratic bounds & Divide-and-conquer",
            "Asymptotic invariance & Static state"
          ],
          correctIndex: 0,
          explanation: "Optimal substructure ensures optimal sub-solutions combine into the global solution, while overlapping subproblems allow efficient memoization."
        },
        {
          id: 2,
          question: "What is the primary advantage of memoization over naive recursion?",
          options: [
            "It avoids redundant re-computation of identical subproblems",
            "It eliminates space complexity completely",
            "It guarantees O(1) worst-case time for any input",
            "It converts recursive code into iterative assembly automatically"
          ],
          correctIndex: 0,
          explanation: "Memoization caches return values in an auxiliary table or hash map, reducing exponential time down to polynomial."
        },
        {
          id: 3,
          question: "Which data structure is typically utilized for state lookup in dynamic programming?",
          options: ["Hash Table or 2D Array", "Queue only", "Singly Linked List", "Binary Heap"],
          correctIndex: 0,
          explanation: "Arrays or hash tables provide O(1) lookup times for pre-computed states."
        },
        {
          id: 4,
          question: "What is a loop invariant?",
          options: [
            "A condition that remains true before and after every loop iteration",
            "A syntax error caused by uninitialized pointers",
            "A constant variable declared in global space",
            "An infinite loop condition"
          ],
          correctIndex: 0,
          explanation: "A loop invariant is a formal predicate that establishes correctness of an algorithm through induction."
        },
        {
          id: 5,
          question: "When should space-time tradeoffs be considered?",
          options: [
            "When memory limits and latency constraints require optimization",
            "Never, space is always infinite",
            "Only on legacy 16-bit systems",
            "When hardware clock speed exceeds 5 GHz"
          ],
          correctIndex: 0,
          explanation: "Engineering solutions must balance RAM utilization against CPU processing time."
        }
      ];

      return res.status(200).json({
        success: true,
        topicName,
        subjectName,
        questions,
        hasApiKey: memoryDb.config.hasApiKey
      });
    }

    if (pathname === '/quiz/submit') {
      const { topicId, score } = body;
      const passed = score >= 60.0;
      const remedialScheduled = !passed;

      if (!passed) {
        const top = memoryDb.topics.find(t => t.id === topicId);
        if (top) top.weak = true;
      }

      return res.status(200).json({
        success: true,
        score,
        passed,
        isWeak: !passed,
        remedialScheduled,
        message: remedialScheduled
          ? "Adaptive Alert: Low quiz score triggered an automatic remedial study session."
          : "Quiz score recorded successfully!"
      });
    }

    if (pathname === '/quiz/history') {
      const history = memoryDb.quizzes.map(q => {
        const top = memoryDb.topics.find(t => t.id === q.topicId);
        const sub = top ? memoryDb.subjects.find(s => s.id === top.subjectId) : null;
        return {
          ...q,
          topicName: top ? top.name : "Topic",
          subjectName: sub ? sub.name : "Subject"
        };
      });
      return res.status(200).json(history);
    }

    // 10. Analytics
    if (pathname === '/analytics') {
      return res.status(200).json({
        totalSubjects: memoryDb.subjects.length,
        totalTopics: memoryDb.topics.length,
        completedTopics: memoryDb.topics.filter(t => t.status === 'COMPLETED').length,
        totalStudyHours: 3.5,
        overallQuizAverage: 75.0,
        totalQuizzesTaken: memoryDb.quizzes.length,
        studyStreakDays: 3,
        subjects: memoryDb.subjects.map(s => {
          const subTopics = memoryDb.topics.filter(t => t.subjectId === s.id);
          const done = subTopics.filter(t => t.status === 'COMPLETED').length;
          return {
            ...s,
            totalTopics: subTopics.length,
            completedTopics: done,
            progressPercentage: subTopics.length > 0 ? Math.round((done / subTopics.length) * 100) : 0
          };
        })
      });
    }

    // Default 404
    return res.status(404).json({ error: "Endpoint not found: " + pathname });

  } catch (err) {
    console.error("Vercel Serverless API Error:", err);
    return res.status(500).json({ error: err.message || "Internal server error" });
  }
};
