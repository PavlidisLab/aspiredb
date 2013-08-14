/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.Window;
import ubc.pavlab.aspiredb.client.view.filter.SuggestionComboBox;
import ubc.pavlab.aspiredb.shared.LabelValueObject;


public class CreateLabelWindow extends Window {
    interface MyUIBinder extends UiBinder<Widget, CreateLabelWindow> {}
    private static MyUIBinder uiBinder = GWT.create(MyUIBinder.class);

    @UiField(provided = true)
    SuggestionComboBox suggestionComboBox;

    @UiField(provided = true)
    ColorPalette colorPalette;

    public CreateLabelWindow(SuggestionComboBox suggestionComboBox) {
    	this.suggestionComboBox = suggestionComboBox;
        String[] colors = {"FF0000","00FF00","0000FF"};
        String[] labels = {"red","green","blue"};

        colorPalette = new ColorPalette(null, null);

        setWidget(uiBinder.createAndBindUi(this));

    	this.setHeadingText("Make label");
    }

    public LabelValueObject getLabel() {
        final String colour = colorPalette.getValue();

        Object value = this.suggestionComboBox.getSelectedValue();

		final LabelValueObject label;
		if (value instanceof LabelValueObject) {
			label = (LabelValueObject) value;
		} else if (value instanceof String) {
			label = new LabelValueObject((String)value, colour);
		} else {
			label = null;
		}

		return label;
    }
    
    /**
	 * @param event  
	 */
    @UiHandler("okButton")
    protected void onOkButtonClick(ClickEvent event) {
        this.hide();
    }

    /**
	 * @param event  
	 */
    @UiHandler("cancelButton")
    protected void onCancelButtonClick(ClickEvent event) {
        this.hide();
    }
}
