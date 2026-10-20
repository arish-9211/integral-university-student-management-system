package com.studentmanagement.repository;

import com.mongodb.client.MongoDatabase;
import com.studentmanagement.model.User;

public class UserRepository extends MongoRepository<User> {
  public UserRepository(MongoDatabase db) {
    super(db, "users", User.class);
    unique("email");
  }
}
