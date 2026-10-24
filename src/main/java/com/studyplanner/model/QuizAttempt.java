package com.studyplanner.model;

import java.sql.Timestamp;

public class QuizAttempt {
    private int id;
    private int studentId;
    private int topicId;
    private String topicName;
    private String subjectName;
    private double score;
    private int totalQuestions;
    private int correctAnswers;
    private String feedback;
    private Timestamp attemptedAt;

    public QuizAttempt() {}

    public QuizAttempt(int id, int studentId, int topicId, double score, int totalQuestions, int correctAnswers, String feedback) {
        this.id = id;
        this.studentId = studentId;
        this.topicId = topicId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.feedback = feedback;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Timestamp getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(Timestamp attemptedAt) { this.attemptedAt = attemptedAt; }
}
