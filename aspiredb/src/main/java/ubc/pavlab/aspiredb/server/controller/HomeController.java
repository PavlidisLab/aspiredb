package ubc.pavlab.aspiredb.server.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ubc.pavlab.aspiredb.server.security.SecurityServiceImpl;

@Controller
@RemoteProxy
@RequestMapping("/home.html")
public class HomeController {

    protected static Log log = LogFactory.getLog( HomeController.class );

    @RequestMapping(method = RequestMethod.GET)
    public String showHome( ModelMap model ) {

        if ( !SecurityServiceImpl.isUserLoggedIn() ) {

            log.info( "User not logged in, redirecting to login page" );
            return "login";
        }

        return "home";

    }

}