package com.studentmanagement.servlet;

import com.studentmanagement.exception.AppException;
import com.studentmanagement.service.AnalyticsService;
import com.studentmanagement.util.Json;
import jakarta.servlet.http.*;

public class DashboardServlet extends BaseServlet {
  private final AnalyticsService analytics;

  public DashboardServlet(AnalyticsService analytics) {
    this.analytics = analytics;
  }

  protected void handle(HttpServletRequest req, HttpServletResponse res) throws Exception {
    if (!req.getMethod().equals("GET")) throw new AppException(405, "Method not allowed.");
    String path = req.getServletPath();
    if (path.equals("/api/programs")) Json.send(res, 200, analytics.programs());
    else if (path.equals("/api/dashboard")) Json.send(res, 200, analytics.dashboard());
    else if (path.equals("/api/profile"))
      Json.send(res, 200, analytics.profile(req.getParameter("studentId")));
    else if (path.equals("/api/reports")) Json.send(res, 200, analytics.reports());
    else if (path.equals("/api/reports.csv")) {
      res.setContentType("text/csv;charset=UTF-8");
      res.setHeader("Content-Disposition", "attachment; filename=academic-report.csv");
      res.getWriter().write(analytics.csv());
    } else throw new AppException(404, "Endpoint not found.");
  }
}
