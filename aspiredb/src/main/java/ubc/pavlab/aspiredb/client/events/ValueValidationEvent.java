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
package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;

import ubc.pavlab.aspiredb.client.handlers.ValueValidationHandler;
import ubc.pavlab.aspiredb.shared.GwtSerializable;

/**
 * Value validation event
 * 
 * @author frances
 * @version $Id: ValueValidationEvent.java,v 1.1 2013/06/27 19:49:55 frances Exp $
 */
public class ValueValidationEvent extends GwtEvent<ValueValidationHandler> {
    public static Type<ValueValidationHandler> TYPE = new Type<ValueValidationHandler>();
    
    private GwtSerializable value;

    public ValueValidationEvent(GwtSerializable value) {
    	this.value = value;
    }

    @Override
    protected void dispatch( ValueValidationHandler handler ) {
        handler.onValueValidation( this );
    }

    @Override
    public Type<ValueValidationHandler> getAssociatedType() {
        return TYPE;
    }

	public GwtSerializable getValue() {
		return this.value;
	}
}
