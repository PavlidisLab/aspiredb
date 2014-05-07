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

import java.util.List;

import org.springframework.security.access.annotation.Secured;

import ubc.pavlab.aspiredb.server.model.UserGeneSet;
import ubc.pavlab.aspiredb.shared.GeneSetValueObject;

/**
 * User gene Set interface to access the User Gene Set model
 * 
 * @author: Gaya Charath
 * @since: 11/03/14
 */
public interface UserGeneSetDao extends SecurableDaoBase<UserGeneSet> {

    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public List<UserGeneSet> findByName( String geneSetName );
    
    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public List<UserGeneSet> suggestGeneSetNames( String query );

}
