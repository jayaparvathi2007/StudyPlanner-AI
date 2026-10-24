package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.config.AppConfig;
import com.studyplanner.dao.QuizDAO;
import com.studyplanner.dao.TopicDAO;
import com.studyplanner.model.QuizAttempt;
import com.studyplanner.model.QuizQuestion;
import com.studyplanner.model.Topic;
import com.studyplanner.service.AiService;
import com.studyplanner.service.SchedulerService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "QuizServlet", urlPatterns = {"/api/quiz", "/api/quiz/*"})
public class QuizServlet extends BaseServlet {

    private final AiService aiService = new AiService();
    private final QuizDAO quizDAO = new QuizDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final SchedulerService schedulerService = new SchedulerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int studentId = getStudentId(req);
        if (pathInfo != null && pathInfo.contains("/history")) {
            List<QuizAttempt> history = quizDAO.getRecentAttempts(studentId, 20);
            sendJson(resp, history);
        } else {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Supported GET endpoint: /api/quiz/history");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.contains("/generate")) {
                handleGenerateQuiz(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/submit")) {
                handleSubmitQuiz(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown quiz endpoint: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Quiz error: " + e.getMessage());
        }
    }

    private void handleGenerateQuiz(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        int topicId = body.has("topicId") ? body.get("topicId").getAsInt() : -1;
        String topicName = body.has("topicName") ? body.get("topicName").getAsString() : "";
        String subjectName = body.has("subjectName") ? body.get("subjectName").getAsString() : "General";
        String difficulty = body.has("difficulty") ? body.get("difficulty").getAsString() : "MEDIUM";

        if (topicId > 0 && topicName.isEmpty()) {
            Topic t = topicDAO.getById(topicId);
            if (t != null) {
                topicName = t.getName();
                if (t.getSubjectName() != null) subjectName = t.getSubjectName();
                if (t.getDifficulty() != null) difficulty = t.getDifficulty();
            }
        }

        if (topicName.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "topicName or valid topicId is required.");
            return;
        }

        List<QuizQuestion> questions = aiService.generatePracticeQuiz(topicName, subjectName, difficulty);

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("topicId", topicId);
        res.addProperty("topicName", topicName);
        res.addProperty("subjectName", subjectName);
        res.addProperty("hasApiKey", AppConfig.hasApiKey());
        res.add("questions", com.studyplanner.util.JsonUtils.getGson().toJsonTree(questions));

        sendJson(resp, res);
    }

    private void handleSubmitQuiz(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        if (!body.has("topicId") || !body.has("score") || !body.has("totalQuestions") || !body.has("correctAnswers")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "topicId, score, totalQuestions, and correctAnswers are required.");
            return;
        }

        int topicId = body.get("topicId").getAsInt();
        double score = body.get("score").getAsDouble();
        int total = body.get("totalQuestions").getAsInt();
        int correct = body.get("correctAnswers").getAsInt();
        String feedback = body.has("feedback") ? body.get("feedback").getAsString() : "";

        if (feedback.isEmpty()) {
            if (score >= 80.0) {
                feedback = "Outstanding mastery demonstrated! Topic marked as completed with high confidence.";
            } else if (score >= 60.0) {
                feedback = "Satisfactory comprehension, but review edge cases and key definitions.";
            } else {
                feedback = "Critical concept gaps detected (< 60%). Flagged as Weak Topic; remedial session automatically scheduled.";
            }
        }

        int studentId = getStudentId(req);
        QuizAttempt attempt = new QuizAttempt();
        attempt.setStudentId(studentId);
        attempt.setTopicId(topicId);
        attempt.setScore(score);
        attempt.setTotalQuestions(total);
        attempt.setCorrectAnswers(correct);
        attempt.setFeedback(feedback);

        int attemptId = quizDAO.saveAttempt(attempt);

        // Adaptive Trigger: Reschedule remedial session if score < 60%
        boolean remedialScheduled = schedulerService.handleQuizResult(studentId, topicId, score);

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("attemptId", attemptId);
        res.addProperty("score", score);
        res.addProperty("passed", score >= 60.0);
        res.addProperty("isWeak", score < 60.0);
        res.addProperty("remedialScheduled", remedialScheduled);
        res.addProperty("feedback", feedback);
        res.addProperty("message", remedialScheduled
                ? "Adaptive Alert: Low quiz score triggered an automatic remedial study session on your schedule."
                : "Quiz score recorded successfully!");

        sendJson(resp, res);
    }
}
