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
package ubc.pavlab.aspiredb.client.view.filter;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.client.HasRemoveMeHandlers;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.QueryUpdateEvent;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;

/**
 * Expandable Widget
 * 
 * @author frances
 * @version $Id: ExpandableWidget.java,v 1.3 2013/06/17 21:09:26 anton Exp $
 */
public abstract class ExpandableWidget extends Composite implements HasRemoveMeHandlers {
    @UiField
    VerticalPanel queriesPanel;

    protected abstract QueryWidget createQueryWidget();
	
	public QueryWidget addNewRow() {
		QueryWidget queryWidget = createQueryWidget();
        queryWidget.addRemoveMeHandler( new RemoveMeHandler() {
        	@Override
        	public void onRemoveMe( RemoveMeEvent event ) {
        		queriesPanel.remove( event.getWidget() );
        		
    			int widgetCount = queriesPanel.getWidgetCount();
    			if (widgetCount == 1) {
    				QueryWidget firstQuerywidget = (QueryWidget) queriesPanel.getWidget(0);
    				firstQuerywidget.setRemoveButtonVisible(false);
    			}
                aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
            }
        });
	  
        queriesPanel.add( (Widget) queryWidget );

    	int widgetCount = queriesPanel.getWidgetCount();
    	if (widgetCount > 1) {
    		for (int i = 0; i < widgetCount; i++) {
    			QueryWidget currQuerywidget = (QueryWidget)queriesPanel.getWidget(i);
    			currQuerywidget.setRemoveButtonVisible(true);
    		}
    	}
    	
    	return queryWidget;
	}
}
