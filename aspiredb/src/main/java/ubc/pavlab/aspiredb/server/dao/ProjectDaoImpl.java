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

import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantOverlap;

@Repository("projectDao")
public class ProjectDaoImpl extends SecurableDaoBaseImpl<Project> implements ProjectDao {

    protected static Log log = LogFactory.getLog( ProjectDaoImpl.class );

    @Autowired
    Variant2SpecialVariantOverlapDao v2vOverlapDao;

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

                Collection<Variant2SpecialVariantOverlap> overlaps = v2vOverlapDao.loadByProjectIdAndOverlapProjectId(
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
                        "select count(*) from Subject subject join subject.projects projs where projs.id in(:ids )" );

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
                        "select count(*) from Variant v join v.subject subject join subject.projects projs where projs.id in(:ids )" );

        query.setParameterList( "ids", projectIds );

        Long count = ( Long ) query.uniqueResult();

        return count.intValue();

    }

}