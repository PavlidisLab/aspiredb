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

import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;

import java.util.Collection;


public class PhenotypeFilterConfig extends RestrictionFilterConfig {
	private static final long serialVersionUID = -6273822469846069494L;

    private Collection<PhenotypeRestriction> phenotypeRestrictions;

	public PhenotypeFilterConfig() {}

    public PhenotypeFilterConfig(Collection<PhenotypeRestriction> restrictions) {
        this.phenotypeRestrictions = restrictions;
    }

    public Collection<PhenotypeRestriction> getRestrictions() {
        return this.phenotypeRestrictions;
    }
}
