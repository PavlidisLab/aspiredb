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
package ubc.pavlab.aspiredb.client.handlers;

import com.google.gwt.event.shared.EventHandler;
import ubc.pavlab.aspiredb.client.events.SubjectFilterEvent;

/**
 * Handles button event for searching for subjects.
 * 
 * @author Paul
 * @version $Id: SubjectFilterHandler.java,v 1.2 2013/06/11 22:30:54 anton Exp $
 */
public interface SubjectFilterHandler extends EventHandler {

    /**
     * @param event
     */
    void onFilter( SubjectFilterEvent event );
}
