package ubc.pavlab.aspiredb.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.AbstractController;

import ubc.pavlab.aspiredb.server.model.ExtJSFormResult;
import ubc.pavlab.aspiredb.server.model.FileUploadBean;



/**
 * Controller - Spring
 * 
 * @author Loiane Groner http://loiane.com http://loianegroner.com
 */
@Controller
@RemoteProxy
@RequestMapping(value = "/upload_action.html")
public class FileUploadController {
    
    protected static Log log = LogFactory.getLog( HomeController.class );

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
   String create( FileUploadBean uploadItem, BindingResult result ) {
      //  String create(HttpServletRequest request, BindingResult result ) throws IOException {

        ExtJSFormResult extjsFormResult = new ExtJSFormResult();

        if ( result.hasErrors() ) {
            for ( ObjectError error : result.getAllErrors() ) {
                System.err.println( "Error: " + error.getCode() + " - " + error.getDefaultMessage() );
            }

            // set extjs return - error
            extjsFormResult.setSuccess( false );

            return extjsFormResult.toString();
        }

        // Some type of file processing...
        System.err.println( "-------------------------------------------" );
        System.err.println( "Test upload: " + uploadItem.getFile());//request);
        System.err.println( "-------------------------------------------" );

        // set extjs return - sucsess
        extjsFormResult.setSuccess( true );

        return extjsFormResult.toString();

    }
}
