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
package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import ubc.pavlab.aspiredb.client.handlers.GenomeRegionSelectionHandler;
import ubc.pavlab.aspiredb.shared.GenomicRange;

/**
 * author: anton
 * date: 24/02/13
 */
public class GenomeRegionSelectionEvent extends GwtEvent<GenomeRegionSelectionHandler> {
    public static Type<GenomeRegionSelectionHandler> TYPE = new Type<GenomeRegionSelectionHandler>();

    private GenomicRange range;

    public GenomeRegionSelectionEvent(GenomicRange range) {
        this.range = range;
    }

    public GenomicRange getRange() {
        return range;
    }

    @Override
    public Type<GenomeRegionSelectionHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(GenomeRegionSelectionHandler handler) {
        handler.onGenomeRangeSelection( this );
    }
}
