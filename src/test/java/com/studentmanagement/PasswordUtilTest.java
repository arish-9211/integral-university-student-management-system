package com.studentmanagement;

import static org.junit.jupiter.api.Assertions.*;

import com.studentmanagement.util.PasswordUtil;
import org.junit.jupiter.api.Test;

class PasswordUtilTest {
  @Test
  void bcryptHashesAreSaltedAndVerifiable() {
    String first = PasswordUtil.hash("Demo@456");
    String second = PasswordUtil.hash("Demo@456");
    assertNotEquals(first, second);
    assertNotEquals("Demo@456", first);
    assertTrue(PasswordUtil.verify("Demo@456", first));
    assertFalse(PasswordUtil.verify("wrong", first));
    assertFalse(PasswordUtil.verify(null, first));
  }
}
