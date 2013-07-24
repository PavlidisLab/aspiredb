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
package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import ubc.pavlab.aspiredb.client.handlers.TextHandler;

import java.util.List;

/**
 * Text event
 * 
 * @author frances
 * @version $Id: TextEvent.java,v 1.2 2013/06/11 22:30:49 anton Exp $
 */
public class TextEvent extends GwtEvent<TextHandler> {
    public static Type<TextHandler> TYPE = new Type<TextHandler>();
    
    private List<String> text;

    public TextEvent(List<String> text) {
    	this.text = text;
    }

    @Override
    protected void dispatch( TextHandler handler ) {
        handler.onApplyText( this );
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TextHandler> getAssociatedType() {
        return TYPE;
    }

	public List<String> getText() {
		return this.text;
	}
}