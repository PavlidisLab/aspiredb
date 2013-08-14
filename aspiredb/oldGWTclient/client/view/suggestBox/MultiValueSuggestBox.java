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
package ubc.pavlab.aspiredb.client.view.suggestBox;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.QueryUpdateEvent;
import ubc.pavlab.aspiredb.shared.GwtSerializable;
import ubc.pavlab.aspiredb.shared.TextValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SuggestBox that can contain multiple values
 * 
 * @version $Id: MultiValueSuggestBox.java,v 1.12 2013/07/02 18:20:21 anton Exp $
 */
public class MultiValueSuggestBox extends Composite {
	private final String SUGGEST_BOX_ID = DOM.createUniqueId();
	
    private List<String> itemsSelected = new ArrayList<String>();
    private List<ListItem> itemsHighlighted = new ArrayList<ListItem>();
    private List<GwtSerializable> valuesSelected = new ArrayList<GwtSerializable>();
 
	private UnorderedList list;	
	private ComboBox comboBox;
	
	public MultiValueSuggestBox() {
		this(new SimpleComboBox(new LabelProvider() {
			public String getLabel(Object item) {
				return item.toString();
			}
		}));
	}

	public MultiValueSuggestBox(ComboBox box) {
        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        final ListItem item = new ListItem();
        item.setStyleName("multiValueSuggestBox-input-token");

        list = new UnorderedList();
        list.setStyleName("multiValueSuggestBox-list");
        list.add(item);

        panel.add(list);
        panel.getElement().setAttribute("onclick", "document.getElementById('" + SUGGEST_BOX_ID + "').focus()");
		
        box.setHideTrigger( true );
		setComboBox(box);
        box.focus();        
    }
    
	public void setComboBox(ComboBox newComboBox) {
		clear();
		
		ListItem item = (ListItem) list.getWidget(list.getWidgetCount() - 1);
		if (comboBox != null) {
			item.remove(comboBox);
		}
		comboBox = newComboBox;
		comboBox.getCell().getInputElement(this.comboBox.getElement()).setId(SUGGEST_BOX_ID);
	
	    item.add(comboBox);
	    
		
	    // this needs to be on the itemBox rather than box, or backspace will
	    // get executed twice
	    comboBox.addKeyDownHandler(new KeyDownHandler() {
	        // handle key events on the suggest box
	        public void onKeyDown(KeyDownEvent event) {
	            switch (event.getNativeKeyCode()) {
	                case KeyCodes.KEY_ENTER:
	                	if (!comboBox.isForceSelection()) {
	                		String text = comboBox.getText();
	                		if (text != null && !"".equals(text.trim())) {
                                TextValue textValue = new TextValue(text);
		                    	deselectItem(textValue);
	                		}
	                    }
	                    break;
	
	                // handle backspace
	                case KeyCodes.KEY_BACKSPACE:
	                    if (comboBox.getText().trim().isEmpty()) {
	                        if (itemsHighlighted.isEmpty()) {
	                            if (itemsSelected.size() > 0) {
	                                ListItem li = (ListItem) list.getWidget(list.getWidgetCount() - 2);
	                                Paragraph p = (Paragraph) li.getWidget(0);
	                                if (itemsSelected.contains(p.getText())) {
	                                    // remove selected item
	                                	removeSelectedItem(p.getText());
	                                }
	                                list.remove(li);
	                            }
	                        }
	                    }
	                    // continue to delete
	                 
	                // handle delete
	                case KeyCodes.KEY_DELETE:
	                    if (comboBox.getText().trim().isEmpty()) {
	                        for (ListItem li : itemsHighlighted) {
	                            list.remove(li);
	                            Paragraph p = (Paragraph) li.getWidget(0);
	                            removeSelectedItem(p.getText());
	                        }
	                        itemsHighlighted.clear();
	                    }
	                    comboBox.focus();
	                    break;
	            }
	        }
	    });
	
	    comboBox.addSelectionHandler(new SelectionHandler<GwtSerializable>() {
	        // called when an item is selected from list of suggestions
	        public void onSelection(SelectionEvent<GwtSerializable> selectionEvent) {
	            deselectItem(selectionEvent.getSelectedItem());
	        }
	    });
	}
	
	public void clear() {
	    while (itemsSelected.size() > 0) {
	        ListItem li = (ListItem) list.getWidget(list.getWidgetCount() - 2);
	        Paragraph p = (Paragraph) li.getWidget(0);
	        if (itemsSelected.contains(p.getText())) {
	            // remove selected item
	        	removeSelectedItem(p.getText());
	        }
	        list.remove(li);
	    }
	    itemsHighlighted = new ArrayList<ListItem>();	
	}

	public List<GwtSerializable> getValues() {
		return valuesSelected;
	}
	 
	private void removeSelectedItem(String text) {
		int index = itemsSelected.indexOf(text);
		if (index >= 0) {
			valuesSelected.remove(index);
			itemsSelected.remove(index);
            aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
        }
	}

    private void addItem(GwtSerializable item) {
        final Displayable displayable;

        if (item instanceof Displayable) {
            displayable = (Displayable)item;
        } else {
            final String freeText = item.toString();

            displayable = new Displayable() {
                @Override
                public String getLabel() { return freeText;	}

                @Override
                public String getHtmlLabel() { return freeText;	}

                @Override
                public String getTooltip() { return freeText; }
            };
        }

        final ListItem displayItem = new ListItem();
        displayItem.setStyleName("multiValueSuggestBox-token");
        Paragraph p = new Paragraph(displayable.getLabel());

        displayItem.addClickHandler(new ClickHandler() {
            // called when a list item is clicked on
            public void onClick(ClickEvent clickEvent) {
                if (itemsHighlighted.contains(displayItem)) {
                    displayItem.removeStyleDependentName("selected");
                    itemsHighlighted.remove(displayItem);
                }
                else {
                    displayItem.addStyleDependentName("selected");
                    itemsHighlighted.add(displayItem);
                }
            }
        });

        Span span = new Span("x");
        span.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                removeListItem(displayItem);
            }
        });

        // Set tooltip.
        displayItem.setTitle(displayable.getTooltip());

        displayItem.add(p);
        displayItem.add(span);
        // hold the original value of the item selected

        // add selected item
        itemsSelected.add(displayable.getLabel());
        valuesSelected.add(item);

        list.insert(displayItem, list.getWidgetCount() - 1);
    }

	public void addValue(GwtSerializable item) {
        addItem(item);
        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    }
	
	private void deselectItem(GwtSerializable item) {
		addValue(item);

        comboBox.setText("");
        comboBox.focus();
    }
 
    private void removeListItem(ListItem displayItem) {
    	removeSelectedItem(displayItem.getWidget(0).getElement().getInnerHTML());

        list.remove(displayItem);
    }

    public void addValues(Set<? extends GwtSerializable> values) {
        for (GwtSerializable value : values) {
            addItem(value);
        }
        aspiredb.EVENT_BUS.fireEvent(new QueryUpdateEvent());
    }
}