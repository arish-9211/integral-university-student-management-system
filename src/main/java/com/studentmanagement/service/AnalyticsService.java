package com.studentmanagement.service;

import static com.mongodb.client.model.Filters.*;

import com.studentmanagement.model.*;
import com.studentmanagement.repository.*;
import com.studentmanagement.util.Json;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {
  public List<String> programs() {
    return students.distinctText("program").stream().sorted().toList();
  }

  private final StudentRepository students;
  private final CourseRepository courses;
  private final EnrollmentRepository enrollments;
  private final AttendanceRepository attendance;
  private final ResultRepository results;

  public AnalyticsService(
      StudentRepository s,
      CourseRepository c,
      EnrollmentRepository e,
      AttendanceRepository a,
      ResultRepository r) {
    students = s;
    courses = c;
    enrollments = e;
    attendance = a;
    results = r;
  }

  public Map<String, Object> dashboard() {
    var ss = students.all();
    var cs = courses.all();
    var es = enrollments.all();
    var aa = attendance.all();
    var rr = results.all();
    var stats = new LinkedHashMap<String, Object>();
    stats.put("totalStudents", ss.size());
    stats.put(
        "activeStudents", ss.stream().filter(s -> s.status() == StudentStatus.ACTIVE).count());
    stats.put("totalCourses", cs.size());
    stats.put("totalEnrollments", es.size());
    stats.put(
        "averageAttendance",
        AcademicRules.attendance(
                aa.size(), aa.stream().filter(a -> a.status() != AttendanceStatus.ABSENT).count())
            .get("percentage"));
    stats.put(
        "averageMarks",
        AcademicRules.round(rr.stream().mapToDouble(Result::percentage).average().orElse(0)));
    var activity = new ArrayList<Map<String, String>>();
    ss.forEach(
        s ->
            activity.add(
                Map.of(
                    "title",
                    s.firstName() + " " + s.lastName(),
                    "detail",
                    "Student record updated",
                    "at",
                    s.updatedAt())));
    rr.forEach(
        r ->
            activity.add(
                Map.of(
                    "title",
                    r.examName(),
                    "detail",
                    "Result recorded · Grade " + r.grade(),
                    "at",
                    r.updatedAt())));
    aa.forEach(
        a ->
            activity.add(
                Map.of(
                    "title",
                    "Attendance · " + a.date(),
                    "detail",
                    a.status().toString(),
                    "at",
                    a.updatedAt())));
    activity.sort(Comparator.comparing((Map<String, String> a) -> a.get("at")).reversed());
    return Map.of(
        "stats",
        stats,
        "programs",
        ss.stream()
            .collect(Collectors.groupingBy(Student::program, TreeMap::new, Collectors.counting())),
        "semesters",
        ss.stream()
            .collect(
                Collectors.groupingBy(
                    s -> "Semester " + s.semester(), TreeMap::new, Collectors.counting())),
        "grades",
        rr.stream()
            .collect(Collectors.groupingBy(Result::grade, TreeMap::new, Collectors.counting())),
        "admissions",
        ss.stream()
            .collect(
                Collectors.groupingBy(
                    s -> s.admissionDate().substring(0, 7), TreeMap::new, Collectors.counting())),
        "recentStudents",
        ss.stream().limit(5).toList(),
        "activity",
        activity.stream().limit(6).toList());
  }

  public Map<String, Object> profile(String id) {
    var student = students.get(id);
    if (student == null)
      throw new com.studentmanagement.exception.AppException(404, "Student not found.");
    var aa = attendance.all().stream().filter(a -> a.studentId().equals(id)).toList();
    var rr =
        results.all().stream()
            .filter(r -> r.studentId().equals(id))
            .map(
                r -> {
                  var row = Json.GSON.toJsonTree(r).getAsJsonObject();
                  row.addProperty("courseName", courses.get(r.courseId()).courseName());
                  return row;
                })
            .toList();
    var enrolled =
        enrollments.all().stream()
            .filter(e -> e.studentId().equals(id))
            .map(
                e -> {
                  var row = Json.GSON.toJsonTree(courses.get(e.courseId())).getAsJsonObject();
                  var records = aa.stream().filter(a -> a.courseId().equals(e.courseId())).toList();
                  row.add(
                      "attendance",
                      Json.GSON.toJsonTree(
                          AcademicRules.attendance(
                              records.size(),
                              records.stream()
                                  .filter(a -> a.status() != AttendanceStatus.ABSENT)
                                  .count())));
                  return row;
                })
            .toList();
    return Map.of(
        "student",
        student,
        "courses",
        enrolled,
        "results",
        rr,
        "attendance",
        AcademicRules.attendance(
            aa.size(), aa.stream().filter(a -> a.status() != AttendanceStatus.ABSENT).count()));
  }

  public Map<String, Object> reports() {
    var aa = attendance.all();
    var rr = results.all();
    var rows =
        students.all().stream()
            .map(
                s -> {
                  var a = aa.stream().filter(x -> x.studentId().equals(s.id())).toList();
                  var r = rr.stream().filter(x -> x.studentId().equals(s.id())).toList();
                  var row = new LinkedHashMap<String, Object>();
                  row.put("id", s.id());
                  row.put("enrollmentNumber", s.enrollmentNumber());
                  row.put("name", s.firstName() + " " + s.lastName());
                  row.put("program", s.program());
                  row.put(
                      "attendance",
                      AcademicRules.attendance(
                          a.size(),
                          a.stream().filter(x -> x.status() != AttendanceStatus.ABSENT).count()));
                  row.put(
                      "averageMarks",
                      r.isEmpty()
                          ? null
                          : AcademicRules.round(
                              r.stream().mapToDouble(Result::percentage).average().orElse(0)));
                  row.put("exams", r.size());
                  return row;
                })
            .toList();
    return Map.of("students", rows);
  }

  public String csv() {
    var text =
        new StringBuilder(
            "Enrollment number,Student,Program,Classes held,Attended,Attendance %,Average marks"
                + " %,Exams\r\n");
    for (var s : students.all()) {
      var aa = attendance.all().stream().filter(a -> a.studentId().equals(s.id())).toList();
      var rr = results.all().stream().filter(r -> r.studentId().equals(s.id())).toList();
      var summary =
          AcademicRules.attendance(
              aa.size(), aa.stream().filter(a -> a.status() != AttendanceStatus.ABSENT).count());
      var values =
          List.of(
              s.enrollmentNumber(),
              s.firstName() + " " + s.lastName(),
              s.program(),
              "" + summary.get("held"),
              "" + summary.get("attended"),
              "" + summary.get("percentage"),
              rr.isEmpty()
                  ? ""
                  : String.valueOf(
                      AcademicRules.round(
                          rr.stream().mapToDouble(Result::percentage).average().orElse(0))),
              "" + rr.size());
      text.append(values.stream().map(this::csvCell).collect(Collectors.joining(",")))
          .append("\r\n");
    }
    return text.toString();
  }

  private String csvCell(String value) {
    if (value.matches("^[=+@\t\r-].*")) value = "'" + value;
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }
}
