package com.studentmanagement.servlet;

import com.studentmanagement.exception.AppException;
import com.studentmanagement.util.Json;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    try {
      handle(request, response);
    } catch (AppException e) {
      Json.send(response, e.getStatus(), Map.of("error", e.getMessage()));
    } catch (Exception e) {
      System.err.println("Request failed: " + e.getClass().getSimpleName());
      Json.send(
          response,
          500,
          Map.of("error", "Something went wrong. Please retry or contact the administrator."));
    }
  }

  protected abstract void handle(HttpServletRequest request, HttpServletResponse response)
      throws Exception;
}
