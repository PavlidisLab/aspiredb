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

package ubc.pavlab.aspiredb.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.model.PhenotypeValueType;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.Disjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.Junction;
import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubic.basecode.ontology.model.OntologyResource;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.providers.HumanPhenotypeOntologyService;

@Component("phenotypeUtil")
public class PhenotypeUtil {

    public static final String HUMAN_PHENOTYPE_URI_PREFIX = "http://purl.obolibrary.org/obo/";

    public static final String VALUE_PRESENT = "1";

    public static final String VALUE_ABSENT = "0";

    private static Log log = LogFactory.getLog( PhenotypeUtil.class.getName() );

    @Autowired
    OntologyService os;

    @Autowired
    PhenotypeDao phenotypeDao;

    public void setNameUriValueType( PhenotypeValueObject phenotype, String key ) throws InvalidDataException {
        if ( isUri( key ) ) {
            phenotype.setUri( key );
            OntologyResource resource = os.getTerm( HUMAN_PHENOTYPE_URI_PREFIX + key );
            if ( resource == null ) {
                throw new InvalidDataException( HUMAN_PHENOTYPE_URI_PREFIX + key + " not found in Ontology" );
            }
            phenotype.setName( resource.getLabel() );
            phenotype.setValueType( PhenotypeValueType.HPONTOLOGY.toString() );
        } else if ( key.trim().equalsIgnoreCase( "GENDER" ) ) {
            phenotype.setName( key );
            phenotype.setValueType( PhenotypeValueType.GENDER.toString() );
        } else {
            phenotype.setName( key );
            phenotype.setValueType( PhenotypeValueType.CUSTOM.toString() );
        }

    }

    public void setValue( PhenotypeValueObject phenotype, String value ) throws InvalidDataException {
        if ( phenotype.getValueType().equals( PhenotypeValueType.HPONTOLOGY.toString() ) ) {
            phenotype.setDbValue( convertValueToHPOntologyStandardValue( value ) );
        } else if ( phenotype.getValueType().equalsIgnoreCase( PhenotypeValueType.GENDER.toString() ) ) {
            value = value.trim().toUpperCase();
            if ( value.equals( "MALE" ) || value.equals( "FEMALE" ) || value.equals( "M" ) || value.equals( "F" ) ) {
                phenotype.setDbValue( value );
            } else {
                throw new InvalidDataException( "Invalid Gender value, use MALE, FEMALE, M, or F" );
            }
        } else {
            phenotype.setDbValue( value );
        }

    }

    /**
     * we want to use the standard 0,1,2,3 values of the human phenotype ontology but some of the data we are loading in
     * has Y and N. This method is to convert special cases to the standard value. Hopefully we can enforce a standard
     * and get rid of this method
     */
    public static String convertValueToHPOntologyStandardValue( String value ) throws InvalidDataException {
        try {
            Integer intValue = Integer.parseInt( value );

            if ( intValue != 1 && intValue != 0 ) {
                throw new InvalidDataException( "Invalid HP Ontology value. Must be one of 1,0,Y,N" );
            }

        } catch ( NumberFormatException nfe ) {
            if ( value != null && value.trim().equalsIgnoreCase( "Y" ) ) {
                return VALUE_PRESENT;
            } else if ( value != null && value.trim().equalsIgnoreCase( "N" ) ) {
                return VALUE_ABSENT;
            } else {
                throw new InvalidDataException( "Invalid HP Ontology value. Must be one of 1,0,Y,N" );
            }
        }
        return value;
    }

    public static boolean isUri( String uriOrName ) {
        if ( uriOrName == null ) {
            return false;
        } else {
            return uriOrName.trim().startsWith( "HP_" );
        }
    }

