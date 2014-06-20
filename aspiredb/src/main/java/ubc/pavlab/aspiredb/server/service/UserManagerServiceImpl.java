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
package ubc.pavlab.aspiredb.server.service;

import gemma.gsec.SecurityService;
import gemma.gsec.authentication.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.server.security.authentication.UserService;

/**
 * User Gene Set Service DWR's Created to access the User Gene Set Mysql values for the client side development
 * 
 * @author Gaya Charath
 * @since: 11/03/14
 */
@Service("userManagerService")
@RemoteProxy(name = "UserManagerService")
public class UserManagerServiceImpl implements UserManagerService {

    private static Logger log = LoggerFactory.getLogger( UserGeneSetServiceImpl.class );

    @Autowired(required = false)
    private UserCache userCache = new NullUserCache();

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SecurityService securityService;

    private UserManager userManager;

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#getCurrentUser()
     */
    @Override
    public User getCurrentUser() {
        return ( User ) userManager.getCurrentUser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#getCurrentUsername()
     */
    @Override
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if ( auth == null || !auth.isAuthenticated() ) {
            throw new IllegalStateException( "Not authenticated!" );
        }

        if ( auth.getPrincipal() instanceof UserDetails ) {
            return ( ( UserDetails ) auth.getPrincipal() ).getUsername();
        }
        return auth.getPrincipal().toString();
    }

    /**
     * Create group using gsec
     */
    @Override
    @Transactional
    public String createUserGroup( String groupName ) {

        try {
            securityService.createGroup( groupName );
        } catch ( Exception exception ) {
            return exception.toString();
        }
        return "Success";
    }

    @Override
    @Transactional
    public List<UserGroup> loadUserEditableGroups() {

        Collection<String> usergroups = securityService.getGroupsEditableBy( getCurrentUser() );
        List<UserGroup> UserGroup = new ArrayList<UserGroup>();

        for ( String usergroup : usergroups ) {
            UserGroup.add( ( ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup ) userService
                    .findGroupByName( usergroup ) );
        }
        return UserGroup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#findGroupByName(java.lang.String)
     */
    @Override
    @Transactional
    public UserGroup findGroupByName( String name ) {
        return ( ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup ) this.userService
                .findGroupByName( name );
    }

}
