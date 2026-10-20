package com.studentmanagement.util;

import com.google.gson.*;
import com.studentmanagement.exception.AppException;
import jakarta.servlet.http.*;
import java.io.*;

public final class Json {
  public static final Gson GSON = new Gson();

  private Json() {}

  public static JsonObject body(HttpServletRequest request) throws IOException {
    StringBuilder text = new StringBuilder();
    char[] buffer = new char[2048];
    int count;
    while ((count = request.getReader().read(buffer)) != -1) {
      text.append(buffer, 0, count);
      if (text.length() > 32768) throw new AppException(413, "Request is too large.");
    }
    try {
      return JsonParser.parseString(text.toString()).getAsJsonObject();
    } catch (RuntimeException e) {
      throw new AppException(400, "A valid JSON object is required.");
    }
  }

  public static void send(HttpServletResponse response, int status, Object value)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json;charset=UTF-8");
    GSON.toJson(value, response.getWriter());
  }
}
