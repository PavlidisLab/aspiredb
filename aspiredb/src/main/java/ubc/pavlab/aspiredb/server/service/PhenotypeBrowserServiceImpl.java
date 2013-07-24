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

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ubc.pavlab.aspiredb.client.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.providers.HumanPhenotypeOntologyService;

import java.util.*;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: PhenotypeBrowserServiceImpl.java,v 1.23 2013/06/12 19:51:18 anton Exp $
 */
@Component
public class PhenotypeBrowserServiceImpl implements PhenotypeBrowserService  {
	
	protected static Log log = LogFactory.getLog( PhenotypeBrowserServiceImpl.class );

    @Autowired private PhenotypeDao phenotypeDao;
    @Autowired private OntologyService ontologyService;
    @Autowired private NeurocartaQueryService neurocartaQueryService;
    
	public List<PhenotypeSummaryValueObject> getPhenotypesBySubjectIds(Collection<Long> subjectIds,
            Collection<Long> projectIds) throws NeurocartaServiceException{
		
		Collection<Phenotype> phenotypes = loadPhenotypesBySubjectIds( subjectIds );	       

        Map<String, PhenotypeSummaryValueObject> pvoMap = constructPhenotypeSummaryValueObjects(phenotypes, subjectIds, projectIds);

        List<PhenotypeSummaryValueObject> phenotypeSummaries = new ArrayList<PhenotypeSummaryValueObject>(pvoMap.values());

// TODO: temporarily disabled
//        for (PhenotypeSummaryValueObject phenotypeSummary : phenotypeSummaries ) {
//        	fillInferredPhenotypeSummaries( phenotypeSummary, pvoMap );
//        }
        
        return phenotypeSummaries;
	}

	private Map<String, PhenotypeSummaryValueObject> constructPhenotypeSummaryValueObjects(
            Collection<Phenotype> phenotypes, Collection<Long> subjectIds, Collection<Long> projectIds) throws NeurocartaServiceException {
		
		Map<String, PhenotypeSummaryValueObject> phenotypeNameToSummary = new LinkedHashMap<String, PhenotypeSummaryValueObject>();

        // Make PhenotypeSummaryValueObjects and populate their value counts.
        for ( Phenotype phenotype : phenotypes ) {
        	PhenotypeSummaryValueObject phenotypeSummary = phenotypeNameToSummary.get( phenotype.getName() );
            
            // Create new PhenotypeSummaryValueObject.
            if ( phenotypeSummary == null ) {
            	List<String> possibleValues = phenotypeDao.getListOfPossibleValuesByName( projectIds, phenotype.getName() );
            	phenotypeSummary = makePhenotypeSummaryValueObject( phenotype, possibleValues, subjectIds );
                phenotypeNameToSummary.put(phenotype.getName(), phenotypeSummary);
                // FIXME: temporary hack
                if ( (possibleValues.size() == 1 && (possibleValues.contains("1") || possibleValues.contains("0")))
                      || (possibleValues.size() == 2 && possibleValues.contains("1") && possibleValues.contains("0"))
                    )
                {
                    phenotypeSummary.setInferredBinaryType(true);
                }
            }
            
            addSubjectToPhenotypeCountingSet(phenotypeSummary, phenotype) ;
        }

        for ( PhenotypeSummaryValueObject summary : phenotypeNameToSummary.values() ) {
            summary.initializeInferredPhenotypes();
        }

		return phenotypeNameToSummary;
	}
	
	private PhenotypeSummaryValueObject makePhenotypeSummaryValueObject (
			Phenotype phenotype, List<String> possibleValues, Collection<Long> subjectIds ) throws NeurocartaServiceException {
		Map<String, Set<Long>> valueToSubjectIds = new HashMap<String, Set<Long>>();
		
        valueToSubjectIds.put("Unknown", new HashSet<Long>(subjectIds));  // Initialize with unknown.
		// Pre-populate possible values.
		for ( String phenotypeValue : possibleValues ) {
	        valueToSubjectIds.put( phenotypeValue, new HashSet<Long>() );
		}
		
		PhenotypeSummaryValueObject phenotypeSummary = new PhenotypeSummaryValueObject( phenotype.convertToValueObject(),
		        valueToSubjectIds );

        phenotypeSummary.setNeurocartaPhenotype(isNeurocartaPhenotype( PhenotypeUtil.HUMAN_PHENOTYPE_URI_PREFIX+phenotypeSummary.getUri() ));

        return phenotypeSummary;
	}
	
