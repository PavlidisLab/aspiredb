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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.QueryUpdateEvent;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;
import ubc.pavlab.aspiredb.shared.query.RestrictionFilterConfig;

public abstract class FilterWidget extends ExpandableWidget {
	interface MyUIBinder extends UiBinder<Widget, FilterWidget> {}
	private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

	@UiField
	VerticalPanel widgetContainer;
    
	@UiField
	HTML titleHTML;

	@UiField
	HTML queriesPanelTitleHTML;

    @UiField
    Image removeFilterImage;

    protected abstract String getWidgetTitle();
    protected abstract String getWidgetBackgroundColor();

    public abstract RestrictionFilterConfig getFilterConfig();
    public abstract void setFilterConfig(RestrictionFilterConfig filterConfig);
    
    
    public FilterWidget() {
        initWidget( uiBinder.createAndBindUi( this ) );
    	this.titleHTML.setHTML("<b>" + getWidgetTitle() + "</b>");
        this.getElement().getStyle().setBackgroundColor(getWidgetBackgroundColor());
    }

    /**
	 * @param event
	 */
    @UiHandler("removeFilterImage")
    public void onClick ( ClickEvent event ) {
    	this.fireEvent( new RemoveMeEvent( this ) );
    }
    
    /**
	 * @param event  
	 */
    @UiHandler("addButton")
    public void onAddButtonClick (ClickEvent event) {
    	addNewRow();
    }

	@Override
	public HandlerRegistration addRemoveMeHandler(RemoveMeHandler handler) {
		return this.addHandler( handler, RemoveMeEvent.TYPE );
	}
}
