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
package ubc.pavlab.aspiredb.shared;

import java.util.ArrayList;
import java.util.List;

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.server.util.ConfigUtils;

/**
 * Sets an upper bound to the number of items to returned.
 * 
 * @author anton
 */
@DataTransferObject
public class BoundedList<T> {
    private static int MAX_SIZE = ConfigUtils.getInt( "aspiredb.view.maxRecords", 50000 );
    private List<T> items = new ArrayList<T>();
    private boolean moreResultsAvailable = false;
    private int totalSize;

    public BoundedList( List<T> items ) {
        this.items.addAll( items.subList( 0, Math.min( MAX_SIZE, items.size() ) ) );
        if ( items.size() > MAX_SIZE ) {
            moreResultsAvailable = true;
        }
        this.totalSize = items.size();
    }

    public List<T> getItems() {
        return items;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public boolean isMoreResultsAvailable() {
        return moreResultsAvailable;
    }

}
