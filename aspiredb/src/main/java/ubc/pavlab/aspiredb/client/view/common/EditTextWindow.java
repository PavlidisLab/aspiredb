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
package ubc.pavlab.aspiredb.client.view.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import ubc.pavlab.aspiredb.client.events.TextEvent;
import ubc.pavlab.aspiredb.client.handlers.TextHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit text window
 * 
 * @author frances
 * @version $Id: EditTextWindow.java,v 1.3 2013/06/11 22:30:39 anton Exp $
 */
public class EditTextWindow extends Window {
    interface MyUIBinder extends UiBinder<Widget, EditTextWindow> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );
   
    @UiField(provided = true)
    BorderLayoutData southData = new BorderLayoutData(30);

    @UiField
    RichTextArea richTextArea;
    
    @UiField
    Button okButton;   
            
    @UiField
    Button clearButton;

    @UiField
    Button cancelButton;

	private List<Integer> invalidLineIndices = new ArrayList<Integer>(0);
	private String originalHtml = "";

    public EditTextWindow() {
    	this.setModal(true);
    	
    	// It should NOT be closable. Otherwise, the close button should do the same thing as what the cancel button does.
    	this.setClosable(false);  

    	setWidget( uiBinder.createAndBindUi( this ) );
    }
     
	private static String getErrorHtml(String text) {
		return "<font color=\"red\">" + text + "</font>";
	}
	private static String getDivHtml(String text) {
		return "<div>" + text + "</div>";
	}

	private static List<String> parseTextLines(String html) {
		// Assume each line's text is between startTag and endTag. 
		final String startTag = "<div>";
		final String endTag = "</div>";

		String[] splittedLines = html.split(startTag);
		
		List<String> textLines = new ArrayList<String>(); 
		
		for (String splittedLine: splittedLines) {
			int endIndex = splittedLine.indexOf(endTag);

			String strippedLine = endIndex >= 0 ? splittedLine.substring(0, endIndex) : splittedLine;

			String text = new HTML(strippedLine).getText();
			if (text.equals("")) {
				continue;
			}

			textLines.add(text);

			// If we type "1<Enter>2" and then go to the very beginning of the textarea and type "a:y", we will 
			// get this html "<div>a:y</div>1<div>2</div>". So, the following code will add "1" to textLines. 
			if (endIndex > 0 && endIndex + endTag.length() < splittedLine.length()) {
				int nextStartIndex = endIndex + endTag.length();
				
				String nextText = new HTML(splittedLine.substring(nextStartIndex, splittedLine.length())).getText();
				if (nextText.equals("")) {
					continue;
				}
				
				textLines.add(nextText);
			}
		}
		
		return textLines;
	}
	
	public void clear() {
		this.richTextArea.setHTML("");
	}
	
	public void setInvalidLineIndicies(List<Integer> invalidLineIndices) {
		this.invalidLineIndices = invalidLineIndices;
	}
	
	public void showWindowWithText(List<String> incomingText) {
		String newHtml = "";
		String[] textLines = parseTextLines(this.richTextArea.getHTML()).toArray((new String[0]));
		
		List<Integer> newInvalidLineIndices = new ArrayList<Integer>();
	
		int textLineIndex = 0;
		int newIndex = 0;
	
		for (String currIncomingText: incomingText) {
			if (currIncomingText == null) {
				continue;
			}
			
			boolean currIncomingTextFound = false;
			String matchedHtml = "";
	
			while (textLineIndex < textLines.length) {
				String currTextLine = textLines[textLineIndex];
				
				if (this.invalidLineIndices.contains(textLineIndex)) {
					newInvalidLineIndices.add(newIndex);				
					matchedHtml += getDivHtml(getErrorHtml(currTextLine));
					newIndex++;
				} else if (currTextLine.equalsIgnoreCase(currIncomingText)) {
					currIncomingTextFound = true;
					matchedHtml += getDivHtml(currIncomingText);
					newIndex++;
					textLineIndex++;
					break;
				}
				textLineIndex++;
			}
			
			newHtml += matchedHtml;
			
			if (!currIncomingTextFound) {
				newHtml += getDivHtml(currIncomingText);
				newIndex++;
			}
		}
		
		while (textLineIndex < textLines.length) {
			if (this.invalidLineIndices.contains(textLineIndex)) {
				newInvalidLineIndices.add(newIndex);
				newHtml += getDivHtml(getErrorHtml(textLines[textLineIndex]));
				newIndex++;
			}
			textLineIndex++;
		}
		
		this.invalidLineIndices = newInvalidLineIndices;
		
		originalHtml = newHtml;
		
		this.richTextArea.setHTML(newHtml);
		this.show();
	}

    /**
	 * @param event  
	 */
    @UiHandler("okButton")
    public void onOkButtonClick (ClickEvent event) {
    	this.invalidLineIndices = new ArrayList<Integer>(0);
    	this.fireEvent(new TextEvent(parseTextLines(richTextArea.getHTML())));
    	this.hide();
    }    

    /**
	 * @param event  
	 */
    @UiHandler("clearButton")
    public void onClearButtonClick (ClickEvent event) {
    	richTextArea.setHTML("");
    }    

    /**
	 * @param event  
	 */
    @UiHandler("cancelButton")
    public void onCancelButtonClick (ClickEvent event) {
    	this.richTextArea.setHTML(originalHtml);    	
    	this.hide();
    }    

    public HandlerRegistration addTextHandler( TextHandler handler ) {
        return this.addHandler( handler, TextEvent.TYPE );
    }
}
