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
import ubc.pavlab.aspiredb.client.handlers.DrawHeatmapHandler;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * 
 * @author anton
 *
 */
public class DrawHeatmapEvent extends GwtEvent<DrawHeatmapHandler> {
    public static Type<DrawHeatmapHandler> TYPE = new Type<DrawHeatmapHandler>();

    private List<AspireDbFilterConfig> filterConfigs;
    
    public DrawHeatmapEvent() {
    	this.filterConfigs = new ArrayList<AspireDbFilterConfig>();
    }

    public DrawHeatmapEvent( AspireDbFilterConfig filter ) {
    	this();
    	this.filterConfigs.add( filter );
    }

    public DrawHeatmapEvent( Collection<AspireDbFilterConfig> filters ) {
    	this();
    	this.filterConfigs.addAll( filters );
    }
    
    public List<AspireDbFilterConfig> getFilterConfigs() {
		return filterConfigs;
	}

	public void setFilterConfigs(List<AspireDbFilterConfig> filterConfigs) {
		this.filterConfigs = filterConfigs;
	}

	@Override
    public com.google.gwt.event.shared.GwtEvent.Type<DrawHeatmapHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch( DrawHeatmapHandler handler ) {
        handler.onDrawHeatmap( this );
    }

}