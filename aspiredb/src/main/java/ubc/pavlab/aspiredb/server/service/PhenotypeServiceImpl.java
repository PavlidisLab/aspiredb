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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaCache;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.PhenotypeValueType;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeEnrichmentValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.query.PhenotypeProperty;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;
import ubic.basecode.math.MultipleTestCorrection;
import ubic.basecode.math.SpecFunc;
import ubic.basecode.ontology.model.OntologyResource;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.providers.HumanPhenotypeOntologyService;
import cern.colt.list.DoubleArrayList;

/**
 * Functions for suggesting ontology terms, phenotype enrichment, and phenotype inference.
 * 
 * @version $Id$
 */
@RemoteProxy(name = "PhenotypeService")
@Service("phenotypeService")
public class PhenotypeServiceImpl implements PhenotypeService {

    protected static Log log = LogFactory.getLog( PhenotypeServiceImpl.class );

    private static final String HUMAN_PHENOTYPE_URI_PREFIX = "http://purl.org/obo/owl/HP#";

    public static boolean isUri( String uriOrName ) {
        if ( uriOrName == null ) return false;

        return uriOrName.trim().startsWith( "HP_" );
    }

    DecimalFormat dformat = new DecimalFormat( "#.#####" );
    @Autowired
    PhenotypeDao phenotypeDao;
    @Autowired
    OntologyService ontologyService;

    @Autowired
    ProjectDao projectDao;
    @Autowired
    private NeurocartaCache neurocartaCache;

    @Autowired
    private NeurocartaQueryService neurocartaQueryService;

    public String convertValueToHPOntologyStandardValue( String value ) {
        try {
            Integer.parseInt( value );
        } catch ( NumberFormatException nfe ) {
            if ( value != null && value.trim().equalsIgnoreCase( "Y" ) ) {
                return PhenotypeUtil.VALUE_PRESENT;
            }
            return PhenotypeUtil.VALUE_ABSENT;

        }

        return value;
    }

