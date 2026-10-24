package com.studyplanner.dao;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.model.Exam;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    public List<Exam> getAll(int studentId) {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT e.id, e.subject_id, e.title, e.exam_date, e.target_score, e.priority, e.notes, e.created_at, " +
                     "s.name AS subject_name, s.code AS subject_code, s.color AS subject_color " +
                     "FROM exams e " +
                     "JOIN subjects s ON e.subject_id = s.id " +
                     "WHERE s.student_id = ? " +
                     "ORDER BY e.exam_date ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                LocalDate today = LocalDate.now();
                while (rs.next()) {
                    Exam ex = mapResultSetToExam(rs, today);
                    list.add(ex);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Exam> getUpcoming(int studentId) {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT e.id, e.subject_id, e.title, e.exam_date, e.target_score, e.priority, e.notes, e.created_at, " +
                     "s.name AS subject_name, s.code AS subject_code, s.color AS subject_color " +
                     "FROM exams e " +
                     "JOIN subjects s ON e.subject_id = s.id " +
                     "WHERE s.student_id = ? AND e.exam_date >= CURRENT_DATE " +
                     "ORDER BY e.exam_date ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                LocalDate today = LocalDate.now();
                while (rs.next()) {
                    Exam ex = mapResultSetToExam(rs, today);
                    list.add(ex);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Exam getById(int id) {
        String sql = "SELECT e.id, e.subject_id, e.title, e.exam_date, e.target_score, e.priority, e.notes, e.created_at, " +
                     "s.name AS subject_name, s.code AS subject_code, s.color AS subject_color " +
                     "FROM exams e " +
                     "JOIN subjects s ON e.subject_id = s.id " +
                     "WHERE e.id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExam(rs, LocalDate.now());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int create(Exam e) {
        String sql = "INSERT INTO exams (subject_id, title, exam_date, target_score, priority, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getSubjectId());
            ps.setString(2, e.getTitle());
            ps.setDate(3, e.getExamDate());
            ps.setInt(4, e.getTargetScore() > 0 ? e.getTargetScore() : 90);
            ps.setString(5, e.getPriority() != null ? e.getPriority() : "HIGH");
            ps.setString(6, e.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public boolean update(Exam e) {
        String sql = "UPDATE exams SET title = ?, exam_date = ?, target_score = ?, priority = ?, notes = ?, subject_id = ? " +
                     "WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setDate(2, e.getExamDate());
            ps.setInt(3, e.getTargetScore());
            ps.setString(4, e.getPriority());
            ps.setString(5, e.getNotes());
            ps.setInt(6, e.getSubjectId());
            ps.setInt(7, e.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM exams WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Exam mapResultSetToExam(ResultSet rs, LocalDate today) throws SQLException {
        Exam ex = new Exam();
        ex.setId(rs.getInt("id"));
        ex.setSubjectId(rs.getInt("subject_id"));
        ex.setTitle(rs.getString("title"));
        Date examDate = rs.getDate("exam_date");
        ex.setExamDate(examDate);
        ex.setTargetScore(rs.getInt("target_score"));
        ex.setPriority(rs.getString("priority"));
        ex.setNotes(rs.getString("notes"));
        ex.setCreatedAt(rs.getTimestamp("created_at"));
        ex.setSubjectName(rs.getString("subject_name"));
        ex.setSubjectCode(rs.getString("subject_code"));
        ex.setSubjectColor(rs.getString("subject_color"));

        if (examDate != null) {
            long days = ChronoUnit.DAYS.between(today, examDate.toLocalDate());
            ex.setDaysRemaining(days);
        }
        return ex;
    }
}
