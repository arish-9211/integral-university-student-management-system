package com.studentmanagement.model;

/** Immutable domain model: Java records provide a constructor and read-only accessors. */
public record Enrollment(
    String id, String studentId, String courseId, String createdAt, String updatedAt) {}
