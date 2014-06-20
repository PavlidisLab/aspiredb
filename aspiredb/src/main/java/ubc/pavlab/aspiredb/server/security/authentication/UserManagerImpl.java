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

package ubc.pavlab.aspiredb.server.security.authentication;

import gemma.gsec.SecurityService;
import gemma.gsec.authentication.UserDetailsImpl;
import gemma.gsec.authentication.UserExistsException;
import gemma.gsec.authentication.UserManager;
import gemma.gsec.model.GroupAuthority;
import gemma.gsec.model.User;
import gemma.gsec.model.UserGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.util.AuthorityConstants;

/**
 * Implementation for Spring Security, plus some other handy methods.
 * 
 * @author pavlidis
 * @version $Id: UserManagerImpl.java,v 1.6 2013/06/11 23:02:56 cmcdonald Exp $
 */
@Service("userManager")
public class UserManagerImpl implements UserManager {

    /**
     * Name of the default user group (not to be confused with the group authority GROUP_USER).
     */
    private static final String USER_GROUP_NAME = "Users";

    protected final Log logger = LogFactory.getLog( getClass() );

    /**
     * 
     */
    private boolean enableAuthorities = false;

    /**
     * 
     */
    private boolean enableGroups = true;

    /**
     * 
     */
    private String rolePrefix = "GROUP_";

