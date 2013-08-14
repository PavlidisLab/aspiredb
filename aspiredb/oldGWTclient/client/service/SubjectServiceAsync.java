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
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

import java.util.Collection;
import java.util.List;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: SubjectServiceAsync.java,v 1.14 2013/06/19 18:23:34 anton Exp $
 */
public interface SubjectServiceAsync {

    public void getSubject( Long projectId, Long subjectId, AsyncCallback<SubjectValueObject> callback );

    public void getPhenotypeSummaries( List<Long> subjectIds, Collection<Long> projectIds,
            AsyncCallback<List<PhenotypeSummaryValueObject>> aspireAsyncCallback );

    public void getSubjectsWithPhenotypesBySubjectIds( List<Long> subjectIds,
            AsyncCallback<List<SubjectValueObject>> aspireAsyncCallback );

    void addLabel( Collection<Long> subjectIds, LabelValueObject label, AsyncCallback<LabelValueObject> async );

    void suggestProperties( AsyncCallback<Collection<Property>> async );

    void suggestLabels( SuggestionContext suggestionContext, AsyncCallback<List<LabelValueObject>> async );

    void suggestValues( Property property, SuggestionContext suggestionContext,
            AsyncCallback<Collection<PropertyValue>> async );

    void removeLabel(Collection<Long> subjectIds, LabelValueObject label, AsyncCallback<Void> async)
            ;

    void removeLabel(Long subjectId, LabelValueObject label, AsyncCallback<Void> async)
            ;
}
