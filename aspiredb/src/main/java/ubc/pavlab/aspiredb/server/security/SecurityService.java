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

package ubc.pavlab.aspiredb.server.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.Securable;
import ubc.pavlab.aspiredb.server.util.AuthorityConstants;

/**
 * @author paul
 * @version $Id: SecurityService.java,v 1.6 2013/06/11 22:31:01 anton Exp $
 */
public interface SecurityService {

    /**
     * This is defined in spring-security AuthenticationConfigBuilder, and can be set in the <security:anonymous />
     * configuration of the <security:http/> namespace config
     */
    public static final String ANONYMOUS = AuthorityConstants.ANONYMOUS_USER_NAME;

    /**
     * @param userName
     * @param groupName
     */
    public abstract void addUserToGroup( String userName, String groupName );

    /**
     * @param securables
     * @return
     */
    public abstract Map<Securable, Boolean> areNonPublicButReadableByCurrentUser(
            Collection<? extends Securable> securables );

    /**
     * A securable is considered "owned" if 1) the user is the actual owner assigned in the ACL or 2) the user is an
     * administrator. In other words, for an administrator, the value will always be true.
     * 
     * @param securables
     * @return
     */
    public abstract Map<Securable, Boolean> areOwnedByCurrentUser( Collection<? extends Securable> securables );

    /**
     * @param securables
     * @return
     * @throws AuthorizationServiceException if the collection is empty, see comments in
     *         {@link ubic.gemma.security.authorization.acl.AclCollectionEntryVoter AclCollectionEntryVoter}
     */
    @Secured({ "ACL_SECURABLE_COLLECTION_READ" })
    public abstract java.util.Map<Securable, Boolean> arePrivate( Collection<? extends Securable> securables );

    /**
     * @throws AuthorizationServiceException if the collection is empty, see comments in
     *         {@link ubic.gemma.security.authorization.acl.AclCollectionEntryVoter AclCollectionEntryVoter}
     */
    @Secured({ "ACL_SECURABLE_COLLECTION_READ" })
    public abstract Map<Securable, Boolean> areShared( Collection<? extends Securable> securables );

    /**
     * @param securables
     * @return the subset which are private, if any
     */
    public abstract Collection<Securable> choosePrivate( Collection<? extends Securable> securables );

    /**
     * @param securables
     * @return the subset that are public, if any
     * @throws AuthorizationServiceException if the collection is empty, see comments in
     *         {@link ubic.gemma.security.authorization.acl.AclCollectionEntryVoter AclCollectionEntryVoter}
     */
    @Secured({ "ACL_SECURABLE_COLLECTION_READ" })
    public abstract Collection<Securable> choosePublic( Collection<? extends Securable> securables );

    /**
     * If the group already exists, an exception will be thrown.
     * 
     * @param groupName
     */
    @Transactional
    public abstract void createGroup( String groupName );

    /**
     * @param s
     * @return list of userNames who can edit the given securable.
     * @throws AuthorizationServiceException if the collection is empty, see comments in
     *         {@link ubic.gemma.security.authorization.acl.AclCollectionEntryVoter AclCollectionEntryVoter}
     */
    @Secured({ "ACL_SECURABLE_READ" })
    public abstract Collection<String> editableBy( Securable s );

    /**
     * We make this available to anonymous
     * 
     * @return
     */
    public abstract Integer getAuthenticatedUserCount();

    /**
     * @return user names
     */
    @Secured("GROUP_ADMIN")
    public abstract Collection<String> getAuthenticatedUserNames();

    /**
     * This methods is only available to administrators.
     * 
     * @return collection of all available security ids (basically, user names and group authorities.
     */
    @Secured("GROUP_ADMIN")
    public abstract Collection<Sid> getAvailableSids();

    /**
     * @param s
     * @throws AuthorizationServiceException if the collection is empty, see comments in
     *         {@link ubic.gemma.security.authorization.acl.AclCollectionEntryVoter AclCollectionEntryVoter}
     */
    @Secured({ "ACL_SECURABLE_COLLECTION_READ" })
    public abstract Map<Securable, Collection<String>> getGroupsEditableBy( Collection<? extends Securable> securables );

    /**
     * @param s
     * @return
     */
    @Secured({ "ACL_SECURABLE_READ" })
    public abstract Collection<String> getGroupsEditableBy( Securable s );

    /**
     * @param s
     * @return
     */
    @Secured({ "ACL_SECURABLE_COLLECTION_READ" })
    public abstract Map<Securable, Collection<String>> getGroupsReadableBy( Collection<? extends Securable> securables );

    /**
     * @param s
     * @return
     */
    @Secured({ "ACL_SECURABLE_READ" })
    public abstract Collection<String> getGroupsReadableBy( Securable s );

