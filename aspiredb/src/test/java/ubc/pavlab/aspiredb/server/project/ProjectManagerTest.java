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

package ubc.pavlab.aspiredb.server.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import gemma.gsec.SecurityService;
import gemma.gsec.authentication.UserDetailsImpl;
import gemma.gsec.authentication.UserManager;
import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.CharacteristicDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.security.authorization.acl.AclTestUtils;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

public class ProjectManagerTest extends BaseSpringContextTest {

    @Autowired
    private UserManager userManager;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProjectManager projectManager;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    CharacteristicDao characteristicDao;

    @Autowired
    SubjectDao subjectDao;

    @Autowired
    VariantDao variantDao;

    @Autowired
    AclTestUtils aclTestUtils;

    String authorizedUsername = RandomStringUtils.randomAlphabetic( 6 );

    String aDifferentUsername = RandomStringUtils.randomAlphabetic( 5 );

    //    String patientId1 = RandomStringUtils.randomAlphabetic( 5 );
    //
    //    String patientId2 = RandomStringUtils.randomAlphabetic( 6 );

    String groupName = RandomStringUtils.randomAlphabetic( 4 );
    String anotherGroupName = RandomStringUtils.randomAlphabetic( 5 );

    //    String projectName = RandomStringUtils.randomAlphabetic( 5 );
    //    String projectDescription = RandomStringUtils.randomAlphabetic( 7 );
    //    String anotherProjectName = RandomStringUtils.randomAlphabetic( 4 );

    @Before
    public void setup() throws Exception {

        super.runAsAdmin();
        try {
            userManager.loadUserByUsername( authorizedUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", authorizedUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        try {
            userManager.loadUserByUsername( aDifferentUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "foo", aDifferentUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        List<GrantedAuthority> authos = new ArrayList<GrantedAuthority>();
        //        authos.add( new GrantedAuthorityImpl( "GROUP_USER" ) );
        authos.add( new SimpleGrantedAuthority( "GROUP_USER" ) );

        this.userManager.createGroup( groupName, authos );

        this.userManager.addUserToGroup( authorizedUsername, groupName );

        this.userManager.createGroup( anotherGroupName, authos );

        this.userManager.addUserToGroup( authorizedUsername, anotherGroupName );

    }

    @After
    public void tearDown() throws Exception {
        super.runAsAdmin();
        this.userManager.deleteUser( authorizedUsername );
        this.userManager.deleteGroup( groupName );

        this.userManager.deleteUser( aDifferentUsername );
        this.userManager.deleteGroup( anotherGroupName );
    }

    @Test
    public void testAddSubjectVariantsToProjectSecurity() {

        super.runAsAdmin();

        final String patientId = RandomStringUtils.randomAlphabetic( 5 );
        final String projectName = RandomStringUtils.randomAlphabetic( 5 );

        CharacteristicValueObject cvo = new CharacteristicValueObject();

        cvo.setKey( "testChar" );
        cvo.setValue( "testcharvalue" );

        Map<String, CharacteristicValueObject> charMap = new HashMap<String, CharacteristicValueObject>();
        charMap.put( cvo.getKey(), cvo );

        CNVValueObject cnv = new CNVValueObject();

        cnv.setCharacteristics( charMap );
        cnv.setType( "GAIN" );

        GenomicRange gr = new GenomicRange( "X", 3, 234 );

        cnv.setGenomicRange( gr );

        cnv.setPatientId( patientId );

        // Another CNV from the same patient
        CNVValueObject cnv2 = new CNVValueObject();
        cnv2.setCharacteristics( charMap );
        cnv2.setType( "LOSS" );
        cnv2.setGenomicRange( gr );
        cnv2.setPatientId( patientId );

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( cnv );
        cnvList.add( cnv2 );

        try {

            projectManager.addSubjectVariantsToProject( projectName, true, cnvList );

        } catch ( Exception e ) {
            log.error( e.getLocalizedMessage(), e );
            fail( "projectManager.addSubjectVariantsToProject threw an exception " + e );

        }

        projectManager.alterGroupWritePermissions( projectName, groupName, true );

        TransactionTemplate tt = new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectName );

                aclTestUtils.checkHasAcl( project );

                assertFalse( "Project should not be viewable by user '" + aDifferentUsername + "', acl is "
                        + aclTestUtils.getAcl( project ),
                        securityService.isViewableByUser( project, aDifferentUsername ) );

                // now to test
                List<Subject> subjects = project.getSubjects();
                assertEquals( 1, subjects.size() );

                Subject subject = project.getSubjects().iterator().next();

                aclTestUtils.checkHasAcl( subject );

                assertFalse( securityService.isViewableByUser( subject, aDifferentUsername ) );

                // equals to cnvList
                assertEquals( 2, subject.getVariants().size() );

                variantDao.findBySubjectPatientId( project.getId(), patientId );

                // for ( Variant v : variantCollection ) {
                // // aclTestUtils.checkHasAcl( v );
                // // assertFalse( securityService.isViewableByUser( v, aDifferentUsername ) );
                //
                // Characteristic c = v.getCharacteristics().iterator().next();
                // aclTestUtils.checkHasAcl( c );
                // assertFalse( securityService.isViewableByUser( c, aDifferentUsername ) );
                // }

            }
        } );

