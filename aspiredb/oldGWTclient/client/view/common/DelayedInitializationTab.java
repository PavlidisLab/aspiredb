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

import com.google.gwt.user.client.ui.Composite;


/**
 * Needed a way to keep track of whether a tab had been initialized with data or not
 * 
 * @author cmcdonald
 * @version $Id: DelayedInitializationTab.java,v 1.1 2013/02/14 18:37:10 anton Exp $
 */
public abstract class DelayedInitializationTab extends Composite {
    
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void markInitialized() {
        this.initialized = true;
    }    
    
    public void initialize() {
        initializeTabData();
        this.initialized = true;
    }
    
    public void clear() {
        clearTabData();
        this.initialized = false;
    }
        
    protected abstract void initializeTabData();  
    protected abstract void clearTabData();  

    
}