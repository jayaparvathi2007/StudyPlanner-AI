package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.dao.StudentDAO;
import com.studyplanner.dao.SubjectDAO;
import com.studyplanner.dao.TopicDAO;
import com.studyplanner.model.Student;
import com.studyplanner.model.Subject;
import com.studyplanner.model.Topic;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth", "/api/auth/*"})
public class AuthServlet extends BaseServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains("/me")) {
            handleGetMe(req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Supported GET endpoint: /api/auth/me");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.contains("/login")) {
                handleLogin(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/register")) {
                handleRegister(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/logout")) {
                handleLogout(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown auth endpoint: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + e.getMessage());
        }
    }

    private void handleGetMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("studentId") != null) {
            int studentId = (Integer) session.getAttribute("studentId");
            Student s = studentDAO.getStudent(studentId);
            if (s != null) {
                JsonObject res = new JsonObject();
                res.addProperty("authenticated", true);
                res.add("student", com.studyplanner.util.JsonUtils.getGson().toJsonTree(s));
                sendJson(resp, res);
                return;
            }
        }

        JsonObject unauth = new JsonObject();
        unauth.addProperty("authenticated", false);
        sendJson(resp, unauth);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        if (!body.has("email") || !body.has("password")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
            return;
        }

        String email = body.get("email").getAsString();
        String password = body.get("password").getAsString();

        Student student = studentDAO.authenticate(email, password);
        if (student != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("studentId", student.getId());
            session.setAttribute("studentName", student.getName());

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Welcome back, " + student.getName() + "!");
            res.add("student", com.studyplanner.util.JsonUtils.getGson().toJsonTree(student));
            sendJson(resp, res);
        } else {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        if (!body.has("name") || !body.has("email") || !body.has("password")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name, email, and password are required.");
            return;
        }

        String name = body.get("name").getAsString().trim();
        String email = body.get("email").getAsString().trim().toLowerCase();
        String password = body.get("password").getAsString();
        double hours = body.has("targetDailyHours") ? body.get("targetDailyHours").getAsDouble() : 3.0;

        if (name.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name cannot be empty.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters.");
            return;
        }

        try {
            int newId = studentDAO.register(name, email, password, hours);
            if (newId > 0) {
                // Initialize a starter sample subject for the new student so their dashboard is lively!
                Subject starter = new Subject();
                starter.setStudentId(newId);
                starter.setName("General Computer Science & Engineering");
                starter.setCode("CS101");
                starter.setColor("#4f46e5");
                starter.setPriority("HIGH");
                starter.setDescription("Core foundational topics & algorithms.");
                int subId = subjectDAO.create(starter);
                if (subId > 0) {
                    Topic t1 = new Topic(0, subId, "Algorithm Complexity & Big-O", "EASY", 2.0, "NOT_STARTED", 3, false);
                    Topic t2 = new Topic(0, subId, "Dynamic Programming Basics", "HARD", 4.0, "NOT_STARTED", 2, true);
                    topicDAO.create(t1);
                    topicDAO.create(t2);
                }

                // Log user in automatically upon successful registration
                HttpSession session = req.getSession(true);
                session.setAttribute("studentId", newId);
                session.setAttribute("studentName", name);

                Student student = studentDAO.getStudent(newId);
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("message", "Registration successful! Welcome to StudyPlanner AI.");
                res.add("student", com.studyplanner.util.JsonUtils.getGson().toJsonTree(student));
                sendJson(resp, res);
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("already registered")) {
                sendError(resp, HttpServletResponse.SC_CONFLICT, "This email is already registered. Please sign in.");
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration error: " + e.getMessage());
            }
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("message", "Logged out successfully.");
        sendJson(resp, res);
    }
}
