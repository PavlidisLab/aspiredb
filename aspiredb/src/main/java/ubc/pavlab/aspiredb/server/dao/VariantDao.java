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
import java.util.Map;

import org.springframework.security.access.annotation.Secured;

import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectOverlapFilterConfig;

public interface VariantDao extends VariantDaoBase<Variant>, RemotePaging<Variant> {

    /**
     * Keys for {@link VariantDao#getSubjectVariantIdsByPhenotype(PhenotypeFilterConfig) }
     */
    public static final int SUBJECT_IDS_KEY = 0;
    public static final int VARIANT_IDS_KEY = 1;

    @Secured({ "GROUP_USER", "AFTER_ACL_SUBJECT_ATTRIBUTE_COLLECTION_READ" })
    public Collection<Variant> findByLabel( LabelValueObject label );

    @Secured({ "GROUP_USER", "AFTER_ACL_SUBJECT_ATTRIBUTE_COLLECTION_READ" })
    public List<Variant> findByPhenotype( PhenotypeFilterConfig filterConfig );

    @Secured({ "GROUP_USER", "AFTER_ACL_SUBJECT_ATTRIBUTE_COLLECTION_READ" })
    public Variant findByUserVariantId( String userVariantId, String patientId );

    public List<Long> getProjectOverlapVariantIds( ProjectOverlapFilterConfig overlapFilter );

    public Map<Integer, Collection<Long>> getSubjectVariantIdsByPhenotype( PhenotypeFilterConfig filterConfig );
}
