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

package ubc.pavlab.aspiredb.server.ontology;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import ubic.basecode.ontology.model.OntologyIndividual;
import ubic.basecode.ontology.model.OntologyResource;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.providers.AbstractOntologyService;
import ubic.basecode.ontology.providers.DiseaseOntologyService;
import ubic.basecode.ontology.providers.HumanPhenotypeOntologyService;
import ubic.basecode.ontology.search.OntologySearch;

/**
 * Has a static method for finding out which ontologies are loaded into the system and a general purpose find method
 * that delegates to the many ontology services. NOTE: Logging messages from this service are important for tracking
 * changes to annotations.
 * 
 * @author pavlidis
 * @version $I$
 */
@Component("ontologyService")
public class OntologyServiceImpl implements OntologyService {

    private static Log log = LogFactory.getLog( OntologyServiceImpl.class.getName() );

    private DiseaseOntologyService diseaseOntologyService;

    private HumanPhenotypeOntologyService humanPhenotypeOntologyService;

    private Collection<AbstractOntologyService> ontologyServices = new HashSet<AbstractOntologyService>();

    @Override
    public void afterPropertiesSet() {

        this.diseaseOntologyService = new DiseaseOntologyService();

        this.humanPhenotypeOntologyService = new HumanPhenotypeOntologyService();

        this.ontologyServices.add( this.diseaseOntologyService );

        this.ontologyServices.add( this.humanPhenotypeOntologyService );

        for ( AbstractOntologyService serv : this.ontologyServices ) {
            serv.startInitializationThread( false );
        }

    }

    @Override
    public Collection<OntologyIndividual> findIndividuals( String givenSearch ) {

        String query = OntologySearch.stripInvalidCharacters( givenSearch );
        Collection<OntologyIndividual> results = new HashSet<OntologyIndividual>();

        for ( AbstractOntologyService ontology : ontologyServices ) {
            Collection<OntologyIndividual> found = ontology.findIndividuals( query );
            if ( found != null ) results.addAll( found );
        }

        return results;
    }

    @Override
    public Collection<OntologyTerm> findTerms( String search ) {

        String query = OntologySearch.stripInvalidCharacters( search );

        Collection<OntologyTerm> results = new HashSet<OntologyTerm>();

        if ( StringUtils.isBlank( query ) ) {
            return results;
        }

        for ( AbstractOntologyService ontology : ontologyServices ) {
            if ( ontology.isOntologyLoaded() ) {
                Collection<OntologyTerm> found = ontology.findTerm( query );
                if ( found != null ) results.addAll( found );
            }
        }

        return results;
    }

    @Override
    public DiseaseOntologyService getDiseaseOntologyService() {
        return diseaseOntologyService;
    }

    @Override
    public HumanPhenotypeOntologyService getHumanPhenotypeOntologyService() {
        return humanPhenotypeOntologyService;
    }

    @Override
    public OntologyResource getResource( String uri ) {
        for ( AbstractOntologyService ontology : ontologyServices ) {
            OntologyResource resource = ontology.getResource( uri );
            if ( resource != null ) return resource;
        }
        return null;
    }

    @Override
    public OntologyTerm getTerm( String uri ) {
        for ( AbstractOntologyService ontology : ontologyServices ) {
            OntologyTerm term = ontology.getTerm( uri );
            if ( term != null ) return term;
        }
        return null;
    }

    @Override
    public boolean isObsolete( String uri ) {
        OntologyTerm t = this.getTerm( uri );
        if ( t != null && t.isTermObsolete() ) return true;
        return false;
    }

    @Override
    public void reinitializeAllOntologies() {
        for ( AbstractOntologyService serv : this.ontologyServices ) {
            serv.startInitializationThread( true );
        }
    }

}