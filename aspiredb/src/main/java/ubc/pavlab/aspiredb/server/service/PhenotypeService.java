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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeEnrichmentValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.query.PhenotypeProperty;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * Functions for suggesting ontology terms, phenotype enrichment, and phenotype inference.
 * 
 * @version $Id$
 */
public interface PhenotypeService {

    public List<PhenotypeEnrichmentValueObject> getPhenotypeEnrichmentValueObjects( Collection<Long> activeProjects,
            Collection<Long> subjectIds ) throws NotLoggedInException;

    public Map<String, PhenotypeValueObject> getPhenotypes( Long subjectId ) throws NotLoggedInException,
            NotLoggedInException;

    public Map<String, Collection<GeneValueObject>> populateDescendantPhenotypes( String phenotypeUri )
            throws NeurocartaServiceException, BioMartServiceException;

    public Collection<PhenotypeProperty> suggestPhenotypes( SuggestionContext suggestionContext )
            throws NotLoggedInException;

    public Collection<PropertyValue> suggestPhenotypeValues( PhenotypeProperty property,
            SuggestionContext suggestionContext ) throws NotLoggedInException;

    public List<PhenotypeEnrichmentValueObject> getPhenotypeEnrichmentValueObjects( Collection<Long> activeProjects,
            Collection<Long> subjectIds, Collection<Long> complementSubjectIds ) throws NotLoggedInException;

}
