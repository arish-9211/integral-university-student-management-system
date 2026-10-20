package com.studentmanagement.repository;

import com.mongodb.client.MongoDatabase;
import com.studentmanagement.model.Result;

public class ResultRepository extends MongoRepository<Result> {
  public ResultRepository(MongoDatabase db) {
    super(db, "results", Result.class);
    unique("studentId", "courseId", "examName", "examDate");
  }
}
