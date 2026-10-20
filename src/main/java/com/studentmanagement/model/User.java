package com.studentmanagement.model;

/** Immutable domain model: Java records provide a constructor and read-only accessors. */
public record User(String id, String name, String email, String passwordHash) {}
