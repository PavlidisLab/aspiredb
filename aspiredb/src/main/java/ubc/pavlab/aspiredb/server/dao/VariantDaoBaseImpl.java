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

import gemma.gsec.SecurityService;
import gemma.gsec.authentication.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.model.Variant2VariantOverlap;
import ubc.pavlab.aspiredb.server.util.GenomeBin;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.NumericValue;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
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
 * @author anton
 */
public abstract class VariantDaoBaseImpl<T extends Variant> extends DaoBaseImpl<T> implements VariantDaoBase<T> {

    private Class<T> elementClass;

    @Autowired
    private Variant2SpecialVariantOverlapDao variant2SpecialVariantOverlapDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private PhenotypeUtil phenotypeUtils;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserManager userManager;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected VariantDaoBaseImpl( Class elementClass ) {
        super( elementClass );
        assert elementClass.isAssignableFrom( elementClass );
        this.elementClass = elementClass;
    }

    @Override
    public void printCacheStatistics() {
        String regionName = this.elementClass.getName();
        Statistics stats = this.getSessionFactory().getStatistics();
        SecondLevelCacheStatistics secondLevelStats = stats.getSecondLevelCacheStatistics( regionName );
        log.info( "SecondLevelCache:" + regionName + "," + secondLevelStats );
        // Map cacheEntries = secondLevelStats.getEntries();
        // log.info( "cacheEntries=" + cacheEntries.size() + ", keyset="
        // + StringUtils.collectionToCommaDelimitedString( cacheEntries.keySet() ) );
    }

