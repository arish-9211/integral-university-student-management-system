package com.studentmanagement.repository;

import com.mongodb.client.MongoDatabase;
import com.studentmanagement.model.Attendance;

public class AttendanceRepository extends MongoRepository<Attendance> {
  public AttendanceRepository(MongoDatabase db) {
    super(db, "attendance", Attendance.class);
    unique("studentId", "courseId", "date");
    index("courseId", "date");
  }
}
