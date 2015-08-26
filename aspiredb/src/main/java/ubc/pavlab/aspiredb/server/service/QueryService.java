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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Query;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.OntologyTermValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.suggestions.PhenotypeSuggestion;

import com.sencha.gxt.data.shared.loader.PagingLoadResult;

/**
 * Methods for various query operations such as querying a list of subjects and variants given a list of filters,
 * getting a list of gene suggestions, ontology term suggestions. Also deals with saving and loading of queries.
 * 
 * @author anton
 */
public interface QueryService {

    public BoundedList<SubjectValueObject> querySubjects( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException;

    public BoundedList<VariantValueObject> queryVariants( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException;

    public PagingLoadResult<OntologyTermValueObject> getOntologyTermSuggestions( String query );

    public List<String> getValuesForOntologyTerm( String ontologyTermUri );

    public PagingLoadResult<PhenotypeSuggestion> getPhenotypeSuggestionLoadResult( String query,
            Collection<Long> activeProjects );

    public PagingLoadResult<GeneValueObject> getGeneSuggestionLoadResult( String query )
            throws BioMartServiceException, BioMartServiceException;

    public PagingLoadResult<NeurocartaPhenotypeValueObject> getNeurocartaPhenotypeSuggestionLoadResult( String query )
            throws NeurocartaServiceException, NeurocartaServiceException;

    public PagingLoadResult<SubjectValueObject> getSubjectSuggestionLoadResult( String query );

    public List<Serializable> getVariantLocationValueObjects( Property property, List<String> text )
            throws BioMartServiceException, NeurocartaServiceException;

    public List<PhenotypeSuggestion> getPhenotypeSuggestions( List<String> names );

    public List<SubjectValueObject> getSubjects( List<String> subjectIds );

    public Long saveQuery( String name, Set<AspireDbFilterConfig> filters );

    public Set<AspireDbFilterConfig> loadQuery( String name );

    public Collection<String> getSavedQueryNames();

    public void deleteQuery( String name );

    public boolean isQueryName( String name );

    public int getSubjectCount( Set<AspireDbFilterConfig> filters ) throws NotLoggedInException,
            ExternalDependencyException;

    public int getVariantCount( Set<AspireDbFilterConfig> filters ) throws NotLoggedInException,
            ExternalDependencyException;

    /**
     * Combination of {@link QueryService#getSubjectCount(Set)} and {@link QueryService#getVariantCount(Set)} that
     * avoids redundant filtering.
     * 
     * @param filters
     * @return Map with keys {@link VariantDao.SUBJECT_IDS_KEY} and {@link VariantDao.VARIANT_IDS_KEY}
     * @throws NotLoggedInException
     * @throws ExternalDependencyException
     */
    public Map<Integer, Integer> getSubjectVariantCounts( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException;

    public Map<Integer, Collection<Object>> getSubjectsVariants( Set<AspireDbFilterConfig> filters )
            throws NotLoggedInException, ExternalDependencyException;

    Query getQuery( Long id );

    Map<Integer, Integer> getSubjectGenes( Set<AspireDbFilterConfig> filters ) throws NotLoggedInException,
            ExternalDependencyException;

    /**
     * Returns an instance of the project from the filters if any.
     * 
     * @param filters
     * @return
     */
    public Project getProject( Set<AspireDbFilterConfig> filters );

}