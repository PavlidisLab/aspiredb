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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class UploadProgressInputStream extends FilterInputStream {

    private List<ProgressListener> listeners;
    private long bytesRead = 0;
    private long totalBytes = 0;

    public UploadProgressInputStream( final InputStream in, final long totalBytes ) {
        super( in );

        this.totalBytes = totalBytes;

        listeners = new ArrayList<ProgressListener>();
    }

    public void addListener( final ProgressListener listener ) {
        listeners.add( listener );
    }

    @Override
    public int read() throws IOException {
        int b = super.read();

        this.bytesRead++;

        updateListeners( bytesRead, totalBytes );

        return b;
    }

    @Override
    public int read( final byte b[] ) throws IOException {
        return read( b, 0, b.length );
    }

    @Override
    public int read( final byte b[], final int off, final int len ) throws IOException {
        int bytesRead = in.read( b, off, len );

        this.bytesRead = this.bytesRead + bytesRead;

        updateListeners( this.bytesRead, totalBytes );

        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        super.close();

        updateListeners( totalBytes, totalBytes );
    }

    private void updateListeners( final long bytesRead, final long totalBytes ) {

        for ( ProgressListener listener : listeners ) {

            listener.update( bytesRead, totalBytes, listeners.size() );
        }
    }
}