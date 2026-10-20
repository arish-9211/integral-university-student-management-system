package com.studentmanagement.model;

/** Immutable domain model: Java records provide a constructor and read-only accessors. */
public record Course(
    String id,
    String courseCode,
    String courseName,
    String description,
    int credits,
    int semester,
    String status,
    String createdAt,
    String updatedAt) {}
