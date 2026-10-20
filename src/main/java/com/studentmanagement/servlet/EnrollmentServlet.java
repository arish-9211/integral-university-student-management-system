package com.studentmanagement.servlet;

import com.studentmanagement.service.EnrollmentService;

public class EnrollmentServlet extends CrudServlet {
  public EnrollmentServlet(EnrollmentService service) {
    super(service);
  }
}
