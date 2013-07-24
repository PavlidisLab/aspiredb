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

import org.apache.commons.fileupload.ProgressListener;
import ubc.pavlab.aspiredb.shared.fileuploader.UploadProgressChangeEvent;

public final class UploadProgressListener implements ProgressListener {

    private static final double COMPLETE_PERECENTAGE = 100d;
    private int percentage = -1;
    private String fileName;
    private UploadProgress uploadProgress;

    public UploadProgressListener( final String fileName, final UploadProgress uploadProgress ) {
        this.fileName = fileName;
        this.uploadProgress = uploadProgress;
    }

    @Override
    public void update( final long bytesRead, final long totalBytes, final int items ) {
        int percentage = ( int ) Math.floor( ( ( double ) bytesRead / ( double ) totalBytes ) * COMPLETE_PERECENTAGE );

        if ( this.percentage == percentage ) {
            return;
        }

        this.percentage = percentage;

        UploadProgressChangeEvent event = new UploadProgressChangeEvent();
        event.setFilename( this.fileName );
        event.setPercentage( percentage );

        synchronized ( this.uploadProgress ) {
            this.uploadProgress.add( event );
            this.uploadProgress.notifyAll();
        }
    }
}