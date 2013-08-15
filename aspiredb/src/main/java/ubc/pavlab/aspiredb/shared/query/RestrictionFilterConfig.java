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

import org.directwebremoting.annotations.DataTransferObject;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;

/**
 *
 */
@DataTransferObject
public abstract class RestrictionFilterConfig extends AspireDbFilterConfig {
	private static final long serialVersionUID = -4256615467308247969L;

    private RestrictionExpression restriction;


    protected RestrictionFilterConfig() {
    }

    protected RestrictionFilterConfig(RestrictionExpression restriction) {
        this.restriction = restriction;
    }

    public RestrictionExpression getRestriction() {
        return restriction;
    }

    public void setRestriction(RestrictionExpression restriction) {
        this.restriction = restriction;
    }
}
