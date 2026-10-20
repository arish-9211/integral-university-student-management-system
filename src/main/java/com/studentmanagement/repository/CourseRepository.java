package com.studentmanagement.repository;

import com.mongodb.client.MongoDatabase;
import com.studentmanagement.model.Course;

public class CourseRepository extends MongoRepository<Course> {
  public CourseRepository(MongoDatabase db) {
    super(db, "courses", Course.class);
    unique("courseCode");
  }
}
