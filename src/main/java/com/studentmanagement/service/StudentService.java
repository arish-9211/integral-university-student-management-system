package com.studentmanagement.service;

import static com.mongodb.client.model.Filters.*;

import com.google.gson.JsonObject;
import com.studentmanagement.exception.AppException;
import com.studentmanagement.model.Student;
import com.studentmanagement.repository.*;
import com.studentmanagement.validation.Validator;
import java.util.List;

public class StudentService extends CrudService<Student> {
  private final EnrollmentRepository enrollments;

  public StudentService(StudentRepository repo, EnrollmentRepository enrollments, Object lock) {
    super(
        repo,
        Student.class,
        lock,
        List.of("firstName", "lastName", "enrollmentNumber", "email"),
        List.of("program", "semester", "status"),
        List.of("createdAt", "firstName", "enrollmentNumber", "semester", "admissionDate"));
    this.enrollments = enrollments;
  }

  protected void validate(JsonObject data) {
    Validator.student(data);
  }

  protected void beforeDelete(String id) {
    if (enrollments.count(eq("studentId", id)) > 0)
      throw new AppException(409, "Remove this student's enrollments and academic records first.");
  }
}
