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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.AspireDbPagingLoadConfig;
import ubc.pavlab.aspiredb.shared.AspireDbPagingLoadConfigBean;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;

/**
 * author: anton date: 22/05/13
 */
public class QueryServiceTest extends BaseSpringContextTest {

    @Autowired
    private QueryService queryService;

    @Autowired
    private PersistentTestObjectHelper persistentTestObjectHelper;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    private PhenotypeUtil phenotypeUtil;

    private Project project;

    private String HP_HEAD = "Abnormality of the head";
    private String HP_FACE = "Abnormality of the face";
    private String HP_MOUTH = "Abnormality of the mouth";
    private String HP_NERVOUS = "Abnormality of the nervous system";

    private Collection<Long> activeProjectIds;

    private static Log log = LogFactory.getLog( QueryServiceTest.class.getName() );

    @Before
    public void setUp() {
        createSubjectWithPhenotypes( "1", "0", "0", "0" );
        createSubjectWithPhenotypes( "0", "1", "0", "0" );
        createSubjectWithPhenotypes( "0", "0", "1", "0" );
        createSubjectWithPhenotypes( "0", "0", "0", "1" );
        createSubjectWithPhenotypes( "1", "0", "1", "0" );
    }

    @After
    public void tearDown() throws Exception {
        for ( Subject s : project.getSubjects() ) {
            try {
                
                for (Phenotype p : s.getPhenotypes() ) {
                    phenotypeDao.remove( p );
                }
                
                subjectDao.remove( s );
                
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        projectDao.remove( project );
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
        activeProjectIds = projectIds;

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

    /**
     * Counts the number of Subjects that satisfies "restriction"
     * 
     * @param restriction
     * @return
     * @throws NotLoggedInException
     * @throws ExternalDependencyException
     */
    private List<SubjectValueObject> querySubjects( PhenotypeRestriction restriction ) throws NotLoggedInException,
            ExternalDependencyException {
        Collection<Long> projectIds = new HashSet<Long>();
        projectIds.add( project.getId() );
        ProjectFilterConfig projConfig = new ProjectFilterConfig();
        projConfig.setProjectIds( projectIds );

        PhenotypeFilterConfig phenoConfig = new PhenotypeFilterConfig();
        phenoConfig.setRestriction( restriction );
        phenoConfig.setActiveProjectIds( activeProjectIds );
        
        Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
        filters.add( projConfig );
        filters.add( phenoConfig );
        AspireDbPagingLoadConfig loadConfig = new AspireDbPagingLoadConfigBean();
        loadConfig.setFilters( filters );
        return queryService.querySubjects( filters ).getItems();
    }

    /**
     * Tests for absence of Phenotypes
     */
    @Test
    public void testPhenoHasNoValueSubjects() {

        try {
            int found = 0;

            assertEquals( 5, project.getSubjects().size() );

            Collection<String> phenoNames = phenotypeDao.getExistingNames( activeProjectIds );
            assertEquals( 4, phenoNames.size() );

            PhenotypeRestriction restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the head" );
            restriction.setValue( "0" );
            List<SubjectValueObject> result = querySubjects( restriction );
            log.debug( "head 0 size=" + result.size() );
            for ( SubjectValueObject svo : result ) {
                log.debug( "head 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_NERVOUS ) != -1 ) {
                    found++;
                }
            }
            assertEquals( 1, result.size() );
            assertEquals( 1, found );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the face" );
            restriction.setValue( "0" );
            result = querySubjects( restriction );
            found = 0;
            for ( SubjectValueObject svo : result ) {
                log.debug( "face 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_HEAD ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_NERVOUS ) != -1 ) {
                    found++;
                }
            }
            log.debug( "face 0 result.size=" + result.size() );
            assertEquals( 2, result.size() );
            assertEquals( 2, found );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the mouth" );
            restriction.setValue( "0" );
            result = querySubjects( restriction );
            found = 0;
            for ( SubjectValueObject svo : result ) {
                log.debug( "mouth 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_HEAD ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_FACE ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_NERVOUS ) != -1 ) {
                    found++;
                }
            }
            log.debug( "mouth 0 result.size=" + result.size() );
            assertEquals( 3, result.size() );
            assertEquals( 3, found );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the nervous system" );
            restriction.setValue( "0" );
            result = querySubjects( restriction );
            found = 0;
            for ( SubjectValueObject svo : result ) {
                log.debug( "nervous 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_HEAD ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_FACE ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_MOUTH ) != -1 ) {
                    found++;
                }
            }
            log.debug( "nervous 0 result.size=" + result.size() );
            assertEquals( 4, result.size() );
            assertEquals( 4, found );

        } catch ( Exception e ) {
            fail( e.getMessage() );
        }

    }

    /**
     * Tests for presence of Phenotypes
     */
    @Test
    public void testPhenoHasValueSubjects() {

        try {
            Collection<String> phenoNames = phenotypeDao.getExistingNames( activeProjectIds );
            assertEquals( 4, phenoNames.size() );

            PhenotypeRestriction restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the head" );
            restriction.setValue( "1" );
            List<SubjectValueObject> result = querySubjects( restriction );
            assertEquals( 4, result.size() );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the face" );
            restriction.setValue( "1" );
            result = querySubjects( restriction );
            for ( SubjectValueObject svo : result ) {
                log.debug( "face 1 svo " + svo.getPatientId() );
            }
            assertEquals( 3, result.size() );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the mouth" );
            restriction.setValue( "1" );
            result = querySubjects( restriction );
            for ( SubjectValueObject svo : result ) {
                log.debug( "mouth 1 svo " + svo.getPatientId() );
            }
            assertEquals( 2, result.size() );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the nervous system" );
            restriction.setValue( "1" );
            result = querySubjects( restriction );
            for ( SubjectValueObject svo : result ) {
                log.debug( "nervous 1 svo " + svo.getPatientId() );
            }
            assertEquals( 1, result.size() );

        } catch ( Exception e ) {
            fail( e.getMessage() );
        }

    }

}
