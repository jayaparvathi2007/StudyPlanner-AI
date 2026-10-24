package com.studyplanner.dao;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.model.AiRecommendation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AiRecommendationDAO {

    public int save(AiRecommendation r) {
        String sql = "INSERT INTO ai_recommendations (student_id, rec_type, content) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getStudentId());
            ps.setString(2, r.getRecType() != null ? r.getRecType() : "DAILY_STRATEGY");
            ps.setString(3, r.getContent());

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

    public AiRecommendation getLatest(int studentId, String recType) {
        String sql = "SELECT id, student_id, rec_type, content, created_at FROM ai_recommendations " +
                     "WHERE student_id = ? " + (recType != null ? "AND rec_type = ? " : "") +
                     "ORDER BY created_at DESC LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            if (recType != null) {
                ps.setString(2, recType);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AiRecommendation r = new AiRecommendation();
                    r.setId(rs.getInt("id"));
                    r.setStudentId(rs.getInt("student_id"));
                    r.setRecType(rs.getString("rec_type"));
                    r.setContent(rs.getString("content"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
