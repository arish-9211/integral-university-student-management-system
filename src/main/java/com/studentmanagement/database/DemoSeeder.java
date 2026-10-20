package com.studentmanagement.database;

import static com.mongodb.client.model.Filters.*;

import com.studentmanagement.model.*;
import com.studentmanagement.repository.*;
import com.studentmanagement.service.*;
import com.studentmanagement.util.PasswordUtil;
import java.time.*;
import java.util.*;

/** Explicit opt-in, stable identities, and insert-only behavior make repeated seeding safe. */
public class DemoSeeder {
  public static void seed(
      UserRepository users,
      StudentRepository students,
      CourseRepository courses,
      EnrollmentRepository enrollments,
      AttendanceRepository attendance,
      ResultRepository results) {
    String now = Instant.now().toString();
    if (users.first(eq("email", "admin@studentmanagement.local")) == null) {
      String id = UUID.randomUUID().toString();
      users.save(
          id,
          new User(
              id, "Arish Ahmad", "admin@studentmanagement.local", PasswordUtil.hash("Admin@123")),
          true);
    }
    String[] first = {
      "Aarav", "Meera", "Kabir", "Anaya", "Rohan", "Isha", "Vihaan", "Tara", "Arjun", "Naina",
      "Dev", "Sana", "Aditya", "Riya", "Neil", "Zoya", "Kunal", "Diya", "Rehan", "Avni"
    };
    String[] last = {
      "Sharma", "Kapoor", "Verma", "Joshi", "Mehta", "Rao", "Sethi", "Nair", "Bose", "Shah",
      "Batra", "Khan", "Roy", "Sen", "Jain", "Ali", "Das", "Singh", "Malik", "Patel"
    };
    String[] courseNames = {
      "Java Programming",
      "Database Management",
      "Web Technologies",
      "Data Structures",
      "Computer Networks",
      "Software Engineering"
    };
    var studentIds = new ArrayList<String>();
    var courseIds = new ArrayList<String>();
    for (int i = 0; i < 20; i++) {
      String number = "SMS-2026-" + String.format("%03d", i + 1);
      Student existing = students.first(eq("enrollmentNumber", number));
      if (existing != null) {
        studentIds.add(existing.id());
        continue;
      }
      String id = UUID.randomUUID().toString();
      studentIds.add(id);
      students.save(
          id,
          new Student(
              id,
              number,
              first[i],
              last[i],
              i % 2 == 0 ? "MALE" : "FEMALE",
              "2005-" + String.format("%02d", i % 12 + 1) + "-15",
              "student" + (i + 1) + "@example.com",
              "202-555-" + String.format("%04d", 100 + i),
              new String[] {"BCA", "B.Tech CSE", "B.Sc IT"}[i % 3],
              3,
              "Fictional Campus Residence, Block " + (i % 4 + 1),
              "2025-" + String.format("%02d", i % 5 + 6) + "-01",
              i == 19 ? StudentStatus.INACTIVE : StudentStatus.ACTIVE,
              now,
              now),
          true);
    }
    for (int i = 0; i < 6; i++) {
      String code = "CS" + (301 + i);
      Course existing = courses.first(eq("courseCode", code));
      if (existing != null) {
        courseIds.add(existing.id());
        continue;
      }
      String id = UUID.randomUUID().toString();
      courseIds.add(id);
      courses.save(
          id,
          new Course(
              id,
              code,
              courseNames[i],
              "Foundations and practical applications of " + courseNames[i].toLowerCase() + ".",
              4,
              3,
              "ACTIVE",
              now,
              now),
          true);
    }
    for (int i = 0; i < 20; i++)
      for (int j = 0; j < 3; j++) {
        String sid = studentIds.get(i), cid = courseIds.get((i + j) % 6);
        var pair = and(eq("studentId", sid), eq("courseId", cid));
        if (enrollments.count(pair) == 0) {
          String id = UUID.randomUUID().toString();
          enrollments.save(id, new Enrollment(id, sid, cid, now, now), true);
        }
        for (int day = 1; day <= 5; day++) {
          String date = "2026-09-" + String.format("%02d", day + 7);
          if (attendance.count(and(pair, eq("date", date))) == 0) {
            String id = UUID.randomUUID().toString();
            AttendanceStatus status =
                (i + j + day) % 7 == 0
                    ? AttendanceStatus.ABSENT
                    : (i + day) % 9 == 0 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
            attendance.save(id, new Attendance(id, sid, cid, date, status, now, now), true);
          }
        }
        if (results.count(and(pair, eq("examName", "Midterm"), eq("examDate", "2026-09-18")))
            == 0) {
          String id = UUID.randomUUID().toString();
          double marks = 43 + (i * 7 + j * 11) % 56;
          results.save(
              id,
              new Result(
                  id,
                  sid,
                  cid,
                  "Midterm",
                  "2026-09-18",
                  100,
                  marks,
                  marks,
                  AcademicRules.grade(marks),
                  now,
                  now),
              true);
        }
      }
  }
}
