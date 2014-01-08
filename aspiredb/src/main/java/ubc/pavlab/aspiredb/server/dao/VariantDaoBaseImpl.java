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

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantOverlap;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.NumericValue;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.GenomicLocationProperty;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectOverlapFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.SubjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: VariantDaoBaseImpl.java,v 1.32 2013/07/02 18:20:21 anton Exp $
 */

public abstract class VariantDaoBaseImpl<T extends Variant> extends SecurableDaoBaseImpl<T> implements
        VariantDaoBase<T> {

    private static Log log = LogFactory.getLog( VariantDaoBaseImpl.class.getName() );

    private Class<T> elementClass;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected VariantDaoBaseImpl( Class elementClass ) {
        super( elementClass );
        assert elementClass.isAssignableFrom( elementClass );
        this.elementClass = elementClass;
    }

    @Autowired
    private Variant2SpecialVariantOverlapDao variant2SpecialVariantOverlapDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private PhenotypeUtil phenotypeUtils;

    @Override
    public Collection<T> findByGenomicLocation( GenomicRange range, Collection<Long> activeProjectIds ) {
        SimpleRestriction restriction = new SimpleRestriction( new GenomicLocationProperty(), Operator.IS_IN_SET, range );

        Session session = this.getSessionFactory().getCurrentSession();
        Criteria criteria = session.createCriteria( this.elementClass );
        criteria.createAlias( "location", "location" );
        criteria.createAlias( "subject", "subject" ).createAlias( "subject.projects", "project" )
                .add( Restrictions.in( "project.id", activeProjectIds ) );

        criteria.add( CriteriaBuilder.buildCriteriaRestriction( restriction, CriteriaBuilder.EntityType.VARIANT ) );

        List<T> variants = criteria.list();
        return variants;
    }

    @Override
    public Collection<String> suggestValuesForEntityProperty( Property property, SuggestionContext suggestionContext ) {
        Session session = currentSession();

        Criteria criteria = session.createCriteria( this.elementClass );
        if ( suggestionContext.getValuePrefix() != null ) {
            // TODO: escape certain chars
            String valuePrefix = suggestionContext.getValuePrefix();
            String valueWildcard = "%" + valuePrefix + "%";
            criteria.add( Restrictions.like( property.getName(), valueWildcard ) );
        }
        criteria.setProjection( Projections.distinct( Projections.property( property.getName() ) ) );
        if ( suggestionContext.getActiveProjectIds() != null && !suggestionContext.getActiveProjectIds().isEmpty() ) {
            criteria.createAlias( "subject", "subject" ).createAlias( "subject.projects", "project" )
                    .add( Restrictions.in( "project.id", suggestionContext.getActiveProjectIds() ) );
        }

        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<T> findBySubjectId( String id ) {

        List<Subject> subjects = this.getSessionFactory().getCurrentSession().createCriteria( Subject.class )
                .add( Restrictions.eq( "patientId", id ) ).list();

        if ( subjects.size() == 0 ) {
            return new ArrayList<T>();
        }

        List<T> variants = this.getSessionFactory().getCurrentSession().createCriteria( this.elementClass )
                .add( Restrictions.in( "subject", subjects ) ).list();
        return variants;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<? extends T> loadPage( int offset, int limit, String sortField, String sortDirection,
            Set<AspireDbFilterConfig> filters ) throws BioMartServiceException, NeurocartaServiceException {
        assert ( filters != null );

        List<Long> variantIds = getFilteredIds( filters );
        List<T> variants = new ArrayList<T>( this.load( variantIds ) );
        if ( limit == 0 ) {
            limit = variants.size();
        }
        int pageSize = Math.min( limit, variants.size() );
        List<T> variantPage = variants.subList( 0, pageSize );

        return new PageBean( variantPage, variantIds.size() );
    }

    @Override
    public Collection<? extends T> load( Set<AspireDbFilterConfig> filters ) throws BioMartServiceException,
            NeurocartaServiceException {
        return loadPage( 0, 0, null, null, filters );
    }

    private List<Long> getFilteredIds( Set<AspireDbFilterConfig> filters ) throws BioMartServiceException,
            NeurocartaServiceException {
        Iterator<AspireDbFilterConfig> iterator = filters.iterator();
        AspireDbFilterConfig filterConfig = iterator.next();
        // First iteration

        log.info( "findIds for filterconfig:" + filterConfig.getClass() );
        List<Long> variantIds = findIds( filterConfig );
        log.info( "findIds finished for filterconfig:" + filterConfig.getClass() );

        while ( iterator.hasNext() ) {
            filterConfig = iterator.next();
            log.info( "findIds for filterconfig:" + filterConfig.getClass() );
            List<Long> ids = findIds( filterConfig );

            log.info( "findIds finished for filterconfig:" + filterConfig.getClass() );

            // intersect results
            variantIds.retainAll( ids );

            // if size is 0 -> stop
            if ( variantIds.isEmpty() ) break;
        }
        return variantIds;
    }

    private List<Long> findIds( AspireDbFilterConfig filter ) throws BioMartServiceException,
            NeurocartaServiceException {
        // Project overlap filter requires a little more data processing than the other filters and uses precalculated
        // database table
        // as it doesn't quite fit the same paradigm as the other filters I am breaking it off into its own method
        if ( filter instanceof ProjectOverlapFilterConfig ) {

            return this.getProjectOverlapIds( ( ProjectOverlapFilterConfig ) filter );

        }

        Session session = this.getSessionFactory().getCurrentSession();
        Criteria criteria = session.createCriteria( this.elementClass );

        addSingleFilter( filter, criteria );

        log.info( " criteria.list() in findIds" );
        criteria.setProjection( Projections.distinct( Projections.id() ) );
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
        } else if ( filter.getClass() == ProjectFilterConfig.class ) {
            ProjectFilterConfig projectFilter = ( ProjectFilterConfig ) filter;
            criteria.createAlias( "subject", "subject" ).createAlias( "subject.projects", "project" )
                    .add( Restrictions.in( "project.id", projectFilter.getProjectIds() ) );
        } else if ( filter.getClass() == SubjectFilterConfig.class ) {
            SubjectFilterConfig subjectFilter = ( SubjectFilterConfig ) filter;
            RestrictionExpression restrictionExpression = subjectFilter.getRestriction();
            Criterion criterion = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                    CriteriaBuilder.EntityType.VARIANT );
            criteria.createAlias( "subject", "subject" ).createAlias( "subject.labels", "subject_label",
                    CriteriaSpecification.LEFT_JOIN );
            criteria.add( criterion );
        } else if ( filter.getClass() == PhenotypeFilterConfig.class ) {
            PhenotypeFilterConfig filterConfig = ( PhenotypeFilterConfig ) filter;
            filterConfig = phenotypeUtils.expandOntologyTerms( filterConfig, filterConfig.getActiveProjectIds() );
            RestrictionExpression restrictionExpression = filterConfig.getRestriction();
            Criterion junction = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                    CriteriaBuilder.EntityType.VARIANT );
            criteria.createAlias( "subject", "subject" ).createAlias( "subject.phenotypes", "phenotype" );
            criteria.add( junction );
        }
    }

    private void addSingleVariantFilter( RestrictionExpression restrictionExpression, Criteria criteria ) {
        criteria.createAlias( "location", "location" ).createAlias( "subject", "subject" )
        // .createAlias("subject.labels", "subject_label", CriteriaSpecification.LEFT_JOIN)
        // .createAlias("labels", "variant_label", CriteriaSpecification.LEFT_JOIN)
                .createAlias( "characteristics", "characteristic", CriteriaSpecification.LEFT_JOIN );
        Criterion junction = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                CriteriaBuilder.EntityType.VARIANT );
        criteria.add( junction );
    }

    public List<Long> getProjectOverlapIds( ProjectOverlapFilterConfig overlapFilter ) {

        List<Long> activeProjectsVariantIds = getVariantIdsForProjects( overlapFilter.getProjectIds() );

        PhenotypeRestriction phenRestriction = overlapFilter.getPhenotypeRestriction();

        Boolean hasPhenotypeRestriction = phenRestriction != null && phenRestriction.getValue() != null
                && phenRestriction.getName() != null;

        List<Long> overlapProjsPhenoAssociatedVariantIds = new ArrayList<Long>();

        // Get variants in specified overlapping projects with specified phenotype for easier checking later
        if ( hasPhenotypeRestriction ) {
            overlapProjsPhenoAssociatedVariantIds = getPhenoAssociatedVariantIdsForProjects( overlapFilter );
        }

        SimpleRestriction overlapRestriction = ( SimpleRestriction ) overlapFilter.getRestriction1();

        // if this is false then retrieve all overlaps
        Boolean hasOverlapRestriction = validateOverlapRestriction( overlapRestriction );

        SimpleRestriction numVariantsOverlapRestriction = ( SimpleRestriction ) overlapFilter.getRestriction2();

        Boolean hasSecondaryOverlapRestriction = validateOverlapRestriction( numVariantsOverlapRestriction );

        SimpleRestriction supportOfVariantsOverlapRestriction = ( SimpleRestriction ) overlapFilter.getRestriction3();

        Boolean hasTertiaryOverlapRestriction = validateOverlapRestriction( supportOfVariantsOverlapRestriction );

        Set<Long> variantIdsWithOverlap = new HashSet<Long>();

        log.info( "Iterating through variants in projectids:" + overlapFilter.getProjectIds()
                + " for overlap with variants in projectids:" + overlapFilter.getOverlapProjectIds()
                + "\n hasOverlapRestriction:" + hasOverlapRestriction + " hasSecondaryOverlapRestriction:"
                + hasSecondaryOverlapRestriction +" hasTertiaryOverlapRestriction: "+ hasTertiaryOverlapRestriction + " hasPhenotypeRestriction:" + hasPhenotypeRestriction );

        // Iterate over all variants in active Projects to see if they meet the restriction criteria
        for ( Long vId : activeProjectsVariantIds ) {

            Collection<Variant2SpecialVariantOverlap> infos = new ArrayList<Variant2SpecialVariantOverlap>();

            if ( hasOverlapRestriction ) {
                infos = variant2SpecialVariantOverlapDao.loadByVariantIdAndOverlap( vId,
                        ( SimpleRestriction ) overlapFilter.getRestriction1(), overlapFilter.getOverlapProjectIds() );
            } else {
                infos = variant2SpecialVariantOverlapDao.loadByVariantId( vId, overlapFilter.getOverlapProjectIds() );
            }

            if ( hasPhenotypeRestriction || hasSecondaryOverlapRestriction ) {

                Set<Long> variantIdsWithAssociatedPhenotype = new HashSet<Long>();

                if ( hasPhenotypeRestriction ) {

                    for ( Variant2SpecialVariantOverlap info : infos ) {

                        // if the overlapped variant has the specified phenotype associated with it
                        if ( overlapProjsPhenoAssociatedVariantIds.contains( info.getOverlapSpecialVariantId() ) ) {
                            variantIdsWithAssociatedPhenotype.add( vId );
                            // all of this 'infos' deal with the same variantId so we can break if there is one
                            break;
                        }

                    }

                }

                Set<Long> variantIdsMeetingSecondaryRestriction = new HashSet<Long>();

                // This will be the case where the user asks: show me variants that "do"/"do not" overlap with x number
                // of variants in DGV/DECIPHER
                if ( hasSecondaryOverlapRestriction ) {

                    if ( meetsSecondaryOverlapRestriction( numVariantsOverlapRestriction, infos ) ) {

                        // This will be the case where the wants to ensure the overlapped variant does/doesn't have
                        // support from multiple sources
                        if ( hasTertiaryOverlapRestriction ) {

                            if ( meetsTertiaryOverlapRestriction( supportOfVariantsOverlapRestriction, infos ) ) {
                                variantIdsMeetingSecondaryRestriction.add( vId );
                            }

                        } else {
                            variantIdsMeetingSecondaryRestriction.add( vId );
                        }
                    }

                }

                // Determine whether variant vId meets phenotype and secondary overlap restrictions
                if ( hasPhenotypeRestriction && hasSecondaryOverlapRestriction ) {

                    if ( variantIdsWithAssociatedPhenotype.size() > 0
                            && variantIdsMeetingSecondaryRestriction.size() > 0 ) {
                        variantIdsWithOverlap.add( vId );
                    } else {
                        continue;
                    }

                } else if ( hasPhenotypeRestriction ) {

                    if ( variantIdsWithAssociatedPhenotype.size() > 0 ) {
                        variantIdsWithOverlap.add( vId );
                    }

                } else if ( hasSecondaryOverlapRestriction ) {
                    if ( variantIdsMeetingSecondaryRestriction.size() > 0 ) {
                        variantIdsWithOverlap.add( vId );
                    }
                }

            } else if ( infos.size() > 0 ) {
                variantIdsWithOverlap.add( vId );
            }

        }

        List<Long> overlapToReturn = new ArrayList<Long>();

        overlapToReturn.addAll( variantIdsWithOverlap );

        return overlapToReturn;

    }

    private Boolean validateOverlapRestriction( SimpleRestriction r ) {
        // TODO test other things like value and type, discern if it is percentage or number of bases

        return r != null && r.getValue() != null && r.getOperator() != null;
    }

    // This will be the case where the user asks: show me variants that "do"/"do not" overlap with x number of variants
    // in DGV/DECIPHER
    private Boolean meetsSecondaryOverlapRestriction( SimpleRestriction overlapRestriction,
            Collection<Variant2SpecialVariantOverlap> overlaps ) {

        Operator o = overlapRestriction.getOperator();

        NumericValue numeric = ( NumericValue ) overlapRestriction.getValue();

        Integer value = numeric.getValue();

        if ( o.equals( Operator.NUMERIC_GREATER ) ) {
            return overlaps.size() > value;
        } else if ( o.equals( Operator.NUMERIC_LESS ) ) {
            return overlaps.size() < value;
        } else if ( o.equals( Operator.NUMERIC_EQUAL ) ) {
            return overlaps.size() == value;
        } else if ( o.equals( Operator.NUMERIC_NOT_EQUAL ) ) {
            return overlaps.size() != value;
        }

        return false;

    }

    private Boolean meetsTertiaryOverlapRestriction( SimpleRestriction overlapRestriction,
            Collection<Variant2SpecialVariantOverlap> overlaps ) {

        Set<String> supportSet = new HashSet<String>();

        // note all of these overlaps are associated with the same variantId
        for ( Variant2SpecialVariantOverlap overlap : overlaps ) {

            Variant v = load( overlap.getOverlapSpecialVariantId() );

            String supportKey = projectDao.load( overlap.getOverlapProjectId() ).getVariantSupportCharacteristicKey();

            for ( Characteristic c : v.getCharacteristics() ) {

                if ( c.getKey().equals( supportKey ) && c.getValue() != null ) {

                    supportSet.add( c.getValue() );

                }

            }

        }

        Operator o = overlapRestriction.getOperator();

        NumericValue numeric = ( NumericValue ) overlapRestriction.getValue();

        Integer value = numeric.getValue();

        if ( o.equals( Operator.NUMERIC_GREATER ) ) {
            return supportSet.size() > value;
        } else if ( o.equals( Operator.NUMERIC_LESS ) ) {
            return supportSet.size() < value;
        } else if ( o.equals( Operator.NUMERIC_EQUAL ) ) {
            return supportSet.size() == value;
        } else if ( o.equals( Operator.NUMERIC_NOT_EQUAL ) ) {
            return supportSet.size() != value;
        }

        return false;

    }

    private List<Long> getPhenoAssociatedVariantIdsForProjects( ProjectOverlapFilterConfig overlapFilter ) {

        List<Long> ids = new ArrayList<Long>();

        PhenotypeFilterConfig phenConfig = new PhenotypeFilterConfig();
        phenConfig.setRestriction( overlapFilter.getPhenotypeRestriction() );
        phenConfig.setActiveProjectIds( overlapFilter.getOverlapProjectIds() );

        Set<AspireDbFilterConfig> phenFilterSet = new HashSet<AspireDbFilterConfig>();
        phenFilterSet.add( phenConfig );

        try {

            StopWatch timer = new StopWatch();
            timer.start();

            log.info( "fetching phenotype associated variant ids for overlapped projects" );
            ids = getFilteredIds( phenFilterSet );

            if ( timer.getTime() > 100 ) {
                log.info( "fetching phenotype associated variant ids for overlapped projects took " + timer.getTime()
                        + "ms" );
            }
        } catch ( Exception e ) {
            log.error( "exception while getting projectOverlapIds for phenotype" );
        }

        return ids;

    }

    private List<Long> getVariantIdsForProjects( Collection<Long> projectIds ) {

        ProjectFilterConfig projectFilterConfig = new ProjectFilterConfig();
        projectFilterConfig.setProjectIds( projectIds );

        Set<AspireDbFilterConfig> filterSet = new HashSet<AspireDbFilterConfig>();
        filterSet.add( projectFilterConfig );

        List<Long> projectsVariantIds = new ArrayList<Long>();

        try {
            // get the variantIds of all the variants in the activeProjects to iterate over later and search for overlap
            projectsVariantIds = getFilteredIds( filterSet );
        } catch ( Exception e ) {
            log.error( "exception while getting projectOverlapIds" );
        }

        return projectsVariantIds;

    }
}