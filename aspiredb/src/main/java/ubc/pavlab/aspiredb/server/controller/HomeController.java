package ubc.pavlab.aspiredb.server.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ubc.pavlab.aspiredb.server.valueobjects.TestValueObject;

 
@Controller
@RequestMapping("/home.html")
public class HomeController {
 
	@RequestMapping(method = RequestMethod.GET)
	public String showHome(ModelMap model) {
 
		
		return "home";
 
	}
	
	
	public TestValueObject getTestValueObject(){
		
		
		return new TestValueObject("test");
	}
 
}