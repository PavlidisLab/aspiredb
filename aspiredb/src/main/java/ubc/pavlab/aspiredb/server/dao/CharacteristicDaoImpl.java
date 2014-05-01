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

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ubc.pavlab.aspiredb.server.model.Characteristic;

@Repository
public class CharacteristicDaoImpl extends SecurableDaoBaseImpl<Characteristic> implements CharacteristicDao {
    @Autowired
    public CharacteristicDaoImpl( SessionFactory sessionFactory ) {
        super( Characteristic.class );
        super.setSessionFactory( sessionFactory );
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
}
