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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectOverlapFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.SubjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.SubjectLabelProperty;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;
import ubic.basecode.util.BatchIterator;

/**
 * @author anton
 * @version $Id: SubjectDaoImpl.java,v 1.20 2013/06/19 18:23:35 anton Exp $
 */
@Repository
public class SubjectDaoImpl extends SecurableDaoBaseImpl<Subject> implements SubjectDao {

    @Autowired
    private PhenotypeUtil phenotypeUtils;

    @Autowired
    private VariantDao variantDao;

    @Autowired
    public SubjectDaoImpl( SessionFactory sessionFactory ) {
        super( Subject.class );
        super.setSessionFactory( sessionFactory );
    }

    @Override
    public Collection<Subject> findByLabel( LabelValueObject label ) {
        Criteria criteria = currentSession().createCriteria( Subject.class );
        SimpleRestriction restrictionExpression = new SimpleRestriction( new SubjectLabelProperty(),
                Operator.TEXT_EQUAL, label );
        Criterion criterion = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                CriteriaBuilder.EntityType.SUBJECT );
        criteria.add( criterion );
        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Subject> findByPatientIds( Project project, Collection<String> patientIds ) {

        Set<Subject> result = new HashSet<Subject>();
        BatchIterator<String> it = BatchIterator.batches( patientIds, 200 );
        Query query = this
                .getSessionFactory()
                .getCurrentSession()
                .createQuery(
                        "from Subject as subject"
                                + " where subject.patientId in (:patientIds) and :project in elements(subject.projects)" );
        query.setParameter( "project", project );
        // Query query = this.getSessionFactory().getCurrentSession()
        // .createQuery( "from Subject as subject where subject.patientId in (:patientIds)" );
        for ( ; it.hasNext(); ) {
            query.setParameterList( "patientIds", it.next() );
            result.addAll( query.list() );
        }
        return result;

    }

    @Override
    @Transactional(readOnly = true)
    public Subject findByPatientId( Project project, String patientId ) {

        Query query = this
                .getSessionFactory()
                .getCurrentSession()
                .createQuery(
                        "from Subject as subject"
                                + " where subject.patientId = :patientId and :project in elements(subject.projects)" );
        query.setParameter( "patientId", patientId );
        query.setParameter( "project", project );

        // The patientId is unique within a project so there should be only one result
        return ( Subject ) query.uniqueResult();
    }

    // This patientId is no longer unique so this method could throw hibernate nonUnique result exception
    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public Subject findByPatientId( String patientId ) {
        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "from Subject as subject where subject.patientId = :patientId" );
        query.setParameter( "patientId", patientId );

