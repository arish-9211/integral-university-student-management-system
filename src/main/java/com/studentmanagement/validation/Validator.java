package com.studentmanagement.validation;

import com.google.gson.*;
import com.studentmanagement.exception.AppException;
import java.time.LocalDate;
import java.util.*;

public final class Validator {
  private Validator() {}

  public static String text(JsonObject data, String field, int max) {
    JsonElement value = data.get(field);
    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
      fail(field + " is required.");
    String text = value.getAsString().trim();
    if (text.isEmpty() || text.length() > max)
      fail(field + " must contain 1–" + max + " characters.");
    data.addProperty(field, text);
    return text;
  }

  public static double number(
      JsonObject data, String field, double min, double max, boolean integer) {
    double value;
    try {
      value = data.get(field).getAsDouble();
    } catch (RuntimeException e) {
      fail(field + " must be a number.");
      return 0;
    }
    if (!Double.isFinite(value)
        || value < min
        || value > max
        || (integer && value != Math.floor(value))) fail(field + " is outside the allowed range.");
    data.addProperty(field, value);
    return value;
  }

  public static String choice(JsonObject data, String field, String... allowed) {
    String value = text(data, field, 40);
    if (!Arrays.asList(allowed).contains(value)) fail("Invalid " + field + ".");
    return value;
  }

  public static LocalDate date(JsonObject data, String field) {
    try {
      return LocalDate.parse(text(data, field, 10));
    } catch (RuntimeException e) {
      fail(field + " must be a valid ISO date (YYYY-MM-DD).");
      return null;
    }
  }

  public static void student(JsonObject data) {
    String enrollment = text(data, "enrollmentNumber", 30).toUpperCase(Locale.ROOT);
    if (!enrollment.matches("[A-Z0-9-]+"))
      fail("Enrollment number must contain letters, digits or hyphens.");
    data.addProperty("enrollmentNumber", enrollment);
    text(data, "firstName", 60);
    text(data, "lastName", 60);
    choice(data, "gender", "FEMALE", "MALE", "OTHER", "PREFER_NOT_TO_SAY");
    LocalDate birth = date(data, "dateOfBirth"), admission = date(data, "admissionDate");
    if (birth.isAfter(LocalDate.now())
        || admission.isAfter(LocalDate.now())
        || !birth.isBefore(admission))
      fail("Birth must precede admission; dates cannot be in the future.");
    String email = text(data, "email", 120).toLowerCase(Locale.ROOT);
    if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) fail("Enter a valid email address.");
    data.addProperty("email", email);
    if (!text(data, "phone", 20).matches("[+0-9 ()-]{7,20}")
        || data.get("phone").getAsString().replaceAll("\\D", "").length() < 7)
      fail("Enter a valid phone number.");
    text(data, "program", 80);
    number(data, "semester", 1, 12, true);
    text(data, "address", 400);
    choice(data, "status", "ACTIVE", "INACTIVE", "GRADUATED");
  }

  public static void course(JsonObject data) {
    String code = text(data, "courseCode", 30).toUpperCase(Locale.ROOT);
    if (!code.matches("[A-Z0-9-]+")) fail("Course code must contain letters, digits or hyphens.");
    data.addProperty("courseCode", code);
    text(data, "courseName", 100);
    text(data, "description", 500);
    number(data, "credits", 1, 10, true);
    number(data, "semester", 1, 12, true);
    choice(data, "status", "ACTIVE", "INACTIVE");
  }

  public static void marks(JsonObject data) {
    text(data, "examName", 80);
    date(data, "examDate");
    double maximum = number(data, "maximumMarks", 1, 1000, false);
    number(data, "obtainedMarks", 0, maximum, false);
  }

  public static void fail(String message) {
    throw new AppException(400, message);
  }
}