    /**
     * TODO make this work with ontology propagation
     * 
     * @param uriPhenotypes -all the phenotypes for a specific uri in the db for subjectIds and complementSubjectIds
     * @param subjectIds
     * @param complementSubjectIds
     */
    public PhenotypeEnrichmentValueObject getPhenotypeEnrichment( Collection<Phenotype> uriPhenotypes,
            Collection<Long> subjectIds, Collection<Long> complementSubjectIds ) {

        Integer successes = 0;

        Integer compSuccesses = 0;

        Integer positives = 0;

        Integer n = subjectIds.size();

        Integer complementGroupSize = complementSubjectIds.size();

        Integer totalSize = subjectIds.size() + complementSubjectIds.size();

        for ( Phenotype p : uriPhenotypes ) {
            // this should always be true the way we are currently using this method.
            if ( p.getValue().equals( "1" ) ) {

                if ( subjectIds.contains( p.getSubject().getId() )
                        || complementSubjectIds.contains( p.getSubject().getId() ) ) {
                    positives++;
                }

                if ( subjectIds.contains( p.getSubject().getId() ) ) {
                    successes++;
                }

                if ( complementSubjectIds.contains( p.getSubject().getId() ) ) {
                    compSuccesses++;
                }

            }
        }

        if ( successes == 0 || successes == n ) {
            return null;
        }

        // do it this way because of possible unobserved phenotypes(no recorded value) for certain subjects, this could
        // be wrong
        Integer negatives = totalSize - positives;

        // note lower.tail: logical; if TRUE (default), probabilities are P[X <= x],
        // otherwise, P[X > x].
        // Since we want P[X >= x], we want to set x = x - 1 and lower.tail false
        double pValue;

        pValue = SpecFunc.phyper( successes - 1, positives, negatives, n, false );

        PhenotypeEnrichmentValueObject vo = new PhenotypeEnrichmentValueObject();

        vo.setPValue( pValue );

        Phenotype valueGrabber = uriPhenotypes.iterator().next();
        vo.setUri( valueGrabber.getUri() );
        vo.setName( valueGrabber.getName() );
        vo.setInGroupTotal( successes );
        vo.setOutGroupTotal( compSuccesses );
        vo.setTotal( totalSize );

        vo.setInGroupTotalString( vo.getInGroupTotal().toString() + "/" + n.toString() );
        vo.setOutGroupTotalString( vo.getOutGroupTotal().toString() + "/" + complementGroupSize.toString() );

        vo.setPValueString( dformat.format( pValue ) );

        return vo;

    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public List<PhenotypeEnrichmentValueObject> getPhenotypeEnrichmentValueObjects( Collection<Long> activeProjects,
            Collection<Long> subjectIds, Collection<Long> complementSubjectIds ) throws NotLoggedInException {

        ArrayList<PhenotypeEnrichmentValueObject> list = new ArrayList<PhenotypeEnrichmentValueObject>();

        Collection<String> distinctUris = phenotypeDao.getDistinctOntologyUris( activeProjects );

        for ( String uri : distinctUris ) {
            // TODO change this fetching to be more efficient when we make this functionality more robust(currently it
            // grabs all phenotypes in the projects for a specific uri
            // change later to grab only for subjectIds and complementSubjectIds)
            Collection<Phenotype> phenotypes = phenotypeDao.findPresentByProjectIdsAndUri( activeProjects, uri );

            PhenotypeEnrichmentValueObject pevo = null;
            try {
                pevo = getPhenotypeEnrichment( phenotypes, subjectIds, complementSubjectIds );
            } catch ( Exception e ) {
                String phenoName = "";
                if ( phenotypes.size() > 0 ) {
                    phenoName = phenotypes.iterator().next().getName();
                }
                log.warn( "Error calculating phenotype enrichment for URI " + uri + " (" + phenoName + ")" + " for "
                        + subjectIds.size() + " subjects in the in-group and " + complementSubjectIds.size()
                        + " in the out-group" );
            }

            if ( pevo != null ) {
                list.add( pevo );
            }

        }

        multipleTestCorrectionForPhenotypeEnrichmentList( list );

        return list;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public List<PhenotypeEnrichmentValueObject> getPhenotypeEnrichmentValueObjects( Collection<Long> activeProjects,
            Collection<Long> subjectIds ) throws NotLoggedInException {

        Project activeProject = projectDao.load( activeProjects.iterator().next() );

        List<Subject> subjectList = activeProject.getSubjects();
        List<Long> complementSubjectIds = new ArrayList<Long>();

        for ( Subject s : subjectList ) {
            if ( !subjectIds.contains( s.getId() ) ) {
                complementSubjectIds.add( s.getId() );
            }
        }

        return getPhenotypeEnrichmentValueObjects( activeProjects, subjectIds, complementSubjectIds );
    }

    @Override
    @RemoteMethod
    @Transactional
    public Map<String, PhenotypeValueObject> getPhenotypes( Long subjectId ) throws NotLoggedInException {

        StopWatch timer = new StopWatch();
        timer.start();

        Collection<Phenotype> phenotypes = phenotypeDao.findBySubjectId( subjectId );

        if ( timer.getTime() > 100 ) {
            log.info( "loading phenotypes for subjectId: " + subjectId + " took " + timer.getTime() + "ms" );
        }

        // Insert phenotype loaded from DB.
        Map<String, PhenotypeValueObject> valueObjectsMap = new HashMap<String, PhenotypeValueObject>();
        for ( Phenotype phenotype : phenotypes ) {
            valueObjectsMap.put( phenotype.getName(), phenotype.convertToValueObject() );
        }

        // FIXME: disabled temporarily
        // Insert inferred phenotype values using Ontology.
        // valueObjectsMap = addDescendantsAndAncestors(valueObjectsMap);
        // propagateAbsentPresentValues(valueObjectsMap);

        return valueObjectsMap;
    }

    public void multipleTestCorrectionForPhenotypeEnrichmentList( List<PhenotypeEnrichmentValueObject> list ) {

        DoubleArrayList doubleArrayList = new DoubleArrayList();

        for ( PhenotypeEnrichmentValueObject pvo : list ) {
            doubleArrayList.add( pvo.getPValue() );
        }

        doubleArrayList = MultipleTestCorrection.benjaminiHochberg( doubleArrayList );

        for ( int i = 0; i < doubleArrayList.size(); i++ ) {
            list.get( i ).setPValueCorrected( doubleArrayList.get( i ) );
            list.get( i ).setPValueCorrectedString( dformat.format( doubleArrayList.get( i ) ) );
        }

    }

    @Override
    @RemoteMethod
    @Transactional
    // TODO: Test
    public Map<String, Collection<GeneValueObject>> populateDescendantPhenotypes( String phenotypeUri )
            throws NeurocartaServiceException, BioMartServiceException {

        Map<String, Collection<GeneValueObject>> valueObjectsMap = new HashMap<String, Collection<GeneValueObject>>();
        // List<GeneValueObject> gvos = (List<GeneValueObject>) this.neurocartaQueryService.getPhenotypes(names)
        // .fetchGenesAssociatedWithPhenotype(phenotypeUri);

        HumanPhenotypeOntologyService hpoService = ontologyService.getHumanPhenotypeOntologyService();
        OntologyTerm ontologyTerm = hpoService.getTerm( phenotypeUri );

        if ( ontologyTerm == null ) { // Not an ontology term.
            return null;
        }

        Collection<OntologyTerm> descendantsTerms = ontologyTerm.getChildren( false );

        for ( OntologyTerm childTerm : descendantsTerms ) {
            String uri = PhenotypeUtil.HUMAN_PHENOTYPE_URI_PREFIX + childTerm.getLocalName();
            Collection<GeneValueObject> gvos = neurocartaQueryService.fetchGenesAssociatedWithPhenotype( uri );
            valueObjectsMap.put( childTerm.getTerm(), gvos );
        }

        return valueObjectsMap;

    }

    public boolean setNameUriValueType( Phenotype phenotype, String key ) {
        if ( isUri( key ) ) {

            phenotype.setUri( key );

            OntologyResource resource = ontologyService.getTerm( HUMAN_PHENOTYPE_URI_PREFIX + key );

            if ( resource == null ) {
                log.error( "No ontology resource for " + key );
                return false;
            }

            phenotype.setName( resource.getLabel() );
            phenotype.setType( PhenotypeValueType.HPONTOLOGY.toString() );

        } else if ( key.trim().equalsIgnoreCase( "GENDER" ) ) {
            phenotype.setName( key );
            phenotype.setType( PhenotypeValueType.GENDER.toString() );

        } else {
            phenotype.setName( key );
            phenotype.setType( PhenotypeValueType.CUSTOM.toString() );
        }

        return true;
    }

    public boolean setValue( Phenotype phenotype, String value ) {
        if ( phenotype.getType().equals( PhenotypeValueType.HPONTOLOGY.toString() ) ) {
            phenotype.setValue( convertValueToHPOntologyStandardValue( value ) );
        } else if ( phenotype.getType().equals( PhenotypeValueType.GENDER.toString() ) ) {

            value = value.trim().toUpperCase();

            if ( value.equals( "M" ) || value.equals( "F" ) ) {

                phenotype.setValue( value );

            } else {
                log.error( "Invalid GENDER: " + value );
                return false;
            }
        } else {
            phenotype.setValue( value );
        }

        return true;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<PhenotypeProperty> suggestPhenotypes( SuggestionContext suggestionContext )
            throws NotLoggedInException {

        Collection<PhenotypeProperty> phenotypes = new ArrayList<PhenotypeProperty>();

        List<String> names = phenotypeDao.getExistingPhenotypes( suggestionContext.getValuePrefix(), false,
                suggestionContext.getActiveProjectIds() );
        for ( String name : names ) {
            PhenotypeProperty phenotypeProperty = new PhenotypeProperty();
            phenotypeProperty.setExistInDatabase( true );
            phenotypeProperty.setName( name );
            phenotypeProperty.setDisplayName( name );
            phenotypes.add( phenotypeProperty );
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
        return phenotypes;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<PropertyValue> suggestPhenotypeValues( PhenotypeProperty property,
            SuggestionContext suggestionContext ) throws NotLoggedInException {
        Collection<PropertyValue> propertyValues = new ArrayList<PropertyValue>();
        List<String> results = phenotypeDao.getExistingValues( property.getName() );
        for ( String result : results ) {
            propertyValues.add( new PropertyValue<TextValue>( new TextValue( result ) ) );
        }
        return propertyValues;
    }

    /**
     * Expand map by adding rows for descendants/ancestors of terms currently in the map(from db).
     * 
     * @param phenotypeMap
     */
    private Map<String, PhenotypeValueObject> addDescendantsAndAncestors( Map<String, PhenotypeValueObject> phenotypeMap ) {
        // Pre-condition:
        // db phenotypes are loaded into phenotypeMap
        HumanPhenotypeOntologyService hpoService = ontologyService.getHumanPhenotypeOntologyService();

        Map<String, PhenotypeValueObject> inferredPhenotypeMap = new HashMap<String, PhenotypeValueObject>();

        for ( PhenotypeValueObject phenotype : phenotypeMap.values() ) {
            // Create full ontology tree branch (up and down from current term)
            Long subjectId = phenotype.getSubjectId();

            OntologyTerm ontologyTerm = hpoService.getTerm( PhenotypeUtil.HUMAN_PHENOTYPE_URI_PREFIX
                    + phenotype.getUri() );

            if ( ontologyTerm == null ) continue;

            Collection<OntologyTerm> descendantsTerms = ontologyTerm.getChildren( false );

            // Add descendants (some are present in db some are not) to each phenotype object.
            for ( OntologyTerm descendantTerm : descendantsTerms ) {
                PhenotypeValueObject childPhenotype = phenotypeMap.get( descendantTerm.getLabel() );
                if ( childPhenotype == null ) {
                    childPhenotype = inferredPhenotypeMap.get( descendantTerm.getLabel() );
                    if ( childPhenotype == null ) {
                        childPhenotype = createPhenotypeValueObject( subjectId, descendantTerm );
                        inferredPhenotypeMap.put( childPhenotype.getName(), childPhenotype );
                    }
                }
            }

            // Ancestors
            Collection<OntologyTerm> ancestorTerms = ontologyTerm.getParents( false );
            for ( OntologyTerm ancestorTerm : ancestorTerms ) {
                PhenotypeValueObject ancestorPhenotype = phenotypeMap.get( ancestorTerm.getLabel() );
                if ( ancestorPhenotype == null ) {
                    ancestorPhenotype = inferredPhenotypeMap.get( ancestorTerm.getLabel() );
                    if ( ancestorPhenotype == null ) {
                        ancestorPhenotype = createPhenotypeValueObject( subjectId, ancestorTerm );
                        inferredPhenotypeMap.put( ancestorTerm.getLabel(), ancestorPhenotype );
                    }
                }
            }
        }

        // Add inferred terms not stored in database.
        inferredPhenotypeMap.putAll( phenotypeMap );
        phenotypeMap = inferredPhenotypeMap;

        for ( PhenotypeValueObject phenotype : phenotypeMap.values() ) {
            // Create full ontology tree branch (up and down from current term)

            OntologyTerm ontologyTerm = hpoService.getTerm( PhenotypeUtil.HUMAN_PHENOTYPE_URI_PREFIX
                    + phenotype.getUri() );

            if ( ontologyTerm == null ) {
                ontologyTerm = hpoService.getTerm( phenotype.getUri() );
                if ( ontologyTerm == null ) {
                    continue;
                }
            }

            Collection<OntologyTerm> descendantsTerms = ontologyTerm.getChildren( false );

            // Add descendants (some are present in db some are not) to each phenotype object.
            for ( OntologyTerm descendantTerm : descendantsTerms ) {
                PhenotypeValueObject childPhenotype = phenotypeMap.get( descendantTerm.getLabel() );
                if ( childPhenotype != null ) {
                    phenotype.addChildIfAbsent( childPhenotype );
                }
            }

            // Ancestors
            Collection<OntologyTerm> ancestorTerms = ontologyTerm.getParents( false );
            for ( OntologyTerm ancestorTerm : ancestorTerms ) {
                PhenotypeValueObject ancestorPhenotype = phenotypeMap.get( ancestorTerm.getLabel() );
                if ( ancestorPhenotype != null ) {
                    ancestorPhenotype.addChildIfAbsent( phenotype );
                }
            }
        }
        return phenotypeMap;
    }

    private PhenotypeValueObject createPhenotypeValueObject( Long subjectId, OntologyTerm ontologyTerm ) {
        return new PhenotypeValueObject( subjectId, ontologyTerm.getUri(), ontologyTerm.getLabel(), "Unknown" );
    }

    /**
     * Propagate absent/present values to ancestors/descendants.
     * 
     * @param phenotypeMap
     */
    private void propagateAbsentPresentValues( Map<String, PhenotypeValueObject> phenotypeMap ) {
        for ( PhenotypeValueObject phenotype : phenotypeMap.values() ) {
            for ( PhenotypeValueObject child : phenotype.getDescendantPhenotypes().values() ) {
                // Propagate 'Present' (1) values to ancestor.
                if ( child.getDbValue() != null && phenotype.getDbValue() == null ) {
                    if ( child.getDbValue().equals( PhenotypeUtil.VALUE_PRESENT ) ) {
                        phenotype.setInferredValue( PhenotypeUtil.VALUE_PRESENT );
                        continue;
                    }
                }
                // Propagate 'Absent' (0) values to descendants.
                if ( phenotype.getDbValue() != null && child.getDbValue() == null ) {
                    if ( phenotype.getDbValue().equals( PhenotypeUtil.VALUE_ABSENT ) ) {
                        child.setInferredValue( PhenotypeUtil.VALUE_ABSENT );
                    }
                }
            }
        }
    }

}