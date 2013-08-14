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
import ubc.pavlab.aspiredb.client.handlers.VariantFilterHandler;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Document Me
 * 
 * @author Paul
 * @version $Id: VariantFilterEvent.java,v 1.3 2013/06/11 22:30:49 anton Exp $
 */
public class VariantFilterEvent extends GwtEvent<VariantFilterHandler> {

    public static Type<VariantFilterHandler> TYPE = new Type<VariantFilterHandler>();
    
    private List<AspireDbFilterConfig> filterConfigs;
    
    public VariantFilterEvent() {
    	filterConfigs = new ArrayList<AspireDbFilterConfig>();
    }

    public VariantFilterEvent( AspireDbFilterConfig filter ) {
    	this();
    	filterConfigs.add( filter );
    }

    public VariantFilterEvent( List<AspireDbFilterConfig> filterConfigs ) {
    	this();
    	this.filterConfigs.addAll( filterConfigs );
    }

    public List<AspireDbFilterConfig> getFilterConfigs() {
		return filterConfigs;
	}

	public void setFilterConfigs(List<AspireDbFilterConfig> filterConfigs) {
		this.filterConfigs = filterConfigs;
	}

	@Override
    public com.google.gwt.event.shared.GwtEvent.Type<VariantFilterHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch( VariantFilterHandler handler ) {
        handler.onFilter( this );
    }

}
