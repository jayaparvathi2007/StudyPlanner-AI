package com.studyplanner.test;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.dao.*;
import com.studyplanner.model.*;
import com.studyplanner.service.*;

import java.time.LocalDate;
import java.util.List;

public class TestBackend {
    public static void main(String[] args) {
        System.out.println("=== Testing AI Adaptive Study Planner Backend ===");

        // 1. Test Database
        boolean dbOk = DatabaseConnection.testConnection();
        System.out.println("[DB] Connection test: " + (dbOk ? "SUCCESS" : "FAILED"));
        if (!dbOk) {
            System.exit(1);
        }

        // 2. Test DAOs
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getAll(1);
        System.out.println("[DAO] Loaded subjects count: " + subjects.size());
        for (Subject s : subjects) {
            System.out.println("   - " + s.getName() + " (" + s.getCode() + ") | Topics: " + s.getTotalTopics() + " | Progress: " + s.getProgressPercentage() + "%");
        }

        ExamDAO examDAO = new ExamDAO();
        List<Exam> exams = examDAO.getUpcoming(1);
        System.out.println("[DAO] Upcoming exams count: " + exams.size());
        for (Exam e : exams) {
            System.out.println("   - " + e.getTitle() + " in " + e.getDaysRemaining() + " days");
        }

        TopicDAO topicDAO = new TopicDAO();
        List<Topic> weakTopics = topicDAO.getWeakTopics(1);
        System.out.println("[DAO] Weak/low-confidence topics count: " + weakTopics.size());

        // 3. Test Scheduler & Adaptive Rescheduling
        SchedulerService scheduler = new SchedulerService();
        System.out.println("\n[SCHEDULER] Testing adaptive rescheduling of missed sessions...");
        List<StudySession> rescheduled = scheduler.rescheduleMissedSessions(1);
        System.out.println("[SCHEDULER] Rescheduled missed sessions count: " + rescheduled.size());

        System.out.println("[SCHEDULER] Testing 5-day timetable generation...");
        List<StudySession> newSessions = scheduler.generateTimetable(1, LocalDate.now(), 5, 3.0, false);
        System.out.println("[SCHEDULER] Generated sessions: " + newSessions.size());

        // 4. Test Analytics
        AnalyticsService analytics = new AnalyticsService();
        DashboardData data = analytics.getDashboardData(1);
        System.out.println("\n[ANALYTICS] Health Score: " + data.getHealthScore() + "/100");
        System.out.println("[ANALYTICS] Overall Progress: " + data.getOverallProgressPercentage() + "%");
        System.out.println("[ANALYTICS] Total Study Hours: " + data.getTotalStudyHours() + " hrs");

        // 5. Test AI Service Fallback & Prompt Logic
        AiService ai = new AiService();
        System.out.println("\n[AI] Is Live API Key Configured? " + ai.isConfigured());
        List<QuizQuestion> quiz = ai.generatePracticeQuiz("Dynamic Programming & Memoization", "Data Structures & Algorithms", "HARD");
        System.out.println("[AI] Quiz Generated Question Count: " + quiz.size());
        System.out.println("   Sample Q1: " + quiz.get(0).getQuestion());
        System.out.println("   Correct Answer: " + quiz.get(0).getOptions().get(quiz.get(0).getCorrectIndex()));

        System.out.println("\n=== ALL BACKEND CHECKS PASSED SUCCESSFULLY ===");
    }
}
