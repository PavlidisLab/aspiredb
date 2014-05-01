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

package ubc.pavlab.aspiredb.server.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.annotation.Secured;

import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

public interface SubjectDao extends SecurableDaoBase<Subject>, RemotePaging<Subject> {

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Subject findByPatientId( Project p, String patientId );

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Subject findByPatientId( String patientId );
    
    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<Subject> findByPatientIds(Collection<String> patientIds);

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Collection<Subject> findPatients( String queryString );

    public Collection<String> suggestValuesForEntityProperty( Property property, SuggestionContext suggestionContext );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<Subject> findByLabel( LabelValueObject labelEntity );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<Subject> findByPhenotype( PhenotypeFilterConfig filter );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<Subject> loadByVariantIds( List<Long> variantIds );
}
