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
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO operations for creating, updating, loading, and removing entities from the database.
 * 
 * @author ptan
 * @version $Id$
 * @param <T>
 */
public abstract class DaoBaseImpl<T> extends HibernateDaoSupport implements DaoBase<T> {

    Log log = LogFactory.getLog( DaoBaseImpl.class );

    // Generic class
    private Class<T> elementClass;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected DaoBaseImpl( Class elementClass ) {
        assert elementClass.isAssignableFrom( elementClass );
        this.elementClass = elementClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#create(java.util.Collection)
     */
    @Override
    @Transactional
    public Collection<? extends T> create( Collection<? extends T> entities ) {

        // this.getHibernateTemplate().saveOrUpdateAll( entities );

        final int BATCH_SIZE = 1024;

        nullCheck( entities );
        if ( entities.isEmpty() ) {
            return entities;
        }
        Session sess = this.getSessionFactory().getCurrentSession();

        int count = 0;
        for ( T e : entities ) {
            sess.save( e );

            if ( count % BATCH_SIZE == 0 ) {
                sess.flush();
                sess.clear();
            }

            count++;
        }

        sess.flush();
        sess.clear();

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

    /**
     * 
     * 
     */
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
        return this.getHibernateTemplate().findByNamedParam(
                "from   " + elementClass.getSimpleName() + " where id in (:ids)", "ids", ids );
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
    public void remove( Collection<? extends T> entities ) {
        nullCheck( entities );
        if ( entities.isEmpty() ) {
            return;
        }
        this.getHibernateTemplate().deleteAll( entities );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.persistence.BaseDao#remove(java.lang.Long)
     */
    @Override
    @Transactional
    public void remove( Long id ) {
        nullCheck( id );
        this.getHibernateTemplate().delete( this.load( id ) );
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
    public void update( Collection<? extends T> entities ) {
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

    /**
     * @param entity
     */
    private void nullCheck( Object entity ) {
        if ( entity == null ) {
            throw new IllegalArgumentException( "Argument cannot be null" );
        }
    }

}
