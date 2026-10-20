package com.studentmanagement.servlet;

import com.studentmanagement.service.StudentService;

public class StudentServlet extends CrudServlet {
  public StudentServlet(StudentService service) {
    super(service);
  }
}
