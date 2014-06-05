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

package ubc.pavlab.aspiredb.server.security.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.Securable;
import ubc.pavlab.aspiredb.server.security.SecurityService;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

/**
 * Tests the SecurityService: testing the permissions.
 * 
 * @author keshav
 * @version $Id: SecurityServiceTest.java,v 1.4 2013/03/01 21:46:23 cmcdonald Exp $
 */
public class SecurityServiceTest extends BaseSpringContextTest {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AclService aclService;

    @Autowired
    private UserManager userManager;

    private ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();

    @Autowired
    private PersistentTestObjectHelper testObjectHelper;

    @Autowired
    private SubjectDao individualDao;

    String patientId = RandomStringUtils.randomAlphabetic( 4 );

    Subject individual;

    @Before
    public void setup() {

        // admin
        individual = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId );

    }

    @Test
    public void testUserCanEdit() {
        Collection<String> editableBy = this.securityService.editableBy( this.individual );
        assertTrue( editableBy.contains( "administrator" ) );
        assertTrue( !editableBy.contains( "aspiredbAgent" ) );

        assertTrue( this.securityService.isEditableByUser( this.individual, "administrator" ) );
    }

    @Test
    public void testUserCanRead() {
        Collection<String> us = this.securityService.readableBy( this.individual );
        assertTrue( us.contains( "administrator" ) );
        assertTrue( us.contains( "aspiredbAgent" ) );

        assertTrue( this.securityService.isViewableByUser( this.individual, "administrator" ) );
        assertTrue( this.securityService.isViewableByUser( this.individual, "aspiredbAgent" ) );
    }

    @Test
    public void testSetOwner() {
        Subject ind = testObjectHelper
                .createPersistentTestSubjectObjectWithCNV( RandomStringUtils.randomAlphabetic( 4 ) );

        String username = "first_" + randomName();
        makeUser( username );

        this.securityService.setOwner( ind, username );

        Sid owner = this.securityService.getOwner( ind );
        assertTrue( owner instanceof PrincipalSid );
        assertEquals( username, ( ( PrincipalSid ) owner ).getPrincipal() );

    }

    private void makeUser( String username ) {
        try {
            this.userManager.loadUserByUsername( username );
        } catch ( UsernameNotFoundException e ) {
            this.userManager.createUser( new UserDetailsImpl( "foo", username, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }
    }

    @Test
    public void testMakeIndividualReadWrite() throws Exception {

        String indPatientId = RandomStringUtils.randomAlphabetic( 4 );
        Subject ind = testObjectHelper.createPersistentTestSubjectObjectWithCNV( indPatientId );
        assertTrue( "This should be private because all data should be private", this.securityService.isPrivate( ind ) );

        String username = "first_" + randomName();
        String usertwo = "second_" + randomName();
        makeUser( username );
        makeUser( usertwo );

        this.securityService.makeOwnedByUser( ind, username );

        assertTrue( this.securityService.isEditableByUser( ind, username ) );

        this.runAsUser( username );

        /*
         * Create a group, do stuff...
         */
        String groupName = randomName();
        this.securityService.createGroup( groupName );
        this.securityService.makeWriteableByGroup( ind, groupName );

        /*
         * Add another user to the group.
         */

        this.securityService.addUserToGroup( usertwo, groupName );

        /*
         * Now, log in as another user.
         */
        this.runAsUser( usertwo );

        ind = individualDao.findByPatientId( indPatientId );
        ind.setPatientId( RandomStringUtils.randomAlphabetic( 5 ) );
        individualDao.update( ind );
        // no exception == happy.

        this.runAsUser( username );
        this.securityService.makeUnreadableByGroup( ind, groupName );
        // should still work.
        ind = individualDao.findByPatientId( indPatientId );

        this.runAsUser( usertwo );
        // should be locked out.

        ind = individualDao.findByPatientId( indPatientId );
        assertNull( ind );

        try {
            this.userManager.deleteGroup( groupName );
            fail( "Should have gotten 'access denied'" );
        } catch ( AccessDeniedException ok ) {
            // expected behaviour
        }
        this.runAsUser( username );
        this.userManager.deleteGroup( groupName );

    }

    private MutableAcl getAcl( Securable s ) {
        ObjectIdentity oi = this.objectIdentityRetrievalStrategy.getObjectIdentity( s );

        try {
            return ( MutableAcl ) this.aclService.readAclById( oi );
        } catch ( NotFoundException e ) {
            return null;
        }
    }

    /**
     * Tests that the same ACL can not be added to a securable object.
     * 
     * @throws Exception
     */
    @Test
    public void testDuplicateAcesNotAddedOnIndividual() throws Exception {
        // make private experiment
        Subject ind = testObjectHelper
                .createPersistentTestSubjectObjectWithCNV( RandomStringUtils.randomAlphabetic( 4 ) );
        assertTrue( "This should be private because all data should be private", this.securityService.isPrivate( ind ) );
        // add user and add the user to the group
        String username = "bananabread" + randomName();
        String groupName = "bakedgoods" + randomName();
        makeUser( username );
        this.securityService.makeOwnedByUser( ind, username );
        assertTrue( this.securityService.isEditableByUser( ind, username ) );
        this.runAsUser( username );

        this.securityService.createGroup( groupName );

        MutableAcl acl = getAcl( ind );
        int numberOfAces = acl.getEntries().size();

        this.securityService.makeReadableByGroup( ind, groupName );
        MutableAcl aclAfterReadableAdded = getAcl( ind );
        assertEquals( numberOfAces + 1, aclAfterReadableAdded.getEntries().size() );

        this.securityService.makeWriteableByGroup( ind, groupName );
        MutableAcl aclAfterWritableAdded = getAcl( ind );
        assertEquals( numberOfAces + 2, aclAfterWritableAdded.getEntries().size() );

        // this time the acl there and should not be added again
        this.securityService.makeReadableByGroup( ind, groupName );
        MutableAcl aclAfterReadableAddedAgain = getAcl( ind );
        assertEquals( numberOfAces + 2, aclAfterReadableAddedAgain.getEntries().size() );

        // check writable too
        this.securityService.makeWriteableByGroup( ind, groupName );
        MutableAcl aclAfterWritableAddedAgain = getAcl( ind );
        assertEquals( numberOfAces + 2, aclAfterWritableAddedAgain.getEntries().size() );

    }

    /**
     * Test to ensure that on creation of principal using a username that does not exist in system exception is thrown.
     * Principal ids are created in these method calls on SecurityService.
     */
    @Test
    public void testSetPrincipalSID() {
        String username = "first_" + randomName();
        Subject ind = testObjectHelper
                .createPersistentTestSubjectObjectWithCNV( RandomStringUtils.randomAlphabetic( 4 ) );

        try {
            this.securityService.setOwner( ind, username );
            fail();
        } catch ( Exception e ) {

        }

        try {
            this.securityService.makeOwnedByUser( ind, username );
            fail();
        } catch ( Exception e ) {

        }

    }
}
