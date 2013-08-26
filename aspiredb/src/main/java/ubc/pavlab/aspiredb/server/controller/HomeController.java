package ubc.pavlab.aspiredb.server.controller;


import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;



 
@Controller
@RemoteProxy
@RequestMapping("/home.html")
public class HomeController {
 
	@RequestMapping(method = RequestMethod.GET)
	public String showHome(ModelMap model) {
 
		
		return "home";
 
	}
	
	
 
}