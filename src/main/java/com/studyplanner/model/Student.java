package com.studyplanner.model;

import java.sql.Timestamp;

public class Student {
    private int id;
    private String name;
    private String email;
    private transient String passwordHash;
    private transient String salt;
    private double targetDailyHours;
    private Timestamp createdAt;

    public Student() {}

    public Student(int id, String name, String email, double targetDailyHours) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.targetDailyHours = targetDailyHours;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public double getTargetDailyHours() { return targetDailyHours; }
    public void setTargetDailyHours(double targetDailyHours) { this.targetDailyHours = targetDailyHours; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
