package com.studyplanner.dao;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    public List<Subject> getAll(int studentId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT s.id, s.student_id, s.name, s.code, s.color, s.priority, s.description, s.created_at, " +
                     "COUNT(t.id) AS total_topics, " +
                     "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_topics, " +
                     "MIN(e.exam_date) AS next_exam_date " +
                     "FROM subjects s " +
                     "LEFT JOIN topics t ON s.id = t.subject_id " +
                     "LEFT JOIN exams e ON s.id = e.subject_id AND e.exam_date >= CURRENT_DATE " +
                     "WHERE s.student_id = ? " +
                     "GROUP BY s.id, s.student_id, s.name, s.code, s.color, s.priority, s.description, s.created_at " +
                     "ORDER BY s.id ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subject sub = new Subject();
                    sub.setId(rs.getInt("id"));
                    sub.setStudentId(rs.getInt("student_id"));
                    sub.setName(rs.getString("name"));
                    sub.setCode(rs.getString("code"));
                    sub.setColor(rs.getString("color"));
                    sub.setPriority(rs.getString("priority"));
                    sub.setDescription(rs.getString("description"));
                    sub.setCreatedAt(rs.getTimestamp("created_at"));

                    int total = rs.getInt("total_topics");
                    int completed = rs.getInt("completed_topics");
                    sub.setTotalTopics(total);
                    sub.setCompletedTopics(completed);
                    sub.setProgressPercentage(total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0.0);

                    Date nextExam = rs.getDate("next_exam_date");
                    sub.setNextExamDate(nextExam != null ? nextExam.toString() : null);

                    list.add(sub);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Subject getById(int id) {
        String sql = "SELECT id, student_id, name, code, color, priority, description, created_at FROM subjects WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Subject sub = new Subject();
                    sub.setId(rs.getInt("id"));
                    sub.setStudentId(rs.getInt("student_id"));
                    sub.setName(rs.getString("name"));
                    sub.setCode(rs.getString("code"));
                    sub.setColor(rs.getString("color"));
                    sub.setPriority(rs.getString("priority"));
                    sub.setDescription(rs.getString("description"));
                    sub.setCreatedAt(rs.getTimestamp("created_at"));
                    return sub;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int create(Subject sub) {
        String sql = "INSERT INTO subjects (student_id, name, code, color, priority, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sub.getStudentId());
            ps.setString(2, sub.getName());
            ps.setString(3, sub.getCode());
            ps.setString(4, sub.getColor() != null ? sub.getColor() : "#4f46e5");
            ps.setString(5, sub.getPriority() != null ? sub.getPriority() : "MEDIUM");
            ps.setString(6, sub.getDescription());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Subject sub) {
        String sql = "UPDATE subjects SET name = ?, code = ?, color = ?, priority = ?, description = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sub.getName());
            ps.setString(2, sub.getCode());
            ps.setString(3, sub.getColor());
            ps.setString(4, sub.getPriority());
            ps.setString(5, sub.getDescription());
            ps.setInt(6, sub.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
