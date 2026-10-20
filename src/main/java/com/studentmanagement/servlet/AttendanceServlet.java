package com.studentmanagement.servlet;

import com.studentmanagement.service.AttendanceService;

public class AttendanceServlet extends CrudServlet {
  public AttendanceServlet(AttendanceService service) {
    super(service);
  }
}
