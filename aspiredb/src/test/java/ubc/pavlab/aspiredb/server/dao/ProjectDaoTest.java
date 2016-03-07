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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import gemma.gsec.authentication.UserDetailsImpl;
import gemma.gsec.authentication.UserManager;
import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.security.authorization.acl.AclTestUtils;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

public class ProjectDaoTest extends BaseSpringContextTest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    PersistentTestObjectHelper testObjectHelper;

    @Autowired
    UserManager userManager;

    @Autowired
    AclTestUtils aclUtils;

    String projectName = "ProjectDaoTest";

    private Subject subject;
    private Subject subject2;
    private Project project;

    @After
    public void cleanup() {
        super.runAsAdmin();
        new InlineTransaction() {
            @Override
            public void instructions() {
                subjectDao.remove( subject );
                subjectDao.remove( subject2 );
                projectDao.remove( project );
            }
        }.execute();
    }

    @Before
    public void setup() {
        super.runAsAdmin();
        new InlineTransaction() {
            @Override
            public void instructions() {
                Project p = new Project();
                p.setName( projectName );
                project = testObjectHelper.createPersistentProject( p );

                subject2 = testObjectHelper.createPersistentTestIndividualObject( "testPatientId2-ProjectDaoTest" );
                subject2 = testObjectHelper.addSubjectToProject( subject2, project );

                subject = testObjectHelper.createPersistentTestIndividualObject( "testPatientId-ProjectDaoTest" );
                subject = testObjectHelper.addSubjectToProject( subject, project );

            }
        }.execute();
    }

    @Test
    public void testCreateAndFindProjectWithIndividuals() throws Exception {

        super.runAsAdmin();

        Project persistentProject = projectDao.findByProjectName( projectName );

        assertEquals( projectName, persistentProject.getName() );

        List<Long> projectList = new ArrayList<Long>();

        projectList.add( persistentProject.getId() );

        assertEquals( 2, projectDao.getSubjectCountForProjects( projectList ).intValue() );

    }

    @Test
    public void testProjectDaoSecurity() throws Exception {

        super.runAsAdmin();

        String someUsername = RandomStringUtils.randomAlphabetic( 6 );

        try {
            userManager.loadUserByUsername( someUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", someUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        super.runAsUser( someUsername );

        try {
            Project p = projectDao.findByProjectName( projectName );
            log.debug( "Project '" + projectName + "' has acls " + aclUtils.getAcl( p ) );
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException e ) {

        }

    }

}
