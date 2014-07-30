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
import java.util.HashSet;
import java.util.List;

import jsx3.app.Settings;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import antlr.StringUtils;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.server.security.authentication.UserService;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.query.GeneProperty;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * User Gene Set Service DWR's Created to access the User Gene Set Mysql values for the client side development
 * 
 * @author Gaya Charath
 * @since: 11/03/14
 */
@Service
@RemoteProxy(name = "UserManagerService")
public class UserManagerServiceImpl implements UserManagerService {

    private static Logger log = LoggerFactory.getLogger( UserManagerServiceImpl.class );

    @Autowired(required = false)
    private UserCache userCache = new NullUserCache();

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SecurityService securityService;
    @Autowired
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
    @RemoteMethod   
    public String createUserGroup( String groupName ) {

        try {
            securityService.createGroup( groupName );
        } catch ( Exception exception ) {
            return exception.toString();
        }
        return "Success";
    }
    
    /**
     * Remove user from the group using gsec
     */
    @Override
    @Transactional
    @RemoteMethod   
    public String deleteUserFromGroup( String groupName, String userName ) {

        try {
            securityService.removeUserFromGroup( userName , groupName );
        } catch ( Exception exception ) {
            return exception.toString();
        }
        return "Success";
    }
   
    @Override
    @Transactional
    @RemoteMethod
    public String deleteGroup( String groupName ) {
                        
        if ( !securityService.getGroupsUserCanEdit( getCurrentUsername()).contains( groupName ) ) {
            throw new IllegalArgumentException( "You don't have permission to modify that group" );
        }
        /*
         * Additional checks for ability to delete group handled by ss.
         */
        try {
            userManager.deleteGroup( groupName );
        } catch ( DataIntegrityViolationException div ) {
           // throw div;
            return div.toString();
        } 
        return "Success";
        
    }

    @Override
    @Transactional
    @RemoteMethod   
    public Collection<String> loadUserEditableGroups() {

        Collection<String> usergroups = securityService.getGroupsUserCanEdit( getCurrentUsername() );
        List<UserGroup> UserGroup = new ArrayList<UserGroup>();

        for ( String usergroup : usergroups ) {
            UserGroup.add( ( ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup ) userService.findGroupByName( usergroup ) );
        }
        return usergroups;
    }

    @Override
    @Transactional
    @RemoteMethod   
    public List<String> findGroupMemebers( String groupName ) {
        List<String> members=new ArrayList<String>();
        
        if (groupName == null){
            return members;
        }
        
        members =  userManager.findUsersInGroup( groupName );
        return members;
    }
    
    @Override
    @Transactional
    @RemoteMethod   
    public String addUserToGroup( String groupName, String userName ) {
        
        User userTakingAction = ( User ) userManager.getCurrentUser();

        if ( userTakingAction == null ) {
            throw new IllegalStateException( "Cannot add user to group when user is not logged in" );
        }

        User u;
        if ( userManager.userExists( userName ) ) {
            u = ( User ) userManager.findByUserName( userName );
            if ( !u.getEnabled() ) {
                throw new IllegalArgumentException( "Sorry, that user's account is not enabled." );
            }

            securityService.addUserToGroup( userName, groupName );
        } else if ( userManager.userWithEmailExists( userName ) ) {
            u = ( User ) userManager.findByEmail( userName );
            if ( !u.getEnabled() ) {
                throw new IllegalArgumentException( "Sorry, that user's account is not enabled." );
            }

            String uname = u.getUserName();
            securityService.addUserToGroup( uname, groupName );
        } else {
           // throw new IllegalArgumentException( "Sorry, there is no matching user." );
            return "Sorry, there is no matching user.";
        }

        /*
         * send the user an email.
         */
 /**       String emailAddress = u.getEmail();
        if ( StringUtils.isNotBlank( emailAddress ) ) {
            log.debug( "Sending email notification to " + emailAddress );
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo( emailAddress );
            msg.setFrom( Settings.getAdminEmailAddress() );
            msg.setSubject( "You have been added to a group on Gemma" );

            msg.setText( userTakingAction.getUserName() + " has added you to the group '" + groupName
                    + "'.\nTo view groups you belong to, visit " + GROUP_MANAGER_URL
                    + "\n\nIf you believe you received this email in error, contact " + Settings.getAdminEmailAddress()
                    + "." );

            mailEngine.send( msg );
        }
*/
        return "Success";
      
    }
  
    @Override
    @RemoteMethod   
    public Collection<User> suggestGroupMemebers( SuggestionContext suggestionContext , String groupName){

        Collection<User> users = new ArrayList<User>();

        String query = suggestionContext.getValuePrefix();
        if ( query.length() >= 2 ) {
            final Collection<String> userNames = findGroupMemebers( groupName );          
            
            for ( String username : userNames ) {
                User user = new User();
               // geneProperty.setName( gene.getName() );
              //  geneProperty.setDisplayName( gene.getSymbol() );
               // geneSymbols.add( geneProperty );
            }
        }

        return users;
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