    // TODO Refactor this so we only have one findByGenomicLocation()
    @Override
    @Transactional(readOnly = true)
    public Collection<Long> findByGenomicLocation( GenomicRange range ) {
        SimpleRestriction restriction = new SimpleRestriction( new GenomicLocationProperty(), Operator.IS_IN_SET, range );
        Session session = this.getSessionFactory().getCurrentSession();

        // Criteria criteria = session.createCriteria( this.elementClass );
        // criteria.createAlias( "location", "location" );
        // criteria.createAlias( "subject", "subject" ).createAlias( "subject.projects", "project" )
        // .add( Restrictions.in( "project.id", activeProjectIds ) );
        //
        // criteria.add( CriteriaBuilder.buildCriteriaRestriction( restriction, CriteriaBuilder.EntityType.VARIANT ) );
        //
        // criteria.setProjection( Projections.distinct( Projections.id() ) );
        // Collection<Long> variantIds = criteria.list();
        //
        // // load from cache if it exists
        // Collection<T> variants = new HashSet<>();
        // for ( Long id : variantIds ) {
        // variants.add( ( T ) session.get( this.elementClass, id ) );
        // }

        List<Integer> bins = GenomeBin.relevantBins( range.getChromosome(), range.getBaseStart(), range.getBaseEnd() );
        // String hql =
        // "select variant.id from Variant variant inner join variant.location as location WHERE location.bin in (:bins) and location.chromosome=:chromosome and ((location.start>=:start and location.end<=:end) or (location.start<=:start and location.end>=:start) or (location.start<=:end and location.end>=:end))";
        String hql = "select id from GenomicLocation location WHERE location.bin in (:bins) and location.chromosome=:chromosome and ((location.start>=:start and location.end<=:end) or (location.start<=:start and location.end>=:start) or (location.start<=:end and location.end>=:end))";
        Query query = session.createQuery( hql );
        query.setParameterList( "bins", bins );
        query.setParameter( "chromosome", range.getChromosome() );
        query.setParameter( "start", range.getBaseStart() );
        query.setParameter( "end", range.getBaseEnd() );
        Collection<Long> ids = query.list();

        // List<T> variants = criteria.list();
        return ids;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<VariantValueObject> loadByGenomicLocationIDs( Collection<Long> genomicLocIDs ) {

        if ( genomicLocIDs == null || genomicLocIDs.size() == 0 ) {
            return new ArrayList<>();
        }

        Session session = this.getSessionFactory().getCurrentSession();
        String hql = "select distinct variant.id, location.chromosome, location.start, location.end from Variant variant inner join variant.location as location WHERE location.id in (:locIDs)";
        Query query = session.createQuery( hql );

        query.setParameterList( "locIDs", genomicLocIDs );

        Collection<Object[]> results = query.list();

        Collection<VariantValueObject> ret = new ArrayList<>();
        for ( Object[] r : results ) {
            VariantValueObject vvoOverlapped = new VariantValueObject();
            vvoOverlapped.setId( ( Long ) r[0] );
            vvoOverlapped.setGenomicRange( new GenomicRange( ( String ) r[1], ( int ) r[2], ( int ) r[3] ) );
            ret.add( vvoOverlapped );
        }

        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<T> findByGenomicLocation( GenomicRange range, Collection<Long> activeProjectIds ) {
        SimpleRestriction restriction = new SimpleRestriction( new GenomicLocationProperty(), Operator.IS_IN_SET, range );

        Session session = this.getSessionFactory().getCurrentSession();

        // Criteria criteria = session.createCriteria( this.elementClass );
        // criteria.createAlias( "location", "location" );
        // criteria.createAlias( "subject", "subject" ).createAlias( "subject.projects", "project" )
        // .add( Restrictions.in( "project.id", activeProjectIds ) );
        //
        // criteria.add( CriteriaBuilder.buildCriteriaRestriction( restriction, CriteriaBuilder.EntityType.VARIANT ) );
        //
        // criteria.setProjection( Projections.distinct( Projections.id() ) );
        // Collection<Long> variantIds = criteria.list();
        //
        // // load from cache if it exists
        // Collection<T> variants = new HashSet<>();
        // for ( Long id : variantIds ) {
        // variants.add( ( T ) session.get( this.elementClass, id ) );
        // }

        List<Integer> bins = GenomeBin.relevantBins( range.getChromosome(), range.getBaseStart(), range.getBaseEnd() );
        String hql = "select distinct variant from Variant variant inner join variant.location as location inner join variant.subject.projects as projects WHERE :projectIds in projects.id and location.bin in (:bins) and location.chromosome=:chromosome and ((location.start>=:start and location.end<=:end) or (location.start<=:start and location.end>=:start) or (location.start<=:end and location.end>=:end))";
        Query query = session.createQuery( hql );
        query.setParameterList( "bins", bins );
        query.setParameterList( "projectIds", activeProjectIds );
        query.setParameter( "chromosome", range.getChromosome() );
        query.setParameter( "start", range.getBaseStart() );
        query.setParameter( "end", range.getBaseEnd() );
        Collection<T> variants = query.list();

        // List<T> variants = criteria.list();
        return variants;
    }

    /**
     * Returns list of IDs that satisfy the PhenotypeFilter. Get a list of Subjects first then get the list of all the
     * Variants for that Subject. This is done for performance reasons.
     * 
     * @param filterConfig
     */
    public List<Variant> findByPhenotype( PhenotypeFilterConfig filterConfig ) {

        Collection<Subject> subjects = subjectDao.findByPhenotype( filterConfig );

        if ( subjects.size() == 0 ) {
            return new ArrayList<Variant>();
        }

        return this.getSessionFactory().getCurrentSession().createCriteria( this.elementClass )
                .add( Restrictions.in( "subject", subjects ) ).list();

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<T> findBySubjectPatientId( Long projectId, String id ) {
        if ( projectId == null ) {
            log.warn( "Project id is null, retrieving all variants with patient id " + id );
            return findBySubjectPatientId( id );
        }

        List<Subject> subjects = this.getSessionFactory().getCurrentSession().createCriteria( Subject.class )
                .add( Restrictions.eq( "patientId", id ) ).createAlias( "projects", "project" )
                .add( Restrictions.eq( "project.id", projectId ) ).list();

        if ( subjects.size() == 0 ) {
            return new ArrayList<T>();
        }

        List<T> variants = this.getSessionFactory().getCurrentSession().createCriteria( this.elementClass )
                .add( Restrictions.in( "subject", subjects ) ).list();
        return variants;
    }

    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public Collection<T> findBySubjectPatientId( String id ) {

        List<Subject> subjects = this.getSessionFactory().getCurrentSession().createCriteria( Subject.class )
                .add( Restrictions.eq( "patientId", id ) ).list();

        if ( subjects.size() == 0 ) {
            return new ArrayList<T>();
        }

        List<T> variants = this.getSessionFactory().getCurrentSession().createCriteria( this.elementClass )
                .add( Restrictions.in( "subject", subjects ) ).list();
        return variants;
    }

    /**
     * @param overlapFilter
     * @return
     */
    public List<Long> getProjectOverlapVariantIds( ProjectOverlapFilterConfig overlapFilter ) {

        // overlapProjectIds are required for this method
        if ( overlapFilter.getOverlapProjectIds() == null || overlapFilter.getOverlapProjectIds().isEmpty() ) {
            return new ArrayList<Long>();
        }

        List<Long> activeProjectsVariantIds = getVariantIdsForProjects( overlapFilter.getProjectIds() );

        PhenotypeRestriction phenRestriction = overlapFilter.getPhenotypeRestriction();

        Boolean hasPhenotypeRestriction = phenRestriction != null && phenRestriction.getValue() != null
                && phenRestriction.getName() != null;

        List<Long> overlapProjsPhenoAssociatedVariantIds = new ArrayList<>();

        // Get variants in specified overlapping projects with specified phenotype for easier checking later
        if ( hasPhenotypeRestriction ) {
            overlapProjsPhenoAssociatedVariantIds = getPhenoAssociatedVariantIdsForProjects( overlapFilter );
        }

        SimpleRestriction overlapRestriction1 = ( SimpleRestriction ) overlapFilter.getRestriction1();

        // if this is false then retrieve all overlaps
        Boolean hasOverlapRestriction = validateOverlapRestriction( overlapRestriction1 );

        SimpleRestriction numVariantsOverlapRestriction = ( SimpleRestriction ) overlapFilter.getRestriction2();

        Boolean hasSecondaryOverlapRestriction = validateOverlapRestriction( numVariantsOverlapRestriction );

        SimpleRestriction supportOfVariantsOverlapRestriction = ( SimpleRestriction ) overlapFilter.getRestriction3();

        Boolean hasTertiaryOverlapRestriction = validateOverlapRestriction( supportOfVariantsOverlapRestriction );

        List<Long> variantIdsSatisfyingRestrictions = new ArrayList<>();

        log.info( "Iterating through variants in projectids:" + overlapFilter.getProjectIds()
                + " for overlap with variants in projectids:" + overlapFilter.getOverlapProjectIds()
                + "\n hasOverlapRestriction:" + hasOverlapRestriction + " hasSecondaryOverlapRestriction:"
                + hasSecondaryOverlapRestriction + " hasTertiaryOverlapRestriction: " + hasTertiaryOverlapRestriction
                + " hasPhenotypeRestriction:" + hasPhenotypeRestriction );

        // Iterate over all variants in active Projects to see if they meet the restriction criteria
        for ( Long vId : activeProjectsVariantIds ) {

            Collection<Variant2VariantOverlap> infos = new ArrayList<>();

            if ( hasOverlapRestriction ) {

                infos = getOverlapsSatisfyingInitialOverlapRestriction( vId, overlapRestriction1,
                        overlapFilter.getOverlapProjectIds() );

                if ( infos.size() == 0 ) {

                    NumericValue numeric1 = ( NumericValue ) overlapRestriction1.getValue();
                    Integer value1 = numeric1.getValue();

                    // if this is a less than restriction we have to include overlaps of 0, entries of which do not
                    // exist in the Variant2SpecialVariantOverlap table
                    if ( overlapRestriction1.getOperator().equals( Operator.NUMERIC_LESS_OR_EQUAL ) ) {

                        Collection<Variant2VariantOverlap> allInfos = variant2SpecialVariantOverlapDao.loadByVariantId(
                                vId, overlapFilter.getOverlapProjectIds() );

                        // Further to the comment above, if the vId's overlapinfos returned by the restriction are zero,
                        // but it has overlapinfo's,
                        // we want to skip it because ALL of its overlaps are not less than the restriction
                        if ( allInfos.size() != 0 ) {
                            continue;
                        }
                        // else this vId should fall through to the bottom and get added if there are no more
                        // restrictions

                    } else if ( overlapRestriction1.getOperator().equals( Operator.NUMERIC_GREATER_OR_EQUAL )
                            && value1 < 1 ) {
                        // edge case, all vids implicitly have an overlap of 0 with something, (unless they overlap with
                        // every variant in the project but I am not going to worry about that)
                        // this means that this vId is still in consideration
                    } else {

                        // If this is a "GREATER_THAN" and no overlaps meet the restriction, we skip this vId and
                        // continue with the next one
                        continue;
                    }

                }

            } else {
                // get all overlaps
                infos = variant2SpecialVariantOverlapDao.loadByVariantId( vId, overlapFilter.getOverlapProjectIds() );

                if ( infos.size() == 0 ) {

                    if ( hasSecondaryOverlapRestriction
                            && satisfiesSecondaryOverlapRestriction( numVariantsOverlapRestriction, infos ) ) {
                        // This is for the case where a vId has no overlaps, yet wants all overlaps "less then" a number
                        // so overlaps of '0' (not stored in the database) need to be taken into account
                        // (this behaviour is what was requested in the meeting)
                    } else {

                        continue;
                    }
                }
            }

            // This will be the case where the user asks: show me variants that "do"/"do not" overlap with x number
            // of variants in DGV/DECIPHER
            if ( hasSecondaryOverlapRestriction ) {

                if ( !satisfiesSecondaryOverlapRestriction( numVariantsOverlapRestriction, infos ) ) {

                    // This variant id(vId) does not satisfy the secondary overlap restriction, continue to the next vId
                    continue;

                }

            }

            // This is the case where the user wants to restrict based on the number of different support evidence
            if ( hasTertiaryOverlapRestriction ) {

                if ( !satisfiesTertiaryOverlapRestriction( supportOfVariantsOverlapRestriction, infos ) ) {
                    continue;
                }

            }

            if ( hasPhenotypeRestriction ) {

                Boolean satisfiesPhenotypeRestriction = false;

                for ( Variant2VariantOverlap info : infos ) {

                    // if the overlapped variant has the specified phenotype associated with it
                    if ( overlapProjsPhenoAssociatedVariantIds.contains( info.getOverlapSpecialVariantId() ) ) {
                        satisfiesPhenotypeRestriction = true;
                        // all of this 'infos' deal with the same variantId so we can break on the first one
                        break;
                    }

                }

                if ( !satisfiesPhenotypeRestriction ) {
                    continue;
                }

            }

            // if we get to this point, then we know that this variantId satisfies all the restrictions
            variantIdsSatisfyingRestrictions.add( vId );

        }

        if ( overlapFilter.getInvert() != null && overlapFilter.getInvert() ) {

            activeProjectsVariantIds.removeAll( variantIdsSatisfyingRestrictions );

            return activeProjectsVariantIds;

        }

        return variantIdsSatisfyingRestrictions;

    }

    /**
     * Returns list of Subject and Variant IDs that satisfy the PhenotypeFilter. Get a list of Subjects first then get
     * the list of all the Variants for that Subject. This is done for performance reasons. {@link Bug#3892}
     * 
     * @param filterConfig
     * @return key = {@link VariantDao#SUBJECT_IDS_KEY} and {@link VariantDao#VARIANT_IDS_KEY}
     */
    public Map<Integer, Collection<Long>> getSubjectVariantIdsByPhenotype( PhenotypeFilterConfig filterConfig ) {

        Collection<Subject> subjects = subjectDao.findByPhenotype( filterConfig );

        Collection<Variant> variants = this.getSessionFactory().getCurrentSession().createCriteria( this.elementClass )
                .add( Restrictions.in( "subject", subjects ) ).list();

        Collection<Long> subjectIds = new HashSet<Long>();
        Collection<Long> variantIds = new HashSet<Long>();

        for ( Subject s : subjects ) {
            subjectIds.add( s.getId() );
        }
        for ( Variant v : variants ) {
            variantIds.add( v.getId() );
        }

        Map<Integer, Collection<Long>> map = new HashMap<Integer, Collection<Long>>();
        map.put( VariantDao.SUBJECT_IDS_KEY, subjectIds );
        map.put( VariantDao.VARIANT_IDS_KEY, variantIds );

        return map;

    }

    @Override
    public Collection<? extends T> load( Set<AspireDbFilterConfig> filters ) throws BioMartServiceException,
            NeurocartaServiceException {
        return loadPage( 0, 0, null, null, filters );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<? extends T> loadPage( int offset, int limit, String sortField, String sortDirection,
            Set<AspireDbFilterConfig> filters ) throws BioMartServiceException, NeurocartaServiceException {
        assert ( filters != null );

        List<Long> variantIds = getFilteredIds( filters );
        List<T> variants = new ArrayList<T>( this.load( variantIds ) );
        // List<T> variants = new ArrayList<T>( loadVariants( variantIds ) );
        if ( limit == 0 ) {
            limit = variants.size();
        }
        int pageSize = Math.min( limit, variants.size() );
        List<T> variantPage = variants.subList( 0, pageSize );

        return new PageBean( variantPage, variantIds.size() );
    }

    private Session currentSession() {
        return getSession();
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

    // - use Factory pattern with registration? to map config to appropriate filter subclass
    // FOR NOW: use getClass
    private void addSingleFilter( AspireDbFilterConfig filter, Criteria criteria ) {
        if ( filter.getClass() == VariantFilterConfig.class ) {
            VariantFilterConfig locationFilter = ( VariantFilterConfig ) filter;
            RestrictionExpression restriction = locationFilter.getRestriction();
            addSingleVariantFilter( restriction, criteria );

            // This is now handled by getVariantIdsByProject() for faster performance
            // } else if ( filter.getClass() == ProjectFilterConfig.class ) {
            // ProjectFilterConfig projectFilter = ( ProjectFilterConfig ) filter;
            // criteria.createAlias( "subject", "subject" ).createAlias( "subject.projects", "project" )
            // .add( Restrictions.in( "project.id", projectFilter.getProjectIds() ) );

        } else if ( filter.getClass() == SubjectFilterConfig.class ) {
            SubjectFilterConfig subjectFilter = ( SubjectFilterConfig ) filter;
            RestrictionExpression restrictionExpression = subjectFilter.getRestriction();
            Criterion criterion = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                    CriteriaBuilder.EntityType.VARIANT );
            criteria.createAlias( "subject", "subject" ).createAlias( "subject.labels", "subject_label",
                    CriteriaSpecification.LEFT_JOIN );
            criteria.add( criterion );
        }
    }

    private void addSingleVariantFilter( RestrictionExpression restrictionExpression, Criteria criteria ) {
        criteria.createAlias( "location", "location" ).createAlias( "subject", "subject" );
        // .createAlias("subject.labels", "subject_label", CriteriaSpecification.LEFT_JOIN)
        // .createAlias("labels", "variant_label", CriteriaSpecification.LEFT_JOIN)
        // .createAlias( "characteristics", "characteristic", CriteriaSpecification.LEFT_JOIN );
        Criterion junction = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                CriteriaBuilder.EntityType.VARIANT );
        criteria.add( junction );
    }

    private List<Long> findIds( AspireDbFilterConfig filter ) {

        // for performance reasons as Criteria can be slow
        if ( filter instanceof ProjectFilterConfig ) {

            return this.getVariantIdsByProject( ( ProjectFilterConfig ) filter );

            // Project overlap filter requires a little more data processing than the other filters and uses
            // precalculated
            // database table
            // as it doesn't quite fit the same paradigm as the other filters I am breaking it off into its own method
        } else if ( filter instanceof ProjectOverlapFilterConfig ) {

            return this.getProjectOverlapVariantIds( ( ProjectOverlapFilterConfig ) filter );

        } else if ( filter instanceof PhenotypeFilterConfig ) {
            List<Variant> variants = findByPhenotype( ( PhenotypeFilterConfig ) filter );

            ArrayList<Long> variantIds = new ArrayList<Long>();

            for ( Variant v : variants ) {
                variantIds.add( v.getId() );
            }

            return variantIds;
        }

        Session session = this.getSessionFactory().getCurrentSession();
        Criteria criteria = session.createCriteria( this.elementClass );

        addSingleFilter( filter, criteria );

        criteria.setProjection( Projections.distinct( Projections.id() ) );

        return criteria.list();
    }

    private List<Long> getVariantIdsByProject( ProjectFilterConfig filter ) {

        Long projectId = filter.getProjectIds().iterator().next();

        Query query = this.getSessionFactory().getCurrentSession()
                .createQuery( "select v.id from Variant v left join v.subject.projects p where p.id = :id" );

        query.setParameter( "id", projectId );

        return query.list();
    }

    private List<Long> getFilteredIds( Set<AspireDbFilterConfig> filters ) {
        Iterator<AspireDbFilterConfig> iterator = filters.iterator();
        AspireDbFilterConfig filterConfig = iterator.next();
        // First iteration
        List<Long> variantIds = findIds( filterConfig );

        while ( iterator.hasNext() ) {
            filterConfig = iterator.next();
            List<Long> ids = findIds( filterConfig );

            // intersect results
            variantIds.retainAll( ids );

            // if size is 0 -> stop
            if ( variantIds.isEmpty() ) {
                break;
            }
        }
        return variantIds;
    }

    // This restriction is of the type:
    // "Show me variants that overlap/mutually overlap by less/greater than x bases/percentage"
    // find overlaps for a specific variant Id that meet this restriction
    // Note that the functionality requested by Sanja was that in the case of a "LESS THAN" ALL of a variants overlaps
    // must meet this restriction and we return all of its overlaps
    // however in the case of "GREATER THAN" we only return the overlaps that meet this restriction. (we don't require
    // that "ALL OF THE VARIANTS MUST MEET THE RESTRICTION)
    // so this means that it will either be all overlaps returned or no overlaps
    private Collection<Variant2VariantOverlap> getOverlapsSatisfyingInitialOverlapRestriction( Long vId,
            SimpleRestriction overlapRestriction, Collection<Long> overlapProjectIds ) {

        Collection<Variant2VariantOverlap> allOverlaps = variant2SpecialVariantOverlapDao.loadByVariantId( vId,
                overlapProjectIds );

        Collection<Variant2VariantOverlap> overlapsMeetingRestriction = variant2SpecialVariantOverlapDao
                .loadByVariantIdAndOverlap( vId, overlapRestriction, overlapProjectIds );

        if ( overlapRestriction.getOperator().equals( Operator.NUMERIC_LESS_OR_EQUAL )
                && allOverlaps.size() == overlapsMeetingRestriction.size() ) {
            // we could also have returned overlapsMeetingRestriction as they are identical sets
            return allOverlaps;

        }

        if ( overlapRestriction.getOperator().equals( Operator.NUMERIC_GREATER_OR_EQUAL ) ) {
            return overlapsMeetingRestriction;

        }

        return new ArrayList<Variant2VariantOverlap>();

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

    // This will be the case where the user asks: show me variants that "do"/"do not" overlap with x number of variants
    // in DGV/DECIPHER
    private Boolean satisfiesSecondaryOverlapRestriction( SimpleRestriction overlapRestriction,
            Collection<Variant2VariantOverlap> overlaps ) {

        Operator o = overlapRestriction.getOperator();

        NumericValue numeric = ( NumericValue ) overlapRestriction.getValue();

        Integer value = numeric.getValue();

        if ( o.equals( Operator.NUMERIC_GREATER_OR_EQUAL ) ) {
            return overlaps.size() >= value;
        } else if ( o.equals( Operator.NUMERIC_LESS_OR_EQUAL ) ) {
            return overlaps.size() <= value;
        }

        return false;

    }

    private Boolean satisfiesTertiaryOverlapRestriction( SimpleRestriction overlapRestriction,
            Collection<Variant2VariantOverlap> overlaps ) {
        // If this is a less than, we probably have to take into account the the variants with 0 overlap????
        Set<String> supportSet = new HashSet<String>();

        // check and warn
        if ( overlaps.iterator().hasNext() ) {
            Long projectId = overlaps.iterator().next().getOverlapProjectId();
            if ( projectDao.getOverlapProjectVariantSupportCharacteristicKey( projectId ) == null ) {
                log.warn( "Support key is null for overlap project '" + projectDao.load( projectId ).getName() + "'" );
            }
        }

        // note all of these overlaps are associated with the same variantId
        for ( Variant2VariantOverlap overlap : overlaps ) {

            Variant v = load( overlap.getOverlapSpecialVariantId() );

            String supportKey = projectDao.getOverlapProjectVariantSupportCharacteristicKey( overlap
                    .getOverlapProjectId() );

            for ( Characteristic c : v.getCharacteristics() ) {

                if ( c.getKey().equals( supportKey ) && c.getValue() != null ) {

                    supportSet.add( c.getValue() );

                }

            }

        }

        Operator o = overlapRestriction.getOperator();

        NumericValue numeric = ( NumericValue ) overlapRestriction.getValue();

        Integer value = numeric.getValue();

        if ( o.equals( Operator.NUMERIC_GREATER_OR_EQUAL ) ) {
            return supportSet.size() >= value;
        } else if ( o.equals( Operator.NUMERIC_LESS_OR_EQUAL ) ) {
            return supportSet.size() <= value;
        }

        return false;

    }

    private Boolean validateOverlapRestriction( SimpleRestriction r ) {
        // TODO test other things like value and type, discern if it is percentage or number of bases

        if ( r == null || r.getValue() == null ) {
            return false;
        }

        NumericValue numeric = ( NumericValue ) r.getValue();
        Integer value = numeric.getValue();

        return value != null && value >= 0 && r.getOperator() != null;
    }
}
