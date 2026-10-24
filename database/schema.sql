-- =====================================================================
-- AI Adaptive Study Planner - Database Schema & Seed Data
-- =====================================================================

CREATE DATABASE IF NOT EXISTS ai_study_planner_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ai_study_planner_db;

-- 1. Students / Users
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    target_daily_hours DECIMAL(3,1) DEFAULT 3.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Subjects
CREATE TABLE IF NOT EXISTS subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#4f46e5',
    priority ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Topics
CREATE TABLE IF NOT EXISTS topics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    estimated_hours DECIMAL(4,1) DEFAULT 2.0,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'NOT_STARTED',
    confidence_level INT DEFAULT 3 CHECK (confidence_level BETWEEN 1 AND 5),
    is_weak BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Exams
CREATE TABLE IF NOT EXISTS exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    exam_date DATE NOT NULL,
    target_score INT DEFAULT 90,
    priority ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'HIGH',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Study Sessions (Timetable slots)
CREATE TABLE IF NOT EXISTS study_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    topic_id INT NOT NULL,
    scheduled_date DATE NOT NULL,
    start_time TIME DEFAULT '09:00:00',
    duration_minutes INT DEFAULT 60,
    status ENUM('SCHEDULED', 'COMPLETED', 'MISSED', 'RESCHEDULED') DEFAULT 'SCHEDULED',
    actual_duration_minutes INT DEFAULT 0,
    is_remedial BOOLEAN DEFAULT FALSE,
    notes VARCHAR(255),
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Quiz Attempts
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    topic_id INT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    feedback TEXT,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. AI Recommendations
CREATE TABLE IF NOT EXISTS ai_recommendations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    rec_type ENUM('DAILY_STRATEGY', 'TOPIC_GUIDE', 'ADAPTIVE_ALERT') DEFAULT 'DAILY_STRATEGY',
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- Seed Initial Sample Data (Realistic Academic Coursework)
-- =====================================================================

-- Clean old sample records if re-running
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE ai_recommendations;
TRUNCATE TABLE quiz_attempts;
TRUNCATE TABLE study_sessions;
TRUNCATE TABLE exams;
TRUNCATE TABLE topics;
TRUNCATE TABLE subjects;
TRUNCATE TABLE students;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Default Student
INSERT INTO students (id, name, email, target_daily_hours) VALUES
(1, 'Alex Mercer', 'alex.mercer@university.edu', 3.5);

-- 2. Subjects
INSERT INTO subjects (id, student_id, name, code, color, priority, description) VALUES
(1, 1, 'Data Structures & Algorithms', 'CS301', '#3b82f6', 'HIGH', 'Core algorithms, complexity, graphs, and dynamic programming'),
(2, 1, 'Database Management Systems', 'CS302', '#10b981', 'HIGH', 'Relational algebra, SQL, normalization, and ACID transactions'),
(3, 1, 'Operating Systems', 'CS303', '#f59e0b', 'MEDIUM', 'Process scheduling, deadlocks, memory virtualization, and paging'),
(4, 1, 'Computer Networks', 'CS304', '#8b5cf6', 'MEDIUM', 'OSI model, TCP/IP handshake, routing protocols, and socket programming');

-- 3. Topics
INSERT INTO topics (id, subject_id, name, difficulty, estimated_hours, status, confidence_level, is_weak) VALUES
-- CS301 DSA
(1, 1, 'Binary Search Trees & AVL Trees', 'MEDIUM', 3.0, 'COMPLETED', 4, FALSE),
(2, 1, 'Dynamic Programming & Memoization', 'HARD', 5.0, 'IN_PROGRESS', 2, TRUE),
(3, 1, 'Graph Traversals (BFS & DFS)', 'MEDIUM', 3.0, 'COMPLETED', 4, FALSE),
(4, 1, 'Dijkstra & Shortest Path Algorithms', 'HARD', 4.0, 'NOT_STARTED', 2, TRUE),
-- CS302 DBMS
(5, 2, 'Entity-Relationship (ER) Modeling', 'EASY', 2.0, 'COMPLETED', 5, FALSE),
(6, 2, 'BCNF & Normalization Forms (1NF-3NF)', 'HARD', 4.0, 'IN_PROGRESS', 2, TRUE),
(7, 2, 'ACID Properties & Transaction Isolation', 'MEDIUM', 3.0, 'NOT_STARTED', 3, FALSE),
(8, 2, 'B+ Tree Indexing & Query Optimization', 'HARD', 3.5, 'NOT_STARTED', 2, FALSE),
-- CS303 OS
(9, 3, 'CPU Scheduling (Round Robin, SRTF)', 'MEDIUM', 2.5, 'COMPLETED', 4, FALSE),
(10, 3, 'Deadlock Detection & Banker Algorithm', 'HARD', 3.5, 'IN_PROGRESS', 2, TRUE),
(11, 3, 'Virtual Memory & Page Replacement', 'MEDIUM', 3.0, 'NOT_STARTED', 3, FALSE),
-- CS304 CN
(12, 4, 'TCP vs UDP Protocols & 3-Way Handshake', 'MEDIUM', 2.5, 'COMPLETED', 5, FALSE),
(13, 4, 'Subnetting & CIDR IP Addressing', 'HARD', 4.0, 'NOT_STARTED', 2, TRUE),
(14, 4, 'DNS, HTTP/2, and TLS Security', 'EASY', 2.0, 'NOT_STARTED', 4, FALSE);