	private void addSubjectToPhenotypeCountingSet(
            PhenotypeSummaryValueObject phenotypeSummary, Phenotype subjectPhenotype) {

		Long subjectId = subjectPhenotype.getSubject().getId();
        String phenotypeValue = subjectPhenotype.getValue();

        Map<String, Set<Long>> dbValueToSubjects = phenotypeSummary.getDbValueToSubjectSet();
        Set<Long> subjects = dbValueToSubjects.get(phenotypeValue);
        Set<Long> subjectsWithUnknown = phenotypeSummary.getInferredValueToSubjectSet().get("Unknown");

        if ( subjects == null ) {
            subjects = new HashSet<Long>();
            dbValueToSubjects.put( phenotypeValue, subjects );
        }
        subjects.add( subjectId );
        subjectsWithUnknown.remove( subjectId );
	}

    // FIXME: temporarily disabled
    private PhenotypeSummaryValueObject fillInferredPhenotypeSummaries ( PhenotypeSummaryValueObject phenotypeSummary,
			Map<String, PhenotypeSummaryValueObject> phenotypeToSummaryMap) throws NeurocartaServiceException {
		
        HumanPhenotypeOntologyService hpoService = ontologyService.getHumanPhenotypeOntologyService();
        OntologyTerm ontologyTerm = hpoService.getTerm(
                PhenotypeUtil.HUMAN_PHENOTYPE_URI_PREFIX + phenotypeSummary.getUri() );

        if ( ontologyTerm == null ) { // Not an ontology term.
            return phenotypeSummary;
        }

        // Shown when phenotype grid row is expanded.
        Collection<PhenotypeSummaryValueObject> descendantSummaries = phenotypeSummary.getDescendantOntologyTermSummaries();
        Collection<OntologyTerm> descendantsTerms = ontologyTerm.getChildren( false );
//        if ( descendantsTerms.isEmpty() ) { // Is leaf term.
//            return phenotypeSummary;
//        }
//        // TODO: what about inference of absence?

        // Add to map if phenotype is present in db.
        for ( OntologyTerm childTerm : descendantsTerms ) {
            if ( phenotypeToSummaryMap.keySet().contains( childTerm.getLabel()) ) {
                descendantSummaries.add( phenotypeToSummaryMap.get( childTerm.getLabel() ) );
            }

            phenotypeSummary.setNeurocartaPhenotype(
                    isNeurocartaPhenotype( phenotypeSummary.getUri() ) ||
                    isNeurocartaPhenotype( childTerm.getUri() )
            );
        }

        // Initialize subject sets.
        if ( phenotypeSummary.getInferredValueToSubjectSet().get( "1" ) == null) {
            phenotypeSummary.getInferredValueToSubjectSet().put( "1", new HashSet<Long>() );
        }
        if ( phenotypeSummary.getInferredValueToSubjectSet().get( "0" ) == null) {
            phenotypeSummary.getInferredValueToSubjectSet().put( "0", new HashSet<Long>() );
        }

        Set<Long> presentSubjectSet = phenotypeSummary.getInferredValueToSubjectSet().get( "1" );
        Set<Long> absentSubjectSet = phenotypeSummary.getInferredValueToSubjectSet().get( "0" );

        for ( PhenotypeSummaryValueObject childSummary : descendantSummaries ) {
            // Propagate 'Present' (1) values to ancestor.
            Set<Long> childPresentSubjectSet = childSummary.getInferredValueToSubjectSet().get( "1" );
            if ( childPresentSubjectSet != null ) {
                presentSubjectSet.addAll( childPresentSubjectSet );
            }

            // Propagate 'Absent' (0) values to descendants.
            if (childSummary.getInferredValueToSubjectSet().get( "0" ) == null) {
                childSummary.getInferredValueToSubjectSet().put( "0", new HashSet<Long>() );
            }
            Set<Long> childAbsentSubjectSet = childSummary.getInferredValueToSubjectSet().get( "0" );
            if ( absentSubjectSet != null ) {
                childAbsentSubjectSet.addAll( absentSubjectSet );
            }
        }

        return phenotypeSummary;
	}

    private Collection<Phenotype> loadPhenotypesBySubjectIds(
            Collection<Long> subjectIds) {

        log.info( "loading phenotypes for "+subjectIds.size()+" subjects");
        StopWatch timer = new StopWatch();
        timer.start();

        Collection<Phenotype> phenotypes = phenotypeDao.loadBySubjectIds( subjectIds );

        if ( timer.getTime() > 100 ) {
            log.info( "loading phenotypes for "+subjectIds.size()+" subjects took " + timer.getTime() + "ms" );
        }

        return phenotypes;
    }

	private boolean isNeurocartaPhenotype ( String uri ) throws NeurocartaServiceException {
		return uri != null && this.neurocartaQueryService.isNeurocartaPhenotype(uri);
	}
}
