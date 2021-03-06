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
package ubc.pavlab.aspiredb.server.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

import gemma.gsec.SecurityService;
import ubc.pavlab.aspiredb.server.GenomeCoordinateConverter;
import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.dao.Page;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.QueryDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Query;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.server.util.ConfigUtils;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.OntologyTermValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.GeneProperty;
import ubc.pavlab.aspiredb.shared.query.GenomicLocationProperty;
import ubc.pavlab.aspiredb.shared.query.NeurocartaPhenotypeProperty;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.Junction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.SetRestriction;
import ubc.pavlab.aspiredb.shared.suggestions.PhenotypeSuggestion;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.providers.HumanPhenotypeOntologyService;

/**
 * Methods for various query operations such as querying a list of subjects and variants given a list of filters,
 * getting a list of gene suggestions, ontology term suggestions. Also deals with saving and loading of queries.
 * 
 * @author anton
 */
@Service("queryService")
@RemoteProxy(name = "QueryService")
public class QueryServiceImpl implements QueryService {

    private static Logger log = LoggerFactory.getLogger( QueryServiceImpl.class );

    @Autowired
    private BioMartQueryService bioMartQueryService;
    @Autowired
    private GenomeCoordinateConverter converter;

    @Autowired
    private NeurocartaQueryService neurocartaQueryService;
    @Autowired
    private OntologyService ontologyService;

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private QueryDao queryDao;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private VariantDao variantDao;

    @Autowired
    SecurityService securityService;

    @Override
    @Transactional
    @RemoteMethod
    public void deleteQuery( String name ) {
        List<Query> querys = queryDao.findByName( name );
        if ( querys.size() > 0 ) {
            queryDao.remove( querys.iterator().next() );
        }
    }

    @Override
    public PagingLoadResult<GeneValueObject> getGeneSuggestionLoadResult( String query )
            throws BioMartServiceException {
        if ( query.length() < 2 ) {
            return new PagingLoadResultBean<GeneValueObject>( new ArrayList<GeneValueObject>(), 0, 0 );
        }

        return new PagingLoadResultBean<GeneValueObject>(
                new ArrayList<GeneValueObject>( this.bioMartQueryService.findGenes( query ) ), 0, 0 );
    }

    @Override
    public PagingLoadResult<NeurocartaPhenotypeValueObject> getNeurocartaPhenotypeSuggestionLoadResult( String query )
            throws NeurocartaServiceException {
        if ( query.length() < 3 ) {
            return new PagingLoadResultBean<NeurocartaPhenotypeValueObject>(
                    new ArrayList<NeurocartaPhenotypeValueObject>(), 0, 0 );
        }

        return new PagingLoadResultBean<NeurocartaPhenotypeValueObject>(
                new ArrayList<NeurocartaPhenotypeValueObject>( this.neurocartaQueryService.findPhenotypes( query ) ), 0,
                0 );
    }

    @Override
    public PagingLoadResult<OntologyTermValueObject> getOntologyTermSuggestions( String query ) {
        HumanPhenotypeOntologyService HPOService = ontologyService.getHumanPhenotypeOntologyService();
        // if ( !HPOService.isOntologyLoaded() ) {
        // HPOService.startInitializationThread( true );
        // }

        List<OntologyTermValueObject> suggestions = new ArrayList<OntologyTermValueObject>();

        Collection<OntologyTerm> terms = HPOService.findTerm( query + "*" );

        for ( OntologyTerm term : terms ) {
            suggestions.add( new OntologyTermValueObject( term.getTerm(), term.getUri() ) );
        }

        return new PagingLoadResultBean<OntologyTermValueObject>( suggestions, 0, 0 );
    }

    @Override
    @Transactional(readOnly = true)
    public PagingLoadResult<PhenotypeSuggestion> getPhenotypeSuggestionLoadResult( String query,
            Collection<Long> activeProjects ) {
        if ( query.length() < 3 ) {
            return new PagingLoadResultBean<PhenotypeSuggestion>( new ArrayList<PhenotypeSuggestion>(), 0, 0 );
        }

        return new PagingLoadResultBean<PhenotypeSuggestion>( getPhenotypeSuggestions( query, false, activeProjects ),
                0, 0 );
    }

