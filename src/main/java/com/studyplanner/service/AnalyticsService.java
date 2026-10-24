package com.studyplanner.service;

import com.studyplanner.dao.*;
import com.studyplanner.model.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class AnalyticsService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final StudySessionDAO sessionDAO = new StudySessionDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final AiRecommendationDAO recDAO = new AiRecommendationDAO();

    public DashboardData getDashboardData(int studentId) {
        DashboardData data = new DashboardData();
        Student student = studentDAO.getStudent(studentId);
        if (student == null) {
            student = new Student(1, "Student", "student@university.edu", 3.0);
        }
        data.setStudent(student);

        List<Subject> subjects = subjectDAO.getAll(studentId);
        List<Topic> allTopics = topicDAO.getAll(studentId);
        List<Exam> upcomingExams = examDAO.getUpcoming(studentId);
        List<StudySession> todaySessions = sessionDAO.getTodaySessions(studentId);
        List<Topic> weakTopics = topicDAO.getWeakTopics(studentId);
        List<QuizAttempt> recentQuizzes = quizDAO.getRecentAttempts(studentId, 5);

        data.setSubjects(subjects);
        data.setUpcomingExams(upcomingExams);
        data.setTodaySessions(todaySessions);
        data.setWeakTopics(weakTopics);
        data.setRecentQuizzes(recentQuizzes);

        int totalTopics = allTopics.size();
        int completedTopics = (int) allTopics.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        double overallProgress = totalTopics > 0 ? Math.round((completedTopics * 100.0 / totalTopics) * 10.0) / 10.0 : 0.0;

        int totalStudyMinutes = sessionDAO.getTotalCompletedStudyMinutes(studentId);
        double totalStudyHours = Math.round((totalStudyMinutes / 60.0) * 10.0) / 10.0;
        int missedCount = sessionDAO.getMissedCount(studentId);

        data.setTotalSubjects(subjects.size());
        data.setTotalTopics(totalTopics);
        data.setCompletedTopics(completedTopics);
        data.setOverallProgressPercentage(overallProgress);
        data.setTotalStudyHours(totalStudyHours);
        data.setMissedSessionsCount(missedCount);
        data.setWeakTopicsCount(weakTopics.size());

        // Calculate Adaptive Health Score (0 - 100)
        // 40% Topic completion, 30% Quiz average, 20% Session adherence, 10% Weak topic penalty
        double quizAvg = quizDAO.getOverallAverageScore(studentId);
        if (quizAvg == 0.0 && !recentQuizzes.isEmpty()) {
            quizAvg = 75.0;
        } else if (quizAvg == 0.0) {
            quizAvg = 80.0;
        }

        double sessionAdherence = (missedCount == 0) ? 100.0 : Math.max(20.0, 100.0 - (missedCount * 12.0));
        double weakPenalty = Math.max(0.0, weakTopics.size() * 5.0);

        int healthScore = (int) Math.round(
                (overallProgress * 0.35) +
                (quizAvg * 0.35) +
                (sessionAdherence * 0.30) -
                weakPenalty
        );
        if (healthScore < 10) healthScore = 10;
        if (healthScore > 100) healthScore = 100;
        data.setHealthScore(healthScore);

        AiRecommendation latestRec = recDAO.getLatest(studentId, "DAILY_STRATEGY");
        if (latestRec != null) {
            data.setLatestAiRecommendation(latestRec.getContent());
        }

        return data;
    }

    public Map<String, Object> getDetailedAnalytics(int studentId) {
        Map<String, Object> map = new HashMap<>();

        List<Subject> subjects = subjectDAO.getAll(studentId);
        List<Topic> allTopics = topicDAO.getAll(studentId);
        List<QuizAttempt> recentQuizzes = quizDAO.getRecentAttempts(studentId, 15);
        int totalStudyMinutes = sessionDAO.getTotalCompletedStudyMinutes(studentId);

        // Difficulty Breakdown
        long easyCount = allTopics.stream().filter(t -> "EASY".equalsIgnoreCase(t.getDifficulty())).count();
        long medCount = allTopics.stream().filter(t -> "MEDIUM".equalsIgnoreCase(t.getDifficulty())).count();
        long hardCount = allTopics.stream().filter(t -> "HARD".equalsIgnoreCase(t.getDifficulty())).count();

        // Status Breakdown
        long completedCount = allTopics.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = allTopics.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long notStartedCount = allTopics.stream().filter(t -> "NOT_STARTED".equalsIgnoreCase(t.getStatus())).count();

        // Calculate Study Streak (consecutive days with completed sessions ending today/yesterday)
        int streak = calculateStudyStreak(studentId);

        map.put("totalSubjects", subjects.size());
        map.put("totalTopics", allTopics.size());
        map.put("completedTopics", completedCount);
        map.put("inProgressTopics", inProgressCount);
        map.put("notStartedTopics", notStartedCount);
        map.put("easyCount", easyCount);
        map.put("mediumCount", medCount);
        map.put("hardCount", hardCount);
        map.put("totalStudyHours", Math.round((totalStudyMinutes / 60.0) * 10.0) / 10.0);
        map.put("overallQuizAverage", quizDAO.getOverallAverageScore(studentId));
        map.put("totalQuizzesTaken", quizDAO.getTotalQuizzesTaken(studentId));
        map.put("studyStreakDays", streak);
        map.put("subjects", subjects);
        map.put("recentQuizzes", recentQuizzes);

        return map;
    }

    private int calculateStudyStreak(int studentId) {
        // Query distinct completed session dates in last 30 days
        List<StudySession> sessions = sessionDAO.getByDateRange(studentId,
                LocalDate.now().minusDays(30), LocalDate.now());

        Set<LocalDate> completedDates = new HashSet<>();
        for (StudySession s : sessions) {
            if ("COMPLETED".equalsIgnoreCase(s.getStatus()) && s.getScheduledDate() != null) {
                completedDates.add(s.getScheduledDate().toLocalDate());
            }
        }

        int streak = 0;
        LocalDate checkDate = LocalDate.now();
        // If today not completed yet, check if yesterday was completed
        if (!completedDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1);
        }

        while (completedDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return Math.max(1, streak);
    }
}
