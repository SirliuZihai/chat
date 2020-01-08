package com.zihai.expore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/explore")
public class ExploreController {

	@RequestMapping(value="/home.do",method = RequestMethod.GET)
	public String home(){
		return "404";
	}
}
