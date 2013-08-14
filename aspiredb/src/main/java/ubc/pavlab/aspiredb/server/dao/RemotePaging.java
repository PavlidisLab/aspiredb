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
import org.springframework.transaction.annotation.Transactional;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import java.util.Collection;
import java.util.Set;

public interface RemotePaging<T> {
    
    @Transactional(readOnly=true)
    @Secured({"GROUP_USER", "AFTER_ACL_COLLECTION_READ"})
    public Page<? extends T> loadPage( int offset, int limit,
                                       String sortProperty, String sortDirection,
                                       Set<AspireDbFilterConfig> filters )
            throws BioMartServiceException, NeurocartaServiceException;

    @Transactional(readOnly=true)
    @Secured({"GROUP_USER" ,"AFTER_ACL_COLLECTION_READ"})
    public Collection<? extends T> load( Set<AspireDbFilterConfig> filters )
            throws BioMartServiceException, NeurocartaServiceException;
        
}
