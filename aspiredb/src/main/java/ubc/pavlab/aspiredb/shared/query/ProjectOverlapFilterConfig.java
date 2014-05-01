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

import java.util.Collection;

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;

@DataTransferObject(javascript = "ProjectOverlapFilterConfig")
public class ProjectOverlapFilterConfig extends CompoundRestrictionFilterConfig {

    /**
     * 
     */
    private static final long serialVersionUID = 6091780632280417499L;
    // the 'active projects'
    private Collection<Long> projectIds;
    // the projects that you want to search for overlap
    private Collection<Long> overlapProjectIds;

    public Boolean invert;

    private PhenotypeRestriction phenotypeRestriction;

    public ProjectOverlapFilterConfig() {
    }

    public ProjectOverlapFilterConfig( Collection<Long> specialProjects ) {
        this.projectIds = specialProjects;
    }

    public Collection<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds( Collection<Long> projectIds ) {
        this.projectIds = projectIds;
    }

    public Collection<Long> getOverlapProjectIds() {
        return overlapProjectIds;
    }

    public void setOverlapProjectIds( Collection<Long> overlapProjectIds ) {
        this.overlapProjectIds = overlapProjectIds;
    }

    public PhenotypeRestriction getPhenotypeRestriction() {
        return phenotypeRestriction;
    }

    public void setPhenotypeRestriction( PhenotypeRestriction phenotypeRestriction ) {
        this.phenotypeRestriction = phenotypeRestriction;
    }

    public Boolean getInvert() {
        return invert;
    }

    public void setInvert( Boolean invert ) {
        this.invert = invert;
    }

}
