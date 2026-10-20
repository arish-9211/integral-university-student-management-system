package com.studentmanagement.service;

import static com.mongodb.client.model.Filters.*;

import com.google.gson.JsonObject;
import com.studentmanagement.exception.AppException;
import com.studentmanagement.model.Course;
import com.studentmanagement.repository.*;
import com.studentmanagement.validation.Validator;
import java.util.List;

public class CourseService extends CrudService<Course> {
  private final EnrollmentRepository enrollments;

  public CourseService(CourseRepository repo, EnrollmentRepository enrollments, Object lock) {
    super(
        repo,
        Course.class,
        lock,
        List.of("courseCode", "courseName"),
        List.of("semester", "status"),
        List.of("createdAt", "courseCode", "courseName", "semester"));
    this.enrollments = enrollments;
  }

  protected void validate(JsonObject data) {
    Validator.course(data);
  }

  protected void beforeDelete(String id) {
    if (enrollments.count(eq("courseId", id)) > 0)
      throw new AppException(409, "Remove this course's enrollments and academic records first.");
  }
}
