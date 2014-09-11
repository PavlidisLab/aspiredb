package ubc.pavlab.aspiredb.server.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

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

import ubc.pavlab.aspiredb.server.model.ExtJSFormResult;
import ubc.pavlab.aspiredb.server.model.FileUploadBean;
import ubc.pavlab.aspiredb.server.service.ProjectService;
import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.icu.text.SimpleDateFormat;

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

    private static final String UPLOAD_PATH = System.getProperty( "java.io.tmpdir" ) + File.separator
            + "aspiredbUploads";

    private static final String FILE_SUFFIX = ".csv";

    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = "/upload_action.html", method = RequestMethod.POST)
    public @ResponseBody String uploadFile( FileUploadBean uploadItem, BindingResult result ) {

        ExtJSFormResult extjsFormResult = new ExtJSFormResult();

        if ( result.hasErrors() ) {
            for ( ObjectError error : result.getAllErrors() ) {
                log.error( "Error: " + error.getCode() + " - " + error.getDefaultMessage() );
            }

            // set extjs return - error
            extjsFormResult.setSuccess( false );

            return extjsFormResult.toString();
        }

        if ( uploadItem.getFile().getSize() > 0 ) {
            File serverFile = null;
            try {
                // saveFileFromInputStream( uploadItem.getFile().getInputStream(), "uploadFile", uploadItem.getFile()
                // .getOriginalFilename() );
                serverFile = saveFileFromInputStream( uploadItem.getFile().getInputStream() );

                // Class.forName( "org.relique.jdbc.csv.CsvDriver" );
                //
                // // create a connection
                // // arg[0] is the directory in which the .csv files are held
                // Connection conn = DriverManager.getConnection( "jdbc:relique:csv:" + UPLOAD_PATH );
                // // String filename = uploadItem.getFile().getOriginalFilename()
                // // .substring( 0, uploadItem.getFile().getOriginalFilename().lastIndexOf( '.' ) );
                // String filename = serverFile.getName().substring( 0, serverFile.getName().lastIndexOf( FILE_SUFFIX )
                // );
                // Statement stmt = conn.createStatement();
                // ResultSet results = stmt.executeQuery( "SELECT * FROM " + filename );
                //
                // // FIXME Create a new project
                //
                // // clean up
                // results.close();
                // stmt.close();
                // conn.close();

                // set extjs return - sucsess
                extjsFormResult.setSuccess( true );
                extjsFormResult.setData( "{ \"filePath\" : \"" + serverFile.getAbsolutePath() + "\" } " );
                extjsFormResult.setMessage( "success" );

                log.info( "Successfully saved " + uploadItem.getFile().getOriginalFilename() + " to "
                        + serverFile.getAbsolutePath() );

            } catch ( Exception e ) {
                log.error( e.getLocalizedMessage(), e );
                // set extjs return - sucsess
                extjsFormResult.setSuccess( false );
                extjsFormResult.setMessage( e.getLocalizedMessage() );
            }
        }

        return extjsFormResult.toString();
    }

    public File saveFileFromInputStream( InputStream stream ) throws IOException {

        File uploadDir = new File( UPLOAD_PATH );
        if ( !uploadDir.exists() ) {
            uploadDir.mkdir();
        }
        File temp = File.createTempFile( "aspiredb-temp-" + new SimpleDateFormat( "dd-MM-yyyy" ).format( new Date() )
                + "-", FILE_SUFFIX, uploadDir );
        temp.deleteOnExit();

        return saveFileFromInputStream( stream, temp );
    }

    /* **
     * 
     * @param stream
     * 
     * @param path
     * 
     * @param filename
     * 
     * @throws IOException
     */
    public File saveFileFromInputStream( InputStream stream, File serverFile ) throws IOException {

        // temp.getParentFile().getAbsolutePath(), temp.getName();
        // String csv = path + File.separator + filename;

        CSVWriter writer = new CSVWriter( new FileWriter( serverFile ) );
        String fileContent = getStringFromInputStream( stream );

        String[] Outresults = fileContent.split( "\n" );

        for ( int i = 0; i < Outresults.length; i++ ) {
            String[] passedCSVFile = Outresults[i].toString().split( "," );
            writer.writeNext( passedCSVFile );
        }

        writer.close();

        /**
         * FileOutputStream fs=new FileOutputStream(path + "/"+ filename); byte[] buffer=new byte[1024*1024]; int
         * bytesum = 0; int byteread = 0; while ((byteread=stream.read())!=-1) { bytesum+=byteread;
         * fs.write(buffer,0,byteread); fs.flush(); } fs.close(); stream.close();
         */

        return serverFile;

    }

    // convert InputStream to String
    private static String getStringFromInputStream( InputStream is ) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader( new InputStreamReader( is ) );
            while ( ( line = br.readLine() ) != null ) {
                sb.append( line );
                sb.append( "\n" );
            }

        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( br != null ) {
                try {
                    br.close();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    /**
     * Ajax. DWR can handle this.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public String addSubjectVariantsToExistingProject( String filename, boolean createProject, String projectName,
            String variantType ) {

        String result = "";
        result = projectService.addSubjectVariantsToProject( filename, createProject, projectName, variantType );
        System.err.println( "DWR Uploaded file!" );
        return result;
    }

}