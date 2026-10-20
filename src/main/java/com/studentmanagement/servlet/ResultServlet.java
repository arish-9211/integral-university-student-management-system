package com.studentmanagement.servlet;

import com.studentmanagement.service.ResultService;

public class ResultServlet extends CrudServlet {
  public ResultServlet(ResultService service) {
    super(service);
  }
}
