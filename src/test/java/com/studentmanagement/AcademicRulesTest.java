package com.studentmanagement;

import static org.junit.jupiter.api.Assertions.*;

import com.studentmanagement.exception.AppException;
import com.studentmanagement.service.AcademicRules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AcademicRulesTest {
  @ParameterizedTest
  @CsvSource({
    "100,A+",
    "90,A+",
    "89.999,A",
    "80,A",
    "79.99,B",
    "70,B",
    "60,C",
    "50,D",
    "49.999,F",
    "0,F"
  })
  void gradeBoundaries(double percentage, String grade) {
    assertEquals(grade, AcademicRules.grade(percentage));
  }

  @Test
  void percentagesAreNotRoundedBeforeGrading() {
    double p = AcademicRules.percentage(89.999, 100);
    assertEquals("A", AcademicRules.grade(p));
  }

  @Test
  void calculatesWeightedExamPercentage() {
    assertEquals(75, AcademicRules.percentage(30, 40));
  }

  @Test
  void rejectsInvalidMarks() {
    assertThrows(AppException.class, () -> AcademicRules.percentage(1, 0));
    assertThrows(AppException.class, () -> AcademicRules.percentage(-1, 100));
    assertThrows(AppException.class, () -> AcademicRules.percentage(101, 100));
    assertThrows(AppException.class, () -> AcademicRules.percentage(Double.NaN, 100));
  }

  @ParameterizedTest
  @CsvSource({
    "10,9,90.0,Excellent",
    "4,3,75.0,Good",
    "10,7,70.0,Attendance Warning",
    "0,0,0.0,No attendance yet"
  })
  void attendanceBoundaries(long held, long attended, double percent, String label) {
    var result = AcademicRules.attendance(held, attended);
    assertEquals(percent, result.get("percentage"));
    assertEquals(label, result.get("label"));
    assertEquals(held - attended, result.get("absent"));
  }

  @Test
  void rejectsImpossibleAttendance() {
    assertThrows(IllegalArgumentException.class, () -> AcademicRules.attendance(2, 3));
  }
}
