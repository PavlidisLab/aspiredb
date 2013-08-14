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

import ubc.pavlab.aspiredb.shared.fileuploader.FileEvent;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public final class UploadProgress {

    private static final String SESSION_KEY = "uploadProgress";
    private List<FileEvent> events = new ArrayList<FileEvent>();

    private UploadProgress() {
    }

    public List<FileEvent> getEvents() {

        return events;
    }

    public void add( final FileEvent event ) {
        events.add( event );
    }

    public void clear() {
        events = new ArrayList<FileEvent>();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public static UploadProgress getUploadProgress( final HttpSession session ) {
        Object attribute = session.getAttribute( SESSION_KEY );
        if ( null == attribute ) {
            attribute = new UploadProgress();
            session.setAttribute( SESSION_KEY, attribute );
        }

        return null == attribute ? null : ( UploadProgress ) attribute;
    }
}