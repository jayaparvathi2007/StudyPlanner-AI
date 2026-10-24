package com.studyplanner.dao;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.model.QuizAttempt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {

    public int saveAttempt(QuizAttempt q) {
        String sql = "INSERT INTO quiz_attempts (student_id, topic_id, score, total_questions, correct_answers, feedback) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getStudentId());
            ps.setInt(2, q.getTopicId());
            ps.setDouble(3, q.getScore());
            ps.setInt(4, q.getTotalQuestions());
            ps.setInt(5, q.getCorrectAnswers());
            ps.setString(6, q.getFeedback());

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

    public List<QuizAttempt> getRecentAttempts(int studentId, int limit) {
        List<QuizAttempt> list = new ArrayList<>();
        String sql = "SELECT qa.id, qa.student_id, qa.topic_id, qa.score, qa.total_questions, qa.correct_answers, " +
                     "qa.feedback, qa.attempted_at, " +
                     "t.name AS topic_name, s.name AS subject_name " +
                     "FROM quiz_attempts qa " +
                     "JOIN topics t ON qa.topic_id = t.id " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE qa.student_id = ? " +
                     "ORDER BY qa.attempted_at DESC LIMIT ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, limit > 0 ? limit : 10);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizAttempt q = new QuizAttempt();
                    q.setId(rs.getInt("id"));
                    q.setStudentId(rs.getInt("student_id"));
                    q.setTopicId(rs.getInt("topic_id"));
                    q.setTopicName(rs.getString("topic_name"));
                    q.setSubjectName(rs.getString("subject_name"));
                    q.setScore(rs.getDouble("score"));
                    q.setTotalQuestions(rs.getInt("total_questions"));
                    q.setCorrectAnswers(rs.getInt("correct_answers"));
                    q.setFeedback(rs.getString("feedback"));
                    q.setAttemptedAt(rs.getTimestamp("attempted_at"));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getOverallAverageScore(int studentId) {
        String sql = "SELECT AVG(score) FROM quiz_attempts WHERE student_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.round(rs.getDouble(1) * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTotalQuizzesTaken(int studentId) {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE student_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
