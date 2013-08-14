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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;

/**
 * Save Query Window
 * 
 * @author frances
 * @version $Id: SaveQueryWindow.java,v 1.2 2013/06/11 21:19:59 frances Exp $
 */
public class SaveQueryWindow extends Window {
    interface MyUIBinder extends UiBinder<Widget, SaveQueryWindow> {}
    private static MyUIBinder uiBinder = GWT.create(MyUIBinder.class);

    @UiField
    TextBox textBox;

    public SaveQueryWindow() {
        setWidget(uiBinder.createAndBindUi( this ));

    	this.setHeadingText("Save query");
    }

    public String getQueryName() {
    	return this.textBox.getText();
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