        try {

            projectManager.deleteProject( projectName );

        } catch ( Exception e ) {
            log.error( e.getLocalizedMessage(), e );

        }

    }

    @Test
    public void testAlterGroupWritePermissionsForProject() {

        super.runAsAdmin();

        final String patientId = RandomStringUtils.randomAlphabetic( 5 );
        final String projectName = RandomStringUtils.randomAlphabetic( 7 );

        CharacteristicValueObject cvo = new CharacteristicValueObject();

        cvo.setKey( "testChar" );
        cvo.setValue( "testcharvalue" );

        Map<String, CharacteristicValueObject> charMap = new HashMap<String, CharacteristicValueObject>();
        charMap.put( cvo.getKey(), cvo );

        CNVValueObject cnv = new CNVValueObject();

        cnv.setCharacteristics( charMap );
        cnv.setType( "GAIN" );

        GenomicRange gr = new GenomicRange( "X", 3, 234 );

        cnv.setGenomicRange( gr );

        cnv.setPatientId( patientId );

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( cnv );

        try {

            projectManager.addSubjectVariantsToProject( projectName, true, cnvList );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

            e.printStackTrace();
        }

        // authorizedUsername is in groupName
        projectManager.alterGroupWritePermissions( projectName, groupName, true );

        // make sure authorizedUsername has read access to all the stuff in a project after security change
        TransactionTemplate tt = new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectName );

                assertTrue( securityService.isViewableByUser( project, authorizedUsername ) );

                Subject subject = project.getSubjects().iterator().next();

                assertTrue( securityService.isViewableByUser( subject, authorizedUsername ) );

                variantDao.findBySubjectPatientId( project.getId(), patientId );

                // for ( Variant v : variantCollection ) {
                //
                // assertTrue( securityService.isViewableByUser( v, authorizedUsername ) );
                //
                // assertTrue( securityService.isViewableByUser( v.getCharacteristics().iterator().next(),
                // authorizedUsername ) );
                // }

            }
        } );

        // make sure aDifferentUserName doesn't have access to stuff in project
        TransactionTemplate tt2 = new TransactionTemplate( transactionManager );
        tt2.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectName );

                assertFalse( securityService.isViewableByUser( project, aDifferentUsername ) );

                Subject subject = project.getSubjects().iterator().next();

                assertFalse( securityService.isViewableByUser( subject, aDifferentUsername ) );

                variantDao.findBySubjectPatientId( project.getId(), patientId );

                // for ( Variant v : variantCollection ) {
                //
                // assertFalse( securityService.isViewableByUser( v, aDifferentUsername ) );
                //
                // assertFalse( securityService.isViewableByUser( v.getCharacteristics().iterator().next(),
                // aDifferentUsername ) );
                // }

            }
        } );

        // test out a couple of dao methods

        super.runAsUser( authorizedUsername );

        long projectId = projectDao.findByProjectName( projectName ).getId();

        variantDao.findBySubjectPatientId( projectId, patientId );

        super.runAsUser( aDifferentUsername );

        try {
            projectId = projectDao.findByProjectName( projectName ).getId();
            fail( "should have got Access Denied" );
        } catch ( AccessDeniedException e ) {

        }

        Collection<Variant> vCollection2 = variantDao.findBySubjectPatientId( projectId, patientId );

        assertTrue( vCollection2.isEmpty() );

        super.runAsAdmin();
        // test removing permissions

        // authorizedUsername is in groupName
        projectManager.alterGroupWritePermissions( projectName, groupName, false );

        // make sure authorizedUsername does not have read access to all the stuff in a project after security change
        TransactionTemplate tt3 = new TransactionTemplate( transactionManager );
        tt3.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectName );

                assertFalse( securityService.isViewableByUser( project, authorizedUsername ) );

                Subject subject = project.getSubjects().iterator().next();

                assertFalse( securityService.isViewableByUser( subject, authorizedUsername ) );

                variantDao.findBySubjectPatientId( project.getId(), patientId );

                // for ( Variant v : variantCollection ) {
                //
                // assertFalse( securityService.isViewableByUser( v, authorizedUsername ) );
                //
                // assertFalse( securityService.isViewableByUser( v.getCharacteristics().iterator().next(),
                // authorizedUsername ) );
                // }

            }
        } );

        // test out a couple of dao methods to make sure authorizedUsername has no access
        super.runAsUser( authorizedUsername );

        try {
            projectId = projectDao.findByProjectName( projectName ).getId();
            fail( "should have got Access Denied" );
        } catch ( AccessDeniedException e ) {

        }

        Collection<Variant> vCollection3 = variantDao.findBySubjectPatientId( projectId, patientId );

        assertTrue( vCollection3.isEmpty() );

        super.runAsAdmin();

        try {

            projectManager.deleteProject( projectName );

        } catch ( Exception e ) {
            log.error( e.getLocalizedMessage(), e );

        }

    }

    @Test
    public void testCreateProject() {

        super.runAsAdmin();

        final String project_name = RandomStringUtils.randomAlphabetic( 5 );
        final String project_description = RandomStringUtils.randomAlphabetic( 5 );

        new InlineRollbackTransaction() {
            @Override
            public void instructions() {
                try {
                    Project p = projectManager.createProject( project_name, project_description );
                    aclTestUtils.checkHasAcl( p );
                } catch ( Exception e ) {
                    // log.error( e.getMessage() );
                    fail( "projectManager.createproject threw an exception" );
                }
            }
        }.execute();

    }

    @Test
    public void testDeleteProject() {

        super.runAsAdmin();

        final String patientId = RandomStringUtils.randomAlphabetic( 5 );
        final String projectId = RandomStringUtils.randomAlphabetic( 5 );

        CharacteristicValueObject cvo = new CharacteristicValueObject();

        cvo.setKey( "testChar" );
        cvo.setValue( "testcharvalue" );

        Map<String, CharacteristicValueObject> charMap = new HashMap<String, CharacteristicValueObject>();
        charMap.put( cvo.getKey(), cvo );

        CNVValueObject cnv = new CNVValueObject();

        cnv.setCharacteristics( charMap );
        cnv.setType( "GAIN" );

        GenomicRange gr = new GenomicRange( "X", 3, 234 );

        cnv.setGenomicRange( gr );

        cnv.setPatientId( patientId );

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( cnv );

        try {
            // this creates a project
            projectManager.addSubjectVariantsToProject( projectId, true, cnvList );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        new InlineTransaction() {
            @Override
            public void instructions() {

                Project p = projectDao.findByProjectName( projectId );
                aclTestUtils.checkHasAcl( p );

                // collect Ids
                Collection<Long> subjectIds = new HashSet<>();
                Collection<Long> variantIds = new HashSet<>();
                Collection<Long> characteristicIds = new HashSet<>();
                for ( Subject s : p.getSubjects() ) {
                    assertNotNull( s.getId() );
                    subjectIds.add( s.getId() );
                    for ( Variant v : s.getVariants() ) {
                        assertNotNull( v.getId() );
                        variantIds.add( v.getId() );
                        for ( Characteristic c : v.getCharacteristics() ) {
                            assertNotNull( c.getId() );
                            characteristicIds.add( c.getId() );
                        }
                    }
                }
                assertTrue( subjectIds.size() > 0 );
                assertTrue( variantIds.size() > 0 );
                assertTrue( characteristicIds.size() > 0 );

                try {

                    projectManager.deleteProject( projectId );

                } catch ( Exception e ) {
                    log.error( e.getLocalizedMessage(), e );
                    fail( "projectManager.deleteProject failed" );

                }

                aclTestUtils.checkDeletedAcl( p );

                // now let's try loading the ids that should have been removed!
                assertEquals( null, projectDao.load( p.getId() ) );
                assertEquals( 0, subjectDao.load( subjectIds ).size() );
                assertEquals( 0, variantDao.load( variantIds ).size() );
                assertEquals( 0, characteristicDao.load( characteristicIds ).size() );
            }
        }.execute();

    }

    @Test
    public void testQuickDeleteProject() {

        super.runAsAdmin();

        final String patientId = RandomStringUtils.randomAlphabetic( 5 );
        final String projectName = RandomStringUtils.randomAlphabetic( 5 );

        CharacteristicValueObject cvo = new CharacteristicValueObject();

        cvo.setKey( "testChar" );
        cvo.setValue( "testcharvalue" );

        Map<String, CharacteristicValueObject> charMap = new HashMap<String, CharacteristicValueObject>();
        charMap.put( cvo.getKey(), cvo );

        CNVValueObject cnv = new CNVValueObject();

        cnv.setCharacteristics( charMap );
        cnv.setType( "GAIN" );

        GenomicRange gr = new GenomicRange( "X", 3, 234 );

        cnv.setGenomicRange( gr );

        cnv.setPatientId( patientId );

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( cnv );

        try {
            // this creates a project
            projectManager.addSubjectVariantsToProject( projectName, true, cnvList );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        new InlineTransaction() {
            @Override
            public void instructions() {

                Project p = projectDao.findByProjectName( projectName );
                aclTestUtils.checkHasAcl( p );

                // collect Ids
                Collection<Long> subjectIds = new HashSet<>();
                Collection<Long> variantIds = new HashSet<>();
                Collection<Long> characteristicIds = new HashSet<>();
                for ( Subject s : p.getSubjects() ) {
                    assertNotNull( s.getId() );
                    subjectIds.add( s.getId() );
                    for ( Variant v : s.getVariants() ) {
                        assertNotNull( v.getId() );
                        variantIds.add( v.getId() );
                        for ( Characteristic c : v.getCharacteristics() ) {
                            assertNotNull( c.getId() );
                            characteristicIds.add( c.getId() );
                        }
                    }
                }
                assertTrue( subjectIds.size() > 0 );
                assertTrue( variantIds.size() > 0 );
                assertTrue( characteristicIds.size() > 0 );
                try {

                    projectManager.quickDeleteProject( projectName );

                } catch ( Exception e ) {
                    log.error( e.getLocalizedMessage(), e );
                    fail( "projectManager.deleteProject failed" );

                }

                aclTestUtils.checkDeletedAcl( p );

                // now let's try loading the ids that should have been removed!
                assertEquals( null, projectDao.load( p.getId() ) );
                assertEquals( 0, subjectDao.load( subjectIds ).size() );
                assertEquals( 0, variantDao.load( variantIds ).size() );
                assertEquals( 0, characteristicDao.load( characteristicIds ).size() );
            }
        }.execute();

    }

}