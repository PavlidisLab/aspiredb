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
package ubc.pavlab.aspiredb.shared.query.restriction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Superclass of Disjunction (OR-expression) and Conjunction (AND-expression).
 */
public abstract class Junction implements RestrictionExpression {
    protected List<RestrictionExpression> restrictions = new ArrayList<RestrictionExpression>();

    public Collection<RestrictionExpression> getRestrictions() {
        return this.restrictions;
    }

    public void add(RestrictionExpression restriction) {
        this.restrictions.add( restriction );
    }

    public void addAll(Collection<PhenotypeRestriction> restrictions) {
        this.restrictions.addAll( restrictions );
    }

    public void replaceAll(Collection<RestrictionExpression> restrictions) {
        this.restrictions.clear();
        this.restrictions.addAll( restrictions );
    }
}
