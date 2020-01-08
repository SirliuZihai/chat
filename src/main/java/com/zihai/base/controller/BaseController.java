package com.zihai.base.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Controller
public class BaseController {
	//("/test")
	public void Home(HttpServletRequest req, HttpServletResponse rep) throws IOException{
		rep.sendRedirect("http://localhost:8080");	
	}
}
