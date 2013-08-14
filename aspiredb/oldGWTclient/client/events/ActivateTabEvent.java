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
import ubc.pavlab.aspiredb.client.handlers.ActivateTabEventHandler;

/**
 * There is probably a better way to change tabs, use event for now
 * 
 * @author cmcdonald
 * 
 */
public class ActivateTabEvent extends GwtEvent<ActivateTabEventHandler> {

    public static Type<ActivateTabEventHandler> TYPE = new Type<ActivateTabEventHandler>();
    
    public static Integer SUBJECT_TAB=2;
    public static Integer VARIANTS_TAB=1;
    
    public Integer tab;

    public ActivateTabEvent(Integer tab) {
        this.tab = tab;

    }

    @Override
    protected void dispatch( ActivateTabEventHandler handler ) {
        handler.onActivateTab( this );
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ActivateTabEventHandler> getAssociatedType() {
        return TYPE;
    }

}
