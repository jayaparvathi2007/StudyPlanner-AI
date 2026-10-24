package com.studyplanner.servlet;

import com.studyplanner.service.AnalyticsService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "AnalyticsServlet", urlPatterns = {"/api/analytics"})
public class AnalyticsServlet extends BaseServlet {

    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int studentId = getStudentId(req);
            Map<String, Object> data = analyticsService.getDetailedAnalytics(studentId);
            sendJson(resp, data);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load analytics: " + e.getMessage());
        }
    }
}
