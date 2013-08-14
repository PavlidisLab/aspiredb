package ubc.pavlab.aspiredb.client.view.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.events.TextEvent;
import ubc.pavlab.aspiredb.client.handlers.TextHandler;
import ubc.pavlab.aspiredb.client.service.QueryService;
import ubc.pavlab.aspiredb.client.service.QueryServiceAsync;
import ubc.pavlab.aspiredb.client.view.common.EditTextWindow;
import ubc.pavlab.aspiredb.client.view.suggestBox.Displayable;
import ubc.pavlab.aspiredb.shared.GwtSerializable;
import ubc.pavlab.aspiredb.shared.query.*;

import java.util.*;

public class VariantLocationPropertyQueryWidget extends PropertyQueryWidget {
    private static final String EDIT_FILTER_WINDOW_TITLE = "Enter list";

	private final QueryServiceAsync queryService = GWT.create( QueryService.class );
	
	private EditTextWindow editFilterWindow;

    public VariantLocationPropertyQueryWidget(Collection<Property> properties) {
    	super(properties);

    	Image openEditWindowImage = new Image("page_upload.png");
    	openEditWindowImage.setStyleName("gwt-Hyperlink");
    	
    	String tooltip = EDIT_FILTER_WINDOW_TITLE + " ...";
		openEditWindowImage.setAltText(tooltip);	
		openEditWindowImage.setTitle(tooltip);
		
		openEditWindowImage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
		    	if (editFilterWindow == null) {
		    		editFilterWindow = new EditTextWindow();
		    		editFilterWindow.setHeadingText(EDIT_FILTER_WINDOW_TITLE);

			    	editFilterWindow.addTextHandler( new TextHandler() {
			 			@Override
			 			public void onApplyText(TextEvent filterTextEvent) {
			 				final List<String> filterText = filterTextEvent.getText();

			 		       AsyncCallback<List<GwtSerializable>> callback = new AsyncCallback<List<GwtSerializable>>() {
			 		            @Override
			 		            public void onFailure( Throwable caught ) {}

			 		            @Override
			 		            public void onSuccess( List<GwtSerializable> verifiedValues ) {
			 		            	processVerifiedValues(verifiedValues, filterText);	            	
			 		            }
			 		        };

			 		        queryService.getVariantLocationValueObjects( propertyComboBox.getValue(), filterText, new AspireAsyncCallback<List<GwtSerializable>>(callback) );
			 		    }        	
			         });
		    	}
		    	
		    	List<GwtSerializable> values = multiValueSuggestBox.getValues();
		    	List<String> editFilterWindowText = new ArrayList<String>(values.size()); 
				
				for (Object value: values) {
					final String text;
					if (value instanceof Displayable) {
						text = ((Displayable)value).getLabel();
					} else {
						text = value.toString();
					}
		    		editFilterWindowText.add(text);    		
		    	}
		    	
		    	editFilterWindow.showWindowWithText(editFilterWindowText);
			}
		});

		int index = this.widgetContainer.getWidgetIndex(this.removeImage);
		this.widgetContainer.insert(openEditWindowImage, index);
    }

//    @Override
//    public RestrictionExpression getRestrictionExpression() {
//        Property restrictionProperty = propertyComboBox.getValue();
//        Operator restrictionOperator = operatorComboBox.getValue();
//
//        Set<GwtSerializable> values = new HashSet<GwtSerializable>();
//
//        if (this.multiValueSuggestBox.isVisible()) {
//            List<GwtSerializable> restrictionValues = this.multiValueSuggestBox.getValues();
//            for (GwtSerializable restrictionValue: restrictionValues) {
//                values.add( restrictionValue );
//            }
//        } else if (this.textBox.isVisible()) {
//            String query = this.textBox.getText();
//
//            GwtSerializable restrictionValue = restrictionProperty instanceof NumericProperty
//                    ? new NumericValue(new Integer(query))
//                    : new TextValue(query);
//
//            values.add( restrictionValue );
//        }
//
//        SetRestriction setRestriction = new SetRestriction(restrictionProperty, restrictionOperator, values);
//
//        return setRestriction;
//    }

//    @Override
//    public void setRestrictionExpression(RestrictionExpression restrictionTree) {
//        Disjunction disjunction = (Disjunction) restrictionTree;
//
//        boolean isFirstTime = true;
//
//        for (RestrictionExpression restriction: disjunction.getRestrictions()) {
//            SimpleRestriction simpleRestriction = (SimpleRestriction)restriction;
//            Property restrictionProperty = simpleRestriction.getProperty();
//
//            if (isFirstTime) {
//                updateComponentsWithSelectedProperty(restrictionProperty);
//                propertyComboBox.setValue(restrictionProperty);
//                operatorComboBox.setValue(simpleRestriction.getOperator());
//
//                isFirstTime = false;
//            }
//
//            GwtSerializable value = simpleRestriction.getValue();
//
//            if (this.multiValueSuggestBox.isVisible()) {
//                this.multiValueSuggestBox.addValue(value);
//            } else if (this.textBox.isVisible()) {
//                this.textBox.setText(value.toString());
//            }
//        }
//    }

    @Override
	public SuggestionComboBox getSuggestionComboBox(Property property) {
		SuggestionComboBox comboBox = new VariantLocationPropertyValueComboBox( property, true );
		int minChars = 1;
		
		if (property instanceof GeneProperty) {
			minChars = 2;
        } else if (property instanceof NeurocartaPhenotypeProperty) {
        	minChars = 3;
        } 
		comboBox.setMinChars(minChars);

		return comboBox;
	}

	public void setRemoveButtonVisible(boolean visible) {
		removeImage.setVisible(visible);
	}
	
    @UiHandler("propertyComboBox")
    public void onSelection(SelectionEvent<Property> event) {
    	super.onSelection(event);

    	if (editFilterWindow != null) {
    		editFilterWindow.clear();
    	}
    }

	private void processVerifiedValues(List<GwtSerializable> verifiedValues, List<String> filterText) {
		multiValueSuggestBox.clear();
    	
    	List<Integer> invalidFilterIndicies = new ArrayList<Integer>(); 
    	
    	for (int i = 0; i < verifiedValues.size(); i++) {
    		final GwtSerializable verifiedValue = verifiedValues.get(i);

    		if (verifiedValue == null) {
    			invalidFilterIndicies.add(i);
    		} else {
    			multiValueSuggestBox.addValue(verifiedValue);
    		}
    	}

		if (invalidFilterIndicies.size() > 0) {
			String errorMessage = "The following text cannot be parsed:" ;
			
			for (Integer index: invalidFilterIndicies) {
				errorMessage += "<br />" + filterText.get(index);
			}
			
	        MessageBox messageBox = new MessageBox( "Text not parsed", errorMessage);
	        messageBox.show();
    	}
    	editFilterWindow.setInvalidLineIndicies(invalidFilterIndicies);        
    }
}