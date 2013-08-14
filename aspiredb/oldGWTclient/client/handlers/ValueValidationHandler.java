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
package ubc.pavlab.aspiredb.client.handlers;

import com.google.gwt.event.shared.EventHandler;

import ubc.pavlab.aspiredb.client.events.ValueValidationEvent;

/**
 * Value validation handler
 * 
 * @author frances
 * @version $Id: ValueValidationHandler.java,v 1.1 2013/06/27 19:49:56 frances Exp $
 */
public interface ValueValidationHandler extends EventHandler {
    public void onValueValidation(ValueValidationEvent event);
}
