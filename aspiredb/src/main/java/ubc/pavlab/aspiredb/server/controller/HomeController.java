package ubc.pavlab.aspiredb.server.controller;


import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ubc.pavlab.aspiredb.server.valueobjects.TestValueObject;

 
@Controller
@RemoteProxy
@RequestMapping("/home.html")
public class HomeController {
 
	@RequestMapping(method = RequestMethod.GET)
	public String showHome(ModelMap model) {
 
		
		return "home";
 
	}
	
	@RemoteMethod
	public TestValueObject getTestValueObject(){
		
		
		return new TestValueObject("test");
	}
 
}