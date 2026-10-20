package com.studentmanagement.service;

import com.google.gson.JsonObject;
import com.studentmanagement.model.Attendance;
import com.studentmanagement.repository.*;
import com.studentmanagement.validation.Validator;
import java.time.LocalDate;
import java.util.List;

public class AttendanceService extends RelatedService<Attendance> {
  public AttendanceService(
      AttendanceRepository repo,
      StudentRepository students,
      CourseRepository courses,
      EnrollmentRepository enrollments,
      Object lock) {
    super(
        repo,
        Attendance.class,
        lock,
        students,
        courses,
        enrollments,
        List.of("date", "status"),
        List.of("studentId", "courseId", "date", "status"));
  }

  protected void validate(JsonObject data) {
    references(data, true);
    if (Validator.date(data, "date").isAfter(LocalDate.now()))
      Validator.fail("Attendance cannot be in the future.");
    Validator.choice(data, "status", "PRESENT", "ABSENT", "LATE");
  }
}