    /**
     * @param userName
     * @return
     */
    public abstract Collection<String> getGroupsUserCanEdit( String userName );

    /**
     * @param s
     * @return
     */
    @Secured("ACL_SECURABLE_READ")
    public abstract Sid getOwner( Securable s );

    /**
     * Pretty much have to be either the owner of the securables or administrator to call this.
     * 
     * @param securables
     * @return
     * @throws AccessDeniedException if the current user is not allowed to access the information.
     */
    @Secured("ACL_SECURABLE_COLLECTION_READ")
    public abstract Map<Securable, Sid> getOwners( Collection<? extends Securable> securables );

    /**
     * @param s
     * @return true if the current user can edit the securable
     */
    @Secured("ACL_SECURABLE_READ")
    public abstract boolean isEditable( Securable s );

    /**
     * @param s
     * @param groupName
     * @return
     */
    @Secured("ACL_SECURABLE_READ")
    public abstract boolean isEditableByGroup( Securable s, String groupName );

    /**
     * @param s
     * @param userName
     * @return true if the user has WRITE permissions or ADMIN
     */
    @Secured("ACL_SECURABLE_READ")
    public abstract boolean isEditableByUser( Securable s, String userName );

    /**
     * @param s
     * @return true if the owner is the same as the current authenticated user. Special case: if the owner is an
     *         administrator, and the uc
     */
    public abstract boolean isOwnedByCurrentUser( Securable s );

    /**
     * Convenience method to determine the visibility of an object. Even though in aspiredb all data should be private,
     * leaving this in for testing purposes
     * 
     * @param s
     * @return true if anonymous users can view (READ) the object, false otherwise. If the object doesn't have an ACL,
     *         return true (be safe!)
     * @see org.springframework.security.acls.jdbc.BasicLookupStrategy
     */
    public abstract boolean isPrivate( Securable s );

    /**
     * Convenience method to determine the visibility of an object. Even though in aspiredb all data should be private,
     * leaving this in for testing purposes
     * 
     * @param s
     * @return the negation of isPrivate().
     */
    public abstract boolean isPublic( Securable s );

    @Secured("ACL_SECURABLE_READ")
    public abstract boolean isReadableByGroup( Securable s, String groupName );

    public abstract boolean isShared( Securable s );

    /**
     * @param s
     * @param userName
     * @return true if the given user can read the securable, false otherwise. (READ or ADMINISTRATION required)
     */
    @Secured({ "ACL_SECURABLE_READ" })
    public abstract boolean isViewableByUser( Securable s, String userName );

    /**
     * Administrative method to allow a user to get access to an object. This is useful for cases where a data set is
     * loaded by admin but we need to hand it off to a user.
     * 
     * @param s
     * @param userName
     */
    @Secured("GROUP_ADMIN")
    public abstract void makeOwnedByUser( Securable s, String userName );

    /**
     * Adds read permission.
     * 
     * @param s
     * @param groupName
     * @throws AccessDeniedException
     */
    @Secured("ACL_SECURABLE_EDIT")
    public abstract void makeReadableByGroup( Securable s, String groupName ) throws AccessDeniedException;

    /**
     * Remove read permissions; also removes write permissions.
     * 
     * @param s
     * @param groupName, with or without GROUP_
     * @throws AccessDeniedException
     */
    @Secured("ACL_SECURABLE_EDIT")
    public abstract void makeUnreadableByGroup( Securable s, String groupName ) throws AccessDeniedException;

    /**
     * Remove write permissions. Leaves read permissions, if present.
     * 
     * @param s
     * @param groupName
     * @throws AccessDeniedException
     */
    @Secured("ACL_SECURABLE_EDIT")
    public abstract void makeUnwriteableByGroup( Securable s, String groupName ) throws AccessDeniedException;

    /**
     * Adds write (and read) permissions.
     * 
     * @param s
     * @param groupName
     * @throws AccessDeniedException
     */
    @Secured("ACL_SECURABLE_EDIT")
    public abstract void makeWriteableByGroup( Securable s, String groupName ) throws AccessDeniedException;

    /**
     * @param s
     * @return list of userNames of users who can read the given securable.
     */
    @Secured("ACL_SECURABLE_EDIT")
    public abstract Collection<String> readableBy( Securable s );

    /**
     * @param userName
     * @param groupName
     */
    public abstract void removeUserFromGroup( String userName, String groupName );

    /**
     * Change the 'owner' of an object to a specific user. Note that this doesn't support making the owner a
     * grantedAuthority.
     * 
     * @param s
     * @param userName
     */
    @Secured("GROUP_ADMIN")
    public abstract void setOwner( Securable s, String userName );

    public String getGroupAuthorityNameFromGroupName( String groupName );
}