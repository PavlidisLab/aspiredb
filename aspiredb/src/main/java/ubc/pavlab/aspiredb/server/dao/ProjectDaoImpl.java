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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gemma.gsec.SecurityService;
import gemma.gsec.acl.domain.AclService;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant2VariantOverlap;

@Repository("projectDao")
public class ProjectDaoImpl extends SecurableDaoBaseImpl<Project> implements ProjectDao {

    protected static Log log = LogFactory.getLog( ProjectDaoImpl.class );

    @Autowired
    Variant2SpecialVariantOverlapDao v2vOverlapDao;

    @Autowired
    private AclService aclService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    public ProjectDaoImpl( SessionFactory sessionFactory ) {
        super( Project.class );
        super.setSessionFactory( sessionFactory );
    }

    @Override
    @Transactional(readOnly = true)
    public Project findByProjectName( String projectName ) {

        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "from Project as proj where proj.name=:proj" );

        query.setParameter( "proj", projectName );

        return ( Project ) query.uniqueResult();

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Project> getOverlapProjects( Collection<Long> ids ) {

        // Currently Only supports 1 project

        if ( ids == null || ids.isEmpty() ) {
            return new ArrayList<Project>();
        }

        Long activeProjectId = ids.iterator().next();

        Collection<Project> projects = this.loadAll();
        Collection<Project> overlapProjects = new ArrayList<Project>();

        for ( Project p : projects ) {

            if ( p.getSpecialData() == null || !p.getSpecialData() ) {

                Collection<Variant2VariantOverlap> overlaps = v2vOverlapDao.loadByProjectIdAndOverlapProjectId(
                        activeProjectId, p.getId() );

                if ( overlaps.size() > 0 ) {
                    overlapProjects.add( p );
                }
            }
        }

        return overlapProjects;

    }

    @Override
    @Transactional(readOnly = true)
    public String getOverlapProjectVariantSupportCharacteristicKey( Long projectId ) {
        return this.load( projectId ).getVariantSupportCharacteristicKey();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Project> getSpecialOverlapProjects() {

        Collection<Project> projects = this.loadAll();
        Collection<Project> overlapProjects = new ArrayList<Project>();

        for ( Project p : projects ) {

            if ( p.getSpecialData() != null && p.getSpecialData() ) {
                overlapProjects.add( p );
            }
        }

        return overlapProjects;

    }

    @Override
    @Transactional(readOnly = true)
    public Integer getSubjectCountForProjects( Collection<Long> projectIds ) {

        Query query = this
                .getSessionFactory()
                .getCurrentSession()
                .createQuery(
                        "select count(*) from Subject subject join subject.project proj where proj.id in (:ids )" );

        query.setParameterList( "ids", projectIds );

        Long count = ( Long ) query.uniqueResult();

        return count.intValue();

    }

    @Override
    @Transactional(readOnly = true)
    public Integer getVariantCountForProjects( Collection<Long> projectIds ) {

        Query query = this
                .getSessionFactory()
                .getCurrentSession()
                .createQuery(
                        "select count(*) from Variant v join v.subject subject join subject.project proj where proj.id in (:ids )" );

        query.setParameterList( "ids", projectIds );

        Long count = ( Long ) query.uniqueResult();

        return count.intValue();

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Object[]> getVariantLocationsForProjects( Long projectId ) {

        Query query = this
                .getSessionFactory()
                .getCurrentSession()
                .createQuery(
                        "select v.location.chromosome, v.location.id from Variant v join v.subject subject join subject.project proj where proj.id = :id" );

        query.setParameter( "id", projectId );

        Collection<Object[]> locIDs = query.list();

        return locIDs;

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Subject> getSubjects( Long projectId ) {
        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "select subject from Subject subject join subject.project proj where proj.id = :id" );

        query.setParameter( "id", projectId );

        return query.list();
    }

    @Override
    public Collection<Long> getAllVariantsForProject( Long projectId ) {
        Query query = this
                .getSessionFactory()
                .getCurrentSession()
                .createQuery(
                        "select variant.id from Variant variant join variant.subject.project proj where proj.id = :id" );

        query.setParameter( "id", projectId );

        return query.list();
    }

    @Override
    public void quickDelete( Long projectId ) {
        // Order of deletion is currently: Characteristic, Variant, Phenotype, Subject, Project, (orphaned labels)? 

        this.getSessionFactory().getCurrentSession().flush();
        this.getSessionFactory().getCurrentSession().clear();

        // Characteristics
        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery(
                        "DELETE FROM Characteristic c WHERE c.variant.id IN "
                                + "(SELECT v.id FROM Variant v "
                                + "inner join v.subject s "
                                + "inner join s.project proj where proj.id = :id)" );
        query.setParameter( "id", projectId );
        query.executeUpdate();

        log.info( "Characteristics deleted" );

        // Variant
        query = this.getSessionFactory().getCurrentSession()
                .createQuery(
                        "DELETE FROM Variant v WHERE v.subject.id IN "
                                + "(SELECT s.id FROM Subject s "
                                + "inner join s.project proj where proj.id = :id)" );
        query.setParameter( "id", projectId );
        query.executeUpdate();

        log.info( "Variants deleted" );

        // Phenotype
        query = this.getSessionFactory().getCurrentSession()
                .createQuery(
                        "DELETE FROM Phenotype p WHERE p.subject.id IN "
                                + "(SELECT s.id FROM Subject s "
                                + "inner join s.project proj where proj.id = :id)" );
        query.setParameter( "id", projectId );
        query.executeUpdate();

        log.info( "Phenotypes deleted" );

        // Subject
        query = this.getSessionFactory().getCurrentSession()
                .createQuery(
                        "DELETE FROM Subject s WHERE s.project.id = :id)" );
        query.setParameter( "id", projectId );
        query.executeUpdate();

        log.info( "Subjects deleted" );

        // Project
        query = this.getSessionFactory().getCurrentSession()
                .createQuery(
                        "DELETE FROM Project proj where proj.id = :id" );
        query.setParameter( "id", projectId );
        query.executeUpdate();

        log.info( "Project deleted" );

    }

}
