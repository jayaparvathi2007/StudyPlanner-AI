package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.dao.ExamDAO;
import com.studyplanner.model.Exam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ExamServlet", urlPatterns = {"/api/exams", "/api/exams/*"})
public class ExamServlet extends BaseServlet {

    private final ExamDAO examDAO = new ExamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = extractIdFromPath(req);
        if (id > 0) {
            Exam e = examDAO.getById(id);
            if (e != null) {
                sendJson(resp, e);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Exam not found with ID: " + id);
            }
            return;
        }

        int studentId = getStudentId(req);
        String upcomingOnly = req.getParameter("upcoming");
        List<Exam> list = "true".equalsIgnoreCase(upcomingOnly)
                ? examDAO.getUpcoming(studentId)
                : examDAO.getAll(studentId);

        sendJson(resp, list);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Exam exam = parseRequestBody(req, Exam.class);
            if (exam == null || exam.getTitle() == null || exam.getTitle().trim().isEmpty() || exam.getExamDate() == null || exam.getSubjectId() <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Exam title, valid date, and subjectId are required.");
                return;
            }

            if (exam.getTargetScore() <= 0) exam.setTargetScore(90);
            if (exam.getPriority() == null) exam.setPriority("HIGH");

            int newId = examDAO.create(exam);
            if (newId > 0) {
                Exam created = examDAO.getById(newId);
                sendJson(resp, created);
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create exam record.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating exam: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = extractIdFromPath(req);
            Exam exam = parseRequestBody(req, Exam.class);
            if (exam == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
                return;
            }
            if (id > 0) {
                exam.setId(id);
            }
            if (exam.getId() <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid exam ID required for update.");
                return;
            }

            boolean ok = examDAO.update(exam);
            if (ok) {
                Exam updated = examDAO.getById(exam.getId());
                sendJson(resp, updated);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Exam not found or update failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating exam: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = extractIdFromPath(req);
        if (id <= 0) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid exam ID required in URL path.");
            return;
        }

        boolean ok = examDAO.delete(id);
        if (ok) {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("deletedId", id);
            sendJson(resp, res);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Exam could not be deleted or does not exist.");
        }
    }
}
