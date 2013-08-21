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
package ubc.pavlab.aspiredb.client.view.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.QueryUpdateEvent;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;
import ubc.pavlab.aspiredb.client.events.ValueValidationEvent;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;
import ubc.pavlab.aspiredb.client.handlers.ValueValidationHandler;
import ubc.pavlab.aspiredb.client.view.suggestBox.MultiValueSuggestBox;
import ubc.pavlab.aspiredb.shared.GwtSerializable;
import ubc.pavlab.aspiredb.shared.NumericValue;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.query.*;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.SetRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

/**
 * Property Query Widget
 * 
 * @author frances
 * @version $Id: PropertyQueryWidget.java,v 1.16 2013/07/02 20:19:47 anton Exp $
 */
public abstract class PropertyQueryWidget extends NonExpandableQueryWidget {
    interface MyUIBinder extends UiBinder<Widget, PropertyQueryWidget> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @Deprecated
    @UiField
    HorizontalPanel widgetContainer;
    
    @UiField(provided=true)
    ComboBox<Property> propertyComboBox;

    @UiField(provided=true)
    ComboBox<Operator> operatorComboBox;
    
    @UiField
    TextBox textBox;
    
	@UiField
	MultiValueSuggestBox multiValueSuggestBox;

	@UiField
    HTML exampleValuesHTML;

	@UiField
	Image removeImage;

    public abstract SuggestionComboBox getSuggestionComboBox(Property property);
    
    private static ComboBox constructComboBox(ListStore listStore, LabelProvider labelProvider) {
    	ComboBox comboBox = new ComboBox(listStore, labelProvider);
    	
    	comboBox.setEditable(false);
    	comboBox.setForceSelection(true);
    	comboBox.setTriggerAction(TriggerAction.ALL);
    	
    	return comboBox;
    }
    
    public PropertyQueryWidget(Collection<Property> properties) {
    	ListStore<Property> propertyListStore = new ListStore<Property>(new ModelKeyProvider<Property>() {
    		public String getKey(Property property) { return property.getName(); }
    	}); 	
    	propertyComboBox = constructComboBox(propertyListStore, new LabelProvider<Property>() {
    		public String getLabel(Property property) { return property.getDisplayName(); }
    	});
    	
    	ListStore<Operator> operatorListStore = new ListStore<Operator>(new ModelKeyProvider<Operator>() {
    		public String getKey(Operator operator) { return operator.getDisplayLabel(); } 
    	}); 	
    	operatorComboBox = constructComboBox(operatorListStore, new LabelProvider<Operator>() {
    		public String getLabel(Operator operator) { return operator.getDisplayLabel(); }
    	});
    	
    	initWidget( uiBinder.createAndBindUi( this ) );
		
		if (properties.size() > 0) {
			propertyListStore.addAll(properties);
			
            Property firstProperty = properties.iterator().next();
            propertyComboBox.setValue( firstProperty );
            updateComponentsWithSelectedProperty( firstProperty );
		}

        textBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                aspiredb.EVENT_BUS.fireEvent( new QueryUpdateEvent() );
            }
        });
    }
    
    /**
	 * @param event
	 */
    @UiHandler("removeImage")
    public void onRemoveButtonClick (ClickEvent event) {
    	this.fireEvent( new RemoveMeEvent( this ) );
    }

    /**
     *
     * @param handler
     * @return
     */
    @Override
	public HandlerRegistration addRemoveMeHandler(RemoveMeHandler handler) {
		return this.addHandler( handler, RemoveMeEvent.TYPE );
	}

    /**
	 * @param event  
	 */
    @UiHandler("propertyComboBox")
    public void onSelection(SelectionEvent<Property> event) {
    	Property property = event.getSelectedItem();
    	updateComponentsWithSelectedProperty(property);
    }
    
    private void updateComponentsWithSelectedProperty(Property property) {
		Collection<Operator> operators = property.getOperators();
		ListStore<Operator> operatorListStore = operatorComboBox.getStore();
		
		operatorListStore.clear();
		
		if (operators.size() > 0) {
			operatorListStore.addAll(operators);

			// Select the first operator.
			operatorComboBox.setValue(operators.iterator().next());
		}
		
		DataType dataType = property.getDataType();
		
		boolean acceptMultiValues = dataType instanceof TextDataType || dataType instanceof GenomicRangeDataType;
		multiValueSuggestBox.setVisible(acceptMultiValues);
		textBox.setVisible(!acceptMultiValues);

		final SuggestionComboBox comboBox = getSuggestionComboBox(property);
		
		this.multiValueSuggestBox.setComboBox(comboBox);
		
		comboBox.addValueValidationHandler( new ValueValidationHandler() {
		    @Override
		    public void onValueValidation(ValueValidationEvent event) {
		        multiValueSuggestBox.addValue( event.getValue() );
		        comboBox.setText( "" );
		    }
		} );
		
		this.exampleValuesHTML.setHTML( property.getExampleValues() );
    }

	@Override
	public RestrictionExpression getRestrictionExpression() {

	    Property restrictionProperty = propertyComboBox.getValue();
	    Operator restrictionOperator = operatorComboBox.getValue();
	    
		if (this.multiValueSuggestBox.isVisible()) {

            List<GwtSerializable> valueList = this.multiValueSuggestBox.getValues();
            Set<GwtSerializable> values = new HashSet<GwtSerializable>(valueList);

            SetRestriction setRestriction = new SetRestriction(restrictionProperty, restrictionOperator, values);
            return setRestriction;
		} else if (this.textBox.isVisible()) {
			String query = this.textBox.getText();

            GwtSerializable restrictionValue = restrictionProperty instanceof NumericProperty
					? new NumericValue(new Integer(query))
					: new TextValue(query);
			
	        SimpleRestriction simpleRestriction = new SimpleRestriction(
                    restrictionProperty, restrictionOperator, restrictionValue);
            return simpleRestriction;
        } else {
            throw new IllegalArgumentException();
        }
	}

	@Override
	public void setRestrictionExpression(RestrictionExpression restrictionTree) {
        if ( restrictionTree instanceof SimpleRestriction ) {
            SimpleRestriction simpleRestriction = ( SimpleRestriction ) restrictionTree;

            Property restrictionProperty = simpleRestriction.getProperty();
            updateComponentsWithSelectedProperty( restrictionProperty );

            propertyComboBox.setValue( restrictionProperty );
            operatorComboBox.setValue( simpleRestriction.getOperator() );

            GwtSerializable value = simpleRestriction.getValue();
            this.textBox.setText( value.toString() );

        } else if (restrictionTree instanceof SetRestriction) {
            SetRestriction setRestriction = ( SetRestriction ) restrictionTree;

            Property restrictionProperty = setRestriction.getProperty();
            updateComponentsWithSelectedProperty( restrictionProperty );

            propertyComboBox.setValue( restrictionProperty );
            operatorComboBox.setValue( setRestriction.getOperator() );

            final Set<? extends GwtSerializable> values = setRestriction.getValues();
            this.multiValueSuggestBox.addValues( values );

        } else {
            throw new IllegalArgumentException();
        }
	}

	@Override
	public void setRemoveButtonVisible(boolean visible) {
		removeImage.setVisible(visible);
	}
}
