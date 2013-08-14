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
package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import ubc.pavlab.aspiredb.client.handlers.SelectSubjectHandler;

/**
 * Event triggered when you want to see a subject's info on the subject tab e.g. on the Variants tab when you click on
 * the magnifying glass beside the subject id in the subject column
 * 
 * @author cmcdonald
 * @version $Id: SelectSubjectEvent.java,v 1.4 2013/06/11 22:30:49 anton Exp $
 */
public class SelectSubjectEvent extends GwtEvent<SelectSubjectHandler> {

    public static Type<SelectSubjectHandler> TYPE = new Type<SelectSubjectHandler>();

    public Long subjectId = null;
    public String externalSubjectId = null;

    public SelectSubjectEvent() {
    }

    public SelectSubjectEvent( Long subjectId, String externalSubjectId ) {
        this.subjectId = subjectId;
        this.externalSubjectId = externalSubjectId;
    }

    @Override
    protected void dispatch( SelectSubjectHandler handler ) {
        handler.onSelectSubject( this );
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<SelectSubjectHandler> getAssociatedType() {
        return TYPE;
    }

}
