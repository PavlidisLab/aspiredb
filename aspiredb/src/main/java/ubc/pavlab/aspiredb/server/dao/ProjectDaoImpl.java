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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;

import java.util.Collection;

@Repository("projectDao")
public class ProjectDaoImpl extends SecurableDaoBaseImpl<Project> implements ProjectDao{
    
    protected static Log log = LogFactory.getLog( ProjectDaoImpl.class );
    
    @Autowired
    public ProjectDaoImpl( SessionFactory sessionFactory ) {
        super( Project.class );
        super.setSessionFactory( sessionFactory );
    }
   
    @Override
    @Transactional(readOnly=true)
    public Project findByProjectName( String projectName){
        
        Query query=this.getSessionFactory().getCurrentSession()
                .createQuery( "from Project as proj where proj.name=:proj" );
        
        query.setParameter( "proj", projectName );
        
        return (Project) query.uniqueResult();        
        
    }
    
    @Override
    @Transactional(readOnly=true)
    public Integer getVariantCountForProjects(Collection<Long> projectIds){
        
        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "select count(*) from Variant v join v.subject subject join subject.projects projs where projs.id in(:ids )");        
                
        query.setParameterList( "ids", projectIds  );
        
        Long count = (Long)query.uniqueResult();
        
        return count.intValue();
        
    }
    
    @Override
    @Transactional(readOnly=true)
    public Integer getSubjectCountForProjects(Collection<Long> projectIds){
        
        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "select count(*) from Subject subject join subject.projects projs where projs.id in(:ids )");
        
        query.setParameterList( "ids", projectIds  );
                
        Long count = (Long)query.uniqueResult();
        
        return count.intValue();
        
    }
    
    @Override
    @Transactional
    public void addSubjectToProject(Project project, Subject subject){
        
        Collection<Subject> currentSubjects = project.getSubjects();
        
        for (Subject s: currentSubjects){
            if (s.getPatientId().equals( subject.getPatientId() )){                
                log.error( "Subject names need to be unique in projects. Subject not added" );
                return;
            }
        }
        
        currentSubjects.add( subject);
        
    }
    
        
    
}