package com.studentmanagement.model;

/** Immutable domain model: Java records provide a constructor and read-only accessors. */
public record Attendance(
    String id,
    String studentId,
    String courseId,
    String date,
    AttendanceStatus status,
    String createdAt,
    String updatedAt) {}
