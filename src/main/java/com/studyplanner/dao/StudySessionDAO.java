package com.studyplanner.dao;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.model.StudySession;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudySessionDAO {

    public List<StudySession> getByDate(int studentId, LocalDate date) {
        return getByDateRange(studentId, date, date);
    }

    public List<StudySession> getTodaySessions(int studentId) {
        return getByDate(studentId, LocalDate.now());
    }

    public List<StudySession> getByDateRange(int studentId, LocalDate start, LocalDate end) {
        List<StudySession> list = new ArrayList<>();
        String sql = "SELECT ss.id, ss.student_id, ss.topic_id, ss.scheduled_date, ss.start_time, " +
                     "ss.duration_minutes, ss.status, ss.actual_duration_minutes, ss.is_remedial, ss.notes, " +
                     "ss.completed_at, ss.created_at, " +
                     "t.name AS topic_name, " +
                     "s.id AS subject_id, s.name AS subject_name, s.color AS subject_color " +
                     "FROM study_sessions ss " +
                     "JOIN topics t ON ss.topic_id = t.id " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE ss.student_id = ? AND ss.scheduled_date BETWEEN ? AND ? " +
                     "ORDER BY ss.scheduled_date ASC, ss.start_time ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<StudySession> getOverdueMissedSessions(int studentId) {
        List<StudySession> list = new ArrayList<>();
        String sql = "SELECT ss.id, ss.student_id, ss.topic_id, ss.scheduled_date, ss.start_time, " +
                     "ss.duration_minutes, ss.status, ss.actual_duration_minutes, ss.is_remedial, ss.notes, " +
                     "ss.completed_at, ss.created_at, " +
                     "t.name AS topic_name, " +
                     "s.id AS subject_id, s.name AS subject_name, s.color AS subject_color " +
                     "FROM study_sessions ss " +
                     "JOIN topics t ON ss.topic_id = t.id " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE ss.student_id = ? " +
                     "  AND (ss.status = 'MISSED' OR (ss.status = 'SCHEDULED' AND ss.scheduled_date < CURRENT_DATE)) " +
                     "ORDER BY ss.scheduled_date ASC, ss.start_time ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int create(StudySession s) {
        String sql = "INSERT INTO study_sessions (student_id, topic_id, scheduled_date, start_time, duration_minutes, " +
                     "status, actual_duration_minutes, is_remedial, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getStudentId());
            ps.setInt(2, s.getTopicId());
            ps.setDate(3, s.getScheduledDate());
            ps.setTime(4, s.getStartTime() != null ? s.getStartTime() : Time.valueOf("09:00:00"));
            ps.setInt(5, s.getDurationMinutes() > 0 ? s.getDurationMinutes() : 60);
            ps.setString(6, s.getStatus() != null ? s.getStatus() : "SCHEDULED");
            ps.setInt(7, s.getActualDurationMinutes());
            ps.setBoolean(8, s.isRemedial());
            ps.setString(9, s.getNotes());

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

    public boolean bulkCreate(List<StudySession> sessions) {
        if (sessions == null || sessions.isEmpty()) return true;
        String sql = "INSERT INTO study_sessions (student_id, topic_id, scheduled_date, start_time, duration_minutes, " +
                     "status, actual_duration_minutes, is_remedial, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            for (StudySession s : sessions) {
                ps.setInt(1, s.getStudentId());
                ps.setInt(2, s.getTopicId());
                ps.setDate(3, s.getScheduledDate());
                ps.setTime(4, s.getStartTime() != null ? s.getStartTime() : Time.valueOf("09:00:00"));
                ps.setInt(5, s.getDurationMinutes() > 0 ? s.getDurationMinutes() : 60);
                ps.setString(6, s.getStatus() != null ? s.getStatus() : "SCHEDULED");
                ps.setInt(7, s.getActualDurationMinutes());
                ps.setBoolean(8, s.isRemedial());
                ps.setString(9, s.getNotes());
                ps.addBatch();
            }
            ps.executeBatch();
            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markCompleted(int id, int actualMinutes, String notes) {
        String sql = "UPDATE study_sessions SET status = 'COMPLETED', actual_duration_minutes = ?, " +
                     "notes = COALESCE(?, notes), completed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, actualMinutes > 0 ? actualMinutes : 60);
            ps.setString(2, notes);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markMissed(int id) {
        String sql = "UPDATE study_sessions SET status = 'MISSED' WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE study_sessions SET status = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearFutureScheduledSessions(int studentId, LocalDate fromDate) {
        String sql = "DELETE FROM study_sessions WHERE student_id = ? AND scheduled_date >= ? AND status = 'SCHEDULED'";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalCompletedStudyMinutes(int studentId) {
        String sql = "SELECT COALESCE(SUM(CASE WHEN actual_duration_minutes > 0 THEN actual_duration_minutes ELSE duration_minutes END), 0) " +
                     "FROM study_sessions WHERE student_id = ? AND status = 'COMPLETED'";
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

    public int getMissedCount(int studentId) {
        String sql = "SELECT COUNT(*) FROM study_sessions WHERE student_id = ? AND (status = 'MISSED' OR (status = 'SCHEDULED' AND scheduled_date < CURRENT_DATE))";
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

    public boolean delete(int id) {
        String sql = "DELETE FROM study_sessions WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private StudySession mapResultSetToSession(ResultSet rs) throws SQLException {
        StudySession s = new StudySession();
        s.setId(rs.getInt("id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setTopicId(rs.getInt("topic_id"));
        s.setTopicName(rs.getString("topic_name"));
        s.setSubjectId(rs.getInt("subject_id"));
        s.setSubjectName(rs.getString("subject_name"));
        s.setSubjectColor(rs.getString("subject_color"));
        s.setScheduledDate(rs.getDate("scheduled_date"));
        s.setStartTime(rs.getTime("start_time"));
        s.setDurationMinutes(rs.getInt("duration_minutes"));
        s.setStatus(rs.getString("status"));
        s.setActualDurationMinutes(rs.getInt("actual_duration_minutes"));
        s.setRemedial(rs.getBoolean("is_remedial"));
        s.setNotes(rs.getString("notes"));
        s.setCompletedAt(rs.getTimestamp("completed_at"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
