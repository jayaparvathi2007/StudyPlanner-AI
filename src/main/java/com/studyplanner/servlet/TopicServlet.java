package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.dao.TopicDAO;
import com.studyplanner.model.Topic;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "TopicServlet", urlPatterns = {"/api/topics", "/api/topics/*"})
public class TopicServlet extends BaseServlet {

    private final TopicDAO topicDAO = new TopicDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = extractIdFromPath(req);
        if (id > 0) {
            Topic t = topicDAO.getById(id);
            if (t != null) {
                sendJson(resp, t);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Topic not found with ID: " + id);
            }
            return;
        }

        String subjectIdParam = req.getParameter("subjectId");
        String weakParam = req.getParameter("weak");
        int studentId = getStudentId(req);

        if ("true".equalsIgnoreCase(weakParam)) {
            List<Topic> weakList = topicDAO.getWeakTopics(studentId);
            sendJson(resp, weakList);
        } else if (subjectIdParam != null && !subjectIdParam.trim().isEmpty()) {
            try {
                int subjectId = Integer.parseInt(subjectIdParam);
                List<Topic> list = topicDAO.getBySubject(subjectId);
                sendJson(resp, list);
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid subjectId parameter.");
            }
        } else {
            List<Topic> list = topicDAO.getAll(studentId);
            sendJson(resp, list);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Topic topic = parseRequestBody(req, Topic.class);
            if (topic == null || topic.getName() == null || topic.getName().trim().isEmpty() || topic.getSubjectId() <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Topic name and valid subjectId are required.");
                return;
            }

            if (topic.getDifficulty() == null) topic.setDifficulty("MEDIUM");
            if (topic.getEstimatedHours() <= 0) topic.setEstimatedHours(2.0);
            if (topic.getStatus() == null) topic.setStatus("NOT_STARTED");
            if (topic.getConfidenceLevel() <= 0) topic.setConfidenceLevel(3);

            int newId = topicDAO.create(topic);
            if (newId > 0) {
                Topic created = topicDAO.getById(newId);
                sendJson(resp, created);
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create topic in database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating topic: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = extractIdFromPath(req);
            String pathInfo = req.getPathInfo();

            // Quick update for status or confidence: /api/topics/{id}/progress
            if (pathInfo != null && pathInfo.contains("/progress") && id > 0) {
                JsonObject json = parseRequestBodyAsJsonObject(req);
                String status = json.has("status") ? json.get("status").getAsString() : "IN_PROGRESS";
                int conf = json.has("confidenceLevel") ? json.get("confidenceLevel").getAsInt() : 3;
                boolean ok = topicDAO.updateStatusAndConfidence(id, status, conf);
                if (ok) {
                    sendJson(resp, topicDAO.getById(id));
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to update topic progress.");
                }
                return;
            }

            Topic topic = parseRequestBody(req, Topic.class);
            if (topic == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
                return;
            }
            if (id > 0) {
                topic.setId(id);
            }
            if (topic.getId() <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid topic ID required for update.");
                return;
            }

            boolean ok = topicDAO.update(topic);
            if (ok) {
                Topic updated = topicDAO.getById(topic.getId());
                sendJson(resp, updated);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Topic not found or update failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating topic: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = extractIdFromPath(req);
        if (id <= 0) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid topic ID required in URL path.");
            return;
        }

        boolean ok = topicDAO.delete(id);
        if (ok) {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("deletedId", id);
            sendJson(resp, res);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Topic could not be deleted or does not exist.");
        }
    }
}
