# ⚡ AI Adaptive Study Planner

A full-stack, enterprise-grade academic study planning web application built using **HTML5, CSS3, Vanilla JavaScript, Core Java (Jakarta Servlets & JDBC), and MySQL 8.0**. **Zero Spring Boot dependency**.

The application combines **algorithmic scheduling logic** with **Google Gemini AI** to produce personalized study schedules, adaptively reschedule missed sessions, diagnose learning gaps via AI practice quizzes, and explain complex academic topics.

---

## 📋 Features

1. **Student Authentication & Multi-User Profiles**
   - **Sign In & Registration**: Secure account creation with email, full name, password, and target study hours.
   - **Password Security**: Passwords are cryptographically salted and stretched using SHA-256 (10,000 iterations).
   - **Session Isolation**: Each student maintains their own isolated subjects, syllabus topics, exams, adaptive timetables, and quiz scores.
   - **One-Click Demo Login**: Pre-seeded demo account (`alex.mercer@university.edu` / `password123`).

2. **Student Dashboard**
   - KPI metrics: Total Study Hours logged, Syllabus Progress %, Next Exam countdown, and Adaptive Health Score (0–100%).
   - Today's interactive study timetable with one-click actions: **Mark Done**, **Mark Missed**, and **Launch Practice Quiz**.
   - Upcoming exams timeline with urgency visual indicators (< 7 days flagged).
   - High-level AI strategic recommendation snippet.

