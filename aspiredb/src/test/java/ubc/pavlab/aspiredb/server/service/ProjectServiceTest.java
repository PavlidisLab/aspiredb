/*
 * The aspiredb project
 * 
 * Copyright (c) 2014 University of British Columbia
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
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

/**
 * TODO Document Me
 * 
 * @author ptan
 * @version $Id$
 */
public class ProjectServiceTest extends BaseSpringContextTest {

    @Autowired
    ProjectService projectService;

    @Autowired
    SubjectService subjectService;

    @Autowired
    ProjectManager projectManager;

    @Autowired
    QueryService queryService;

    @Autowired
    ProjectDao projectDao;

    @Autowired
    SubjectDao subjectDao;

    @Autowired
    PhenotypeService phenoService;

    final String projectName = RandomStringUtils.randomAlphabetic( 5 );
    final String testDir = "src/test/resources/data/";
    final String phenotypeFilename = testDir + "/testphenotype.csv";
    final String subjectFilename = testDir + "/testcnv.csv";
    private Set<AspireDbFilterConfig> projectFilter;
    private Project project;
    private Collection<Subject> subjects;

    @Before
    public void setUp() throws Exception {
        project = projectManager.createProject( projectName, "" );
    }

    @After
    public void tearDown() throws Exception {
        new InlineTransaction() {
            @Override
            public void instructions() {
                subjectDao.remove( subjects );
            }
        }.execute();
        projectManager.deleteProject( projectName );
    }

    @Test
    public void testAddSubjectVariantToProject() throws Exception {
        Project project = projectManager.findProject( projectName );
        if ( project != null ) {
            projectService.deleteProject( projectName );
        }
        String msg = projectService.addSubjectVariantsToProject( subjectFilename, true, projectName, "CNV" );
        project = projectManager.findProject( projectName );
        subjects = projectService.getSubjects( projectName );

        assertNotNull( project );

        Collection<Long> ids = new HashSet<>();
        for ( Subject s : subjects ) {
            ids.add( s.getId() );
        }
        subjects = subjectDao.load( ids );

        assertEquals( 3, subjects.size() );
    }

    @Test
    public void testAddSubjectPhenotypeToProject() throws Exception {

        Project project = projectManager.findProject( projectName );
        if ( project != null ) {
            projectService.deleteProject( projectName );
        }
        String msg = projectService.addSubjectPhenotypeToProject( phenotypeFilename, true, projectName );
        project = projectManager.findProject( projectName );
        subjects = projectService.getSubjects( projectName );

        assertNotNull( project );

        Collection<Long> ids = new HashSet<>();
        for ( Subject s : subjects ) {
            ids.add( s.getId() );
        }
        subjects = subjectDao.load( ids );

        assertEquals( 3, subjects.size() );

        for ( Subject s : subjects ) {
            Map<String, PhenotypeValueObject> phenotypes = phenoService.getPhenotypes( s.getId() );
            assertEquals( 5, phenotypes.size() );
        }
    }
}
