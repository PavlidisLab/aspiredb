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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;
import ubc.pavlab.aspiredb.shared.query.restriction.Disjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;

import java.util.Iterator;

/**
 * Container Query Widget
 * 
 * @author frances
 * @version $Id: ContainerQueryWidget.java,v 1.2 2013/06/11 22:30:41 anton Exp $
 */
public abstract class ContainerQueryWidget extends ExpandableQueryWidget{
    interface MyUIBinder extends UiBinder<Widget, ContainerQueryWidget> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );
    
	@UiField
	Image removeImage;

	public ContainerQueryWidget() {
    	initWidget( uiBinder.createAndBindUi( this ) );
    	
    	this.setPixelSize(750, 22);
    }
        
    /**
	 * @param event  
	 */
    @UiHandler("removeImage")
    public void onRemoveButtonClick (ClickEvent event) {
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

	@Override
	public RestrictionExpression getRestrictionExpression() {
        Disjunction restrictionExpression = new Disjunction();

        Iterator<Widget> iterator = this.queriesPanel.iterator();
        while ( iterator.hasNext() ) {
            QueryWidget queryWidget = (QueryWidget) iterator.next();
            restrictionExpression.add( queryWidget.getRestrictionExpression() );
        }

        return restrictionExpression;
	}

	@Override
	public void setRestrictionExpression(RestrictionExpression restriction) {
		if (restriction instanceof Disjunction) {
			Disjunction disjunction = (Disjunction)restriction;
			for (final RestrictionExpression locationRestriction: disjunction.getRestrictions()) {
				QueryWidget queryWidget = addNewRow();
				queryWidget.setRestrictionExpression(locationRestriction);
			}
		}
	}

	public void setRemoveButtonVisible(boolean visible) {
		removeImage.setVisible(visible);
	}
}
