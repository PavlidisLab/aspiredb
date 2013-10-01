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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.model.PhenotypeValueType;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
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

    @Autowired
    OntologyService os;

    
    public void setNameUriValueType( PhenotypeValueObject phenotype, String key ) throws InvalidDataException {
        if ( isUri( key ) ) {
            phenotype.setUri( key );
            OntologyResource resource = os.getTerm( HUMAN_PHENOTYPE_URI_PREFIX + key );
            if (resource == null) {
                throw new InvalidDataException(HUMAN_PHENOTYPE_URI_PREFIX + key+" not found in Ontology");
            }
            phenotype.setName( resource.getLabel() );            
            
            phenotype.setValueType( PhenotypeValueType.HPONTOLOGY.toString() );
        } else if (key.trim().equalsIgnoreCase( "GENDER" )) {
            phenotype.setName( key );
            phenotype.setValueType( PhenotypeValueType.GENDER.toString() );
        } else {
            phenotype.setName( key );
            phenotype.setValueType( PhenotypeValueType.CUSTOM.toString() );
        }
        
    }
    
    public void setValue(PhenotypeValueObject phenotype, String value) throws InvalidDataException{
        if (phenotype.getValueType().equals( PhenotypeValueType.HPONTOLOGY.toString() )) {
            phenotype.setDbValue(  convertValueToHPOntologyStandardValue(value ) );
        } else if (phenotype.getValueType().equalsIgnoreCase( PhenotypeValueType.GENDER.toString() )) {
            value = value.trim().toUpperCase();
            if (value.equals( "MALE" ) || value.equals( "FEMALE" ) || value.equals( "M" ) || value.equals( "F" )){
                phenotype.setDbValue( value );
            } else {
                throw new InvalidDataException("Invalid Gender value, use MALE, FEMALE, M, or F");
            }
        }
        else {
            phenotype.setDbValue(value);
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
            
            if (intValue!=1 && intValue!=0){
                throw new InvalidDataException("Invalid HP Ontology value. Must be one of 1,0,Y,N");
            }
            
        } catch (NumberFormatException nfe){
            if (value != null && value.trim().equalsIgnoreCase( "Y" )) {
                return "1";
            } else if (value != null && value.trim().equalsIgnoreCase( "N" )){
                return "0";
            }else{
                throw new InvalidDataException("Invalid HP Ontology value. Must be one of 1,0,Y,N");
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

    private Collection<PhenotypeRestriction> expandPhenotypeRestriction( PhenotypeRestriction phenotype) {
        HumanPhenotypeOntologyService humanPhenotypeOntology = os.getHumanPhenotypeOntologyService();
        Set<PhenotypeRestriction> expandedRestrictions = new HashSet<PhenotypeRestriction>();
        expandedRestrictions.add(phenotype);

        if (!phenotype.isOntologyTerm()) {
            return expandedRestrictions;
        }

        OntologyTerm term = humanPhenotypeOntology.getTerm( phenotype.getUri() );
        if ( phenotype.isAbsent() ) {
            // Infer absence by looking at parents.
            createRestrictionsUsingAncestors(phenotype.getValue(), term);
        } else if ( phenotype.isPresent() ) {
            // Infer presence by looking at children.
            createRestrictionsForDescendants( phenotype.getValue(), term );
        }

        return expandedRestrictions;
    }

    private Set<PhenotypeRestriction> createRestrictionsForDescendants(String value, OntologyTerm term) {
        Collection<OntologyTerm> childTerms = term.getChildren( false );
        Set<PhenotypeRestriction> expandedRestrictions = new HashSet<PhenotypeRestriction>();
        for (OntologyTerm childTerm : childTerms) {
            PhenotypeRestriction phenotypeRestriction =
                    new PhenotypeRestriction(
                            childTerm.getLabel(),
                            value,
                            childTerm.getUri() );
            expandedRestrictions.add( phenotypeRestriction );
        }
        return expandedRestrictions;
    }

    private Set<PhenotypeRestriction> createRestrictionsUsingAncestors(String value, OntologyTerm term) {
        Collection<OntologyTerm> ancestorTerms = term.getParents( false );
        Set<PhenotypeRestriction> expandedRestrictions = new HashSet<PhenotypeRestriction>();
        for (OntologyTerm ancestorTerm : ancestorTerms) {
            PhenotypeRestriction phenotypeRestriction =
                    new PhenotypeRestriction(
                            ancestorTerm.getLabel(),
                            value,
                            ancestorTerm.getUri() );

            expandedRestrictions.add( phenotypeRestriction );
        }
        return expandedRestrictions;
    }


    private RestrictionExpression expandNodesUsingOntologyInference( RestrictionExpression node ) {
        if (node instanceof PhenotypeRestriction) {
            PhenotypeRestriction phenotype = (PhenotypeRestriction) node;
            Disjunction disjunction = new Disjunction();
            disjunction.addAll( expandPhenotypeRestriction( phenotype ));
            return disjunction;
        } else if (node instanceof Junction) {
            Collection<RestrictionExpression> restrictions = ((Junction) node).getRestrictions();
            Collection<RestrictionExpression> results = new ArrayList();
            for (RestrictionExpression restriction : restrictions) {
                RestrictionExpression result = expandNodesUsingOntologyInference( restriction );
                results.add( result );
            }
            ((Junction) node).replaceAll( results );
            return node;
        } else { // Any other leaf node
            return node;
        }
    }

    public PhenotypeFilterConfig expandOntologyTerms(PhenotypeFilterConfig filterConfig) {
        RestrictionExpression restrictionTree = filterConfig.getRestriction();
        // Traverse and expand ontology terms
        restrictionTree = expandNodesUsingOntologyInference( restrictionTree );
        filterConfig.setRestriction( restrictionTree );
        return filterConfig;
    }
}
