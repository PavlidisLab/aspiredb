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

import org.springframework.security.access.annotation.Secured;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

import java.util.Collection;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: VariantDaoBase.java,v 1.11 2013/06/11 22:30:43 anton Exp $
 */
public interface VariantDaoBase<T extends Variant> extends SecurableDaoBase<T>, RemotePaging<T> {

    @Secured({"GROUP_USER" ,"AFTER_ACL_COLLECTION_READ"})
    public Collection<T> findByGenomicLocation(GenomicRange range, Collection<Long> activeProjectIds);

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<T> findBySubjectPatientId( String id );

    public Collection<String> suggestValuesForEntityProperty(Property property, SuggestionContext suggestionContext);

}
