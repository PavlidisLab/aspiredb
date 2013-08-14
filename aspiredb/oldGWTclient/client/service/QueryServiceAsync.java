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
package ubc.pavlab.aspiredb.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import ubc.pavlab.aspiredb.shared.*;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.QueryValueObject;
import ubc.pavlab.aspiredb.shared.suggestions.PhenotypeSuggestion;

import java.util.Collection;
import java.util.List;

public interface QueryServiceAsync {

    public void querySubjects( AspireDbPagingLoadConfig config,
                             AsyncCallback<PagingLoadResult<SubjectValueObject>> callback );

    public void queryVariants(AspireDbPagingLoadConfig config,
                              AsyncCallback<PagingLoadResult<VariantValueObject>> callback );

    @Deprecated
	public void getOntologyTermSuggestions( String query,
			AsyncCallback<PagingLoadResult<OntologyTermValueObject>> callback );

	public void getValuesForOntologyTerm( String ontologyTermUri, AsyncCallback<List<String>> callback );

    public void getPhenotypeSuggestionLoadResult(String query, Collection<Long> activeProjects, AsyncCallback<PagingLoadResult<PhenotypeSuggestion>> async);

	public void getSubjectSuggestionLoadResult(String query, AsyncCallback<PagingLoadResult<SubjectValueObject>> callback);

	public void getVariantLocationValueObjects(Property property, List<String> text, AsyncCallback<List<GwtSerializable>> callback);

	public void getPhenotypeSuggestions(List<String> names, AsyncCallback<List<PhenotypeSuggestion>> callback);

	public void getSubjects(List<String> subjectIds, AsyncCallback<List<SubjectValueObject>> callback);

	public void getGeneSuggestionLoadResult(String query, AsyncCallback<PagingLoadResult<GeneValueObject>> callback);

	public void getGeneSuggestions(String query, AsyncCallback<Collection<GeneValueObject>> callback);

	public void getNeurocartaPhenotypeSuggestionLoadResult(String query, AsyncCallback<PagingLoadResult<NeurocartaPhenotypeValueObject>> callback);

	public void getNeurocartaPhenotypeSuggestions(String query,	AsyncCallback<Collection<NeurocartaPhenotypeValueObject>> callback);

    public void saveQuery(QueryValueObject queryVO, AsyncCallback<QueryValueObject> callback);

    public void loadQuery(Long id, AsyncCallback<QueryValueObject> callback);

    void getSavedQueries(AsyncCallback<Collection<QueryValueObject>> async);

    void deleteQuery(QueryValueObject query, AsyncCallback<Void> async);

    void getSubjectCount(AspireDbPagingLoadConfig config, AsyncCallback<Integer> async);

    void getVariantCount(AspireDbPagingLoadConfig config, AsyncCallback<Integer> async);
}