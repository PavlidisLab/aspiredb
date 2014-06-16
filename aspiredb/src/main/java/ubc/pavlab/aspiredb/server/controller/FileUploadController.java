package ubc.pavlab.aspiredb.server.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.AbstractController;

import ubc.pavlab.aspiredb.server.model.ExtJSFormResult;
import ubc.pavlab.aspiredb.server.model.FileUploadBean;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.server.security.authentication.UserService;
import ubc.pavlab.aspiredb.server.service.ProjectService;



/**
 * Controller - Spring
 * 
 * @author gaya
 * @reference Loiane Groner http://loiane.com http://loianegroner.com
 */
@Controller
@RemoteProxy
public class FileUploadController {
    
    protected static Log log = LogFactory.getLog( HomeController.class );
    
    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = "/upload_action.html",method = RequestMethod.POST)
    public @ResponseBody
   String uploadFile( FileUploadBean uploadItem, BindingResult result ) {


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
        System.err.println( "Test upload: " + uploadItem.getFile().getOriginalFilename());
        System.err.println( "-------------------------------------------" );
        if(uploadItem.getFile().getSize()>0){                     
            try {      
                SaveFileFromInputStream(uploadItem.getFile().getInputStream(),"uploadFile",uploadItem.getFile().getOriginalFilename());
                try {
                Class.forName( "org.relique.jdbc.csv.CsvDriver" );

                // create a connection
                // arg[0] is the directory in which the .csv files are held
                Connection conn = DriverManager.getConnection( "jdbc:relique:csv:uploadFile/" );
                String filename =uploadItem.getFile().getOriginalFilename().substring(0,uploadItem.getFile().getOriginalFilename().lastIndexOf('.'));
                Statement stmt = conn.createStatement();
                ResultSet results = stmt.executeQuery( "SELECT * FROM "+ filename);
             //  addSubjectVariantsToExistingProject( filename, true, "test", "CNV" );

                // clean up
                results.close();
                stmt.close();
                conn.close();
                }catch ( Exception e ) {
                    return e.toString();
                }

                
                
            } catch (IOException e) {      
                System.out.println(e.getMessage());      
                return null;      
            }      
        }
        
        // set extjs return - sucsess
        extjsFormResult.setSuccess( true );

        return extjsFormResult.toString();

    }
    /* **    
    
     * @param stream    
     * @param path    
     * @param filename    
     * @throws IOException    
     */     
    public void SaveFileFromInputStream(InputStream stream,String path,String filename) throws IOException      
    {            
     FileOutputStream fs=new FileOutputStream(path + "/"+ filename);  
     byte[]  buffer=new byte[1024*1024];  
     int bytesum = 0;      
        int byteread = 0;   
          while ((byteread=stream.read())!=-1)  
          {  
              bytesum+=byteread;  
                
                fs.write(buffer,0,byteread);      
                fs.flush();      
                
          }  
          fs.close();      
          stream.close();      
    } 
    
    /**
     * Ajax. DWR can handle this.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
     public String addSubjectVariantsToExistingProject( String filename, boolean createProject, String projectName,String variantType ) {
       
        String result="";
        result =projectService.addSubjectVariantsToProject( filename, createProject, projectName, variantType );
        System.err.println( "DWR Uploaded file!" );
        return result;
    }
    
}