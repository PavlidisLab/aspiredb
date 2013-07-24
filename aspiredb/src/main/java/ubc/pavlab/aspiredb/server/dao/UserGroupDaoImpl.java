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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.GroupAuthority;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 */
@Repository
public class UserGroupDaoImpl extends DaoBaseImpl<UserGroup> implements UserGroupDao {

    protected final Log log = LogFactory.getLog( getClass() );

    /**
     * @param sessionFactory
     */
    @Autowired
    public UserGroupDaoImpl( SessionFactory sessionFactory ) {
        super(UserGroup.class);
        super.setSessionFactory( sessionFactory );
    }

    
    @Override
    public void addAuthority( UserGroup group, String authority ) {

        for ( GroupAuthority ga : group.getAuthorities() ) {
            if ( ga.getAuthority().equals( authority ) ) {
                log.warn( "Group already has authority " + authority );
                return;
            }
        }

        GroupAuthority ga = new GroupAuthority();
        ga.setAuthority( authority );

        group.getAuthorities().add( ga );

        this.getHibernateTemplate().update( group );

    }

   
    @Override
    public void addToGroup( UserGroup group, User user ) {
        group.getGroupMembers().add( user );
        this.getHibernateTemplate().update( group );
    }

    
    @Override
    public Collection<UserGroup> findGroupsForUser( User user ) {
        return this.getHibernateTemplate().findByNamedParam(
                "select ug from UserGroup ug inner join ug.groupMembers memb where memb = :user", "user", user );
    }

    
    @Override
    public void removeAuthority( UserGroup group, String authority ) {

        for ( Iterator<GroupAuthority> iterator = group.getAuthorities().iterator(); iterator.hasNext(); ) {
            GroupAuthority ga = iterator.next();
            if ( ga.getAuthority().equals( authority ) ) {
                iterator.remove();
            }
        }

        this.getHibernateTemplate().update( group );
    }
    
    @Override
    public UserGroup findByUserGroupName( final java.lang.String name ) {
        return this.findByUserGroupName(
                "from UserGroup as userGroup where userGroup.name = :name",
                name );
    }
    
    public UserGroup findByUserGroupName( final java.lang.String queryString, final java.lang.String name ) {
        java.util.List<String> argNames = new java.util.ArrayList<String>();
        java.util.List<Object> args = new java.util.ArrayList<Object>();
        args.add( name );
        argNames.add( "name" );
        java.util.Set<UserGroup> results = new java.util.LinkedHashSet<UserGroup>( this.getHibernateTemplate()
                .findByNamedParam( queryString, argNames.toArray( new String[argNames.size()] ), args.toArray() ) );
        Object result = null;
        if ( results.size() > 1 ) {
            throw new org.springframework.dao.InvalidDataAccessResourceUsageException(
                    "More than one instance of 'UserGroup"
                            + "' was found when executing query --> '" + queryString + "'" );
        } else if ( results.size() == 1 ) {
            result = results.iterator().next();
        }

        return ( UserGroup ) result;
    }
}