    @Override
    public List<PhenotypeSuggestion> getPhenotypeSuggestions( List<String> names ) {
        // List<PhenotypeSuggestion> phenotypeSuggestions = new ArrayList<PhenotypeSuggestion>(names.size());
        // for (String name: names) {
        // List<PhenotypeSuggestion> currPhenotypeSuggestions = getPhenotypeSuggestions(name, true);
        //
        // if (currPhenotypeSuggestions.size() > 0) {
        // phenotypeSuggestions.add(currPhenotypeSuggestions.get(0));
        // } else {
        // phenotypeSuggestions.add(null);
        // }
        // }
        //
        // return phenotypeSuggestions;
        return null;
    }

    @Override
    @RemoteMethod
    public Collection<String> getSavedQueryNames() {

        Collection<String> queryNames = new ArrayList<String>();
        Collection<Query> queries = queryDao.loadAll();

        for ( Query query : queries ) {
            queryNames.add( query.getName() );
        }
        return queryNames;
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public int getSubjectCount( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException {
        return querySubjects( filters ).getTotalSize();
    }

    @Override
    public List<SubjectValueObject> getSubjects( List<String> subjectIds ) {
        List<SubjectValueObject> vos = new ArrayList<SubjectValueObject>();

        for ( String subjectId : subjectIds ) {
            Subject subject = this.subjectDao.findByPatientId( subjectId );
            vos.add( subject == null ? null : subject.convertToValueObject() );
        }

        return vos;
    }

    @Transactional
    @Override
    public PagingLoadResult<SubjectValueObject> getSubjectSuggestionLoadResult( String query ) {
        if ( query.length() < 2 ) {
            return new PagingLoadResultBean<SubjectValueObject>( new ArrayList<SubjectValueObject>(), 0, 0 );
        }

        List<SubjectValueObject> vos = new ArrayList<SubjectValueObject>();

        for ( Subject subject : this.subjectDao.findPatients( query ) ) {
            vos.add( subject.convertToValueObject() );
        }

        return new PagingLoadResultBean<SubjectValueObject>( vos, 0, 0 );
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Map<Integer, Collection<Object>> getSubjectsVariants( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException {

        Map<Integer, Collection<Long>> svIds = new HashMap<>();
        Map<Integer, Collection<Object>> ret = new HashMap<>();

        Set<Long> svoIds = new HashSet<Long>();
        Set<Long> vvoIds = new HashSet<Long>();

        Set<AspireDbFilterConfig> filtersTrimmed = new HashSet<>();
        Collection<Long> subjectPhenoIds = new HashSet<>();
        Collection<Long> variantPhenoIds = new HashSet<>();

        for ( AspireDbFilterConfig f : filters ) {

            // treat PhenotypeFilters as a special case because calling this twice
            // is both redundant and slow (see Bug #3892)
            if ( f instanceof PhenotypeFilterConfig ) {
                svIds = variantDao.getSubjectVariantIdsByPhenotype( ( PhenotypeFilterConfig ) f );

                subjectPhenoIds = svIds.get( VariantDao.SUBJECT_IDS_KEY );
                variantPhenoIds = svIds.get( VariantDao.VARIANT_IDS_KEY );

            } else {
                filtersTrimmed.add( f );
            }
        }

        StopWatch timer = new StopWatch();
        timer.start();
        List<VariantValueObject> vvos = queryVariants( filtersTrimmed ).getItems();
        timer.stop();
        log.info( " query variants took " + timer );
        for ( VariantValueObject v : vvos ) {
            vvoIds.add( v.getId() );
        }

        // need a separate call for Subject and Variants because
        // we can have a Subject without any Variants and those won't get counted!
        if ( ConfigUtils.hasSubjectConfig( filters ) || !ConfigUtils.hasVariantConfig( filters ) ) {
            timer = new StopWatch();
            timer.start();
            List<SubjectValueObject> svos = querySubjects( filtersTrimmed ).getItems();
            timer.stop();
            log.info( " query subjects took " + timer );
            for ( SubjectValueObject s : svos ) {
                svoIds.add( s.getId() );
            }
        } else {
            // if there's no Subject filter then we can just get it
            // from Variant IDs! This saves us from executing redundant queries.
            for ( VariantValueObject v : vvos ) {
                svoIds.add( v.getSubjectId() );
            }
        }

        // intersect PhenoIds with Ids from other filters
        if ( !subjectPhenoIds.isEmpty() ) {
            svoIds.retainAll( subjectPhenoIds );
            vvoIds.retainAll( variantPhenoIds );
        }

        Collection<Object> retSvos = new ArrayList<>();
        for ( Subject s : subjectDao.load( svoIds ) ) {
            retSvos.add( s.convertToValueObjectWithPhenotypes() );
        }
        Collection<Object> retVvos = new ArrayList<>();
        for ( Variant v : variantDao.load( vvoIds ) ) {
            retVvos.add( v.toValueObject() );
        }

        ret.put( VariantDao.SUBJECT_IDS_KEY, retSvos );
        ret.put( VariantDao.VARIANT_IDS_KEY, retVvos );

        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Map<Integer, Integer> getSubjectVariantCounts( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException {

        Map<Integer, Integer> ret = new HashMap<>();

        Map<Integer, Collection<Object>> vos = getSubjectsVariants( filters );

        ret.put( VariantDao.SUBJECT_IDS_KEY, vos.get( VariantDao.SUBJECT_IDS_KEY ).size() );
        ret.put( VariantDao.VARIANT_IDS_KEY, vos.get( VariantDao.VARIANT_IDS_KEY ).size() );

        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Map<Integer, Integer> getSubjectGenes( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException {

        Map<Integer, Collection<Long>> svIds = new HashMap<>();
        Map<Integer, Integer> ret = new HashMap<>();

        Set<Long> svoIds = new HashSet<>();
        Set<Long> vvoIds = new HashSet<>();

        int subjectCount = 0;
        int variantCount = 0;

        Set<AspireDbFilterConfig> filtersTrimmed = new HashSet<>();
        Collection<Long> subjectPhenoIds = new HashSet<>();
        Collection<Long> variantPhenoIds = new HashSet<>();

        for ( AspireDbFilterConfig f : filters ) {

            // treat PhenotypeFilters as a special case because calling this twice
            // is both redundant and slow (see Bug #3892)
            if ( f instanceof PhenotypeFilterConfig ) {
                svIds = variantDao.getSubjectVariantIdsByPhenotype( ( PhenotypeFilterConfig ) f );

                subjectPhenoIds = svIds.get( VariantDao.SUBJECT_IDS_KEY );
                variantPhenoIds = svIds.get( VariantDao.VARIANT_IDS_KEY );

            } else {
                filtersTrimmed.add( f );
            }
        }

        StopWatch timer = new StopWatch();
        timer.start();
        List<VariantValueObject> vvos = queryVariants( filtersTrimmed ).getItems();
        timer.stop();
        log.info( " query variants took " + timer );
        for ( VariantValueObject v : vvos ) {
            vvoIds.add( v.getId() );
        }

        // need a separate call for Subject and Variants because
        // we can have a Subject without any Variants and those won't get counted!
        if ( ConfigUtils.hasSubjectConfig( filters ) ) {
            timer = new StopWatch();
            timer.start();
            List<SubjectValueObject> svos = querySubjects( filtersTrimmed ).getItems();
            timer.stop();
            log.info( " query subjects took " + timer );
            for ( SubjectValueObject s : svos ) {
                svoIds.add( s.getId() );
            }
        } else {
            // if there's no Subject filter then we can just get it
            // from Variant IDs! This saves us from executing redundant queries.
            for ( VariantValueObject v : vvos ) {
                svoIds.add( v.getSubjectId() );
            }
        }

        // intersect PhenoIds with Ids from other filters
        if ( !subjectPhenoIds.isEmpty() ) {
            svoIds.retainAll( subjectPhenoIds );
            vvoIds.retainAll( variantPhenoIds );
        }

        subjectCount = svoIds.size();
        variantCount = vvoIds.size();

        ret.put( VariantDao.SUBJECT_IDS_KEY, subjectCount );
        ret.put( VariantDao.VARIANT_IDS_KEY, variantCount );

        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getValuesForOntologyTerm( String ontologyTermUri ) {
        List<String> results = phenotypeDao.getExistingValues( ontologyTermUri );
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public int getVariantCount( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException {
        return queryVariants( filters ).getTotalSize();
    }

    @Override
    @RemoteMethod
    public List<Serializable> getVariantLocationValueObjects( @SuppressWarnings("rawtypes") Property property,
            List<String> text ) throws BioMartServiceException, NeurocartaServiceException {
        @SuppressWarnings("rawtypes")
        final List valueObjects;

        if ( property instanceof GeneProperty ) {
            valueObjects = this.bioMartQueryService.getGenes( text );
        } else if ( property instanceof NeurocartaPhenotypeProperty ) {
            valueObjects = this.neurocartaQueryService.getPhenotypes( text );
        } else if ( property instanceof GenomicLocationProperty ) {
            valueObjects = new ArrayList<Object>( text.size() );
            for ( String currText : text ) {
                GenomicRangeParser.ParseResult parseResult = GenomicRangeParser.parse( currText );
                if ( parseResult != null && parseResult.isValid() ) {

                    if ( parseResult.getStartBase() < 0 || parseResult.getEndBase() < 0 ) {
                        valueObjects.add( new GenomicRange( parseResult.getChromosome() ) );
                    } else {

                        valueObjects.add( new GenomicRange( parseResult.getChromosome(), parseResult.getStartBase(),
                                parseResult.getEndBase() ) );
                    }
                } else {
                    valueObjects.add( null );
                }
            }
        } else {
            int textSize = text.size();
            valueObjects = new ArrayList<Object>( textSize );
            for ( int i = 0; i < textSize; i++ ) {
                valueObjects.add( null );
            }
        }
        return valueObjects;
    }

    @Override
    @RemoteMethod
    public boolean isQueryName( String name ) {

        List<Query> querys = queryDao.findByName( name );

        if ( querys.size() > 0 ) {
            return true;
        }

        return false;

    }

    @Override
    @Transactional
    @RemoteMethod
    public Set<AspireDbFilterConfig> loadQuery( String name ) {
        List<Query> querys = queryDao.findByName( name );

        Set<AspireDbFilterConfig> configs = ( Set<AspireDbFilterConfig> ) querys.iterator().next().getObject();
        // should only be one for one user
        return configs;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public BoundedList<SubjectValueObject> querySubjects( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException {
        // fill in genomic locations, etc
        preProcessFilters( filters );

        // Default sort
        String sortField = "id";
        String sortDir = "ASC";

        log.info( "searching for subjects" );

        Page<Subject> subjects = ( Page<Subject> ) subjectDao.loadPage( 0, 0, sortField, sortDir, filters );

        List<SubjectValueObject> vos = new ArrayList<SubjectValueObject>();
        for ( Subject subject : subjects ) {
            // this doesn't take into account the security of the labels, i.e. all users with access to the subject can
            // see all the subjects labels
            SubjectValueObject vo = subject.convertToValueObject();

            Integer numVariants = subject.getVariants().size();

            vo.setVariants( numVariants != null ? numVariants : 0 );

            vo.setNumOfPhenotypes( subject.getPhenotypes().size() );

            vos.add( vo );
        }

        log.info( "returning " + vos.size() + " subjects" );

        BoundedList<SubjectValueObject> ret = new BoundedList<>( vos );
        if ( ret.isMoreResultsAvailable() ) {
            log.warn( "additional results not shown" );
        }

        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public BoundedList<VariantValueObject> queryVariants( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException {

        // fill in genomic locations, etc
        preProcessFilters( filters );

        String sortProperty = "id";// getSortColumn( config );
        String sortDirection = "ASC";// getSortDirection( config );

        log.info( "searching for variants" );

        StopWatch timer = new StopWatch();
        timer.start();
        Page<? extends Variant> page = variantDao.loadPage( 0, 0, sortProperty, sortDirection, filters );

        if ( timer.getTime() > 100 ) {
            log.info( "loading " + page.getTotalCount() + " variants took " + timer.getTime() + "ms" );
        }

        Collection<? extends Variant> variants = page;

        List<VariantValueObject> vos = convertToValueObjects( variants );

        log.debug( "returning " + vos.size() + " variants" );

        Project project = getProject( filters );

        populateSubjectsIntoVariants( project, vos );

        BoundedList<VariantValueObject> ret = new BoundedList<>( vos );
        if ( ret.isMoreResultsAvailable() ) {
            log.warn( "additional results not shown" );
        }

        return ret;
    }

    @Override
    public Project getProject( Set<AspireDbFilterConfig> filters ) {
        Project project = null;
        for ( AspireDbFilterConfig filter : filters ) {
            if ( filter instanceof ProjectFilterConfig ) {
                ProjectFilterConfig projConfig = ( ProjectFilterConfig ) filter;
                Long projId = projConfig.getProjectIds().iterator().next();
                project = projectDao.load( projId );
                break;
            }
        }
        return project;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Query getQuery( Long id ) {
        Query q = queryDao.load( id );
        return q;
    }

    @Override
    @Transactional
    @RemoteMethod
    public Long saveQuery( String name, Set<AspireDbFilterConfig> filters ) {
        final List<Query> queries = queryDao.findByName( name );
        Query savedQuery;
        if ( queries.isEmpty() ) {
            Query query = new Query( name, ( Serializable ) filters );
            savedQuery = queryDao.create( query );
        } else if ( queries.size() == 1 ) {
            Query query = queries.iterator().next();
            query.setObject( ( Serializable ) filters );
            queryDao.update( query );
            savedQuery = query;
        } else {
            throw new IllegalStateException( "Found more than one saved query with same name belonging to one user." );
        }

        securityService.makePrivate( savedQuery );

        return savedQuery.getId();
    }

    private void addGenomicLocations( RestrictionExpression restrictionExpression ) throws ExternalDependencyException {

        if ( restrictionExpression.getClass() == SetRestriction.class ) {
            SetRestriction restriction = ( SetRestriction ) restrictionExpression;
            // Expand NeurocartaPhenotype node -> set of genes.
            if ( restriction.getProperty() instanceof NeurocartaPhenotypeProperty ) {
                Collection<Object> values = restriction.getValues();
                for ( Object value : values ) {
                    NeurocartaPhenotypeValueObject vo = ( NeurocartaPhenotypeValueObject ) value;
                    Collection<GeneValueObject> geneValueObjects = this.neurocartaQueryService
                            .fetchGenesAssociatedWithPhenotype( vo.getUri() );
                    vo.setGenes( geneValueObjects );

                    log.info( "Fetched " + vo.getGenes().size() + " genes associated with phenotype '" + vo.getName()
                            + "' (" + vo.getUri() + ")" );
                }
            } else if ( restriction.getProperty() instanceof GeneProperty ) {
                Collection<Object> values = restriction.getValues();
                for ( Object value : values ) {
                    GeneValueObject vo = ( GeneValueObject ) value;
                    if ( vo.getGenomicRange() == null ) {
                        throw new IllegalStateException( "Genomic range wasn't set in GeneValueObject." );
                    }
                }
            }
        } else if ( restrictionExpression instanceof Junction ) {
            for ( RestrictionExpression restriction : ( ( Junction ) restrictionExpression ).getRestrictions() ) {
                addGenomicLocations( restriction );
            }
        }
    }

    private List<VariantValueObject> convertToValueObjects( Collection<? extends Variant> variants ) {
        List<VariantValueObject> variantValueObjects = new LinkedList<VariantValueObject>();

        for ( Variant variant : variants ) {

            try {
                VariantValueObject valueObject = variant.toValueObject();
                converter.fillInCytobandCoordinates( valueObject.getGenomicRange() );
                variantValueObjects.add( valueObject );
            } catch ( Exception e ) {
                log.warn( "Failed to convert variant to value object. " + variant, e );
            }

        }

        return variantValueObjects;
    }

    private List<PhenotypeSuggestion> getPhenotypeSuggestions( String query, boolean isExactMatch,
            Collection<Long> activeProjects ) {
        // HumanPhenotypeOntologyService HPOService = ontologyService.getHumanPhenotypeOntologyService();

        Map<String, PhenotypeSuggestion> phenotypeSuggestions = new HashMap<String, PhenotypeSuggestion>();
        List<String> names = phenotypeDao.getExistingPhenotypes( query, isExactMatch, activeProjects );
        for ( String name : names ) {
            PhenotypeSuggestion phenotypeSuggestion = new PhenotypeSuggestion( name );
            phenotypeSuggestion.setExistInDatabase( true );
            phenotypeSuggestions.put( phenotypeSuggestion.getName(), phenotypeSuggestion );
        }

        // Bug 3647: Disabled Ontology suggestions for now since phenotype inference has been disabled
        /*
         * String ontologyQuery = query; if (isExactMatch) { ontologyQuery = "\"" + ontologyQuery + "\""; } else {
         * ontologyQuery = ontologyQuery.trim(); String[] queryTerms = ontologyQuery.split(" "); if (queryTerms.length >
         * 1) { String lastTerm = queryTerms[queryTerms.length-1]; String completePhrase = ""; for (int i=0; i <
         * queryTerms.length - 1; i++) { completePhrase+=queryTerms[i]+" "; } completePhrase = completePhrase.trim();
         * completePhrase = "+\""+completePhrase +"\""; ontologyQuery = completePhrase + " +" + lastTerm + "*"; } else {
         * ontologyQuery += "*"; } } Collection<OntologyTerm> terms = HPOService.findTerm( ontologyQuery );
         * 
         * // if (terms.size() > 100) { // List<PhenotypeSuggestion> suggestions = new ArrayList<PhenotypeSuggestion>(
         * phenotypeSuggestions.values() ); // return new PagingLoadResultBean<PhenotypeSuggestion>( suggestions , 0, 0
         * ); // }
         * 
         * // Include parents and children of phenotypes we have in database. for (OntologyTerm term : terms) { // If
         * exact match is needed, make sure that if the query string // does not have spaces, term does not have spaces
         * either. if (isExactMatch && (!ontologyQuery.contains(" ")) && term.getLabel().contains(" ")) { continue; }
         * 
         * if ( phenotypeSuggestions.containsKey( term.getLabel() ) ) { PhenotypeSuggestion phenotypeSuggestion =
         * phenotypeSuggestions.get( term.getLabel() ); phenotypeSuggestion.setOntologyTerm( true );
         * phenotypeSuggestion.setUri( term.getUri() ); } else { // isParent or isChild Map<String, OntologyTerm>
         * childTerms = toMap( term.getChildren( false ) ); Map<String, OntologyTerm> parentTerms = toMap(
         * term.getParents( false ) ); Collection<String> termsToCheckInDB = new ArrayList<String>();
         * termsToCheckInDB.addAll( childTerms.keySet() ); termsToCheckInDB.addAll( parentTerms.keySet() );
         * 
         * if ( phenotypeDao.isInDatabase( termsToCheckInDB ) ) { PhenotypeSuggestion phenotypeSuggestion = new
         * PhenotypeSuggestion( term.getLabel(), term.getUri() ); phenotypeSuggestion.setOntologyTerm( true );
         * phenotypeSuggestions.put( term.getLabel(), phenotypeSuggestion ); } } }
         */
        return new ArrayList<PhenotypeSuggestion>( phenotypeSuggestions.values() );
    }

    /**
     * adding subject value object to variants
     * 
     * @param vos
     * @param project
     */
    private void populateSubjectsIntoVariants( Project project, List<VariantValueObject> vos ) {

        Set<String> ids = new HashSet<String>();
        for ( VariantValueObject vvo : vos ) {
            ids.add( vvo.getPatientId() );
        }

        Collection<Subject> subjects = null;
        if ( project != null ) {
            subjects = subjectDao.findByPatientIds( project, ids );
        } else {
            subjects = subjectDao.findByPatientIds( ids );
        }

        Map<String, Subject> id2subject = new HashMap<String, Subject>();
        for ( Subject s : subjects ) {
            id2subject.put( s.getPatientId(), s );
        }
        for ( VariantValueObject vvo : vos ) {
            String id = vvo.getPatientId();
            if ( id2subject.containsKey( id ) ) {
                vvo.setSubject( id2subject.get( id ).convertToValueObject() );
            }
        }
    }

    // Expand terms, fill in missing data.
    private void preProcessFilters( Set<AspireDbFilterConfig> filters ) throws ExternalDependencyException {
        for ( AspireDbFilterConfig filter : filters ) {
            if ( filter instanceof VariantFilterConfig ) {
                VariantFilterConfig variantFilter = ( VariantFilterConfig ) filter;
                RestrictionExpression restrictionExpression = variantFilter.getRestriction();
                addGenomicLocations( restrictionExpression );
            }
        }
    }

    /*
     * @Override
     * 
     * @Deprecated public Collection<NeurocartaPhenotypeValueObject> getNeurocartaPhenotypeSuggestions(String query)
     * throws NeurocartaServiceException { if (query.length() < 3) { return new
     * ArrayList<NeurocartaPhenotypeValueObject>(); }
     * 
     * return new ArrayList<NeurocartaPhenotypeValueObject>(this.neurocartaQueryService.findPhenotypes(query)); }
     */

    /*
     * @Override public Collection<GeneValueObject> getGeneSuggestions(String query) throws BioMartServiceException { if
     * (query.length() < 2) { return new ArrayList<GeneValueObject>(); }
     * 
     * return this.bioMartQueryService.findGenes(query); }
     */
}
