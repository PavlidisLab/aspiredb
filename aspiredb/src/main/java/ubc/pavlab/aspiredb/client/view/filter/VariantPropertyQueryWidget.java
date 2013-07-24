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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.QueryUpdateEvent;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.VariantTypeRestriction;

import java.util.Collection;
import java.util.Iterator;

/**
 * Variant Property Query Widget
 * 
 * @author frances
 * @version $Id: VariantPropertyQueryWidget.java,v 1.12 2013/06/28 18:06:00 frances Exp $
 */
public class VariantPropertyQueryWidget extends ExpandableQueryWidget {
	interface MyUIBinder extends UiBinder<Widget, VariantPropertyQueryWidget> {}
	private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );
	
	private Collection<Property> properties;
	
	@UiField
	CheckBox typeCheckbox;
	
	@UiField
	PushButton addButton;
	
    private VariantType variantType;
    
	public VariantPropertyQueryWidget(String name, Collection<Property> properties) {
		initWidget( uiBinder.createAndBindUi( this ) );

    	this.variantType = VariantType.findByName(name);
    	this.properties = properties;

    	this.typeCheckbox.setText(name);
	}
	
	public boolean isSelected() {
		return this.typeCheckbox.getValue();
	}
	
    /**
	 * @param event  
	 */
    @UiHandler("addButton")
    public void onAddButtonClick (ClickEvent event) {
    	addNewRow();
    }

    /**
	 * @param event
	 */
    @UiHandler("typeCheckbox")
    public void onClick ( ClickEvent event ) {
    	updateComponents();
        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    }

    private void updateComponents() {
    	boolean isTypeChecked = typeCheckbox.getValue();
   		addButton.setVisible(isTypeChecked);
   		this.queriesPanel.setVisible(isTypeChecked);
   		
   		if (this.queriesPanel.isVisible() && this.queriesPanel.getWidgetCount() <= 0) {
   			addNewRow();
   		}
    }
    
	@Override
	public HandlerRegistration addRemoveMeHandler(RemoveMeHandler handler) {
		return this.addHandler( handler, RemoveMeEvent.TYPE );
	}

	@Override
	public RestrictionExpression getRestrictionExpression() {
        Conjunction conjunction = new Conjunction();
        conjunction.add(new VariantTypeRestriction(this.variantType));
        Iterator<Widget> iterator = this.queriesPanel.iterator();
        while ( iterator.hasNext() ) {
            QueryWidget queryWidget = (QueryWidget) iterator.next();
            
            conjunction.add( queryWidget.getRestrictionExpression() );
        }
		return conjunction;
	}

	@Override
	public void setRestrictionExpression(RestrictionExpression restriction) {
		if (restriction instanceof Conjunction) {
			Conjunction conjunction = (Conjunction)restriction;
			for (final RestrictionExpression propertyRestriction: conjunction.getRestrictions()) {
				if (!(propertyRestriction instanceof VariantTypeRestriction)) {
					typeCheckbox.setValue(true);

					QueryWidget queryWidget = addNewRow();	
					queryWidget.setRestrictionExpression(propertyRestriction);
					updateComponents();
				}
			}
		}
	}

	@Override
	public void setRemoveButtonVisible(boolean visible) {
		// Because no remove button is in this widget, there is nothing to do.
	}

	@Override
	protected QueryWidget createQueryWidget() {
		return new PropertyQueryWidget(properties) {
    		@Override
    		public SuggestionComboBox getSuggestionComboBox(Property property) {
    			return new VariantPropertyValueComboBox( property, true );
    		}
    	};
	}
}
