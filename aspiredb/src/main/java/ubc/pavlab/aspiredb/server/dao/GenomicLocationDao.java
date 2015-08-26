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

import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.shared.GenomicRange;

/**
 * DAO operations for the GenomicLocation such as returning the list of IDs given a genomic location.
 * 
 * @version $Id: GenomicLocationDao.java,v 1.4 2013/05/01 17:53:54 anton Exp $
 */
public interface GenomicLocationDao extends DaoBase<GenomicLocation> {

    /**
     * Return the list of GenomicLocation IDs given a genomic location.
     * 
     * @param range
     * @return
     */
    public Collection<Long> findByGenomicLocation( GenomicRange range );

}
