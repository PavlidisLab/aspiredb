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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ubc.pavlab.aspiredb.server.model.Characteristic;

/**
 * DAO operations for Variant Characteristics.
 * 
 * @author ptan
 * @version $Id$
 */
@Repository
public class CharacteristicDaoImpl extends DaoBaseImpl<Characteristic> implements CharacteristicDao {
    @Autowired
    public CharacteristicDaoImpl( SessionFactory sessionFactory ) {
        super( Characteristic.class );
        super.setSessionFactory( sessionFactory );
    }

    private Session currentSession() {
        return getSession();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getKeysMatching( String partialName ) {
        return currentSession()
                .createQuery( "select distinct c.key from Characteristic as c where c.key like :partialName" )
                .setParameter( "partialName", "%" + partialName + "%" ).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getValuesForKey( String key ) {
        return currentSession().createQuery( "select distinct c.value from Characteristic as c where c.key = :key" )
                .setParameter( "key", key ).list();
    }

    @Override
    public Collection<String> getKeysMatching( String partialName, Long projectId ) {
        return currentSession()
                .createQuery(
                        "select distinct c.key from Subject as s left join s.projects as p left join s.variants as v left join v.characteristics as c WHERE c.key LIKE :partialName AND p.id = :projectId" )
                .setParameter( "partialName", "%" + partialName + "%" ).setParameter( "projectId", projectId ).list();
    }
}
