package com.studyplanner.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class StudySession {
    private int id;
    private int studentId;
    private int topicId;
    private String topicName;
    private int subjectId;
    private String subjectName;
    private String subjectColor;
    private Date scheduledDate;
    private Time startTime;
    private int durationMinutes;
    private String status; // SCHEDULED, COMPLETED, MISSED, RESCHEDULED
    private int actualDurationMinutes;
    private boolean isRemedial;
    private String notes;
    private Timestamp completedAt;
    private Timestamp createdAt;

    public StudySession() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectColor() { return subjectColor; }
    public void setSubjectColor(String subjectColor) { this.subjectColor = subjectColor; }

    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(int actualDurationMinutes) { this.actualDurationMinutes = actualDurationMinutes; }

    public boolean isRemedial() { return isRemedial; }
    public void setRemedial(boolean remedial) { isRemedial = remedial; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
