package com.studentmanagement.servlet;

import com.studentmanagement.exception.AppException;
import com.studentmanagement.service.CrudService;
import com.studentmanagement.util.Json;
import jakarta.servlet.http.*;
import java.util.*;

public class CrudServlet extends BaseServlet {
  private final CrudService<?> service;

  public CrudServlet(CrudService<?> service) {
    this.service = service;
  }

  protected void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String path = request.getPathInfo();
    String id = path == null || path.equals("/") ? null : path.substring(1);
    if (id != null && !id.matches("[a-f0-9-]{36}"))
      throw new AppException(404, "Record not found.");
    switch (request.getMethod()) {
      case "GET" -> {
        var params = new HashMap<String, String>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        Json.send(response, 200, id == null ? service.list(params) : service.detail(id));
      }
      case "POST" -> {
        if (id != null) throw new AppException(405, "Create records at the collection URL.");
        Json.send(response, 201, service.save(null, Json.body(request)));
      }
      case "PUT" -> {
        if (id == null) throw new AppException(400, "A record ID is required.");
        Json.send(response, 200, service.save(id, Json.body(request)));
      }
      case "DELETE" -> {
        if (id == null) throw new AppException(400, "A record ID is required.");
        service.delete(id);
        Json.send(response, 200, Map.of("message", "Record deleted successfully."));
      }
      default -> throw new AppException(405, "Method not allowed.");
    }
  }
}