    @Autowired(required = false)
    private UserCache userCache = new NullUserCache();

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#addGroupAuthority(java.lang.String,
     * org.springframework.security.core.GrantedAuthority)
     */
    @Override
    @Transactional
    public void addGroupAuthority( String groupName, GrantedAuthority authority ) {
        UserGroup g = loadGroup( groupName );

        for ( gemma.gsec.model.GroupAuthority ga : g.getAuthorities() ) {
            if ( ga.getAuthority().equals( authority.getAuthority() ) ) {
                logger.warn( "Group already has authority" + authority.getAuthority() );
                return;
            }
        }

        GroupAuthority auth = new ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.GroupAuthority();
        auth.setAuthority( authority.getAuthority() );

        g.getAuthorities().add( auth );

        userService.update( g );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#addUserToGroup(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional
    public void addUserToGroup( String username, String groupName ) {
        User u = loadUser( username );
        UserGroup g = loadGroup( groupName );
        userService.addUserToGroup( g, u );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#changePassword(java.lang.String, java.lang.String)
     */
    @Override
    @Secured({ "GROUP_USER" })
    @Transactional
    public void changePassword( String oldPassword, String newPassword ) throws AuthenticationException {
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if ( currentAuthentication == null ) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException( "Can't change password as no Authentication object found in context "
                    + "for current user." );
        }

        String username = currentAuthentication.getName();

        // reauthenticate( username, oldPassword );

        logger.debug( "Changing password for user '" + username + "'" );

        User u = loadUser( username );
        u.setPassword( newPassword );
        userService.update( u );

        SecurityContextHolder.getContext().setAuthentication(
                createNewAuthentication( currentAuthentication, newPassword ) );

        userCache.removeUserFromCache( username );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#changePasswordForUser(java.lang.String, java.lang.String)
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "RUN_AS_ADMIN" })
    @Transactional
    public String changePasswordForUser( String email, String username, String newPassword )
            throws AuthenticationException {
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if ( currentAuthentication == null ) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException( "Can't change password as no Authentication object found in context "
                    + "for current user." );
        }

        User u = userService.findByEmail( email );

        if ( u == null ) {
            throw new UsernameNotFoundException( "No user found for that email address." );
        }

        String foundUsername = u.getUserName();

        if ( !foundUsername.equals( username ) ) {
            throw new AccessDeniedException( "The wrong user name was provided for the email address." );
        }

        logger.debug( "Changing password for user '" + username + "'" );

        u.setPassword( newPassword );
        u.setEnabled( false );
        u.setSignupToken( generateSignupToken( username ) );
        u.setSignupTokenDatestamp( new Date() );
        userService.update( u );

        userCache.removeUserFromCache( username );

        return u.getSignupToken();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#createGroup(java.lang.String, java.util.List)
     */
    @Override
    @Transactional
    public void createGroup( String groupName, List<GrantedAuthority> authorities ) {

        UserGroup g = new ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup();
        g.setName( groupName );
        for ( GrantedAuthority ga : authorities ) {
            GroupAuthority groupAuthority = new ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.GroupAuthority();
            groupAuthority.setAuthority( ga.getAuthority() );
            g.getAuthorities().add( groupAuthority );
        }

        userService.create( g );

    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.security.authentication.UserManagerI#createUser(org.springframework.security.core.userdetails.UserDetails
     * )
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "RUN_AS_ADMIN" })
    @Transactional
    public void createUser( UserDetails user ) {

        /*
         * UserDetails is not an entity, so this method is not directly managed by the Audit or ACL advice. However, it
         * runs in a transaction and calls two service methods which are intercepted. This means it is intercepted
         * before the transaction is flushed.
         */

        validateUserName( user.getUsername() );

        User u = new ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User();
        u.setUserName( user.getUsername() );
        u.setPassword( user.getPassword() );
        u.setEnabled( user.isEnabled() );

        if ( user instanceof UserDetailsImpl ) {
            u.setSignupToken( ( ( UserDetailsImpl ) user ).getSignupToken() );
            u.setSignupTokenDatestamp( ( ( UserDetailsImpl ) user ).getSignupTokenDatestamp() );
        }

        if ( user instanceof UserDetailsImpl ) {
            u.setEmail( ( ( UserDetailsImpl ) user ).getEmail() );
        }

        try {
            u = userService.create( u );
        } catch ( UserExistsException e ) {
            throw new RuntimeException( e );
        }

        // Add the user to the default user group.
        UserGroup g = loadGroup( USER_GROUP_NAME );
        userService.addUserToGroup( g, u );

        /*
         * We don't log the user in automatically, because we require that new users click a confirmation link in an
         * email.
         */
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#deleteGroup(java.lang.String)
     */
    @Override
    @Transactional
    public void deleteGroup( String groupName ) {
        UserGroup group = loadGroup( groupName );
        userService.delete( group );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#deleteUser(java.lang.String)
     */
    @Override
    @Transactional
    public void deleteUser( String username ) {

        User user = loadUser( username );

        userService.delete( user );
        userCache.removeUserFromCache( username );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#findAllGroups()
     */
    @Override
    public List<String> findAllGroups() {
        Collection<UserGroup> groups = userService.listAvailableGroups();

        List<String> result = new ArrayList<String>();
        for ( UserGroup group : groups ) {
            result.add( group.getName() );
        }
        return result;

    }

    @Override
    public Collection<String> findAllUsers() {
        Collection<User> users = userService.loadAll();

        List<String> result = new ArrayList<String>();
        for ( User u : users ) {
            result.add( u.getUserName() );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#findbyEmail(java.lang.String)
     */
    @Override
    @Secured({ "GROUP_USER", "RUN_AS_ADMIN" })
    public User findbyEmail( String emailAddress ) {
        return findByEmail( emailAddress );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#findbyEmail(java.lang.String)
     */
    @Override
    @Secured({ "GROUP_USER", "RUN_AS_ADMIN" })
    public User findByEmail( String emailAddress ) {
        return userService.findByEmail( emailAddress );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#findByUserName(java.lang.String)
     */
    @Override
    public User findByUserName( String userName ) {
        return this.userService.findByUserName( userName );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#findGroupAuthorities(java.lang.String)
     */
    @Override
    public List<GrantedAuthority> findGroupAuthorities( String groupName ) {

        String groupToSearch = groupName;
        if ( groupName.startsWith( rolePrefix ) ) {
            groupToSearch = groupToSearch.replaceFirst( rolePrefix, "" );
        }

        UserGroup group = loadGroup( groupToSearch );

        List<GrantedAuthority> result = new ArrayList<GrantedAuthority>();
        for ( gemma.gsec.model.GroupAuthority ga : group.getAuthorities() ) {
            result.add( new GrantedAuthorityImpl( ga.getAuthority() ) );
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#findGroupByName(java.lang.String)
     */
    @Override
    @Transactional
    public UserGroup findGroupByName( String name ) {
        return this.userService.findGroupByName( name );
    }

    /**
     * @Override
     * @Transactional public UserGroup createUserGroup( String groupName ) { UserGroup group =
     *                this.userService.findGroupByName( groupName ); String returnString =""; if (
     *                userService.findGroupByName( groupName )!= null ) { returnString = "Group already exists"; }else {
     *                userService.create( group ); userService.addGroupAuthority( group, "ADMIN"); } return group; }
     */

    /**
     * Exception err = processCommandLine( "Project ", args ); authenticate( applicationContext ); if ( err != null ) {
     * return err; } if ( createUser ) { if ( aspireUserPassword == null || aspireUserName == null ) { log.error(
     * "missing -aspireuser or -aspireuserpassword options" ); bail( AbstractCLI.ErrorCode.MISSING_OPTION ); } try {
     * userManager.loadUserByUsername( aspireUserName ); log.error( "User name already exists" ); bail(
     * AbstractCLI.ErrorCode.INVALID_OPTION ); } catch ( UsernameNotFoundException e ) { String encodedPassword =
     * passwordEncoder.encodePassword( aspireUserPassword, aspireUserName ); UserDetailsImpl u = new UserDetailsImpl(
     * encodedPassword, aspireUserName, true, null, null, null, new Date() ); userManager.createUser( u ); } } else if (
     * deleteUser ) { if ( aspireUserName == null ) { log.error( "missing -aspireuser or -aspireuserpassword options" );
     * bail( AbstractCLI.ErrorCode.MISSING_OPTION ); } try { userManager.loadUserByUsername( aspireUserName );
     * userManager.deleteByUserName( aspireUserName ); } catch ( UsernameNotFoundException e ) { log.error(
     * "User name doesn't exist" ); bail( AbstractCLI.ErrorCode.INVALID_OPTION ); } } else if ( changePassword ) { if (
     * aspireUserName == null || aspireUserPassword == null ) { log.error( "missing -aspireuser or -aspireuserpassword"
     * ); bail( AbstractCLI.ErrorCode.INVALID_OPTION ); } try { String encodedPassword = passwordEncoder.encodePassword(
     * aspireUserPassword, aspireUserName ); userManager.changePasswordForUser( aspireUserName, encodedPassword ); }
     * catch ( UsernameNotFoundException e ) { log.error( "user does not exist" ); bail(
     * AbstractCLI.ErrorCode.INVALID_OPTION ); } } else if ( createGroup ) { if ( groupName == null ) { log.error(
     * "missing  -groupname option" ); bail( AbstractCLI.ErrorCode.MISSING_OPTION ); } if ( userManager.groupExists(
     * groupName ) ) { log.error( "Group already exists" ); bail( AbstractCLI.ErrorCode.INVALID_OPTION ); }
     * List<GrantedAuthority> authos = new ArrayList<GrantedAuthority>(); authos.add( new GrantedAuthorityImpl(
     * groupName ) ); userManager.createGroup( groupName, authos ); } else if ( deleteGroup ) { if ( groupName == null )
     * { log.error( "missing  -groupname option" ); bail( AbstractCLI.ErrorCode.MISSING_OPTION ); } if (
     * !userManager.groupExists( groupName ) ) { log.error( "Group does not exist" ); bail(
     * AbstractCLI.ErrorCode.INVALID_OPTION ); } userManager.deleteGroup( groupName ); } else if ( assignUserToGroup ) {
     * if ( aspireUserName == null || groupName == null ) { log.error( "missing -aspireuser or -groupname options" );
     * bail( AbstractCLI.ErrorCode.MISSING_OPTION ); } if ( !userManager.groupExists( groupName ) ) { log.error(
     * "Group does not exist, create group first with -creategroup option" ); bail( AbstractCLI.ErrorCode.INVALID_OPTION
     * ); } try { userManager.loadUserByUsername( aspireUserName ); } catch ( UsernameNotFoundException e ) { log.error(
     * "Username does not exist, create user first with -createuser option" ); bail(
     * AbstractCLI.ErrorCode.INVALID_OPTION ); } userManager.addUserToGroup( aspireUserName, groupName ); }
     */

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#findGroupsForUser(java.lang.String)
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "RUN_AS_USER" })
    @Transactional
    public Collection<String> findGroupsForUser( String userName ) {

        Collection<String> result = new HashSet<String>();

        if ( !loggedIn() ) {
            return result;
        }

        User u = loadUser( userName );
        Collection<UserGroup> groups = userService.findGroupsForUser( u );

        for ( UserGroup g : groups ) {
            result.add( g.getName() );
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#findUsersInGroup(java.lang.String)
     */
    @Override
    @Transactional
    public List<String> findUsersInGroup( String groupName ) {

        UserGroup group = loadGroup( groupName );

        Collection<User> groupMembers = group.getGroupMembers();

        List<String> result = new ArrayList<String>();
        for ( User u : groupMembers ) {
            result.add( u.getUserName() );
        }
        return result;

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#generateSignupToken(java.lang.String)
     */
    @Override
    public String generateSignupToken( String username ) {
        return RandomStringUtils.randomAlphanumeric( 32 ).toUpperCase();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#getCurrentUser()
     */
    @Override
    public User getCurrentUser() {
        return getUserForUserName( getCurrentUsername() );
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

    @Override
    public String getRolePrefix() {
        return rolePrefix;
    }

    @Override
    public boolean groupExists( String groupName ) {
        return userService.groupExists( groupName );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#isEnableAuthorities()
     */
    public boolean isEnableAuthorities() {
        return enableAuthorities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#isEnableGroups()
     */
    public boolean isEnableGroups() {
        return enableGroups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#loadAll()
     */
    @Override
    public Collection<User> loadAll() {
        return this.userService.loadAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException {
        List<UserDetails> users = loadUsersByUsername( username );

        if ( users.size() == 0 ) {
            throw new UsernameNotFoundException( "Username " + username + " not found" );
        }

        UserDetails user = users.get( 0 ); // contains no GrantedAuthority[]

        Set<GrantedAuthority> dbAuthsSet = new HashSet<GrantedAuthority>();

        if ( enableAuthorities ) {
            dbAuthsSet.addAll( loadUserAuthorities( user.getUsername() ) );
        }

        if ( enableGroups ) {
            dbAuthsSet.addAll( loadGroupAuthorities( user.getUsername() ) );
        }

        List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>( dbAuthsSet );

        // addCustomAuthorities( user.getUsername(), dbAuths );

        if ( dbAuths.size() == 0 ) {
            throw new UsernameNotFoundException( "User " + username + " has no GrantedAuthority" );
        }

        return createUserDetails( username, ( UserDetailsImpl ) user, dbAuths );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#loggedIn()
     */
    @Override
    public boolean loggedIn() {

        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        // fixme: use a AuthenticationTrustResolver instead.
        return !( currentUser instanceof AnonymousAuthenticationToken );

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#reauthenticate(java.lang.String, java.lang.String)
     */
    @Override
    public void reauthenticate( String username, String password ) {
        // If an authentication manager has been set, re-authenticate the user with the supplied password.
        /*
         * if ( authenticationManager != null ) { logger.debug( "Reauthenticating user '" + username +
         * "' for password change request." );
         * 
         * authenticationManager.authenticate( new UsernamePasswordAuthenticationToken( username, password ) ); } else {
         * logger.debug( "No authentication manager set. Password won't be re-checked." ); }
         */
     // Warning: Autowiring AuthenticationManager here can cause a circular reference error
        logger.warn( "No authentication manager set. Password won't be re-checked." );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#removeGroupAuthority(java.lang.String,
     * org.springframework.security.core.GrantedAuthority)
     */
    @Override
    @Transactional
    public void removeGroupAuthority( String groupName, GrantedAuthority authority ) {

        UserGroup group = loadGroup( groupName );

        userService.removeGroupAuthority( group, authority.getAuthority() );

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#removeUserFromGroup(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional
    public void removeUserFromGroup( String username, String groupName ) {

        User user = userService.findByUserName( username );
        UserGroup group = userService.findGroupByName( groupName );

        if ( user == null || group == null ) {
            throw new IllegalArgumentException( "User or group could not be read" );
        }

        userService.removeUserFromGroup( user, group );

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#renameGroup(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional
    public void renameGroup( String oldName, String newName ) {

        UserGroup group = userService.findGroupByName( oldName );

        group.setName( newName );

        userService.update( group );

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#setEnableAuthorities(boolean)
     */
    public void setEnableAuthorities( boolean enableAuthorities ) {
        this.enableAuthorities = enableAuthorities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#setEnableGroups(boolean)
     */
    public void setEnableGroups( boolean enableGroups ) {
        this.enableGroups = enableGroups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#setRolePrefix(java.lang.String)
     */
    public void setRolePrefix( String rolePrefix ) {
        this.rolePrefix = rolePrefix;
    }

    /**
     * @param userCache the userCache to set
     */
    public void setUserCache( UserCache userCache ) {
        this.userCache = userCache;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService( UserService userService ) {
        this.userService = userService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.security.authentication.UserManager#updateUser(org.springframework.security.core.userdetails.UserDetails
     * )
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "RUN_AS_ADMIN" })
    public void updateUser( UserDetails user ) {
        String username = user.getUsername();
        User u = userService.findByUserName( username );
        if ( u == null ) {
            throw new IllegalArgumentException( "No user could be loaded with name=" + user );
        }

        u.setPassword( user.getPassword() );
        u.setEnabled( user.isEnabled() );
        if ( user instanceof UserDetailsImpl ) {
            u.setEmail( ( ( UserDetailsImpl ) user ).getEmail() );
        }

        userService.update( u );

        userCache.removeUserFromCache( user.getUsername() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManagerI#userExists(java.lang.String)
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "RUN_AS_ADMIN" })
    public boolean userExists( String username ) {
        return userService.findByUserName( username ) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#userWithEmailExists(java.lang.String)
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "RUN_AS_ADMIN" })
    public boolean userWithEmailExists( String emailAddress ) {
        return userService.findByEmail( emailAddress ) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.security.authentication.UserManager#validateSignupToken(java.lang.String, java.lang.String)
     */
    @Override
    @Secured({ "IS_AUTHENTICATED_ANONYMOUSLY", "RUN_AS_ADMIN" })
    public boolean validateSignupToken( String username, String key ) {

        UserDetailsImpl u = ( UserDetailsImpl ) loadUserByUsername( username );

        if ( u.isEnabled() ) {
            logger.warn( "User is already enabled, skipping token validation" );
            return true;
        }

        String storedTok = u.getSignupToken();
        Date storedDate = u.getSignupTokenDatestamp();

        if ( storedTok == null || storedDate == null ) {
            throw new IllegalArgumentException( "User does not have a token" );
        }

        Date oneWeekAgo = DateUtils.addWeeks( new Date(), -2 );

        if ( !storedTok.equals( key ) || storedDate.before( oneWeekAgo ) ) {
            return false;
        }

        u.setEnabled( true );

        updateUser( u );

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.security.provisioning.JdbcUserDetailsManager#updateUser(org.springframework.security.core
     * .userdetails.UserDetails)
     */
    protected Authentication createNewAuthentication( Authentication currentAuth, String newPassword ) {
        UserDetails user = loadUserByUsername( currentAuth.getName() );

        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken( user,
                user.getPassword(), user.getAuthorities() );
        newAuthentication.setDetails( currentAuth.getDetails() );

        return newAuthentication;
    }

    /**
     * Create a fresh UserDetails based on a given one.
     * 
     * @param username
     * @param userFromUserQuery
     * @param combinedAuthorities
     * @return
     */
    protected UserDetails createUserDetails( String username, UserDetailsImpl userFromUserQuery,
            List<GrantedAuthority> combinedAuthorities ) {
        return new UserDetailsImpl( userFromUserQuery.getPassword(), username, userFromUserQuery.isEnabled(),
                combinedAuthorities, userFromUserQuery.getEmail(), userFromUserQuery.getSignupToken(),
                userFromUserQuery.getSignupTokenDatestamp() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl#loadGroupAuthorities(java.lang.String)
     */
    protected List<GrantedAuthority> loadGroupAuthorities( String username ) {
        User u = loadUser( username );

        Collection<GroupAuthority> authorities = userService.loadGroupAuthorities( u );

        List<GrantedAuthority> result = new ArrayList<GrantedAuthority>();
        for ( GroupAuthority ga : authorities ) {
            String roleName = getRolePrefix() + ga.getAuthority();
            result.add( new GrantedAuthorityImpl( roleName ) );
        }

        return result;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl#loadUserAuthorities(java.lang.String)
     */
    protected List<GrantedAuthority> loadUserAuthorities( String username ) {
        throw new UnsupportedOperationException( "Use the group-based authorities instead" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl#loadUsersByUsername(java.lang.String)
     */
    protected List<UserDetails> loadUsersByUsername( String username ) {
        List<UserDetails> result = new ArrayList<UserDetails>();
        User u = loadUser( username );

        UserDetails ud = new UserDetailsImpl( u );

        result.add( ud );
        return result;
    }

    /**
     * @param username
     * @return user, or null if the user is anonymous.
     * @throws UsernameNotFoundException if the user does not exist in the system
     */
    private User getUserForUserName( String username ) throws UsernameNotFoundException {

        if ( AuthorityConstants.ANONYMOUS_USER_NAME.equals( username ) ) {
            return null;
        }

        User u = userService.findByUserName( username );

        if ( u == null ) {
            throw new UsernameNotFoundException( username + " not found" );
        }

        return u;
    }

    /**
     * @param groupName
     * @return
     */
    private UserGroup loadGroup( String groupName ) {
        UserGroup group = userService.findGroupByName( groupName );

        if ( group == null ) {
            throw new UsernameNotFoundException( "Group could not be read" );
        }

        return group;
    }

    /**
     * @param username
     * @return
     */
    private User loadUser( String username ) {
        User user = userService.findByUserName( username );
        if ( user == null ) {
            throw new UsernameNotFoundException( "User with name " + username + " could not be loaded" );
        }
        return user;
    }

    private void validateUserName( String username ) {

        boolean ok = StringUtils.isNotBlank( username );
        if ( AuthorityConstants.ADMIN_GROUP_AUTHORITY.equals( username ) ) {
            ok = false;
        } else if ( AuthorityConstants.ADMIN_GROUP_NAME.equals( username ) ) {
            ok = false;
        } else if ( AuthorityConstants.AGENT_GROUP_AUTHORITY.equals( username ) ) {
            ok = false;
        } else if ( AuthorityConstants.AGENT_GROUP_NAME.equals( username ) ) {
            ok = false;
        } else if ( AuthorityConstants.IS_AUTHENTICATED_ANONYMOUSLY.equals( username ) ) {
            ok = false;
        } else if ( AuthorityConstants.RUN_AS_ADMIN_AUTHORITY.equals( username ) ) {
            ok = false;
        } else if ( AuthorityConstants.USER_GROUP_AUTHORITY.equals( username ) ) {
            ok = false;
        } else if ( AuthorityConstants.USER_GROUP_NAME.equals( username ) ) {
            ok = false;
        } else if ( username.toUpperCase().startsWith( this.getRolePrefix() ) ) {
            ok = false;
        }

        if ( !ok ) {
            throw new IllegalArgumentException( "Username=" + username + " is not allowed" );
        }

    }

}
