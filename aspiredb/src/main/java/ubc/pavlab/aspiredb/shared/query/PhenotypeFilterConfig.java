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
package ubc.pavlab.aspiredb.shared.query;

import java.util.ArrayList;
import java.util.Collection;

import org.directwebremoting.annotations.DataTransferObject;

@DataTransferObject(javascript = "PhenotypeFilterConfig")
public class PhenotypeFilterConfig extends RestrictionFilterConfig {
    private static final long serialVersionUID = -6273822469846069494L;

    public PhenotypeFilterConfig() {
    }

    // its possible we could use the project filter config for this, just putting it here to get it out of the
    // queryvariants method signature
    Collection<Long> activeProjectIds = new ArrayList<Long>();

    public Collection<Long> getActiveProjectIds() {
        return activeProjectIds;
    }

    public void setActiveProjectIds( Collection<Long> activeProjectIds ) {
        this.activeProjectIds = activeProjectIds;
    }

}
