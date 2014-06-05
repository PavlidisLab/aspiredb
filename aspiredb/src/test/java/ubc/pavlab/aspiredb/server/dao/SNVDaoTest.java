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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.SNV;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

public class SNVDaoTest extends BaseSpringContextTest {

    @Autowired
    private SNVDao snvDao;

    @Autowired
    PersistentTestObjectHelper testObjectHelper;

    @Autowired
    UserManager userManager;

    @Autowired
    private ProjectDao projectDao;

    String authorizedUsername = RandomStringUtils.randomAlphabetic( 6 );

    String aDifferentUsername = RandomStringUtils.randomAlphabetic( 5 );

    String projectName = RandomStringUtils.randomAlphabetic( 4 );
    String groupName = RandomStringUtils.randomAlphabetic( 4 );

    Long projectId;

    @Before
    public void setup() throws Exception {

        Project detachedProject = new Project();

        detachedProject.setName( projectName );

        Project p1 = projectDao.create( detachedProject );

        projectId = p1.getId();

        try {
            userManager.loadUserByUsername( authorizedUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", authorizedUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        List<GrantedAuthority> authos = new ArrayList<GrantedAuthority>();
        authos.add( new GrantedAuthorityImpl( groupName ) );

        userManager.createGroup( groupName, authos );

        userManager.addUserToGroup( authorizedUsername, groupName );

        try {
            userManager.loadUserByUsername( aDifferentUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "foo", aDifferentUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

    }

    @Test
    public void testCreate() {
        TransactionTemplate tt = new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                // Just a stub to test the plumbing.
                SNV snv = new SNV();
                snvDao.create( snv );

            }
        } );
    }

    @Test
    public void testUpdateLoad() {

        SNV snv = testObjectHelper.createPersistentTestSNVObject();
        Long id = snv.getId();

        // "567id" was the value set in testObjectHelper.createPersistentTestIndelObject()
        assertEquals( snv.getDbSNPID(), "567id" );

        snv.setObservedBase( "newObservedBase" );

        snvDao.update( snv );

        SNV updatedSNV = snvDao.load( id );

        assertEquals( updatedSNV.getObservedBase(), "newObservedBase" );

    }

}
