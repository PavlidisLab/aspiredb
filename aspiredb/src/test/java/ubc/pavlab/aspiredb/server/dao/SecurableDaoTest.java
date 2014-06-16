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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gemma.gsec.SecurityService;

import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.security.authorization.acl.AclTestUtils;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

/**
 * IndividualDao implements SecurableDao so this tests whether spring security is configured properly through the
 * SecurableDao interface
 * 
 * @version $Id: SecurableDaoTest.java,v 1.9 2013/06/11 23:01:32 cmcdonald Exp $
 */
public class SecurableDaoTest extends BaseSpringContextTest {

    @Autowired
    UserManager userManager;

    @Autowired
    MutableAclService aclService;

    @Autowired
    SecurityService securityService;

    @Autowired
    AclTestUtils aclTestUtils;

    @Autowired
    PersistentTestObjectHelper testObjectHelper;

    @Autowired
    SubjectDao individualDao;

    @Autowired
    CNVDao cnvDao;

    @Autowired
    AclTestUtils aclUtils;

    String ownerUsername = RandomStringUtils.randomAlphabetic( 6 );

    String aDifferentUsername = RandomStringUtils.randomAlphabetic( 5 );

    @Before
    public void setup() throws Exception {

        try {
            userManager.loadUserByUsername( ownerUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", ownerUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        try {
            userManager.loadUserByUsername( aDifferentUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "foo", aDifferentUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

    }

    /**
     * @throws Exception
     */
    @Test
    public void testIndividualDaoMethodSecurityForAnonymousUser() throws Exception {

        String patientId = RandomStringUtils.randomAlphabetic( 4 );

        super.runAsUser( this.ownerUsername );

        Subject ind = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId );

        aclTestUtils.checkHasAcl( ind );
        // ownerUserName will own the individual
        assertTrue( "User should own the individual", securityService.isOwnedByCurrentUser( ind ) );

        // test that anonymous user can't do CRUD operations from SecurableDao
        super.runAsAnon();

        assertFalse( "User shouldn't own the individual", securityService.isOwnedByCurrentUser( ind ) );

        // test create
        try {

            Subject anonInd = testObjectHelper.createDetachedIndividualObject( "bad" );
            individualDao.create( anonInd );
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException e ) {

        }

        // test load
        try {

            individualDao.loadAll();
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException e ) {

        }

        // test update
        ind.setPatientId( "badder" );
        try {
            individualDao.update( ind );
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException e ) {

        }

        // testRemove
        try {

            individualDao.remove( ind );
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException e ) {

        }

    }

    @Test
    public void testIndividualDaoMethodSecurityForRegisteredUser() throws Exception {

        String patientId = RandomStringUtils.randomAlphabetic( 4 );

        super.runAsUser( this.ownerUsername );

        Subject ind = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId );

        aclTestUtils.checkHasAcl( ind );

        // ownerUserName will own the individual
        assertTrue( "User '" + ownerUsername + "' should own the individual",
                securityService.isOwnedByCurrentUser( ind ) );

        // test update
        ind.setPatientId( "badder" );
        try {
            individualDao.update( ind );
        } catch ( AccessDeniedException e ) {
            fail( "Should not have gotten an access denied" );
        }

        super.runAsUser( this.aDifferentUsername );

        assertFalse( "User '" + aDifferentUsername + "' should not own the individual",
                securityService.isOwnedByCurrentUser( ind ) );

        // test update
        ind.setPatientId( "evenbadder" );
        try {
            individualDao.update( ind );
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException e ) {
            // "Should have gotten an access denied"
        }

        // testRemove
        try {

            individualDao.remove( ind );
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException e ) {
            // "Should have gotten an access denied"
        }

    }

}