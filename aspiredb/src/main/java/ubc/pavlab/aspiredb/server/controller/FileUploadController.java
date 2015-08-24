/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.server.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

import com.ibm.icu.text.SimpleDateFormat;

/**
 * Controller for file uploads.
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
                serverFile = saveFileFromInputStream( uploadItem.getFile().getInputStream() );

                // set extjs return - sucsess
                extjsFormResult.setSuccess( true );
                extjsFormResult.setData( "{ \"filePath\" : \"" + serverFile.getAbsolutePath().replace( '\\', '/' )
                        + "\" } " );
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

        BufferedReader in = null;
        BufferedWriter out = null;

        in = new BufferedReader( new InputStreamReader( stream ) );
        out = new BufferedWriter( new FileWriter( serverFile ) );
        String line;
        while ( ( line = in.readLine() ) != null ) {
            out.write( line + "\n" );
        }
        in.close();
        out.close();

        return serverFile;

    }

}