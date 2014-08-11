package ubc.pavlab.aspiredb.server.controller;

import gemma.gsec.authentication.UserManager;

import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RemoteProxy
public class RegisterController {
    
   // @Autowired
  //  private PasswordEncoder passwordEncoder;

    @Autowired
    private UserManager userManager;

   // private RecaptchaTester recaptchaTester = new DefaultRecaptchaTester();

    @RequestMapping("/register.html")
    public String showregister( ModelMap model ) {
        return "register";
    }

   /** @RequestMapping("/keep_alive.html")
    public void loadUser( HttpServletRequest request, HttpServletResponse response ) throws IOException {

        String jsonText = null;

        if ( !SecurityServiceImpl.isUserLoggedIn() ) {
            jsonText = "{success:false}";

        } else {
            jsonText = "{success:true}";
        }

        JSONUtil jsonUtil = new JSONUtil( request, response );

        jsonUtil.writeToResponse( jsonText );

    }*/

}