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
import ubc.pavlab.aspiredb.client.handlers.SubjectFilterHandler;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Event triggered by searching in the subject grid.
 * 
 * @author Paul
 * @version $Id: SubjectFilterEvent.java,v 1.5 2013/06/11 22:30:49 anton Exp $
 */
public class SubjectFilterEvent extends GwtEvent<SubjectFilterHandler> {
    public static Type<SubjectFilterHandler> TYPE = new Type<SubjectFilterHandler>();

    private List<AspireDbFilterConfig> filterConfigs;
    
    public SubjectFilterEvent() {
    	filterConfigs = new ArrayList<AspireDbFilterConfig>();
    }

    public SubjectFilterEvent( AspireDbFilterConfig filter ) {
    	this();
    	this.filterConfigs.add( filter );
    }

    public SubjectFilterEvent( Collection<? extends AspireDbFilterConfig> filters ) {
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
    public com.google.gwt.event.shared.GwtEvent.Type<SubjectFilterHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch( SubjectFilterHandler handler ) {
        handler.onFilter( this );
    }

}
