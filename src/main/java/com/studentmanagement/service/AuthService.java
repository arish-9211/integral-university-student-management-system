package com.studentmanagement.service;

import static com.mongodb.client.model.Filters.*;

import com.studentmanagement.exception.AppException;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.util.PasswordUtil;
import java.util.*;

public class AuthService {
  private final UserRepository users;
  private final String dummyHash = PasswordUtil.hash("unusable-placeholder");
  private final Map<String, Attempt> attempts = new HashMap<>();

  private record Attempt(int count, long start) {}

  public AuthService(UserRepository users) {
    this.users = users;
  }

  public synchronized User login(String email, String password, String remote) {
    long now = System.currentTimeMillis();
    attempts.entrySet().removeIf(e -> now - e.getValue().start() > 300000);
    Attempt prior = attempts.getOrDefault(remote, new Attempt(0, now));
    if (prior.count() >= 10)
      throw new AppException(429, "Too many login attempts. Try again in five minutes.");
    User user = users.first(eq("email", email.trim().toLowerCase(Locale.ROOT)));
    boolean valid = PasswordUtil.verify(password, user == null ? dummyHash : user.passwordHash());
    if (!valid || user == null) {
      attempts.put(remote, new Attempt(prior.count() + 1, prior.start()));
      throw new AppException(401, "Email or password is incorrect.");
    }
    attempts.remove(remote);
    return user;
  }
}
