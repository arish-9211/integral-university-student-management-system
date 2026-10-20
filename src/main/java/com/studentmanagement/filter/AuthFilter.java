package com.studentmanagement.filter;

import com.studentmanagement.util.Json;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

public class AuthFilter implements Filter {
  public void doFilter(ServletRequest raw, ServletResponse output, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) raw;
    HttpServletResponse res = (HttpServletResponse) output;
    res.setHeader("X-Content-Type-Options", "nosniff");
    res.setHeader("X-Frame-Options", "DENY");
    res.setHeader("Referrer-Policy", "same-origin");
    res.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self'"
            + " data:; frame-ancestors 'none'; base-uri 'self'; form-action 'self'");
    res.setHeader("Cache-Control", "no-store");
    String path = req.getRequestURI();
    boolean api = path.startsWith("/api/");
    boolean write = !req.getMethod().equals("GET") && !req.getMethod().equals("HEAD");
    if (write) {
      String origin = req.getHeader("Origin");
      if (origin != null && !origin.equals(req.getScheme() + "://" + req.getHeader("Host"))) {
        Json.send(res, 403, Map.of("error", "Request origin is not allowed."));
        return;
      }
      if (req.getContentType() == null
          || !req.getContentType().toLowerCase().startsWith("application/json")) {
        Json.send(res, 415, Map.of("error", "Use application/json."));
        return;
      }
    }
    boolean publicPath =
        path.equals("/")
            || path.equals("/login.html")
            || path.startsWith("/css/")
            || path.startsWith("/js/")
            || path.startsWith("/images/")
            || path.matches("/errors/(403|404|500).html")
            || path.equals("/api/auth/login");
    if (!publicPath) {
      var session = req.getSession(false);
      if (session == null || session.getAttribute("user") == null) {
        if (api) Json.send(res, 401, Map.of("error", "Please sign in to continue."));
        else res.sendRedirect("/login.html");
        return;
      }
      if (write
          && !String.valueOf(session.getAttribute("csrf")).equals(req.getHeader("X-CSRF-Token"))) {
        Json.send(res, 403, Map.of("error", "Security token expired. Refresh and try again."));
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
