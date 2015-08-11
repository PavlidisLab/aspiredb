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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.util.GenomeBin;
import ubc.pavlab.aspiredb.shared.GenomicRange;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: GenomicLocationDaoImpl.java,v 1.7 2013/06/11 22:30:46 anton Exp $
 */
@Repository
public class GenomicLocationDaoImpl extends DaoBaseImpl<GenomicLocation> implements GenomicLocationDao {

    @Autowired
    public GenomicLocationDaoImpl( SessionFactory sessionFactory ) {
        super( GenomicLocation.class );
        super.setSessionFactory( sessionFactory );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Long> findByGenomicLocation( GenomicRange range ) {

        Session session = this.getSessionFactory().getCurrentSession();
        List<Integer> bins = GenomeBin.relevantBins( range.getChromosome(), range.getBaseStart(), range.getBaseEnd() );

        String hql = "select id from GenomicLocation location WHERE location.bin in (:bins) and location.chromosome=:chromosome and ((location.start>=:start and location.end<=:end) or (location.start<=:start and location.end>=:start) or (location.start<=:end and location.end>=:end))";

        Query query = session.createQuery( hql );
        query.setParameterList( "bins", bins );
        query.setParameter( "chromosome", range.getChromosome() );
        query.setParameter( "start", range.getBaseStart() );
        query.setParameter( "end", range.getBaseEnd() );
        Collection<Long> ids = query.list();

        return ids;
    }
}
