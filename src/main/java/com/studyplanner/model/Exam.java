package com.studyplanner.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Exam {
    private int id;
    private int subjectId;
    private String subjectName;
    private String subjectCode;
    private String subjectColor;
    private String title;
    private Date examDate;
    private int targetScore;
    private String priority; // HIGH, MEDIUM, LOW
    private String notes;
    private Timestamp createdAt;

    // Computed
    private long daysRemaining;

    public Exam() {}

    public Exam(int id, int subjectId, String title, Date examDate, int targetScore, String priority, String notes) {
        this.id = id;
        this.subjectId = subjectId;
        this.title = title;
        this.examDate = examDate;
        this.targetScore = targetScore;
        this.priority = priority;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectColor() { return subjectColor; }
    public void setSubjectColor(String subjectColor) { this.subjectColor = subjectColor; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }

    public int getTargetScore() { return targetScore; }
    public void setTargetScore(int targetScore) { this.targetScore = targetScore; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }
}