-- 4. Upcoming Exams
INSERT INTO exams (id, subject_id, title, exam_date, target_score, priority, notes) VALUES
(1, 1, 'DSA Midterm Examination', DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 92, 'HIGH', 'Covers Trees, Graphs, Sorting, and Dynamic Programming'),
(2, 2, 'DBMS Final Theory Exam', DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY), 90, 'HIGH', 'Includes Relational Algebra, SQL, Normalization, Transactions'),
(3, 3, 'Operating Systems Assessment', DATE_ADD(CURRENT_DATE, INTERVAL 18 DAY), 85, 'MEDIUM', 'Processes, Semaphores, Deadlocks, Paging'),
(4, 4, 'Networks Lab & Theory Exam', DATE_ADD(CURRENT_DATE, INTERVAL 24 DAY), 88, 'MEDIUM', 'OSI model, Subnetting, Socket programming');

-- 5. Study Sessions (Today and Next Few Days)
INSERT INTO study_sessions (student_id, topic_id, scheduled_date, start_time, duration_minutes, status, actual_duration_minutes, is_remedial, notes) VALUES
-- Past / Completed
(1, 1, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '09:00:00', 60, 'COMPLETED', 65, FALSE, 'Reviewed AVL tree rotations and balance factor calculations.'),
(1, 5, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:00:00', 60, 'COMPLETED', 60, FALSE, 'Constructed ER diagrams for library management system.'),
(1, 9, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00', 60, 'COMPLETED', 55, FALSE, 'Solved Round Robin and Priority scheduling turnaround problems.'),
-- Overdue / Missed session (demonstrates adaptive rescheduling!)
(1, 2, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '16:00:00', 90, 'MISSED', 0, FALSE, 'Missed due to college sports event. Needs adaptive rescheduling.'),
-- Today's Scheduled Sessions
(1, 2, CURRENT_DATE, '10:00:00', 90, 'SCHEDULED', 0, TRUE, 'Remedial session: Dynamic Programming (0/1 Knapsack & LCS).'),
(1, 6, CURRENT_DATE, '14:30:00', 60, 'SCHEDULED', 0, FALSE, 'Focus on finding candidate keys and 3NF vs BCNF decomposition.'),
(1, 10, CURRENT_DATE, '17:00:00', 60, 'SCHEDULED', 0, FALSE, 'Practice Banker Algorithm safety check matrices.'),
-- Tomorrow
(1, 4, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '09:30:00', 90, 'SCHEDULED', 0, FALSE, 'Dijkstra shortest path with adjacency list and priority queue.'),
(1, 7, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '14:00:00', 60, 'SCHEDULED', 0, FALSE, 'ACID properties and Dirty Read, Non-repeatable Read phenomena.'),
-- Day + 2
(1, 13, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '10:00:00', 90, 'SCHEDULED', 0, FALSE, 'Subnet mask calculations and host ranges.'),
(1, 8, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00', 60, 'SCHEDULED', 0, FALSE, 'B+ Tree insertion and leaf node split operations.');

-- 6. Quiz Attempts (Illustrates weak topics and scoring history)
INSERT INTO quiz_attempts (student_id, topic_id, score, total_questions, correct_answers, feedback) VALUES
(1, 1, 90.0, 10, 9, 'Excellent grasp on tree balance factors and rotational mechanics.'),
(1, 9, 80.0, 5, 4, 'Solid understanding of CPU scheduling formulas.'),
(1, 2, 40.0, 5, 2, 'Struggled with optimal substructure and state transition relations. Flagged as WEAK topic for AI remedial review.'),
(1, 6, 50.0, 6, 3, 'Lossless join and dependency preservation need revision. Flagged as WEAK topic.');

-- 7. Initial AI Recommendation Sample
INSERT INTO ai_recommendations (student_id, rec_type, content) VALUES
(1, 'DAILY_STRATEGY', 
 '### Priority Focus: DSA Midterm in 6 Days\n\n'
 '1. **Immediate Attention**: Your Dynamic Programming quiz score (40%) and BCNF score (50%) indicate critical comprehension gaps. Focus your first 90 minutes today on Dynamic Programming.\n'
 '2. **Spaced Repetition**: Review Binary Search Tree rotation algorithms for 20 minutes before attempting Dijkstra.\n'
 '3. **High-Yield Practice**: Work through 3 classic DP problems: 0/1 Knapsack, Longest Common Subsequence, and Coin Change.\n'
 '4. **Adaptive Alert**: A missed session from yesterday has been flagged. Timetable adjusted with higher weight on weak topics.'
);