        return ( Subject ) query.uniqueResult();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubc.pavlab.aspiredb.server.dao.SubjectDao#findByPatientIds(java.util.Collection)
     */
    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public Collection<Subject> findByPatientIds( Collection<String> patientIds ) {
        Set<Subject> result = new HashSet<Subject>();
        BatchIterator<String> it = BatchIterator.batches( patientIds, 200 );
        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "from Subject as subject where subject.patientId in (:patientIds)" );
        for ( ; it.hasNext(); ) {
            query.setParameterList( "patientIds", it.next() );
            result.addAll( query.list() );
        }
        return result;
    }

    @Override
    public Collection<Subject> findByPhenotype( PhenotypeFilterConfig filterConfig ) {

        Session session = this.getSessionFactory().getCurrentSession();
        Criteria criteria = session.createCriteria( Subject.class );

        filterConfig = phenotypeUtils.expandOntologyTerms( filterConfig, filterConfig.getActiveProjectIds() );

        RestrictionExpression restrictionExpression = filterConfig.getRestriction();

        criteria.createAlias( "phenotypes", "phenotype" );
        Criterion junction = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                CriteriaBuilder.EntityType.SUBJECT );
        criteria.add( junction );

        return criteria.list();

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Subject> findPatients( String queryString ) {
        // Escape the character '_'. Otherwise, users cannot use it in the query string.
        String queryWithWildcards = "%" + queryString.replaceAll( "_", "`_" ) + "%";
        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "select distinct p from Subject as p where p.patientId like :queryString ESCAPE '`'" );
        query.setParameter( "queryString", queryWithWildcards );
        return query.list();
    }

    @Override
    @Transactional(readOnly = true)
    @Secured({ "GROUP_USER", "AFTER_ACL_COLLECTION_READ" })
    public Collection<? extends Subject> load( Set<AspireDbFilterConfig> filters ) throws BioMartServiceException,
            NeurocartaServiceException {
        return loadPage( 0, 2000, null, null, filters );
    }

    @Override
    public Collection<Subject> loadByVariantIds( List<Long> variantIds ) {

        Collection<Variant> variants = ( Collection<Variant> ) variantDao.load( variantIds );

        HashSet<Subject> subjects = new HashSet<Subject>();

        for ( Variant v : variants ) {

            subjects.add( v.getSubject() );

        }

        return subjects;

    }

    @Override
    @Transactional(readOnly = true)
    public Page<? extends Subject> loadPage( int offset, int limit, String sortProperty, String sortDirection,
            Set<AspireDbFilterConfig> filters ) throws BioMartServiceException, NeurocartaServiceException {

        assert ( filters != null );

        // Apply filters and get ids.
        List<Long> subjectIds = getFilteredIds( filters );

        // Load objects.
        Collection<Subject> results = this.load( subjectIds );

        // FIXME: finish this
        // Sort results.
        List<Subject> sortedResults = new ArrayList<Subject>( results );

        // Get page.
        if ( limit > 0 ) {
            sortedResults = sortedResults.subList( 0, Math.min( limit, sortedResults.size() ) );
        }

        return new PageBean<Subject>( sortedResults, subjectIds.size() );
    }

    @Override
    public Collection<String> suggestValuesForEntityProperty( Property property, SuggestionContext suggestionContext ) {
        Session session = currentSession();

        Criteria criteria = session.createCriteria( Subject.class );
        if ( suggestionContext.getValuePrefix() != null ) {
            String valuePrefix = suggestionContext.getValuePrefix();
            String valueWildcard = valuePrefix.replaceAll( "_", "\\_" ) + "%";

            criteria.add( Restrictions.like( property.getName(), valueWildcard ) );
        }
        criteria.setProjection( Projections.distinct( Projections.property( property.getName() ) ) )
                .createAlias( "projects", "project" )
                .add( Restrictions.in( "project.id", suggestionContext.getActiveProjectIds() ) );

        return criteria.list();
    }

    // - use Factory pattern with registration? to map config to appropriate filter subclass
    // FOR NOW: use getClass
    private void addSingleFilter( AspireDbFilterConfig filter, Criteria criteria ) throws BioMartServiceException,
            NeurocartaServiceException {
        if ( filter.getClass() == VariantFilterConfig.class ) {
            VariantFilterConfig locationFilter = ( VariantFilterConfig ) filter;
            RestrictionExpression restriction = locationFilter.getRestriction();
            addSingleVariantFilter( restriction, criteria );
        } else if ( filter.getClass() == SubjectFilterConfig.class ) {
            SubjectFilterConfig subjectFilter = ( SubjectFilterConfig ) filter;
            RestrictionExpression restrictionExpression = subjectFilter.getRestriction();
            Criterion criterion = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                    CriteriaBuilder.EntityType.SUBJECT );
            criteria.createAlias( "labels", "subject_label", CriteriaSpecification.LEFT_JOIN );
            criteria.add( criterion );
        } else if ( filter.getClass() == ProjectFilterConfig.class ) {
            ProjectFilterConfig projectFilter = ( ProjectFilterConfig ) filter;
            criteria.createAlias( "projects", "project" ).add(
                    Restrictions.in( "project.id", projectFilter.getProjectIds() ) );

        }
    }

    private void addSingleVariantFilter( RestrictionExpression restrictionExpression, Criteria criteria ) {
        criteria.createAlias( "variants", "variant" ).createAlias( "variant.location", "location" )
        // .createAlias("labels", "subject_label", CriteriaSpecification.LEFT_JOIN)
        // .createAlias("variant.labels", "variant_label", CriteriaSpecification.LEFT_JOIN)
                .createAlias( "variant.characteristics", "characteristic", CriteriaSpecification.LEFT_JOIN );
        Criterion junction = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                CriteriaBuilder.EntityType.SUBJECT );
        criteria.add( junction );
    }

    private List<Long> findIds( AspireDbFilterConfig filter ) throws BioMartServiceException,
            NeurocartaServiceException {

        if ( filter instanceof ProjectOverlapFilterConfig ) {

            List<Long> variantIds = variantDao.getProjectOverlapVariantIds( ( ProjectOverlapFilterConfig ) filter );

            Collection<Subject> subjects = this.loadByVariantIds( variantIds );

            ArrayList<Long> subjectIds = new ArrayList<Long>();

            for ( Subject s : subjects ) {
                subjectIds.add( s.getId() );
            }

            return subjectIds;

        } else if ( filter instanceof PhenotypeFilterConfig ) {
            Collection<Subject> subjects = findByPhenotype( ( PhenotypeFilterConfig ) filter );

            ArrayList<Long> subjectIds = new ArrayList<Long>();

            for ( Subject s : subjects ) {
                subjectIds.add( s.getId() );
            }

            return subjectIds;
        }

        Session session = this.getSessionFactory().getCurrentSession();
        Criteria criteria = session.createCriteria( Subject.class );
        addSingleFilter( filter, criteria );
        criteria.setProjection( Projections.distinct( Projections.id() ) );
        return criteria.list();
    }

    private List<Long> getFilteredIds( Set<AspireDbFilterConfig> filterConfigs ) throws BioMartServiceException,
            NeurocartaServiceException {
        Iterator<AspireDbFilterConfig> iterator = filterConfigs.iterator();
        AspireDbFilterConfig filterConfig = iterator.next();
        // First iteration
        List<Long> subjectIds = findIds( filterConfig );

        while ( iterator.hasNext() ) {
            filterConfig = iterator.next();
            List<Long> ids = findIds( filterConfig );

            // Intersect results
            subjectIds.retainAll( ids );

            // Stop if nothing is left to filter.
            if ( subjectIds.isEmpty() ) {
                break;
            }
        }
        return subjectIds;
    }

}
