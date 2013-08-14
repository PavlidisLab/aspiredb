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

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.cell.core.client.LabelProviderSafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.DelayedTask;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.*;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.event.BeforeQueryEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RefreshEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import java.util.Collection;

import ubc.pavlab.aspiredb.client.events.ValueValidationEvent;
import ubc.pavlab.aspiredb.client.handlers.ValueValidationHandler;
import ubc.pavlab.aspiredb.client.view.suggestBox.Displayable;
import ubc.pavlab.aspiredb.shared.GwtSerializable;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;

/**
 * Suggestion ComboBox
 * 
 * @author frances
 * @version $Id: SuggestionComboBox.java,v 1.6 2013/07/04 23:54:19 anton Exp $
 */
public class SuggestionComboBox<T> extends ComboBox<T> {
	private T selectedValue;	
	private String currentText = "";

	private static class SuggestionComboBoxCell<T> extends ComboBoxCell<T> {
		public SuggestionComboBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, final SafeHtmlRenderer<T> renderer) {
			super(store, labelProvider, renderer);
		}

		public String getLastQuery() {
			return this.lastQuery;
		}
	}

	public SuggestionComboBox(ModelKeyProvider<T> modelKeyProvider,	LabelProvider<T> labelProvider) {
		super(new SuggestionComboBoxCell<T>(new ListStore<T>(modelKeyProvider), labelProvider,
				new LabelProviderSafeHtmlRenderer<T>(labelProvider)));

		initializeComboBox();
	}
	
	public SuggestionComboBox(ModelKeyProvider<T> modelKeyProvider, LabelProvider<T> labelProvider,
			AbstractSafeHtmlRenderer<T> renderer) {
		super(new SuggestionComboBoxCell<T>(new ListStore<T>(modelKeyProvider), labelProvider, renderer));

		initializeComboBox();
	}
	private void initializeComboBox() {
		this.addBeforeQueryHandler(new BeforeQueryEvent.BeforeQueryHandler<T>() {
			@Override
			public void onBeforeQuery(BeforeQueryEvent<T> event) {
				SuggestionComboBox<T> comboBox = (SuggestionComboBox<T>) event.getSource();

				if (event.getQuery().length() >= comboBox.getMinChars()) {
					comboBox.expand();

					if (!event.getQuery().equals(comboBox.getLastQuery())) {
						comboBox.getListView().mask("Searching...");
					}
				} else {
					comboBox.getListView().unmask();
					comboBox.collapse();
				}
			}
		});

        this.getListView().addRefreshHandler( new RefreshEvent.RefreshHandler() {
            @Override
            public void onRefresh(RefreshEvent event) {
            	// Make the scrollbar appear after refresh.
            	SuggestionComboBox.this.getListView().unmask();
            }
        });

        this.addSelectionHandler(new SelectionHandler<T>() {
            // called when an item is selected from list of suggestions
            public void onSelection(SelectionEvent<T> selectionEvent) {	
            	selectedValue = selectionEvent.getSelectedItem();         	
            }
        });
		
		this.setQueryDelay( 500 );
		this.setHideTrigger( true );
		this.setTriggerAction(TriggerAction.QUERY);
		this.setMinListWidth(500);
	}
	
	private String getLastQuery() {
		ComboBoxCell<T> comboBoxCell = this.getCell();
		
		final String lastQuery;
		
		if (comboBoxCell instanceof SuggestionComboBoxCell) {
			lastQuery = ((SuggestionComboBoxCell<T>) comboBoxCell).getLastQuery(); 
		} else {
			lastQuery = null;
		}
		
		return lastQuery;
	}

    @Override
    // Fix GXT ComboBox's bug [extgwt-1932]. Code idea is from
    // http://www.sencha.com/forum/showthread.php?196281-GXT-3-rc2-ComboBox-setForceSelection(false)-does-not-work/page2
    protected void onBlur(Event be) {
        currentText = this.getText();

        if (!isForceSelection() && this.selectedValue == null) {
            this.getStore().add( (T) currentText);
            selectedValue = (T) currentText;
        }

//    	if ((!isForceSelection()) && this.selectedValue == null && (!currentText.equals(""))) {
//        	// Assume that if combo box does not force selection, T is either String or Object.
//        	selectedValue = (T) currentText;
//    	}
        
        super.onBlur(be);
    }
    
    public String getCurrentText() {
        return currentText;
    }

	public void setDataProxy(DataProxy<PagingLoadConfig, PagingLoadResult<T>> dataProxy) {
		Loader<PagingLoadConfig, PagingLoadResult<T>> loader = new PagingLoader<PagingLoadConfig, PagingLoadResult<T>>(dataProxy);
		loader.addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, T, PagingLoadResult<T>>(this.getStore()));
		this.setLoader(loader);
	}

	public T getSelectedValue() {
		return this.selectedValue;
	}

    public HandlerRegistration addValueValidationHandler( ValueValidationHandler handler ) {
        return this.addHandler( handler, ValueValidationEvent.TYPE );
    }
    
    protected void validateOnBlur(String textToBeValidated, Collection<PropertyValue> propertyValues) {
        GwtSerializable matchedValue = null;

        for (PropertyValue propertyValue : propertyValues) {
            final String testString;
            if (propertyValue.getValue() instanceof Displayable) {
                testString = ((Displayable)propertyValue.getValue()).getLabel();
            } else {
                testString = propertyValue.toString();                      
            }
            if (textToBeValidated.equalsIgnoreCase(testString)) {
                matchedValue = propertyValue.getValue();
                break;
            }
        }
                
        if (matchedValue == null) {
            final Dialog errorDialog = new Dialog();
            errorDialog.setModal( true );
            errorDialog.setHeadingText("Value not valid");
            errorDialog.add(new HTML("The value <b>" + textToBeValidated + "</b> is not valid."));
            errorDialog.setHideOnButtonClick(true);
            errorDialog.show();
            this.setText( textToBeValidated );
            final SuggestionComboBox thisComboBox = this;
            
            errorDialog.addHideHandler(new HideHandler() {
                @Override
                public void onHide( HideEvent event ) {
                    new DelayedTask() {
                        @Override
                        public void onExecute() {
                            thisComboBox.focus();
                        }
                    }.delay( 500 );
                }
            });
        } else {
            this.fireEvent(new ValueValidationEvent( matchedValue ));
        }
    }
}
