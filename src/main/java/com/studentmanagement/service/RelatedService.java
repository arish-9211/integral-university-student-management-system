package com.studentmanagement.service;

import static com.mongodb.client.model.Filters.*;

import com.google.gson.JsonObject;
import com.studentmanagement.exception.AppException;
import com.studentmanagement.model.*;
import com.studentmanagement.repository.*;
import com.studentmanagement.validation.Validator;
import java.util.List;

public abstract class RelatedService<T> extends CrudService<T> {
  protected final StudentRepository students;
  protected final CourseRepository courses;
  protected final EnrollmentRepository enrollments;

  protected RelatedService(
      MongoRepository<T> repo,
      Class<T> type,
      Object lock,
      StudentRepository students,
      CourseRepository courses,
      EnrollmentRepository enrollments,
      List<String> search,
      List<String> filters) {
    super(repo, type, lock, search, filters, List.of("createdAt", "date", "examDate"));
    this.students = students;
    this.courses = courses;
    this.enrollments = enrollments;
  }

  protected void references(JsonObject data, boolean requireEnrollment) {
    String studentId = Validator.text(data, "studentId", 50),
        courseId = Validator.text(data, "courseId", 50);
    Student student = students.get(studentId);
    Course course = courses.get(courseId);
    if (student == null || course == null)
      throw new AppException(400, "Choose an existing student and course.");
    if (requireEnrollment
        && enrollments.count(and(eq("studentId", studentId), eq("courseId", courseId))) == 0)
      throw new AppException(400, "Enroll the student in this course first.");
    if (!requireEnrollment
        && (student.status() != StudentStatus.ACTIVE || !course.status().equals("ACTIVE")))
      throw new AppException(400, "Only active students and courses can be enrolled.");
  }

  @Override
  protected JsonObject decorate(T item) {
    JsonObject data = super.decorate(item);
    Student student = students.get(data.get("studentId").getAsString());
    Course course = courses.get(data.get("courseId").getAsString());
    data.addProperty(
        "studentName",
        student == null ? "Removed student" : student.firstName() + " " + student.lastName());
    data.addProperty("enrollmentNumber", student == null ? "" : student.enrollmentNumber());
    data.addProperty("courseName", course == null ? "Removed course" : course.courseName());
    data.addProperty("courseCode", course == null ? "" : course.courseCode());
    return data;
  }
}
