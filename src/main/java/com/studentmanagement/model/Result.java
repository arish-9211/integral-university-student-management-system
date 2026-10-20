package com.studentmanagement.model;

/** Immutable domain model: Java records provide a constructor and read-only accessors. */
public record Result(
    String id,
    String studentId,
    String courseId,
    String examName,
    String examDate,
    double maximumMarks,
    double obtainedMarks,
    double percentage,
    String grade,
    String createdAt,
    String updatedAt) {}
