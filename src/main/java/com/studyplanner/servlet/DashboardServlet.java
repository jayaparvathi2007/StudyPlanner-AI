package com.studyplanner.servlet;

import com.studyplanner.model.DashboardData;
import com.studyplanner.service.AnalyticsService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/api/dashboard"})
public class DashboardServlet extends BaseServlet {

    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int studentId = getStudentId(req);
            DashboardData data = analyticsService.getDashboardData(studentId);
            sendJson(resp, data);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load dashboard data: " + e.getMessage());
        }
    }
}
