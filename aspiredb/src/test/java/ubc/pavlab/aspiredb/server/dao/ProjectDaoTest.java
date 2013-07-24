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



import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;





import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;



public class ProjectDaoTest extends BaseSpringContextTest {
    
    @Autowired
    private ProjectDao projectDao;    
        
    @Autowired
    PersistentTestObjectHelper testObjectHelper;
    
    @Autowired
    UserManager userManager;
    
    String projectName = RandomStringUtils.randomAlphabetic( 5 );
       
    @Test
    public void testCreateAndFindProjectWithIndividuals() throws Exception{
        
        String patientId1 = RandomStringUtils.randomAlphabetic( 5 );
        
        String patientId2 = RandomStringUtils.randomAlphabetic( 6 );
        
        super.runAsAdmin();
        
        Subject ind1 = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId1 );
        Subject ind2 = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId2 );
                
        Project detachedProject = new Project();
        
        detachedProject.setName( projectName );
        
        Project p = projectDao.create( detachedProject );
        
        projectDao.addSubjectToProject(p, ind1 );
        projectDao.addSubjectToProject(p, ind2 );
        
        projectDao.update( p );
        
        
        Project persistentProject = projectDao.findByProjectName( projectName );
        
        assertEquals(projectName, persistentProject.getName());
        
        Collection<Subject> projectIndividuals = persistentProject.getSubjects();
        
                
        assertEquals(2, projectIndividuals.size());        
        
        HashSet<Long> indIds = new HashSet<Long>();
        
        for (Subject i: projectIndividuals){            
            indIds.add( i.getId() );
        }
        
        assertTrue(indIds.contains( ind1.getId() ));
        
        assertTrue(indIds.contains( ind2.getId() ));
        
        
    }
    
    @Test
    public void testProjectDaoSecurity() throws Exception{
        
        String patientId1 = RandomStringUtils.randomAlphabetic( 5 );
        
        String patientId2 = RandomStringUtils.randomAlphabetic( 6 );
        
        super.runAsAdmin();
        
        Subject ind1 = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId1 );
        Subject ind2 = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId2 );
        List<Subject> indList = new ArrayList<Subject>();
        
        indList.add( ind1 );
        indList.add( ind2 );
        
        Project detachedProject = new Project();
        
        detachedProject.setName( projectName );
        
        Project p = projectDao.create( detachedProject );
        
        projectDao.addSubjectToProject(p, ind1 );
        projectDao.addSubjectToProject(p, ind2 );
        
        String someUsername = RandomStringUtils.randomAlphabetic( 6 );
        
        try {
            userManager.loadUserByUsername( someUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", someUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }
        
        super.runAsUser( someUsername );
        
        try{
        projectDao.findByProjectName( projectName );
        fail( "Should have gotten an access denied" );
        }
        catch(AccessDeniedException e){
            
        }
        
        
        
        
        
        
        
    }
    
    
    
    @Test
    public void testProjectDaoNumIndividuals() throws Exception{
        
        String patientId1 = RandomStringUtils.randomAlphabetic( 5 );
        
        String patientId2 = RandomStringUtils.randomAlphabetic( 6 );
        
        String projectId = RandomStringUtils.randomAlphabetic( 6 );
        
                
        super.runAsAdmin();
        
        //each individual should have 1 cnv
        Subject ind1 = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId1 );
        Subject ind2 = testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId2 );
        
        
        Project detachedProject = new Project();
        
        detachedProject.setName( projectId);
        
        Project p = projectDao.create( detachedProject );
        
        projectDao.addSubjectToProject(p, ind1 );
        projectDao.addSubjectToProject(p, ind2 );
        projectDao.update( p );
        
        
        Project persistentProject = projectDao.findByProjectName( projectId );
        
        Collection<Long> projectCollection = new ArrayList<Long>();
                
        projectCollection.add( persistentProject.getId() );
        
        assertEquals((Integer)2, projectDao.getSubjectCountForProjects( projectCollection ));
        
        
        
    }
    
    
    
 
}
