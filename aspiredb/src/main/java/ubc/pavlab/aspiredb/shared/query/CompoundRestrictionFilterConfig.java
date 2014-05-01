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
@DataTransferObject(javascript = "CompoundRestrictionFilterConfig")
public abstract class CompoundRestrictionFilterConfig extends AspireDbFilterConfig {
	private static final long serialVersionUID = -4256615467308247969L;

    private RestrictionExpression restriction1;
    
    private RestrictionExpression restriction2;
    
    private RestrictionExpression restriction3;

    protected CompoundRestrictionFilterConfig() {
    }

    protected CompoundRestrictionFilterConfig(RestrictionExpression restriction1, RestrictionExpression restriction2) {
        this.setRestriction1( restriction1 );
        this.setRestriction2( restriction2 );
    }

    public RestrictionExpression getRestriction1() {
        return restriction1;
    }

    public void setRestriction1( RestrictionExpression restriction1 ) {
        this.restriction1 = restriction1;
    }

    public RestrictionExpression getRestriction2() {
        return restriction2;
    }

    public void setRestriction2( RestrictionExpression restriction2 ) {
        this.restriction2 = restriction2;
    }

    public RestrictionExpression getRestriction3() {
        return restriction3;
    }

    public void setRestriction3( RestrictionExpression restriction3 ) {
        this.restriction3 = restriction3;
    }
   
}
