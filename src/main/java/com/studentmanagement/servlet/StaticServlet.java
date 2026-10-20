package com.studentmanagement.servlet;

import com.studentmanagement.util.Json;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;

public class StaticServlet extends HttpServlet {
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String path = req.getRequestURI();
    if (path.equals("/")) {
      res.sendRedirect("/app.html");
      return;
    }
    if (path.startsWith("/api/")) {
      Json.send(res, 404, Map.of("error", "Endpoint not found."));
      return;
    }
    if (!path.matches("/[a-zA-Z0-9/_.-]+") || path.contains("..")) {
      res.setStatus(404);
      path = "/errors/404.html";
    }
    InputStream stream = getClass().getResourceAsStream("/web" + path);
    if (stream == null) {
      res.setStatus(404);
      path = "/errors/404.html";
      stream = getClass().getResourceAsStream("/web" + path);
    }
    String mime =
        path.endsWith(".css")
            ? "text/css"
            : path.endsWith(".js")
                ? "text/javascript"
                : path.endsWith(".svg") ? "image/svg+xml" : "text/html";
    res.setContentType(mime + ";charset=UTF-8");
    if (stream != null) {
      try (var input = stream) {
        input.transferTo(res.getOutputStream());
      }
    }
  }
}
