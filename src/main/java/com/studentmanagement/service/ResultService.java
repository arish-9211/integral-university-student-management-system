package com.studentmanagement.service;

import com.google.gson.JsonObject;
import com.studentmanagement.model.Result;
import com.studentmanagement.repository.*;
import com.studentmanagement.validation.Validator;
import java.util.List;

public class ResultService extends RelatedService<Result> {
  public ResultService(
      ResultRepository repo,
      StudentRepository students,
      CourseRepository courses,
      EnrollmentRepository enrollments,
      Object lock) {
    super(
        repo,
        Result.class,
        lock,
        students,
        courses,
        enrollments,
        List.of("examName"),
        List.of("studentId", "courseId", "grade", "examDate"));
  }

  protected void validate(JsonObject data) {
    references(data, true);
    Validator.marks(data);
    double percentage =
        AcademicRules.percentage(
            data.get("obtainedMarks").getAsDouble(), data.get("maximumMarks").getAsDouble());
    data.addProperty("percentage", AcademicRules.round(percentage));
    data.addProperty("grade", AcademicRules.grade(percentage));
  }
}
