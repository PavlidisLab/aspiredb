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
package ubc.pavlab.aspiredb.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import ubc.pavlab.aspiredb.client.view.AspireDbPanel;

/**
 * Example code for a GwtQuery application FIXME is this used? What does it mean that it is "example code"?
 */
public class aspiredb implements EntryPoint {

    public static EventBus EVENT_BUS = GWT.create( SimpleEventBus.class );

    @Override
    public void onModuleLoad() {

        RootLayoutPanel.get().add( new AspireDbPanel() );

        // after finished loading AspireDb stuff, remove the loading icon
        DOM.removeChild( RootPanel.getBodyElement(), DOM.getElementById( "loading" ) );

    }

}
