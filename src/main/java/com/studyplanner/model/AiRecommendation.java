package com.studyplanner.model;

import java.sql.Timestamp;

public class AiRecommendation {
    private int id;
    private int studentId;
    private String recType;
    private String content;
    private Timestamp createdAt;

    public AiRecommendation() {}

    public AiRecommendation(int id, int studentId, String recType, String content, Timestamp createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.recType = recType;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getRecType() { return recType; }
    public void setRecType(String recType) { this.recType = recType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
