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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ubc.pavlab.aspiredb.server.util.FileUploadUtil;
import ubc.pavlab.aspiredb.shared.fileuploader.FileDto;
import ubc.pavlab.aspiredb.shared.fileuploader.FileEvent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileFilter;

//probably don't really need this class, remove later
public final class UploadProgressServlet extends RemoteServiceServlet implements UploadProgressService {

    /**
     * 
     */
    private static final long serialVersionUID = 338195407465112934L;

    private static final int EVENT_WAIT = 30 * 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger( UploadProgressServlet.class );
    private String uploadDirectory;

    @Override
    public void init() throws ServletException {

        uploadDirectory = FileUploadUtil.getUploadPath();
    }

    @Override
    public void initialise() {
        getThreadLocalRequest().getSession( true );
    }

    @Override
    public List<FileDto> readFiles( final int page, final int pageSize ) {

        File[] listFiles = readFiles( this.uploadDirectory );
        sortFiles( listFiles );

        int firstFile = pageSize * ( page - 1 );
        int lastFile = firstFile + pageSize;

        int fileCount = listFiles.length;
        if ( fileCount < lastFile ) {
            lastFile = fileCount;
        }

        if ( firstFile < fileCount ) {
            List<FileDto> files = new ArrayList<FileDto>();

            for ( int i = firstFile; i < lastFile; i++ ) {

                File file = listFiles[i];
                FileDto fileDto = new FileDto();
                fileDto.setFilename( file.getName() );
                fileDto.setDateUploaded( new Date( file.lastModified() ) );
                files.add( fileDto );
            }
            return files;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<FileEvent> getEvents() {

        HttpSession session = getThreadLocalRequest().getSession();
        UploadProgress uploadProgress = UploadProgress.getUploadProgress( session );

        List<FileEvent> events = null;
        if ( null != uploadProgress ) {
            if ( uploadProgress.isEmpty() ) {
                try {
                    synchronized ( uploadProgress ) {
                        LOGGER.debug( "waiting..." );
                        uploadProgress.wait( EVENT_WAIT );
                    }
                } catch ( final InterruptedException ie ) {
                    LOGGER.debug( "interrupted..." );
                }
            }

            synchronized ( uploadProgress ) {
                events = uploadProgress.getEvents();
                uploadProgress.clear();
            }
        }

        return events;
    }

    @Override
    public int countFiles() {
        return readFiles( this.uploadDirectory ).length;
    }

    private File[] readFiles( final String directory ) {
        File uploadDirectory = new File( directory );
        return uploadDirectory.listFiles( new FileFilter() {

            @Override
            public boolean accept( final File file ) {
                return null == file ? false : file.isFile();
            }
        } );
    }

    private void sortFiles( final File[] listFiles ) {
        Arrays.sort( listFiles, new Comparator<File>() {

            @Override
            public int compare( final File f1, final File f2 ) {
                return Long.valueOf( f2.lastModified() ).compareTo( f1.lastModified() );
            }
        } );
    }
}