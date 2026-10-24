package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.studyplanner.util.JsonUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;

/**
 * Base Servlet providing common utilities: JSON parsing, error handling, and CORS.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final int DEFAULT_STUDENT_ID = 1;

    protected int getStudentId(HttpServletRequest req) {
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("studentId") != null) {
            Object id = session.getAttribute("studentId");
            if (id instanceof Integer) {
                return (Integer) id;
            }
        }
        return DEFAULT_STUDENT_ID;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    protected void sendJson(HttpServletResponse resp, Object data) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = resp.getWriter()) {
            out.print(JsonUtils.toJson(data));
            out.flush();
        }
    }

    protected void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(statusCode);
        JsonObject err = new JsonObject();
        err.addProperty("success", false);
        err.addProperty("error", message);
        err.addProperty("statusCode", statusCode);
        try (PrintWriter out = resp.getWriter()) {
            out.print(JsonUtils.toJson(err));
            out.flush();
        }
    }

    protected String readRequestBody(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    protected <T> T parseRequestBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        String body = readRequestBody(req);
        if (body.trim().isEmpty()) {
            return null;
        }
        return JsonUtils.fromJson(body, clazz);
    }

    protected <T> T parseRequestBody(HttpServletRequest req, Type type) throws IOException {
        String body = readRequestBody(req);
        if (body.trim().isEmpty()) {
            return null;
        }
        return JsonUtils.fromJson(body, type);
    }

    protected JsonObject parseRequestBodyAsJsonObject(HttpServletRequest req) throws IOException {
        String body = readRequestBody(req);
        if (body.trim().isEmpty()) {
            return new JsonObject();
        }
        return JsonParser.parseString(body).getAsJsonObject();
    }

    protected int extractIdFromPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] parts = pathInfo.split("/");
            if (parts.length > 1) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }
}
