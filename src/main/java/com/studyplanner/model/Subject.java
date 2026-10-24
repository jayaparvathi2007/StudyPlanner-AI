package com.studyplanner.model;

import java.sql.Timestamp;

public class Subject {
    private int id;
    private int studentId;
    private String name;
    private String code;
    private String color;
    private String priority; // HIGH, MEDIUM, LOW
    private String description;
    private Timestamp createdAt;

    // Computed attributes for views
    private int totalTopics;
    private int completedTopics;
    private double progressPercentage;
    private String nextExamDate;

    public Subject() {}

    public Subject(int id, int studentId, String name, String code, String color, String priority, String description) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.code = code;
        this.color = color;
        this.priority = priority;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getTotalTopics() { return totalTopics; }
    public void setTotalTopics(int totalTopics) { this.totalTopics = totalTopics; }

    public int getCompletedTopics() { return completedTopics; }
    public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public String getNextExamDate() { return nextExamDate; }
    public void setNextExamDate(String nextExamDate) { this.nextExamDate = nextExamDate; }
}
