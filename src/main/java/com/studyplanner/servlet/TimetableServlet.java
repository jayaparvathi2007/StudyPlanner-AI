package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.dao.StudySessionDAO;
import com.studyplanner.dao.TopicDAO;
import com.studyplanner.model.StudySession;
import com.studyplanner.service.SchedulerService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "TimetableServlet", urlPatterns = {"/api/timetable", "/api/timetable/*"})
public class TimetableServlet extends BaseServlet {

    private final StudySessionDAO sessionDAO = new StudySessionDAO();
    private final SchedulerService schedulerService = new SchedulerService();
    private final TopicDAO topicDAO = new TopicDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String view = req.getParameter("view");
        String dateParam = req.getParameter("date");
        String startDateParam = req.getParameter("startDate");
        int studentId = getStudentId(req);

        try {
            if ("daily".equalsIgnoreCase(view)) {
                LocalDate date = (dateParam != null && !dateParam.trim().isEmpty())
                        ? LocalDate.parse(dateParam)
                        : LocalDate.now();
                List<StudySession> sessions = sessionDAO.getByDate(studentId, date);
                sendJson(resp, sessions);
            } else if ("weekly".equalsIgnoreCase(view)) {
                LocalDate start = (startDateParam != null && !startDateParam.trim().isEmpty())
                        ? LocalDate.parse(startDateParam)
                        : LocalDate.now();
                LocalDate end = start.plusDays(6);
                List<StudySession> sessions = sessionDAO.getByDateRange(studentId, start, end);
                sendJson(resp, sessions);
            } else {
                // Default: today to next 7 days
                LocalDate start = LocalDate.now().minusDays(1);
                LocalDate end = LocalDate.now().plusDays(7);
                List<StudySession> sessions = sessionDAO.getByDateRange(studentId, start, end);
                sendJson(resp, sessions);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid date format: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.contains("/generate")) {
                handleGenerateTimetable(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/reschedule")) {
                handleAdaptiveReschedule(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/complete")) {
                handleMarkCompleted(req, resp);
            } else if (pathInfo != null && pathInfo.contains("/miss")) {
                handleMarkMissed(req, resp);
            } else {
                // Create single manual session
                StudySession s = parseRequestBody(req, StudySession.class);
                if (s == null || s.getTopicId() <= 0 || s.getScheduledDate() == null) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Topic ID and scheduledDate required.");
                    return;
                }
                s.setStudentId(getStudentId(req));
                int newId = sessionDAO.create(s);
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("sessionId", newId);
                sendJson(resp, res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Timetable action failed: " + e.getMessage());
        }
    }

    private void handleGenerateTimetable(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);

        LocalDate startDate = LocalDate.now();
        if (body.has("startDate") && !body.get("startDate").getAsString().trim().isEmpty()) {
            startDate = LocalDate.parse(body.get("startDate").getAsString());
        }

        int daysAhead = body.has("daysAhead") ? body.get("daysAhead").getAsInt() : 7;
        double dailyHours = body.has("dailyHours") ? body.get("dailyHours").getAsDouble() : 3.0;
        boolean clearFuture = !body.has("clearFuture") || body.get("clearFuture").getAsBoolean();

        int studentId = getStudentId(req);
        List<StudySession> generated = schedulerService.generateTimetable(studentId, startDate, daysAhead, dailyHours, clearFuture);

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("count", generated.size());
        res.addProperty("message", String.format("Generated %d study sessions over %d days.", generated.size(), daysAhead));
        sendJson(resp, res);
    }

    private void handleAdaptiveReschedule(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int studentId = getStudentId(req);
        List<StudySession> rescheduled = schedulerService.rescheduleMissedSessions(studentId);

        JsonObject res = new JsonObject();
        res.addProperty("success", true);
        res.addProperty("rescheduledCount", rescheduled.size());
        res.addProperty("message", rescheduled.isEmpty()
                ? "No missed or overdue study sessions found. Your schedule is up to date!"
                : String.format("Successfully recovered and rescheduled %d missed study session(s) into your upcoming timetable.", rescheduled.size()));
        sendJson(resp, res);
    }

    private void handleMarkCompleted(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        if (!body.has("sessionId")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "sessionId is required.");
            return;
        }

        int sessionId = body.get("sessionId").getAsInt();
        int actualMinutes = body.has("actualDurationMinutes") ? body.get("actualDurationMinutes").getAsInt() : 60;
        String notes = body.has("notes") ? body.get("notes").getAsString() : "Completed study session.";

        boolean ok = sessionDAO.markCompleted(sessionId, actualMinutes, notes);
        if (ok) {
            // Optional: update topic status if requested
            if (body.has("topicId") && body.has("markTopicCompleted") && body.get("markTopicCompleted").getAsBoolean()) {
                int topicId = body.get("topicId").getAsInt();
                int conf = body.has("confidenceLevel") ? body.get("confidenceLevel").getAsInt() : 4;
                topicDAO.updateStatusAndConfidence(topicId, "COMPLETED", conf);
                topicDAO.setWeakTopic(topicId, false);
            }

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Session marked as completed. Study time recorded!");
            sendJson(resp, res);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Session not found or update failed.");
        }
    }

    private void handleMarkMissed(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = parseRequestBodyAsJsonObject(req);
        if (!body.has("sessionId")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "sessionId is required.");
            return;
        }

        int sessionId = body.get("sessionId").getAsInt();
        boolean ok = sessionDAO.markMissed(sessionId);
        if (ok) {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Session marked as missed. You can click 'Reschedule Unfinished' to adaptively reassign it.");
            sendJson(resp, res);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Session not found or update failed.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = extractIdFromPath(req);
        if (id <= 0) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid session ID required.");
            return;
        }

        boolean ok = sessionDAO.delete(id);
        if (ok) {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("deletedId", id);
            sendJson(resp, res);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Session not found.");
        }
    }
}
