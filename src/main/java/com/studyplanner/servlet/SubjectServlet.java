package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.dao.SubjectDAO;
import com.studyplanner.model.Subject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SubjectServlet", urlPatterns = {"/api/subjects", "/api/subjects/*"})
public class SubjectServlet extends BaseServlet {

    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = extractIdFromPath(req);
        if (id > 0) {
            Subject s = subjectDAO.getById(id);
            if (s != null) {
                sendJson(resp, s);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found with ID: " + id);
            }
        } else {
            int studentId = getStudentId(req);
            List<Subject> list = subjectDAO.getAll(studentId);
            sendJson(resp, list);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Subject subject = parseRequestBody(req, Subject.class);
            if (subject == null || subject.getName() == null || subject.getName().trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Subject name is required.");
                return;
            }

            int studentId = getStudentId(req);
            subject.setStudentId(studentId);
            if (subject.getColor() == null || subject.getColor().trim().isEmpty()) {
                subject.setColor("#4f46e5");
            }
            if (subject.getCode() == null || subject.getCode().trim().isEmpty()) {
                subject.setCode("SUB" + (int)(Math.random() * 900 + 100));
            }

            int newId = subjectDAO.create(subject);
            if (newId > 0) {
                Subject created = subjectDAO.getById(newId);
                sendJson(resp, created);
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create subject in database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating subject: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = extractIdFromPath(req);
            Subject subject = parseRequestBody(req, Subject.class);
            if (subject == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
                return;
            }
            if (id > 0) {
                subject.setId(id);
            }
            if (subject.getId() <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid subject ID required for update.");
                return;
            }

            boolean ok = subjectDAO.update(subject);
            if (ok) {
                Subject updated = subjectDAO.getById(subject.getId());
                sendJson(resp, updated);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or update failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating subject: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = extractIdFromPath(req);
        if (id <= 0) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid subject ID required in URL path.");
            return;
        }

        boolean ok = subjectDAO.delete(id);
        if (ok) {
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("deletedId", id);
            sendJson(resp, res);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject could not be deleted or does not exist.");
        }
    }
}
