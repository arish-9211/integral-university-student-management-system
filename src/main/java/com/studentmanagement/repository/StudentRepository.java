package com.studentmanagement.repository;

import com.mongodb.client.MongoDatabase;
import com.studentmanagement.model.Student;

public class StudentRepository extends MongoRepository<Student> {
  public StudentRepository(MongoDatabase db) {
    super(db, "students", Student.class);
    unique("enrollmentNumber");
    index("program", "semester", "status");
  }
}
