package com.studyplanner.model;

import java.sql.Timestamp;

public class Topic {
    private int id;
    private int subjectId;
    private String subjectName;
    private String subjectColor;
    private String name;
    private String difficulty; // EASY, MEDIUM, HARD
    private double estimatedHours;
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private int confidenceLevel; // 1 to 5
    private boolean isWeak;
    private Timestamp createdAt;

    public Topic() {}

    public Topic(int id, int subjectId, String name, String difficulty, double estimatedHours,
                 String status, int confidenceLevel, boolean isWeak) {
        this.id = id;
        this.subjectId = subjectId;
        this.name = name;
        this.difficulty = difficulty;
        this.estimatedHours = estimatedHours;
        this.status = status;
        this.confidenceLevel = confidenceLevel;
        this.isWeak = isWeak;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectColor() { return subjectColor; }
    public void setSubjectColor(String subjectColor) { this.subjectColor = subjectColor; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(double estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(int confidenceLevel) { this.confidenceLevel = confidenceLevel; }

    public boolean isWeak() { return isWeak; }
    public void setWeak(boolean weak) { isWeak = weak; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
