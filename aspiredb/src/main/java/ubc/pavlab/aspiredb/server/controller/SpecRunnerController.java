package ubc.pavlab.aspiredb.server.controller;

import gemma.gsec.util.SecurityUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RemoteProxy
@RequestMapping("/SpecRunner.html")
public class SpecRunnerController {

    protected static Log log = LogFactory.getLog( SpecRunnerController.class );

    @RequestMapping(method = RequestMethod.GET)
    public String showSpecRunner( ModelMap model ) {

        if ( !SecurityUtil.isUserLoggedIn() ) {

            log.info( "User not logged in, redirecting to login page" );
            return "login";
        }

        return "specRunner";

    }

}