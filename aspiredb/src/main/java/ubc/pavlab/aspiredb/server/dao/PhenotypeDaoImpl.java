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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.Phenotype;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: PhenotypeDaoImpl.java,v 1.30 2013/07/12 17:11:46 cmcdonald Exp $
 */
@Repository("phenotypeDao")
public class PhenotypeDaoImpl extends DaoBaseImpl<Phenotype> implements PhenotypeDao {

    @Autowired
    SubjectDao individualDao;

    private Session currentSession() {
        return getSession();
    }

    @Autowired
    public PhenotypeDaoImpl( SessionFactory sessionFactory ) {
        super( Phenotype.class );
        super.setSessionFactory( sessionFactory );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Phenotype> findBySubjectId( Long id ) {

        List<Phenotype> phenotypes = currentSession().createCriteria( Phenotype.class )
                .createAlias( "subject", "subject" ).add( Restrictions.eq( "subject.id", id ) ).list();

        return phenotypes;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer findPhenotypeCountBySubjectId( Long id ) {

        Criteria criteria = currentSession().createCriteria( Phenotype.class ).createAlias( "subject", "subject" )
                .add( Restrictions.eq( "subject.id", id ) );
        criteria.setProjection( Projections.distinct( Projections.id() ) );
        List<Long> ids = criteria.list();

        return ids.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Phenotype> findPresentByProjectIdsAndUri( Collection<Long> ids, String uri ) {

        Session session = currentSession();

        Criteria criteria = session.createCriteria( Phenotype.class ).createAlias( "subject", "subject" )
                .createAlias( "subject.projects", "project" ).add( Restrictions.in( "project.id", ids ) )
                .add( Restrictions.eq( "valueType", "HPONTOLOGY" ) ).add( Restrictions.eq( "value", "1" ) )
                .add( Restrictions.eq( "uri", uri ) );

        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctOntologyUris( Collection<Long> activeProjects ) {

        Session session = currentSession();

        Criteria criteria = session.createCriteria( Phenotype.class ).createAlias( "subject", "subject" )
                .createAlias( "subject.projects", "project" ).add( Restrictions.in( "project.id", activeProjects ) )
                .add( Restrictions.eq( "valueType", "HPONTOLOGY" ) );

        criteria.setProjection( Projections.distinct( Projections.property( "uri" ) ) );

        return criteria.list();

    }

    @Override
    public List<String> getExistingNames( Collection<Long> activeProjectIds ) {
        Session session = currentSession();

        Criteria criteria;

        if ( activeProjectIds != null ) {
            criteria = session.createCriteria( Phenotype.class ).createAlias( "subject", "subject" )
                    .createAlias( "subject.projects", "project" )
                    .add( Restrictions.in( "project.id", activeProjectIds ) );
        } else {
            criteria = session.createCriteria( Phenotype.class );
        }

        criteria.setProjection( Projections.distinct( Projections.property( "name" ) ) );

        return criteria.list();
    }

    @Override
    public List<String> getExistingPhenotypes( String name, boolean isExactMatch, Collection<Long> activeProjects ) {
        String queryString = isExactMatch ? name : "%" + name + "%";

        Session session = currentSession();

        Criteria criteria = session.createCriteria( Phenotype.class ).createAlias( "subject", "subject" )
                .createAlias( "subject.projects", "project" ).add( Restrictions.in( "project.id", activeProjects ) )
                .add( Restrictions.like( "name", queryString ) );

        criteria.setProjection( Projections.distinct( Projections.property( "name" ) ) );

        return criteria.list();
    }

    @Override
    public List<String> getExistingURIs( String name ) {
        Query query = currentSession().createQuery( "select distinct p.uri from Phenotype as p where p.name=:name" );
        query.setParameter( "name", name );
        return query.list();
    }

    @Override
    public List<String> getExistingValues( String name ) {
        Query query = currentSession().createQuery( "select distinct p.value from Phenotype as p where p.name=:name" );
        query.setParameter( "name", name );
        return query.list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getListOfPossibleValuesByName( Collection<Long> projectIds, String name ) {
        Session session = currentSession();

        Criteria criteria = session.createCriteria( Phenotype.class );
        criteria.add( Restrictions.eq( "name", name ) );
        criteria.setProjection( Projections.distinct( Projections.property( "value" ) ) );
        criteria.createAlias( "subject", "subject" ).createAlias( "subject.projects", "project" )
                .add( Restrictions.in( "project.id", projectIds ) );

        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getListOfPossibleValuesByUri( Collection<Long> projectIds, String uri ) {
        Session session = currentSession();

        Criteria criteria = session.createCriteria( Phenotype.class ).createAlias( "subject", "subject" );
        criteria.add( Restrictions.eq( "uri", uri ) );

        criteria.createCriteria( "subject.projects" ).add( Restrictions.in( "id", projectIds ) );
        criteria.setProjection( Projections.distinct( Projections.property( "value" ) ) );

        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInDatabase( Collection<String> names ) {
        Session session = currentSession();

        Criteria criteria = session.createCriteria( Phenotype.class );
        criteria.add( Restrictions.in( "name", names ) );

        return !criteria.list().isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Phenotype> loadAllByProjectIds( Collection<Long> projectIds ) {
        Session session = currentSession();

        Criteria criteria = session.createCriteria( Phenotype.class ).createAlias( "subject", "subject" );
        criteria.createCriteria( "subject.projects" ).add( Restrictions.in( "id", projectIds ) );
        criteria.setProjection( Projections.distinct( Projections.id() ) );

        List<Long> ids = criteria.list();

        return this.load( ids );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Phenotype> loadBySubjectIds( Collection<Long> subjectIds ) {
        if ( subjectIds.isEmpty() ) {
            return new HashSet<Phenotype>();
        }
        Query query = currentSession().createQuery( "from Phenotype as p where p.subject.id IN (:subjectIds)" );
        query.setParameterList( "subjectIds", subjectIds );

        return query.list();
    }
}