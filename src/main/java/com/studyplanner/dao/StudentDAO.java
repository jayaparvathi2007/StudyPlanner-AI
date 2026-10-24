package com.studyplanner.dao;

import com.studyplanner.config.DatabaseConnection;
import com.studyplanner.model.Student;
import com.studyplanner.util.PasswordUtils;

import java.sql.*;

public class StudentDAO {

    public Student authenticate(String email, String password) {
        if (email == null || password == null) return null;

        String sql = "SELECT id, name, email, password_hash, salt, target_daily_hours, created_at FROM students WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    String salt = rs.getString("salt");

                    // If salt or hash missing (legacy pre-seeded), allow migration with default password "password123"
                    if (salt == null || hash == null) {
                        if ("password123".equals(password) || "admin123".equals(password)) {
                            int id = rs.getInt("id");
                            updatePassword(id, password);
                            return mapStudent(rs);
                        }
                        return null;
                    }

                    if (PasswordUtils.verifyPassword(password, salt, hash)) {
                        return mapStudent(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int register(String name, String email, String password, double targetDailyHours) throws SQLException {
        if (findByEmail(email) != null) {
            throw new SQLException("Email is already registered.");
        }

        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(password, salt);
        if (targetDailyHours <= 0) targetDailyHours = 3.0;

        String sql = "INSERT INTO students (name, email, password_hash, salt, target_daily_hours) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setDouble(5, targetDailyHours);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public Student findByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT id, name, email, target_daily_hours, created_at FROM students WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Student getStudent(int id) {
        String sql = "SELECT id, name, email, target_daily_hours, created_at FROM students WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePassword(int id, String newPassword) {
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(newPassword, salt);
        String sql = "UPDATE students SET password_hash = ?, salt = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTargetHours(int id, double hours) {
        String sql = "UPDATE students SET target_daily_hours = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, hours);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setTargetDailyHours(rs.getDouble("target_daily_hours"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
