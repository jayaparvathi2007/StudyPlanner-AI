package com.studyplanner.dao;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.model.Topic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TopicDAO {

    public List<Topic> getAll(int studentId) {
        List<Topic> list = new ArrayList<>();
        String sql = "SELECT t.id, t.subject_id, t.name, t.difficulty, t.estimated_hours, t.status, " +
                     "t.confidence_level, t.is_weak, t.created_at, s.name AS subject_name, s.color AS subject_color " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE s.student_id = ? " +
                     "ORDER BY s.name ASC, t.id ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTopic(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Topic> getBySubject(int subjectId) {
        List<Topic> list = new ArrayList<>();
        String sql = "SELECT t.id, t.subject_id, t.name, t.difficulty, t.estimated_hours, t.status, " +
                     "t.confidence_level, t.is_weak, t.created_at, s.name AS subject_name, s.color AS subject_color " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE t.subject_id = ? " +
                     "ORDER BY t.id ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTopic(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Topic getById(int id) {
        String sql = "SELECT t.id, t.subject_id, t.name, t.difficulty, t.estimated_hours, t.status, " +
                     "t.confidence_level, t.is_weak, t.created_at, s.name AS subject_name, s.color AS subject_color " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE t.id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTopic(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Topic> getWeakTopics(int studentId) {
        List<Topic> list = new ArrayList<>();
        String sql = "SELECT t.id, t.subject_id, t.name, t.difficulty, t.estimated_hours, t.status, " +
                     "t.confidence_level, t.is_weak, t.created_at, s.name AS subject_name, s.color AS subject_color " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE s.student_id = ? AND (t.is_weak = TRUE OR t.confidence_level <= 2) " +
                     "ORDER BY t.confidence_level ASC, t.id ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTopic(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int create(Topic t) {
        String sql = "INSERT INTO topics (subject_id, name, difficulty, estimated_hours, status, confidence_level, is_weak) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getSubjectId());
            ps.setString(2, t.getName());
            ps.setString(3, t.getDifficulty() != null ? t.getDifficulty() : "MEDIUM");
            ps.setDouble(4, t.getEstimatedHours() > 0 ? t.getEstimatedHours() : 2.0);
            ps.setString(5, t.getStatus() != null ? t.getStatus() : "NOT_STARTED");
            ps.setInt(6, t.getConfidenceLevel() > 0 ? t.getConfidenceLevel() : 3);
            ps.setBoolean(7, t.isWeak());

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

    public boolean update(Topic t) {
        String sql = "UPDATE topics SET name = ?, difficulty = ?, estimated_hours = ?, status = ?, confidence_level = ?, is_weak = ? " +
                     "WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getDifficulty());
            ps.setDouble(3, t.getEstimatedHours());
            ps.setString(4, t.getStatus());
            ps.setInt(5, t.getConfidenceLevel());
            ps.setBoolean(6, t.isWeak());
            ps.setInt(7, t.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatusAndConfidence(int id, String status, int confidence) {
        String sql = "UPDATE topics SET status = ?, confidence_level = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, confidence);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setWeakTopic(int id, boolean isWeak) {
        String sql = "UPDATE topics SET is_weak = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, isWeak);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM topics WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Topic mapResultSetToTopic(ResultSet rs) throws SQLException {
        Topic t = new Topic();
        t.setId(rs.getInt("id"));
        t.setSubjectId(rs.getInt("subject_id"));
        t.setName(rs.getString("name"));
        t.setDifficulty(rs.getString("difficulty"));
        t.setEstimatedHours(rs.getDouble("estimated_hours"));
        t.setStatus(rs.getString("status"));
        t.setConfidenceLevel(rs.getInt("confidence_level"));
        t.setWeak(rs.getBoolean("is_weak"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            t.setSubjectName(rs.getString("subject_name"));
            t.setSubjectColor(rs.getString("subject_color"));
        } catch (SQLException ignored) {}
        return t;
    }
}
