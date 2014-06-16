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
package ubc.pavlab.aspiredb.server.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.annotation.Secured;

import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

/**
 * author: anton date: 25/04/13
 */
public interface LabelDao extends SecurableDaoBase<Label> {

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    Label findOrCreate( LabelValueObject labelVO );

    @Deprecated
    List<Label> getLabelsMatching( String partialName );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    Collection<Label> getSubjectLabels();

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    Collection<Label> getSubjectLabelsByProjectId( Long projectId );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    Collection<Label> getSubjectLabelsBySubjectId( Long id );

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    Collection<Label> getVariantLabels();

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    Collection<Label> getVariantLabelsByVariantId( Long id );
}
