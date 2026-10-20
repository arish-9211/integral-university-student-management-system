package com.studentmanagement;

import com.studentmanagement.database.*;
import com.studentmanagement.filter.AuthFilter;
import com.studentmanagement.repository.*;
import com.studentmanagement.service.*;
import com.studentmanagement.servlet.*;
import jakarta.servlet.DispatcherType;
import java.util.*;
import org.eclipse.jetty.ee10.servlet.*;
import org.eclipse.jetty.server.*;

public class Application {
  public static void main(String[] args) throws Exception {
    try {
      MongoDBConnection connection = new MongoDBConnection();
      var db = connection.getDatabase();
      var students = new StudentRepository(db);
      var courses = new CourseRepository(db);
      var users = new UserRepository(db);
      var enrollments = new EnrollmentRepository(db);
      var attendance = new AttendanceRepository(db);
      var results = new ResultRepository(db);
      if (Arrays.asList(args).contains("--seed")
          || "true".equalsIgnoreCase(System.getenv("SEED_DEMO")))
        DemoSeeder.seed(users, students, courses, enrollments, attendance, results);
      Object writeLock = new Object();
      Server server = new Server();
      ServerConnector connector = new ServerConnector(server);
      connector.setHost("127.0.0.1");
      connector.setPort(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));
      server.addConnector(connector);
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      context.getSessionHandler().getSessionCookieConfig().setHttpOnly(true);
      context.getSessionHandler().getSessionCookieConfig().setAttribute("SameSite", "Strict");
      context.getSessionHandler().setMaxInactiveInterval(1800);
      context.addFilter(AuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
      context.addServlet(new ServletHolder(new AuthServlet(new AuthService(users))), "/api/auth/*");
      context.addServlet(
          new ServletHolder(
              new StudentServlet(new StudentService(students, enrollments, writeLock))),
          "/api/students/*");
      context.addServlet(
          new ServletHolder(new CourseServlet(new CourseService(courses, enrollments, writeLock))),
          "/api/courses/*");
      context.addServlet(
          new ServletHolder(
              new EnrollmentServlet(
                  new EnrollmentService(
                      enrollments, students, courses, attendance, results, writeLock))),
          "/api/enrollments/*");
      context.addServlet(
          new ServletHolder(
              new AttendanceServlet(
                  new AttendanceService(attendance, students, courses, enrollments, writeLock))),
          "/api/attendance/*");
      context.addServlet(
          new ServletHolder(
              new ResultServlet(
                  new ResultService(results, students, courses, enrollments, writeLock))),
          "/api/results/*");
      var analytics = new AnalyticsService(students, courses, enrollments, attendance, results);
      for (String route :
          List.of(
              "/api/programs",
              "/api/dashboard",
              "/api/profile",
              "/api/reports",
              "/api/reports.csv"))
        context.addServlet(new ServletHolder(new DashboardServlet(analytics)), route);
      context.addServlet(new ServletHolder(new StaticServlet()), "/");
      server.setHandler(context);
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      server.stop();
                    } catch (Exception ignored) {
                    }
                    connection.close();
                  }));
      server.start();
      System.out.println("Student Management System: http://localhost:" + connector.getLocalPort());
      server.join();
    } catch (com.mongodb.MongoException e) {
      System.err.println(
          "Cannot connect to MongoDB. Start MongoDB and check MONGODB_URI. No server was started.");
      System.exit(1);
    }
  }
}
