package com.studentmanagement.servlet;

import com.studentmanagement.service.CourseService;

public class CourseServlet extends CrudServlet {
  public CourseServlet(CourseService service) {
    super(service);
  }
}
