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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.security.SecurityService;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
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
    VariantDao variantDao;

    @Autowired
    AclTestUtils aclTestUtils;

    String authorizedUsername = RandomStringUtils.randomAlphabetic( 6 );

    String aDifferentUsername = RandomStringUtils.randomAlphabetic( 5 );

    String patientId1 = RandomStringUtils.randomAlphabetic( 5 );

    String patientId2 = RandomStringUtils.randomAlphabetic( 6 );

    String groupName = RandomStringUtils.randomAlphabetic( 4 );
    String anotherGroupName = RandomStringUtils.randomAlphabetic( 5 );

    String projectName = RandomStringUtils.randomAlphabetic( 5 );
    String anotherProjectName = RandomStringUtils.randomAlphabetic( 4 );

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
        authos.add( new GrantedAuthorityImpl( "GROUP_USER" ) );

        this.userManager.createGroup( groupName, authos );

        this.userManager.addUserToGroup( authorizedUsername, groupName );

        this.userManager.createGroup( anotherGroupName, authos );

        this.userManager.addUserToGroup( authorizedUsername, anotherGroupName );

    }

    @Test
    public void testAddSubjectVariantsToProjectSecurity() {

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

        GenomicRange gr = new GenomicRange();
        gr.setChromosome( "X" );
        gr.setBaseStart( 3 );
        gr.setBaseEnd( 234 );
        cnv.setGenomicRange( gr );

        cnv.setPatientId( patientId );

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( cnv );

        try {

            projectManager.addSubjectVariantsToProject( projectId, true, cnvList );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        TransactionTemplate tt = new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectId );

                aclTestUtils.checkHasAcl( project );

                assertFalse( securityService.isViewableByUser( project, aDifferentUsername ) );

                Subject subject = project.getSubjects().iterator().next();

                aclTestUtils.checkHasAcl( subject );

                assertFalse( securityService.isViewableByUser( subject, aDifferentUsername ) );

                Collection<Variant> variantCollection = variantDao.findBySubjectPatientId( patientId );

                for ( Variant v : variantCollection ) {
                    aclTestUtils.checkHasAcl( v );
                    assertFalse( securityService.isViewableByUser( v, aDifferentUsername ) );

                    Characteristic c = v.getCharacteristics().iterator().next();
                    aclTestUtils.checkHasAcl( c );
                    assertFalse( securityService.isViewableByUser( c, aDifferentUsername ) );
                }

            }
        } );

    }

    @Test
    public void testAlterGroupWritePermissionsForProject() {

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

        GenomicRange gr = new GenomicRange();
        gr.setChromosome( "X" );
        gr.setBaseStart( 3 );
        gr.setBaseEnd( 234 );
        cnv.setGenomicRange( gr );

        cnv.setPatientId( patientId );

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( cnv );

        try {

            projectManager.addSubjectVariantsToProject( projectId, true, cnvList );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        // authorizedUsername is in groupName
        projectManager.alterGroupWritePermissions( projectId, groupName, true );

        // make sure authorizedUsername has read access to all the stuff in a project after security change
        TransactionTemplate tt = new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectId );

                assertTrue( securityService.isViewableByUser( project, authorizedUsername ) );

                Subject subject = project.getSubjects().iterator().next();

                assertTrue( securityService.isViewableByUser( subject, authorizedUsername ) );

                Collection<Variant> variantCollection = variantDao.findBySubjectPatientId( patientId );

                for ( Variant v : variantCollection ) {

                    assertTrue( securityService.isViewableByUser( v, authorizedUsername ) );

                    assertTrue( securityService.isViewableByUser( v.getCharacteristics().iterator().next(),
                            authorizedUsername ) );
                }

            }
        } );

        new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectId );

                assertFalse( securityService.isViewableByUser( project, aDifferentUsername ) );

                Subject subject = project.getSubjects().iterator().next();

                assertFalse( securityService.isViewableByUser( subject, aDifferentUsername ) );

                Collection<Variant> variantCollection = variantDao.findBySubjectPatientId( patientId );

                for ( Variant v : variantCollection ) {

                    assertFalse( securityService.isViewableByUser( v, aDifferentUsername ) );

                    assertFalse( securityService.isViewableByUser( v.getCharacteristics().iterator().next(),
                            aDifferentUsername ) );
                }

            }
        } );

        // test out a couple of dao methods

        super.runAsUser( authorizedUsername );

        projectDao.findByProjectName( projectId );

        variantDao.findBySubjectPatientId( patientId );

        super.runAsUser( aDifferentUsername );

        try {
            projectDao.findByProjectName( projectId );
            fail( "should have got Access Denied" );
        } catch ( AccessDeniedException e ) {

        }

        Collection<Variant> vCollection2 = variantDao.findBySubjectPatientId( patientId );

        assertTrue( vCollection2.isEmpty() );

        super.runAsAdmin();
        // test removing permissions

        // authorizedUsername is in groupName
        projectManager.alterGroupWritePermissions( projectId, groupName, false );

        new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                Project project = projectDao.findByProjectName( projectId );

                assertFalse( securityService.isViewableByUser( project, authorizedUsername ) );

                Subject subject = project.getSubjects().iterator().next();

                assertFalse( securityService.isViewableByUser( subject, authorizedUsername ) );

                Collection<Variant> variantCollection = variantDao.findBySubjectPatientId( patientId );

                for ( Variant v : variantCollection ) {

                    assertFalse( securityService.isViewableByUser( v, authorizedUsername ) );

                    assertFalse( securityService.isViewableByUser( v.getCharacteristics().iterator().next(),
                            authorizedUsername ) );
                }

            }
        } );

        // test out a couple of dao methods to make sure authorizedUsername has no access
        super.runAsUser( authorizedUsername );

        try {
            projectDao.findByProjectName( projectId );
            fail( "should have got Access Denied" );
        } catch ( AccessDeniedException e ) {

        }

        Collection<Variant> vCollection3 = variantDao.findBySubjectPatientId( patientId );

        assertTrue( vCollection3.isEmpty() );

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

        GenomicRange gr = new GenomicRange();
        gr.setChromosome( "X" );
        gr.setBaseStart( 3 );
        gr.setBaseEnd( 234 );
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

        Project p = projectDao.findByProjectName( projectId );
        aclTestUtils.checkHasAcl( p );

        try {

            projectManager.deleteProject( projectId );

        } catch ( Exception e ) {
            fail( "projectManager.deleteProject failed" );

        }

        aclTestUtils.checkDeletedAcl( p );

    }

}