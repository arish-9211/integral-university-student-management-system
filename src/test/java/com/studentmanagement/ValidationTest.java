package com.studentmanagement;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import com.studentmanagement.exception.AppException;
import com.studentmanagement.validation.Validator;
import org.junit.jupiter.api.Test;

class ValidationTest {
  private JsonObject student() {
    return JsonParser.parseString(
            """
            {"enrollmentNumber":"sms-test-1","firstName":"Test","lastName":"Student","gender":"OTHER","dateOfBirth":"2005-01-01","admissionDate":"2025-08-01","email":"TEST@example.com","phone":"202-555-0100","program":"BCA","semester":3,"address":"Fictional test address","status":"ACTIVE"}
            """)
        .getAsJsonObject();
  }

  @Test
  void normalizesStudent() {
    var s = student();
    Validator.student(s);
    assertEquals("SMS-TEST-1", s.get("enrollmentNumber").getAsString());
    assertEquals("test@example.com", s.get("email").getAsString());
  }

  @Test
  void rejectsBlankName() {
    var s = student();
    s.addProperty("firstName", " ");
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void rejectsInvalidEmail() {
    var s = student();
    s.addProperty("email", "wrong");
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void rejectsInvalidPhone() {
    var s = student();
    s.addProperty("phone", "letters");
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void rejectsOutOfRangeSemester() {
    var s = student();
    s.addProperty("semester", 13);
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void rejectsFractionalSemester() {
    var s = student();
    s.addProperty("semester", 2.5);
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void rejectsImpossibleBirthDate() {
    var s = student();
    s.addProperty("dateOfBirth", "2005-02-30");
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void rejectsAdmissionBeforeBirth() {
    var s = student();
    s.addProperty("admissionDate", "2000-01-01");
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void rejectsUnknownStatus() {
    var s = student();
    s.addProperty("status", "DELETED");
    assertThrows(AppException.class, () -> Validator.student(s));
  }

  @Test
  void validatesMarks() {
    var r =
        JsonParser.parseString(
                "{\"examName\":\"Final\",\"examDate\":\"2026-09-01\",\"maximumMarks\":50,\"obtainedMarks\":51}")
            .getAsJsonObject();
    assertThrows(AppException.class, () -> Validator.marks(r));
    r.addProperty("obtainedMarks", 40);
    assertDoesNotThrow(() -> Validator.marks(r));
  }

  @Test
  void rejectsMissingMarks() {
    assertThrows(AppException.class, () -> Validator.marks(new JsonObject()));
  }
}
