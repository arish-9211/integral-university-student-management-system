package com.studentmanagement.model;

/** Immutable domain model: Java records provide a constructor and read-only accessors. */
public record Student(
    String id,
    String enrollmentNumber,
    String firstName,
    String lastName,
    String gender,
    String dateOfBirth,
    String email,
    String phone,
    String program,
    int semester,
    String address,
    String admissionDate,
    StudentStatus status,
    String createdAt,
    String updatedAt) {}
