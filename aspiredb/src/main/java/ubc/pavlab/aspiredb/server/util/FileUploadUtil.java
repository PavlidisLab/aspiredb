/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubc.pavlab.aspiredb.server.util;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ubic.basecode.util.FileTools;

import javax.servlet.http.HttpServletRequest;
import java.io.*;


/**
 * Utility methods for uploading files.
 * modified from Gemma, some of these methods may be unnecessary for aspiredb
 * 
 * @author pavlidis
 * @version $Id: FileUploadUtil.java,v 1.2 2013/06/11 22:30:47 anton Exp $
 */
public class FileUploadUtil {

    private static Log log = LogFactory.getLog( FileUploadUtil.class.getName() );
    private static final int BUF_SIZE = 32768;
    
    /**
     * @param multipartFile
     */
    public static File copyUploadedFile( FileItemStream fileItemStream, HttpServletRequest request ) throws IOException,
            FileNotFoundException {

        String copiedFilePath = getLocalUploadLocation( request, fileItemStream );

        File copiedFile = new File( copiedFilePath );

        copyFile( fileItemStream, copiedFile );

        if ( !copiedFile.canRead() || copiedFile.length() == 0 ) {
            throw new IllegalArgumentException( "Uploaded file is not readable or of size zero" );
        }

        if ( request != null ) {
            
            // place the data into the request for retrieval on next page
            request.setAttribute( "fileName", fileItemStream.getName() );
            request.setAttribute( "contentType", fileItemStream.getContentType() );
            
            request.setAttribute( "location", copiedFilePath );
        }
        return copiedFile;

    }

    /**
     * @param request
     * @param fileUpload
     * @param file
     * @return
     */
    private static String getLocalUploadLocation( HttpServletRequest request, FileItemStream fileItemStream ) {
        
        String uploadDir = getUploadPath();

        // Create the directory if it doesn't exist
        File uploadDirFile = FileTools.createDir( uploadDir );
        
        //maybe do below later        
/*
        String copiedFile = uploadDirFile.getAbsolutePath()
                + File.separatorChar
                + ( request == null || request.getSession() == null ? RandomStringUtils.randomAlphanumeric( 20 )
                        : request.getSession().getId() ) + "__" + fileItemStream.getName();
   */     
        String copiedFile = uploadDirFile.getAbsolutePath()
                + File.separatorChar
                 + fileItemStream.getName();

        return copiedFile;
    }

    public static File copyUploadedInputStream( InputStream is ) throws IOException, FileNotFoundException {
        // Create the directory if it doesn't exist
        String uploadDir = ConfigUtils.getDownloadPath() + "userUploads";
        File uploadDirFile = FileTools.createDir( uploadDir );

        File copiedFile = new File( uploadDirFile.getAbsolutePath() + File.separatorChar
                + RandomStringUtils.randomAlphanumeric( 50 ) );
        copy( copiedFile, is );
        return copiedFile;
    }

    /**
     * @param file
     * @param copiedFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copyFile( FileItemStream fileItemStream, File copiedFile ) throws FileNotFoundException, IOException {
        log.info( "Copying file " + fileItemStream );
        // write the file to the file specified
        InputStream stream = fileItemStream.openStream();

        copy( copiedFile, stream );
        log.info( "Done copying to " + copiedFile );
    }

    /**
     * @param copiedFile
     * @param stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copy( File copiedFile, InputStream stream ) throws FileNotFoundException, IOException {
        OutputStream bos = new FileOutputStream( copiedFile );
        int bytesRead = 0;
        byte[] buffer = new byte[BUF_SIZE];
        while ( ( bytesRead = stream.read( buffer, 0, BUF_SIZE ) ) != -1 ) {
            bos.write( buffer, 0, bytesRead );
        }

        bos.close();
        stream.close();
    }

    /**
     * @param request
     * @return
     */
    public static String getUploadPath() {
        return ConfigUtils.getDownloadPath() + "userUploads";
    }

    /**
     * @param request
     * @return
     */
    public static String getContextUploadPath() {
        return getUploadPath().replace( File.separatorChar, '/' );
    }

}
