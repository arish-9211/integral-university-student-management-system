package com.studentmanagement.repository;

import com.mongodb.client.MongoDatabase;
import com.studentmanagement.model.Enrollment;

public class EnrollmentRepository extends MongoRepository<Enrollment> {
  public EnrollmentRepository(MongoDatabase db) {
    super(db, "enrollments", Enrollment.class);
    unique("studentId", "courseId");
    index("courseId");
  }
}
