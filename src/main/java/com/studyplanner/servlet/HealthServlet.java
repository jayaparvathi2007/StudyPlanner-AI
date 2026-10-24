package com.studyplanner.servlet;

import com.google.gson.JsonObject;
import com.studyplanner.config.AppConfig;
import com.studyplanner.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "HealthServlet", urlPatterns = {"/api/health"})
public class HealthServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean dbOk = DatabaseConnection.testConnection();
        boolean aiOk = AppConfig.hasApiKey();

        JsonObject res = new JsonObject();
        res.addProperty("status", dbOk ? "UP" : "DEGRADED");
        res.addProperty("database", dbOk ? "CONNECTED" : "DISCONNECTED");
        res.addProperty("aiService", aiOk ? "CONFIGURED (Live Gemini)" : "UNCONFIGURED (Algorithmic Fallback Active)");
        res.addProperty("aiModel", AppConfig.getModel());
        res.addProperty("server", "Apache Tomcat Core Java Servlet Container");

        sendJson(resp, res);
    }
}
