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

import gemma.gsec.model.Securable;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

//this isn't abstract because the security interceptor wasn't working quite right when this class was abstract
//for example calling the remove method didn't remove the appropriate acls
public class SecurableDaoBaseImpl<T extends Securable> extends HibernateDaoSupport implements SecurableDaoBase<T> {

    Log log = LogFactory.getLog( SecurableDaoBaseImpl.class );

    // Generic class
    private Class<T> elementClass;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected SecurableDaoBaseImpl( Class elementClass ) {
        assert elementClass.isAssignableFrom( elementClass );
        this.elementClass = elementClass;
    }

    // Other way we could use to access generic types at runtime in Java (might be useful later)
    /*
     * @SuppressWarnings("unchecked") protected AbstractDao() { Class<?> cl = getClass();
     * 
     * if ( Object.class.getSimpleName().equals( cl.getSuperclass().getSimpleName() ) ) { throw new
     * IllegalArgumentException( "Default constructor does not support direct instantiation" ); }
     * 
     * while ( !AbstractDao.class.getSimpleName().equals( cl.getSuperclass().getSimpleName() ) ) { // case of multiple
     * inheritance, we are trying to get the first available generic info if ( cl.getGenericSuperclass() instanceof
     * ParameterizedType ) { break; } cl = cl.getSuperclass(); }
     * 
     * if ( cl.getGenericSuperclass() instanceof ParameterizedType ) { elementClass = ( Class<T> ) ( ( ParameterizedType
     * ) cl.getGenericSuperclass() ).getActualTypeArguments()[0]; } }
     */

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#create(java.util.Collection)
     */
    @Override
    @Transactional
    public Collection<T> create( Collection<T> entities ) {
        nullCheck( entities );
        if ( entities.isEmpty() ) {
            return entities;
        }
        // this.getHibernateTemplate().saveOrUpdateAll( entities );
        // just save it for faster performance!
        HibernateTemplate template = this.getHibernateTemplate();
        Session session = this.getSession();

        int i = 0;
        for ( T t : entities ) {
            template.saveOrUpdate( t );
            if ( ++i % 10000 == 0 ) {
                session.flush();
                session.clear();
                String percentage = String.format( "%.2f", 100.0 * i / entities.size() );
                log.info( "Created " + i + " (" + percentage + "%) " + t.getClass().getName() + " records ..." );
            }
        }

        return entities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#create(java.lang.Object)
     */
    @Override
    @Transactional
    public T create( T entity ) {
        nullCheck( entity );
        this.getHibernateTemplate().save( entity );
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public long getCountAll() {
        Session session = this.getHibernateTemplate().getSessionFactory().getCurrentSession();
        Number totalSize = ( Number ) session.createCriteria( this.elementClass )
                .setProjection( Projections.rowCount() ).uniqueResult();
        return totalSize.longValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#load(java.util.Collection)
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<T> load( Collection<Long> ids ) {
        if ( ids.isEmpty() ) {
            return new HashSet<T>();
        }
        Collection<T> results = this.getHibernateTemplate().findByNamedParam(
                "from   " + elementClass.getSimpleName() + " where id in (:ids)", "ids", ids );
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#load(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public T load( Long id ) {
        nullCheck( id );
        T entity = this.getHibernateTemplate().get( elementClass, id );
        return entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#loadAll()
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<T> loadAll() {
        return this.getHibernateTemplate().loadAll( elementClass );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#remove(java.util.Collection)
     */
    @Override
    @Transactional
    public void remove( Collection<T> entities ) {
        nullCheck( entities );
        if ( entities.isEmpty() ) {
            return;
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#remove(java.lang.Object)
     */
    @Override
    @Transactional
    public void remove( T entity ) {
        nullCheck( entity );
        this.getHibernateTemplate().delete( entity );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#update(java.util.Collection)
     */
    @Override
    @Transactional
    public void update( Collection<T> entities ) {
        for ( T entity : entities ) {
            this.update( entity );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#update(java.lang.Object)
     */
    @Override
    @Transactional
    public void update( T entity ) {
        nullCheck( entity );
        this.getHibernateTemplate().update( entity );
    }

    protected Session currentSession() {
        return this.getSessionFactory().getCurrentSession();
    }

    /**
     * @param entity
     */
    private void nullCheck( Object entity ) {
        if ( entity == null ) {
            throw new IllegalArgumentException( "Argument cannot be null" );
        }
    }
}