    /**
     * Sets the first URI that matches the phenotype name
     * 
     * @param phenotype
     */
    private void resolveUri( PhenotypeRestriction phenotype ) {
        if ( phenotype == null ) {
            return;
        }
        List<String> uriList = phenotypeDao.getExistingURIs( phenotype.getName() );
        if ( uriList.size() == 0 ) {
            log.warn( " No URI found for phenotype \"" + phenotype.getName() + "\"" );
            return;
        }

        phenotype.setUri( uriList.iterator().next() );
    }

    private void loadOntology() {
        HumanPhenotypeOntologyService humanPhenotypeOntology = os.getHumanPhenotypeOntologyService();
        humanPhenotypeOntology.startInitializationThread( true );
        int c = 0;

        try {
            while ( !humanPhenotypeOntology.isOntologyLoaded() ) {
                Thread.sleep( 10000 );
                log.info( "Waiting for HumanPhenotypeOntology to load" );
                if ( ++c > 10 ) {
                    log.error( "Ontology load timed out" );
                    return;
                }
            }
        } catch ( InterruptedException e ) {
            log.error( "Failed to load the Human Phenotype Ontology service" );
            return;
        }
    }

    /**
     * Builds a list of RestrictionExpressions by comparing the HumanPhenotypeOntology against the Phenotypes in the
     * database and assign Phenotype values as appropriate.
     * 
     * @param phenotype
     * @param activeProjectIds
     * @return
     */
    private Collection<RestrictionExpression> expandPhenotypeRestriction( PhenotypeRestriction phenotype,
            Collection<Long> activeProjectIds ) {

        HumanPhenotypeOntologyService humanPhenotypeOntology = os.getHumanPhenotypeOntologyService();
        Set<RestrictionExpression> expandedRestrictions = new HashSet<RestrictionExpression>();
        expandedRestrictions.add( phenotype );

        if ( !humanPhenotypeOntology.isOntologyLoaded() ) {
            loadOntology();
        }

        resolveUri( phenotype );

        if ( !phenotype.isOntologyTerm() ) {
            log.warn( "\"" + phenotype.getName() + "\" is not an ontology term" );
            return expandedRestrictions;
        }

        OntologyTerm term = humanPhenotypeOntology.getTerm( HUMAN_PHENOTYPE_URI_PREFIX + phenotype.getUri() );
        if ( term == null ) {
            log.error( HUMAN_PHENOTYPE_URI_PREFIX + phenotype.getUri() + " not found in Ontology" );
            return expandedRestrictions;
        }

        Collection<String> existingTerms = phenotypeDao.getExistingNames( activeProjectIds );
        existingTerms.remove( phenotype.getName() );

        if ( phenotype.isAbsent() ) {
            expandedRestrictions.remove( phenotype );

            // conjunction: make sure all the child are Absent
            Conjunction conj = new Conjunction();
            Conjunction conjDescendants = new Conjunction();
            Set<PhenotypeRestriction> descendantRestrictions = new HashSet<PhenotypeRestriction>();
            createRestrictionsForDescendants( VALUE_ABSENT, term, descendantRestrictions, existingTerms );

            /*
             * log.debug( "descendant (" + descendantRestrictions.size() + "): " ); for ( PhenotypeRestriction r :
             * descendantRestrictions ) { log.debug( "; " + r.getName() + "=" + r.getValue() ); } log.debug( "" );
             */

            conjDescendants.addAll( descendantRestrictions );
            conj.add( phenotype );
            conj.add( conjDescendants );
            expandedRestrictions.add( conj );

        } else if ( phenotype.isPresent() ) {
            expandedRestrictions.remove( phenotype );

            // disjunction: count if at least one child penotype is Present
            Disjunction disj = new Disjunction();
            Set<PhenotypeRestriction> descendantRestrictions = new HashSet<PhenotypeRestriction>();
            createRestrictionsForDescendants( VALUE_PRESENT, term, descendantRestrictions, existingTerms );
            descendantRestrictions.add( phenotype );
            disj.addAll( descendantRestrictions );
            expandedRestrictions.add( disj );
        }

        return expandedRestrictions;
    }

