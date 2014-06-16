/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gemma.gsec.SecurityService;
import gemma.gsec.util.SecurityUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.security.authorization.acl.AclTestUtils;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

public class LabelServiceTest extends BaseSpringContextTest {

    @Autowired
    private LabelService labelService;

    @Autowired
    private ProjectManager projectManager;

    @Autowired
    private LabelDao labelDao;

    @Autowired
    private PersistentTestObjectHelper persistentTestObjectHelper;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private VariantDao variantDao;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private VariantService variantService;

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    private PhenotypeUtil phenotypeUtil;

    @Autowired
    private SecurityService securityService;

    @Autowired
    UserManager userManager;

    @Autowired
    AclTestUtils aclUtil;

    private Project project;

    private String HP_HEAD = "Abnormality of the head";
    private String HP_FACE = "Abnormality of the face";
    private String HP_MOUTH = "Abnormality of the mouth";
    private String HP_NERVOUS = "Abnormality of the nervous system";

    private Long subjectId;

    private static Log log = LogFactory.getLog( QueryServiceTest.class.getName() );
    String username = RandomStringUtils.randomAlphabetic( 6 );
    String testname = RandomStringUtils.randomAlphabetic( 6 );

    @Before
    public void setUp() {
        Subject subject = createSubjectWithPhenotypes( "1", "0", "0", "0" );

        subjectId = subject.getId();

        createSubjectWithPhenotypes( "0", "1", "0", "0" );
        createSubjectWithPhenotypes( "0", "0", "1", "0" );
        createSubjectWithPhenotypes( "0", "0", "0", "1" );
        createSubjectWithPhenotypes( "1", "0", "1", "0" );

        try {
            userManager.loadUserByUsername( username );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", username, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        String groupName = randomName();
        this.securityService.createGroup( groupName );
        this.securityService.makeWriteableByGroup( subject, groupName );

        this.securityService.addUserToGroup( username, groupName );

    }

    @Test
    public void testMultipleUsersCreateSameLabelName() {

        Collection<LabelValueObject> lvos = null;

        super.runAsAdmin();

        LabelValueObject lvo = new LabelValueObject();

        lvo.setColour( "red" );
        lvo.setName( "blah" );
        lvo.setIsShown( true );

        // Admin created subject, try adding label
        Collection<Long> subjectIds = new ArrayList<Long>();
        subjectIds.add( subjectId );

        lvo = subjectService.addLabel( subjectIds, lvo );
        assertNotNull( lvo );
        Label l = labelDao.load( lvo.getId() );
        aclUtil.checkHasAcl( l );
        assertTrue( SecurityUtil.getCurrentUsername() + " owns the label", securityService.isOwnedByCurrentUser( l ) );
        assertTrue( SecurityUtil.getCurrentUsername() + " can edit the label", securityService.isEditable( l ) );

        // Now let's try a normal user, try adding label
        super.runAsUser( this.username );

        assertFalse( SecurityUtil.getCurrentUsername() + " does not own the label",
                securityService.isOwnedByCurrentUser( l ) );
        Subject subject = persistentTestObjectHelper.createDetachedIndividualObject( "userSubject" );
        Long userSubjectId = subject.getId();
        subjectIds.clear();
        subjectIds.add( userSubjectId );
        try {
            // try adding admin label as a normal user
            lvo = subjectService.addLabel( subjectIds, l.toValueObject() );
            fail( "User can not use admin label" );
        } catch ( AccessDeniedException e ) {

        }

        try {
            securityService.isEditable( l );
            fail( SecurityUtil.getCurrentUsername() + " can not edit the label" );
        } catch ( AccessDeniedException e ) {

        }

        // try admin delete
        super.runAsAdmin();
        lvos = persistentTestObjectHelper.getLabelsForSubject( subjectId );
        assertEquals( 1, lvos.size() );
        try {
            labelService.deleteSubjectLabel( lvos.iterator().next() );
        } catch ( AccessDeniedException e ) {
            fail( "Admin created label" );
        }

    }

    @Test
    public void testDeleteVariant() {

        super.runAsAdmin();

        LabelValueObject lvo = new LabelValueObject();

        lvo.setColour( "red" );
        lvo.setName( "blah" );
        lvo.setIsShown( true );

        CNV v1 = persistentTestObjectHelper.createPersistentTestCNVObject();
        CNV v2 = persistentTestObjectHelper.createPersistentTestCNVObject();
        Collection<Long> variantIdsToLabel = new ArrayList<>();
        variantIdsToLabel.add( v1.getId() );
        variantIdsToLabel.add( v2.getId() );
        variantService.addLabel( variantIdsToLabel, lvo );
        Collection<LabelValueObject> lvos1 = persistentTestObjectHelper.getLabelsForVariant( v1.getId() );
        Collection<LabelValueObject> lvos2 = persistentTestObjectHelper.getLabelsForVariant( v2.getId() );
        assertEquals( 1, lvos1.size() );
        assertEquals( 1, lvos2.size() );

        // only delete label for v1 while keeping label for v2
        Collection<Long> variantIdsToRemove = new ArrayList<>();
        variantIdsToRemove.add( v1.getId() );
        labelService.removeLabelsFromVariants( lvos1, variantIdsToRemove );

        lvos1 = persistentTestObjectHelper.getLabelsForVariant( v1.getId() );
        lvos2 = persistentTestObjectHelper.getLabelsForVariant( v2.getId() );
        assertEquals( 0, lvos1.size() );
        assertEquals( 1, lvos2.size() );
    }

    private Subject createSubjectWithPhenotypes( String headPhenoValue, String facePhenoValue, String mouthPhenoValue,
            String nervousPhenoValue ) {

        if ( project == null ) {
            project = new Project();
            project.setName( RandomStringUtils.randomAlphabetic( 4 ) );
            project = persistentTestObjectHelper.createPersistentProject( project );
        }

        List<Project> plist = new ArrayList<Project>();
        plist.add( project );
        Collection<Long> projectIds = new ArrayList<Long>();
        projectIds.add( project.getId() );
        Phenotype phenoHead = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_HEAD, "HP_0000234",
                "HPONTOLOGY", headPhenoValue );
        Phenotype phenoFace = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_FACE, "HP_0000271",
                "HPONTOLOGY", facePhenoValue );
        Phenotype phenoMouth = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_MOUTH, "HP_0000153",
                "HPONTOLOGY", mouthPhenoValue );
        Phenotype phenoNervous = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_NERVOUS,
                "HP_0000707", "HPONTOLOGY", nervousPhenoValue );

        List<Subject> subjectList = project.getSubjects();
        log.debug( "subjectList.size=" + subjectList.size() );
        String patientId = "" + subjectList.size();

        if ( headPhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_HEAD + "=" + headPhenoValue;
        }

        if ( facePhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_FACE + "=" + facePhenoValue;
        }

        if ( mouthPhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_MOUTH + "=" + mouthPhenoValue;
        }

        if ( nervousPhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_NERVOUS + "=" + nervousPhenoValue;
        }

        Subject subject = persistentTestObjectHelper.createPersistentTestIndividualObject( patientId );
        subject.setProjects( plist );
        subject.addPhenotype( phenoHead );
        subject.addPhenotype( phenoFace );
        subject.addPhenotype( phenoMouth );
        subject.addPhenotype( phenoNervous );
        subjectList.add( subject );
        subjectDao.update( subject );
        phenotypeDao.update( phenoHead );
        phenotypeDao.update( phenoFace );
        phenotypeDao.update( phenoMouth );
        phenotypeDao.update( phenoNervous );

        projectDao.update( project );

        return subject;
    }

}
