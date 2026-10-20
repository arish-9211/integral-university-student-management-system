package com.studentmanagement.service;

import com.studentmanagement.exception.AppException;
import java.util.Map;

public final class AcademicRules {
  private AcademicRules() {}

  public static double percentage(double obtained, double maximum) {
    if (!Double.isFinite(obtained)
        || !Double.isFinite(maximum)
        || maximum <= 0
        || obtained < 0
        || obtained > maximum)
      throw new AppException(400, "Marks must be between zero and maximum marks.");
    return obtained * 100.0 / maximum;
  }

  public static double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  public static String grade(double percentage) {
    if (percentage >= 90) return "A+";
    if (percentage >= 80) return "A";
    if (percentage >= 70) return "B";
    if (percentage >= 60) return "C";
    if (percentage >= 50) return "D";
    return "F";
  }

  public static Map<String, Object> attendance(long held, long attended) {
    if (held < 0 || attended < 0 || attended > held)
      throw new IllegalArgumentException("Invalid attendance counts");
    double percent = held == 0 ? 0 : attended * 100.0 / held;
    return Map.of(
        "held",
        held,
        "attended",
        attended,
        "absent",
        held - attended,
        "percentage",
        round(percent),
        "label",
        held == 0
            ? "No attendance yet"
            : percent >= 90 ? "Excellent" : percent >= 75 ? "Good" : "Attendance Warning");
  }
}
