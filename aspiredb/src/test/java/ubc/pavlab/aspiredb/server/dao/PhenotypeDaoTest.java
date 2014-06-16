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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

/**
 * @version $Id: PhenotypeDaoTest.java,v 1.7 2013/07/12 17:11:46 cmcdonald Exp $
 */
public class PhenotypeDaoTest extends BaseSpringContextTest {

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    private PersistentTestObjectHelper testObjectHelper;

    String project1Name = RandomStringUtils.randomAlphabetic( 5 );
    String project2Name = RandomStringUtils.randomAlphabetic( 5 );
    String patientId = RandomStringUtils.randomAlphabetic( 5 );
    String patientId2 = RandomStringUtils.randomAlphabetic( 5 );
    String patientId3 = RandomStringUtils.randomAlphabetic( 5 );

    private Subject subject1;
    private Subject subject2;
    private Subject subject3;

    @Before
    public void setup() throws NotLoggedInException {
        new InlineTransaction() {
            @Override
            public void instructions() {

                subject1 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest(
                        patientId, "Abnormality of abnormalities", "uri1", "1" );
                subject2 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest(
                        patientId2, "Abnormality of breakfast cereal", "uri2", "0" );
                subject3 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest(
                        patientId3, "Abnormality of cow", "uri3", "1" );

            }
        }.execute();
    }

    @Test
    public void testfindByProjectIdsAndUri() throws Exception {

        Project p1 = new Project();
        p1.setName( project1Name );

        p1 = testObjectHelper.createPersistentProject( p1 );

        testObjectHelper.addSubjectToProject( subject1, p1 );
        testObjectHelper.addSubjectToProject( subject2, p1 );

        Project p2 = new Project();
        p2.setName( project2Name );

        p2 = testObjectHelper.createPersistentProject( p2 );

        testObjectHelper.addSubjectToProject( subject3, p2 );

        final ArrayList<Long> activeProjects = new ArrayList<Long>();
        activeProjects.add( p1.getId() );

        final ArrayList<Long> activeProjects2 = new ArrayList<Long>();
        activeProjects2.add( p2.getId() );

        new InlineTransaction() {
            @Override
            public void instructions() {
                Collection<Phenotype> phenotypes = phenotypeDao.findPresentByProjectIdsAndUri( activeProjects, "uri1" );

                assertEquals( 1, phenotypes.size() );

                Collection<Phenotype> phenotypes2 = phenotypeDao
                        .findPresentByProjectIdsAndUri( activeProjects2, "uri2" );

                assertEquals( 0, phenotypes2.size() );

            }
        }.execute();

    }

    @Test
    public void testGetDistinctOntologyPhenotypes() throws Exception {

        Project p1 = new Project();
        p1.setName( project1Name );

        p1 = testObjectHelper.createPersistentProject( p1 );

        List<Project> p1List = new ArrayList<Project>();
        p1List.add( p1 );

        testObjectHelper.addSubjectToProject( subject1, p1 );

        testObjectHelper.addSubjectToProject( subject2, p1 );

        Project p2 = new Project();
        p2.setName( project2Name );

        p2 = testObjectHelper.createPersistentProject( p2 );

        testObjectHelper.addSubjectToProject( subject3, p2 );

        final ArrayList<Long> activeProjects = new ArrayList<Long>();
        activeProjects.add( p1.getId() );

        new InlineTransaction() {
            @Override
            public void instructions() {
                Collection<String> distinctPhenotypes = phenotypeDao.getDistinctOntologyUris( activeProjects );

                assertEquals( 2, distinctPhenotypes.size() );

                assertTrue( distinctPhenotypes.contains( "uri1" ) );
                assertTrue( distinctPhenotypes.contains( "uri2" ) );
                assertTrue( !distinctPhenotypes.contains( "uri3" ) );
            }
        }.execute();

    }
}
