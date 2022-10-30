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

import org.springframework.beans.factory.InitializingBean;

import ubic.basecode.ontology.model.OntologyIndividual;
import ubic.basecode.ontology.model.OntologyResource;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.providers.DiseaseOntologyService;
import ubic.basecode.ontology.providers.HumanPhenotypeOntologyService;
import ubic.basecode.ontology.search.OntologySearchException;

/**
 * @author paul
 * @version $Id: OntologyService.java,v 1.4 2013/06/11 22:30:40 anton Exp $
 */
public interface OntologyService extends InitializingBean {

    /**
     * @param givenSearch
     * @return
     */
    public Collection<OntologyIndividual> findIndividuals( String givenSearch ) throws OntologySearchException;

    /**
     * Given a search string will look through the loaded ontologies for terms that match the search term. this a lucene
     * backed search, is inexact and for general terms can return a lot of results.
     * 
     * @param search
     * @return returns a collection of ontologyTerm's
     */
    public Collection<OntologyTerm> findTerms( String search ) throws OntologySearchException;

    /**
     * @return the diseaseOntologyService
     */
    public DiseaseOntologyService getDiseaseOntologyService();

    /**
     * @return the HumanPhenotypeOntologyService
     */
    public HumanPhenotypeOntologyService getHumanPhenotypeOntologyService();

    /**
     * @return the OntologyResource for the specified URI
     */
    public OntologyResource getResource( String uri );

    /**
     * @return the OntologyTerm for the specified URI.
     */
    public OntologyTerm getTerm( String uri );

    public boolean isObsolete( String uri );

    /**
     * Reinitialize all the ontologies "from scratch". This is necessary if indices are old etc. This should be
     * admin-only.
     */
    public void reinitializeAllOntologies();
}