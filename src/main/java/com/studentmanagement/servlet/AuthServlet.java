package com.studentmanagement.servlet;

import com.studentmanagement.exception.AppException;
import com.studentmanagement.service.AuthService;
import com.studentmanagement.util.Json;
import com.studentmanagement.validation.Validator;
import jakarta.servlet.http.*;
import java.util.*;

public class AuthServlet extends BaseServlet {
  private final AuthService auth;

  public AuthServlet(AuthService auth) {
    this.auth = auth;
  }

  protected void handle(HttpServletRequest req, HttpServletResponse res) throws Exception {
    String path = req.getPathInfo();
    if ("/login".equals(path) && req.getMethod().equals("POST")) {
      var body = Json.body(req);
      String email = Validator.text(body, "email", 120),
          password = Validator.text(body, "password", 72);
      var user = auth.login(email, password, req.getRemoteAddr());
      if (req.getSession(false) != null) req.getSession(false).invalidate();
      var session = req.getSession(true);
      session.setAttribute(
          "user", Map.of("name", user.name(), "email", user.email(), "role", "Administrator"));
      session.setAttribute("csrf", UUID.randomUUID().toString());
      session.setMaxInactiveInterval(1800);
      Json.send(
          res,
          200,
          Map.of("user", session.getAttribute("user"), "csrf", session.getAttribute("csrf")));
      return;
    }
    if ("/me".equals(path) && req.getMethod().equals("GET")) {
      var s = req.getSession(false);
      Json.send(res, 200, Map.of("user", s.getAttribute("user"), "csrf", s.getAttribute("csrf")));
      return;
    }
    if ("/logout".equals(path) && req.getMethod().equals("POST")) {
      req.getSession(false).invalidate();
      Json.send(res, 200, Map.of("message", "Signed out."));
      return;
    }
    throw new AppException(404, "Endpoint not found.");
  }
}
