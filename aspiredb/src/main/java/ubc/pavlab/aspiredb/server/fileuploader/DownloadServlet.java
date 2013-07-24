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

import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ubc.pavlab.aspiredb.server.util.FileUploadUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

public final class DownloadServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger( DownloadServlet.class );
    private String uploadDirectory;

    @Override
    public void init() throws ServletException {
        uploadDirectory = FileUploadUtil.getUploadPath();
    }

    @Override
    protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
            throws ServletException, IOException {
        /*
         * downloadFile(request, response);
         */
    }

    private void downloadFile( final HttpServletRequest request, final HttpServletResponse response )
            throws IOException {
        String fileName = ( String ) request.getParameter( "file" );
        fileName = URLDecoder.decode( fileName );

        boolean invalidFileName = null == fileName || fileName.isEmpty() || fileName.contains( "\\" )
                || fileName.contains( "/" ) || fileName.contains( ".." );

        if ( invalidFileName ) {
            throw new IOException( String.format( "error downloading file %s", fileName ) );
        }

        ServletOutputStream outputStream = response.getOutputStream();
        ServletContext context = getServletConfig().getServletContext();
        String mimetype = context.getMimeType( fileName );

        File file = new File( uploadDirectory, fileName );
        response.setContentType( ( mimetype != null ) ? mimetype : "application/octet-stream" );
        response.setContentLength( ( int ) file.length() );
        response.setHeader( "Content-Disposition", String.format( "attachment; filename=\"%s\"", fileName ) );

        Streams.copy( new FileInputStream( file ), outputStream, true );

        LOGGER.info( String.format( "downloaded file %s", file.getAbsolutePath() ) );
    }
}