package com.studyplanner.model;

import java.util.List;

public class DashboardData {
    private Student student;
    private int totalSubjects;
    private int totalTopics;
    private int completedTopics;
    private double overallProgressPercentage;
    private double totalStudyHours;
    private int missedSessionsCount;
    private int weakTopicsCount;
    private int healthScore; // 0 - 100 Adaptive Health
    private List<Subject> subjects;
    private List<Exam> upcomingExams;
    private List<StudySession> todaySessions;
    private List<Topic> weakTopics;
    private List<QuizAttempt> recentQuizzes;
    private String latestAiRecommendation;

    public DashboardData() {}

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public int getTotalSubjects() { return totalSubjects; }
    public void setTotalSubjects(int totalSubjects) { this.totalSubjects = totalSubjects; }

    public int getTotalTopics() { return totalTopics; }
    public void setTotalTopics(int totalTopics) { this.totalTopics = totalTopics; }

    public int getCompletedTopics() { return completedTopics; }
    public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }

    public double getOverallProgressPercentage() { return overallProgressPercentage; }
    public void setOverallProgressPercentage(double overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }

    public double getTotalStudyHours() { return totalStudyHours; }
    public void setTotalStudyHours(double totalStudyHours) { this.totalStudyHours = totalStudyHours; }

    public int getMissedSessionsCount() { return missedSessionsCount; }
    public void setMissedSessionsCount(int missedSessionsCount) { this.missedSessionsCount = missedSessionsCount; }

    public int getWeakTopicsCount() { return weakTopicsCount; }
    public void setWeakTopicsCount(int weakTopicsCount) { this.weakTopicsCount = weakTopicsCount; }

    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }

    public List<Subject> getSubjects() { return subjects; }
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }

    public List<Exam> getUpcomingExams() { return upcomingExams; }
    public void setUpcomingExams(List<Exam> upcomingExams) { this.upcomingExams = upcomingExams; }

    public List<StudySession> getTodaySessions() { return todaySessions; }
    public void setTodaySessions(List<StudySession> todaySessions) { this.todaySessions = todaySessions; }

    public List<Topic> getWeakTopics() { return weakTopics; }
    public void setWeakTopics(List<Topic> weakTopics) { this.weakTopics = weakTopics; }

    public List<QuizAttempt> getRecentQuizzes() { return recentQuizzes; }
    public void setRecentQuizzes(List<QuizAttempt> recentQuizzes) { this.recentQuizzes = recentQuizzes; }

    public String getLatestAiRecommendation() { return latestAiRecommendation; }
    public void setLatestAiRecommendation(String latestAiRecommendation) { this.latestAiRecommendation = latestAiRecommendation; }
}
