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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private SubjectDao subjectDao;

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

        log.info( " criteria.list() in findIds" );
        criteria.setProjection( Projections.distinct( Projections.id() ) );
        return criteria.list();
    }

    /**
     * Returns list of IDs that satisfy the PhenotypeFilter. Get a list of Subjects first then get the list of all the
     * Variants for that Subject. This is done for performance reasons.
     * 
     * @param filterConfig
     */
    public List<Variant> findByPhenotype( PhenotypeFilterConfig filterConfig ) {

        Collection<Subject> subjects = subjectDao.findByPhenotype( filterConfig );

        return this.getSessionFactory().getCurrentSession().createCriteria( this.elementClass )
                .add( Restrictions.in( "subject", subjects ) ).list();

    }

    /**
     * Returns list of Subject and Variant IDs that satisfy the PhenotypeFilter. 
     * Get a list of Subjects first then get the list of all the Variants for that Subject. 
     * This is done for performance reasons. {@link Bug#3892}
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

    public List<Long> getProjectOverlapVariantIds( ProjectOverlapFilterConfig overlapFilter ) {

        List<Long> activeProjectsVariantIds = getVariantIdsForProjects( overlapFilter.getProjectIds() );

        PhenotypeRestriction phenRestriction = overlapFilter.getPhenotypeRestriction();

        Boolean hasPhenotypeRestriction = phenRestriction != null && phenRestriction.getValue() != null
                && phenRestriction.getName() != null;

        List<Long> overlapProjsPhenoAssociatedVariantIds = new ArrayList<Long>();

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

        ArrayList<Long> variantIdsSatisfyingRestrictions = new ArrayList<Long>();

        log.info( "Iterating through variants in projectids:" + overlapFilter.getProjectIds()
                + " for overlap with variants in projectids:" + overlapFilter.getOverlapProjectIds()
                + "\n hasOverlapRestriction:" + hasOverlapRestriction + " hasSecondaryOverlapRestriction:"
                + hasSecondaryOverlapRestriction + " hasTertiaryOverlapRestriction: " + hasTertiaryOverlapRestriction
                + " hasPhenotypeRestriction:" + hasPhenotypeRestriction );

        // Iterate over all variants in active Projects to see if they meet the restriction criteria
        for ( Long vId : activeProjectsVariantIds ) {

            Collection<Variant2SpecialVariantOverlap> infos = new ArrayList<Variant2SpecialVariantOverlap>();

            if ( hasOverlapRestriction ) {

                infos = getOverlapsSatisfyingInitialOverlapRestriction( vId, overlapRestriction1,
                        overlapFilter.getOverlapProjectIds() );

                if ( infos.size() == 0 ) {

                    // if this is a less than restriction we have to include overlaps of 0, entries of which do not
                    // exist in the Variant2SpecialVariantOverlap table
                    if ( overlapRestriction1.getOperator().equals( Operator.NUMERIC_LESS ) ) {
                        
                        Collection<Variant2SpecialVariantOverlap> allInfos = variant2SpecialVariantOverlapDao.loadByVariantId( vId, overlapFilter.getOverlapProjectIds() );                        
                        
                        //Further to the comment above, if the vId's overlapinfos returned by the restriction are zero, but it has overlapinfo's,
                        //we want to skip it because ALL of its overlaps are not less than the restriction
                        if (allInfos.size()!=0){
                            continue;
                        }
                        //else this vId should fall through to the bottom and get added if there are no more restrictions
                        
                        
                    }else{
                        
                        //If this is a "GREATER_THAN" and no overlaps meet the restriction, we skip this vId and continue with the next one
                        continue;
                    }

                }

            } else {
                // get all overlaps
                infos = variant2SpecialVariantOverlapDao.loadByVariantId( vId, overlapFilter.getOverlapProjectIds() );
                
                if ( infos.size() == 0 ){
                    
                    if (hasSecondaryOverlapRestriction  && satisfiesSecondaryOverlapRestriction( numVariantsOverlapRestriction, infos ) ){
                        //This is for the case where a vId has no overlaps, yet wants all overlaps "less then" a number
                        //so overlaps of '0' (not stored in the database) need to be taken into account
                        //(this behaviour is what was requested in the meeting)
                    }else{
                    
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

                for ( Variant2SpecialVariantOverlap info : infos ) {

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
        
        if(overlapFilter.getInvert()!= null && overlapFilter.getInvert()){
            
            activeProjectsVariantIds.removeAll( variantIdsSatisfyingRestrictions );
            
            return activeProjectsVariantIds;
            
        }

        return variantIdsSatisfyingRestrictions;

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

    // This restriction is of the type:
    // "Show me variants that overlap/mutually overlap by less/greater than x bases/percentage"
    // find overlaps for a specific variant Id that meet this restriction
    // Note that the functionality requested by Sanja was that in the case of a "LESS THAN" ALL of a variants overlaps must meet this restriction and we return all of its overlaps
    // however in the case of "GREATER THAN" we only return the overlaps that meet this restriction. (we don't require that "ALL OF THE VARIANTS MUST MEET THE RESTRICTION)
    // so this means that it will either be all overlaps returned or no overlaps
    private Collection<Variant2SpecialVariantOverlap> getOverlapsSatisfyingInitialOverlapRestriction( Long vId,
            SimpleRestriction overlapRestriction, Collection<Long> overlapProjectIds ) {

        Collection<Variant2SpecialVariantOverlap> allOverlaps = variant2SpecialVariantOverlapDao.loadByVariantId( vId,
                overlapProjectIds );

        Collection<Variant2SpecialVariantOverlap> overlapsMeetingRestriction = variant2SpecialVariantOverlapDao
                .loadByVariantIdAndOverlap( vId, overlapRestriction, overlapProjectIds );

        if (overlapRestriction.getOperator().equals( Operator.NUMERIC_LESS ) && allOverlaps.size() == overlapsMeetingRestriction.size() ) {
            //we could also have returned overlapsMeetingRestriction as they are identical sets
            return allOverlaps;
            
        }
        
        
        if (overlapRestriction.getOperator().equals( Operator.NUMERIC_GREATER )){
            return overlapsMeetingRestriction;
            
        }

        return new ArrayList<Variant2SpecialVariantOverlap>();

    }

    // This will be the case where the user asks: show me variants that "do"/"do not" overlap with x number of variants
    // in DGV/DECIPHER
    private Boolean satisfiesSecondaryOverlapRestriction( SimpleRestriction overlapRestriction,
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

    private Boolean satisfiesTertiaryOverlapRestriction( SimpleRestriction overlapRestriction,
            Collection<Variant2SpecialVariantOverlap> overlaps ) {
        // If this is a less than, we probably have to take into account the the variants with 0 overlap????
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
