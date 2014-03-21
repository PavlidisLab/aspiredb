package ubc.pavlab.aspiredb.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import ubc.pavlab.aspiredb.server.security.SecurityServiceImpl;
import ubc.pavlab.aspiredb.server.security.authentication.JSONUtil;

@Controller
@RemoteProxy
public class LoginController {

    @RequestMapping("/login.html")
    public String showLogin( ModelMap model ) {
        return "login";
    }

    @RequestMapping("/keep_alive.html")
    public void loadUser( HttpServletRequest request, HttpServletResponse response ) throws IOException{

        String jsonText = null;
        
        if ( !SecurityServiceImpl.isUserLoggedIn() ) {            
            jsonText = "{success:false}";

        } else {
            jsonText = "{success:true}";
        }

        JSONUtil jsonUtil = new JSONUtil( request, response );
        
        jsonUtil.writeToResponse( jsonText );

    }

}