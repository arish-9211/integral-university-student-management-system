package com.studentmanagement.service;

import static com.mongodb.client.model.Filters.*;

import com.google.gson.JsonObject;
import com.studentmanagement.exception.AppException;
import com.studentmanagement.model.Enrollment;
import com.studentmanagement.repository.*;
import java.util.List;

public class EnrollmentService extends RelatedService<Enrollment> {
  private final AttendanceRepository attendance;
  private final ResultRepository results;

  public EnrollmentService(
      EnrollmentRepository repo,
      StudentRepository students,
      CourseRepository courses,
      AttendanceRepository attendance,
      ResultRepository results,
      Object lock) {
    super(
        repo,
        Enrollment.class,
        lock,
        students,
        courses,
        repo,
        List.of(),
        List.of("studentId", "courseId"));
    this.attendance = attendance;
    this.results = results;
  }

  protected void validate(JsonObject data) {
    references(data, false);
  }

  @Override
  public JsonObject save(String id, JsonObject input) {
    if (id != null) throw new AppException(405, "Remove and recreate an enrollment instead.");
    return super.save(id, input);
  }

  protected void beforeDelete(String id) {
    Enrollment enrollment = require(id);
    var filter =
        and(eq("studentId", enrollment.studentId()), eq("courseId", enrollment.courseId()));
    if (attendance.count(filter) > 0 || results.count(filter) > 0)
      throw new AppException(409, "Delete attendance and results for this enrollment first.");
  }
}
