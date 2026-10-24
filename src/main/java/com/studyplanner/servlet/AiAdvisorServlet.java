package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.config.AppConfig;
import com.studyplanner.dao.*;
import com.studyplanner.model.*;
import com.studyplanner.service.AiService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AiAdvisorServlet", urlPatterns = {"/api/ai", "/api/ai/*"})
public class AiAdvisorServlet extends BaseServlet {

    private final AiService aiService = new AiService();
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final StudySessionDAO sessionDAO = new StudySessionDAO();
    private final AiRecommendationDAO recDAO = new AiRecommendationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.contains("/config")) {
            JsonObject res = new JsonObject();
            res.addProperty("hasApiKey", AppConfig.hasApiKey());
            res.addProperty("model", AppConfig.getModel());
            res.addProperty("provider", "Google Gemini");
            sendJson(resp, res);
            return;
        }

        // Return latest recommendation by default
        int studentId = getStudentId(req);
        AiRecommendation latest = recDAO.getLatest(studentId, "DAILY_STRATEGY");
        if (latest != null) {
            sendJson(resp, latest);
        } else {
            JsonObject empty = new JsonObject();
            empty.addProperty("content", "Click 'Generate AI Strategic Plan' to analyze your exams and coursework.");
            sendJson(resp, empty);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.contains("/config")) {
                handleUpdateConfig(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/explain")) {
                handleExplainTopic(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/recommendations")) {
                handleGenerateRecommendations(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown AI endpoint: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "AI Advisor error: " + e.getMessage());
        }
    }

    private void handleUpdateConfig(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        if (body.has("apiKey")) {
            AppConfig.setApiKey(body.get("apiKey").getAsString());
        }
        if (body.has("model")) {
            AppConfig.setModel(body.get("model").getAsString());
        }

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("hasApiKey", AppConfig.hasApiKey());
        res.addProperty("model", AppConfig.getModel());
        res.addProperty("message", "AI Configuration updated successfully!");
        sendJson(resp, res);
    }

    private void handleGenerateRecommendations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int studentId = getStudentId(req);
        Student student = studentDAO.getStudent(studentId);
        if (student == null) {
            student = new Student(studentId, "Student", "student@university.edu", 3.5);
        }

        List<Subject> subjects = subjectDAO.getAll(studentId);
        List<Topic> topics = topicDAO.getAll(studentId);
        List<Exam> exams = examDAO.getUpcoming(studentId);
        List<QuizAttempt> quizzes = quizDAO.getRecentAttempts(studentId, 5);
        List<StudySession> missed = sessionDAO.getOverdueMissedSessions(studentId);

        String recommendations = aiService.generatePersonalizedRecommendations(
                student, subjects, topics, exams, quizzes, missed);

        // Save to database
        AiRecommendation rec = new AiRecommendation();
        rec.setStudentId(studentId);
        rec.setRecType("DAILY_STRATEGY");
        rec.setContent(recommendations);
        recDAO.save(rec);

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("content", recommendations);
        res.addProperty("hasApiKey", AppConfig.hasApiKey());
        sendJson(resp, res);
    }

    private void handleExplainTopic(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        if (!body.has("topicName")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "topicName is required.");
            return;
        }

        String topicName = body.get("topicName").getAsString();
        String subjectName = body.has("subjectName") ? body.get("subjectName").getAsString() : "Computer Science";
        String difficulty = body.has("difficulty") ? body.get("difficulty").getAsString() : "MEDIUM";
        String style = body.has("style") ? body.get("style").getAsString() : "Comprehensive";

        String explanation = aiService.explainTopic(topicName, subjectName, difficulty, style);

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("topicName", topicName);
        res.addProperty("content", explanation);
        res.addProperty("hasApiKey", AppConfig.hasApiKey());
        sendJson(resp, res);
    }
}
