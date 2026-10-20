package com.studentmanagement.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
  private PasswordUtil() {}

  public static String hash(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(12));
  }

  public static boolean verify(String password, String hash) {
    return password != null && password.length() <= 72 && BCrypt.checkpw(password, hash);
  }
}
