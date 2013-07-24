/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
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

package ubc.pavlab.aspiredb.server.fileuploader;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ubc.pavlab.aspiredb.server.util.FileUploadUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class UploadServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 7328200365178723332L;
    private static final Logger LOGGER = LoggerFactory.getLogger( UploadServlet.class );
    private static final String FILE_SEPERATOR = System.getProperty( "file.separator" );
    private String uploadDirectory;

    @Override
    public void init() throws ServletException {

        uploadDirectory = FileUploadUtil.getUploadPath();
    }

    @Override
    protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
            throws ServletException, IOException {
        try {
            uploadFile( request );
        } catch ( FileUploadException fue ) {
            throw new ServletException( fue );
        }
    }

    private void uploadFile( final HttpServletRequest request ) throws FileUploadException, IOException {

        if ( !ServletFileUpload.isMultipartContent( request ) ) {
            throw new FileUploadException( "error multipart request not found" );
        }

        FileItemFactory fileItemFactory = new DiskFileItemFactory();
        ServletFileUpload servletFileUpload = new ServletFileUpload( fileItemFactory );

        FileItemIterator fileItemIterator = servletFileUpload.getItemIterator( request );

        HttpSession session = request.getSession();
        UploadProgress uploadProgress = UploadProgress.getUploadProgress( session );

        while ( fileItemIterator.hasNext() ) {
            FileItemStream fileItemStream = fileItemIterator.next();

            String filePath = fileItemStream.getName();
            String fileName = filePath.substring( filePath.lastIndexOf( FILE_SEPERATOR ) + 1 );

            UploadProgressListener uploadProgressListener = new UploadProgressListener( fileName, uploadProgress );

            UploadProgressInputStream inputStream = new UploadProgressInputStream( fileItemStream.openStream(),
                    request.getContentLength() );
            inputStream.addListener( uploadProgressListener );

            File file = new File( uploadDirectory, fileName );

            Streams.copy( inputStream, new FileOutputStream( file ), true );

            LOGGER.info( String.format( "uploaded file %s", file.getAbsolutePath() ) );
        }
    }
}