2. **Coursework CRUD (Subjects, Topics, Exams)**
   - **Subjects**: Add, edit, and delete subjects with custom color themes, codes, and priority levels.
   - **Topics**: Organize topics under subjects with Difficulty (`EASY`, `MEDIUM`, `HARD`), Estimated Hours, Status (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`), and Confidence ratings (1 to 5 stars).
   - **Exams**: Schedule exams with target scores, deadlines, and notes.

3. **Daily & Weekly Timetable Engine**
   - Algorithmic study slot generation based on:
     - **Exam Proximity**: Closer exam deadlines receive higher urgency weights.
     - **Topic Difficulty**: Harder topics receive longer, prioritized sessions.
     - **Confidence Deficit**: Low-confidence topics are scheduled earlier.
     - **Cognitive Interleaving**: Alternates subjects across slots to boost memory retention.
   - Toggle between **Daily View** and **7-Day Weekly Grid View**.

4. **Adaptive Rescheduling Engine**
   - Automatically detects missed or overdue study sessions.
   - Reallocates unfinished sessions into upcoming evening buffer hours before the exam date.
   - **Low Quiz Score Trigger**: Automatically marks topics with quiz scores under 60% as **Weak** and schedules an urgent 60-minute remedial session for the next day.

5. **AI Integration (Google Gemini REST API)**
   - **7-Day Strategic Study Plan**: Tactical day-by-day battle plan tailored to your profile.
   - **Concept Explainer**: Explains difficult concepts using three customizable perspectives:
     - *Intuitive Beginner Analogy* (real-world metaphors)
     - *In-depth Technical Breakdown* (step-by-step logic)
     - *High-Yield Exam Cram Sheet* (formulas, invariant rules, and exam traps)
   - **AI Practice Quiz Generator**: Generates 5-question multiple choice quizzes with instant answer feedback and thorough explanations.
   - **Secure Backend**: API keys are handled strictly on the backend via Java Servlets.
   - **Intelligent Fallback**: If an API key is not yet configured, the system gracefully runs high-quality algorithmic responses so you can use the entire app out of the box.

6. **Analytics & Study Discipline**
   - Study Streak counter tracking consecutive days of completed sessions.
   - Progress breakdown bars per subject.
   - Overall quiz average and attempt history.
   - Weak Topics dashboard with quick-action remediation buttons.

7. **MySQL Relational Database**
   - Fully normalized 7-table schema with foreign keys, cascaded deletions, and realistic academic coursework seed data.

---

## 🛠️ Technology Stack

- **Frontend**: HTML5, Modern CSS3 (CSS Variables, Flexbox, Grid), Vanilla JavaScript (ES6+).
- **Backend**: Core Java (Jakarta Servlet API 6.0, JDBC). *No Spring Boot*.
- **Database**: MySQL Server 8.0+.
- **JSON Processing**: Google Gson 2.10.1.
- **Server**: Apache Tomcat 10 / 11.
- **AI Model**: Google Gemini API (`gemini-1.5-flash` / `gemini-2.0-flash`).

---

## 📁 Project Structure

```
project1/
├── pom.xml                                 # Standard Maven configuration
├── build.ps1                               # Automated PowerShell build & deploy script
├── build.bat                               # Windows Command Prompt batch build script
├── run-server.ps1                          # Server startup & status management script
├── database/
│   └── schema.sql                          # MySQL tables and realistic academic seed data
├── lib/
│   ├── gson-2.10.1.jar                     # JSON serialization library
│   ├── mysql-connector-j.jar               # MySQL JDBC Driver
│   └── servlet-api.jar                     # Jakarta Servlet API
├── src/
│   └── main/
│       ├── java/
│       │   └── com/studyplanner/
│       │       ├── config/
│       │       │   ├── AppConfig.java          # Global AI key & model configuration
│       │       │   └── DatabaseConnection.java # Thread-safe MySQL JDBC connection factory
│       │       ├── dao/
│       │       │   ├── StudentDAO.java
│       │       │   ├── SubjectDAO.java
│       │       │   ├── TopicDAO.java
│       │       │   ├── ExamDAO.java
│       │       │   ├── StudySessionDAO.java
│       │       │   ├── QuizDAO.java
│       │       │   └── AiRecommendationDAO.java
│       │       ├── model/
│       │       │   ├── Student.java
│       │       │   ├── Subject.java
│       │       │   ├── Topic.java
│       │       │   ├── Exam.java
│       │       │   ├── StudySession.java
│       │       │   ├── QuizAttempt.java
│       │       │   ├── QuizQuestion.java
│       │       │   └── DashboardData.java
│       │       ├── service/
│       │       │   ├── SchedulerService.java   # Timetable & adaptive rescheduling algorithms
│       │       │   ├── AiService.java          # Gemini HTTP client & fallback engine
│       │       │   └── AnalyticsService.java   # Progress, streaks, & health score
│       │       ├── servlet/
│       │       │   ├── BaseServlet.java        # JSON & CORS base servlet
│       │       │   ├── DashboardServlet.java   # /api/dashboard
│       │       │   ├── SubjectServlet.java     # /api/subjects/*
│       │       │   ├── TopicServlet.java       # /api/topics/*
│       │       │   ├── ExamServlet.java        # /api/exams/*
│       │       │   ├── TimetableServlet.java   # /api/timetable/*
│       │       │   ├── AiAdvisorServlet.java   # /api/ai/*
│       │       │   ├── QuizServlet.java        # /api/quiz/*
│       │       │   ├── AnalyticsServlet.java   # /api/analytics
│       │       │   └── HealthServlet.java      # /api/health
│       │       └── util/
│       │           └── JsonUtils.java          # Custom Gson serializer with SQL Date adapters
│       ├── resources/
│       │   ├── db.properties                   # MySQL connection credentials
│       │   └── ai.properties                   # AI provider & model settings
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── classes/                    # Compiled bytecode (.class files)
│           │   ├── lib/                        # Runtime JARs (gson, mysql-connector)
│           │   └── web.xml                     # Servlet mappings & configuration
│           ├── css/
│           │   ├── style.css                   # Core typography, layout, themes, & modals
│           │   └── components.css              # Timetable calendar, quiz player, cards
│           ├── js/
│           │   ├── app.js                      # Routing, notifications, & AI modal
│           │   ├── dashboard.js                # KPI metrics & today's schedule
│           │   ├── subjects.js                 # Subject, topic, & exam CRUD
│           │   ├── timetable.js                # Calendar view & adaptive slot manager
│           │   ├── ai-advisor.js               # 7-day strategy & concept explainer
│           │   ├── quiz.js                     # AI quiz taker & automatic rescheduling
│           │   └── analytics.js                # Progress bars, streaks, & weak topics
│           └── index.html                      # Single Page Responsive Dashboard
└── README.md
```

---

## 🚀 Quick Setup & Installation

### Step 1: Database Setup
1. Ensure your MySQL Server is running (e.g., service `MySQL80`).
2. Run the schema script using MySQL CLI or MySQL Workbench:
   ```powershell
   Get-Content "database\schema.sql" | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```
3. Verify or edit your credentials in `src/main/resources/db.properties`:
   ```properties
   db.driver=com.mysql.cj.jdbc.Driver
   db.url=jdbc:mysql://localhost:3306/ai_study_planner_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
   db.user=root
   db.password=Harshi@09
   ```
   *(Note: You can also override credentials at runtime via environment variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`).*

---

### Step 2: Build & Package the Application
You can build the project with either PowerShell, Batch, or Maven:

- **Using PowerShell (Recommended)**:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\build.ps1
  ```
- **Using Batch (Windows Command Prompt)**:
  ```cmd
  build.bat
  ```
- **Using Maven**:
  ```bash
  mvn clean package
  ```

The build script will compile all Java sources, package `studyplanner.war`, and automatically deploy it to Tomcat's `webapps/` folder.

---

### Step 3: Run Apache Tomcat Server
- To start Tomcat:
  ```powershell
  $env:CATALINA_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
  & "$env:CATALINA_HOME\bin\catalina.bat" run
  ```
  *(Or use the included helper: `powershell -ExecutionPolicy Bypass -File .\run-server.ps1 start`)*

- Open your browser and navigate to:
  ```
  http://localhost:8080/studyplanner/
  ```

### 👤 Demo Student Login Credentials:
- **Email**: `alex.mercer@university.edu`
- **Password**: `password123`
*(Or click the **"⚡ Demo Login"** button on the sign-in modal, or register a new account!)*

---

## 🔑 AI API Key Configuration (Google Gemini)

The application communicates with Google Gemini API directly from the Java backend using `java.net.http.HttpClient`. Your API key is never exposed to the frontend browser.

### How to get an API key:
1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Sign in with your Google Account and click **"Create API Key"**.
3. Copy the key.

### 3 Ways to Configure the Key:

1. **Option A: From the Web UI (Easiest)**
   - Click the **"Settings ⚙️"** pill located in the lower-left corner of the sidebar.
   - Paste your Gemini API Key and select your preferred model (`gemini-1.5-flash` or `gemini-2.0-flash`).
   - Click **"Save Configuration"**. It takes effect immediately without restarting the server!

2. **Option B: Via Environment Variable**
   - Set the system environment variable:
     ```powershell
     [System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'your_actual_key_here', 'User')
     ```

3. **Option C: In Properties File**
   - Open `src/main/resources/ai.properties` and add your key:
     ```properties
     ai.provider=gemini
     ai.api.key=your_actual_key_here
     ai.model=gemini-1.5-flash
     ```
   - Re-run `build.ps1`.

### What if no API key is provided?
The application includes an **intelligent fallback engine**. If no key is set or network access is restricted:
- The UI displays an info indicator.
- The 7-Day Strategy generator uses adaptive rule-based heuristics based on your actual database data.
- The Topic Explainer provides structured academic breakdowns for your coursework.
- Practice Quizzes generate realistic, high-quality multiple-choice questions for tested computer science topics.
- **Zero crashes, zero fake broken interfaces**. Once you enter an API key, it switches to live Gemini AI instantly.

---

## 🧪 Verification & Testing

A verification test class is included at `com.studyplanner.test.TestBackend`. You can run it directly:

```powershell
$classpath = "src\main\webapp\WEB-INF\classes;lib\servlet-api.jar;lib\gson-2.10.1.jar;lib\mysql-connector-j.jar"
java -cp $classpath com.studyplanner.test.TestBackend
```

It validates:
- MySQL JDBC connectivity.
- Subject, Topic, Exam, and Session retrieval.
- Timetable generation algorithm.
- Adaptive rescheduling of missed sessions.
- Health score and analytics computation.
- AI service prompt and fallback generator.

---

## 🛡️ API Endpoints Summary

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | System status, DB connection test, & AI status |
| `GET` | `/api/dashboard` | Aggregated dashboard stats, exams, & today's schedule |
| `GET`, `POST` | `/api/subjects` | List all subjects / Create subject |
| `PUT`, `DELETE`| `/api/subjects/{id}` | Update / Delete subject |
| `GET`, `POST` | `/api/topics` | List topics / Create topic |
| `PUT`, `DELETE`| `/api/topics/{id}` | Update / Delete topic |
| `GET`, `POST` | `/api/exams` | List exams / Create upcoming exam |
| `GET` | `/api/timetable` | Get daily or weekly study schedule slots |
| `POST` | `/api/timetable/generate` | Algorithmic generation of study timetable |
| `POST` | `/api/timetable/reschedule`| **Adaptive Rescheduling** of missed & overdue sessions |
| `POST` | `/api/timetable/complete` | Mark session completed & record actual study minutes |
| `POST` | `/api/timetable/miss` | Mark session missed |
| `GET`, `POST` | `/api/ai/config` | Get AI status / Set Gemini API key & model |
| `POST` | `/api/ai/recommendations`| Generate personalized 7-day tactical strategy |
| `POST` | `/api/ai/explain` | AI concept breakdown with intuition, formulas, & traps |
| `POST` | `/api/quiz/generate` | AI practice quiz generator (5 multiple-choice questions) |
| `POST` | `/api/quiz/submit` | Record quiz attempt & trigger remedial rescheduling if score < 60% |
| `GET` | `/api/quiz/history` | Quiz score log & feedback |
| `GET` | `/api/analytics` | Study streaks, completion rates, & weak topics |

---

## 💡 License
Built for educational and academic productivity optimization.
