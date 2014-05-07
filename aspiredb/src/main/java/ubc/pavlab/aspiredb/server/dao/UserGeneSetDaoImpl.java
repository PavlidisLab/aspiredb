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

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.UserGeneSet;

/**
 * User Gene Set Dao Implementation to access the User gene Set model
 * 
 * @author: Gaya Charath
 * @since: 11/03/14
 */
@Repository
public class UserGeneSetDaoImpl extends SecurableDaoBaseImpl<UserGeneSet> implements UserGeneSetDao {

    @Autowired
    public UserGeneSetDaoImpl( SessionFactory sessionFactory ) {
        super( UserGeneSet.class );
        super.setSessionFactory( sessionFactory );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGeneSet> findByName( String geneSetName ) {
        Criteria criteria = currentSession().createCriteria( UserGeneSet.class );
        criteria.add( Restrictions.eq( "name", geneSetName ) );
        return criteria.list();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserGeneSet> suggestGeneSetNames( String query ) {
        Criteria criteria = currentSession().createCriteria( UserGeneSet.class );
        criteria.add( Restrictions.ilike( "name", query ) );
        return criteria.list();
    }
    
    
}