    private Set<PhenotypeRestriction> createRestrictionsForDescendants( String value, OntologyTerm term,
            Set<PhenotypeRestriction> expandedRestrictions, Collection<String> remainingTerms ) {
        Collection<OntologyTerm> childTerms = term.getChildren( false );

        for ( OntologyTerm childTerm : childTerms ) {
            PhenotypeRestriction phenotypeRestriction = new PhenotypeRestriction( childTerm.getLabel(), value,
                    childTerm.getUri() );

            HashSet<String> termList = new HashSet<String>();
            termList.add( childTerm.getLabel() );

            if ( remainingTerms.contains( childTerm.getLabel() ) ) {
                expandedRestrictions.add( phenotypeRestriction );
                remainingTerms.remove( childTerm.getLabel() );
                // log.info( "Phenotype \"" + childTerm.getLabel() + " found in database." );
            } else {
                // log.info("Phenotype \"" + childTerm.getLabel() + " not found in database.");
            }

        }
        return expandedRestrictions;
    }

    private Set<PhenotypeRestriction> createRestrictionsUsingAncestors( String value, OntologyTerm term,
            Set<PhenotypeRestriction> expandedRestrictions, Collection<String> remainingTerms ) {

        Collection<OntologyTerm> ancestorTerms = term.getParents( false );

        String s = "";
        for ( OntologyTerm ancestorTerm : ancestorTerms ) {
            s += ancestorTerm.getLabel() + ", ";
            PhenotypeRestriction phenotypeRestriction = new PhenotypeRestriction( ancestorTerm.getLabel(), value,
                    ancestorTerm.getUri() );

            HashSet<String> termList = new HashSet<String>();
            termList.add( ancestorTerm.getLabel() );

            if ( remainingTerms.contains( ancestorTerm.getLabel() ) ) {
                expandedRestrictions.add( phenotypeRestriction );
                remainingTerms.remove( ancestorTerm.getLabel() );
                // log.info( "Phenotype \"" + ancestorTerm.getLabel() + " found in database." );
            } else {
                // log.info("Phenotype \"" + ancestorTerm.getLabel() + " not found in database.");
            }

        }

        return expandedRestrictions;
    }

    private RestrictionExpression expandNodesUsingOntologyInference( RestrictionExpression node,
            Collection<Long> activeProjectIds ) {
        if ( node instanceof PhenotypeRestriction ) {
            // log.info("node instanceof PhenotypeRestriction");
            PhenotypeRestriction phenotype = ( PhenotypeRestriction ) node;
            Disjunction disjunction = new Disjunction();
            disjunction.replaceAll( expandPhenotypeRestriction( phenotype, activeProjectIds ) );
            return disjunction;

        } else if ( node instanceof Junction ) {
            Collection<RestrictionExpression> restrictions = ( ( Junction ) node ).getRestrictions();
            Collection<RestrictionExpression> results = new ArrayList<RestrictionExpression>();
            for ( RestrictionExpression restriction : restrictions ) {
                RestrictionExpression result = expandNodesUsingOntologyInference( restriction, activeProjectIds );
                results.add( result );
            }
            ( ( Junction ) node ).replaceAll( results );
            return node;
        } else { // Any other leaf node
            log.debug( "node leaf node" );
            return node;
        }
    }

    public PhenotypeFilterConfig expandOntologyTerms( PhenotypeFilterConfig filterConfig,
            Collection<Long> activeProjectIds ) {
        StopWatch timer = new StopWatch();
        timer.start();
        RestrictionExpression restrictionTree = filterConfig.getRestriction();
        // Traverse and expand ontology terms
        restrictionTree = expandNodesUsingOntologyInference( restrictionTree, activeProjectIds );
        filterConfig.setRestriction( restrictionTree );
        if ( timer.getTime() > 100 ) {
            log.info( "Expanding ontology terms took " + timer.getTime() + "ms" );
        }
        return filterConfig;
    }
}
