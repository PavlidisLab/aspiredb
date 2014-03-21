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

import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantOverlap;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

/**
 * TODO Document Me
 * 
 * @author cmcdonald
 * @version $Id:$
 */
public interface Variant2SpecialVariantOverlapDao extends DaoBase<Variant2SpecialVariantOverlap> {
    
    public Collection<Variant2SpecialVariantOverlap> loadByVariantId( Long id, Collection<Long> overlapProjectIds );
    
    public Collection<Variant2SpecialVariantOverlap> loadByVariantIdAndOverlap( Long id, SimpleRestriction overlapRestriction, Collection<Long> overlapProjectIds );

    public Collection<Variant2SpecialVariantOverlap> loadByOverlapProjectId(Long overlapProjectId);
    
    public Collection<Variant2SpecialVariantOverlap> loadByProjectIdAndOverlapProjectId(Long projectId, Long overlapProjectId);
    
    public void deleteByOverlapProjectId( Long id );
    
    public void deleteByOverlapProjectIds( Collection<Long> ids );
}