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

import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
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
 * @version $Id: SubjectService.java,v 1.16 2013/06/24 23:26:40 cmcdonald Exp $
 */
public interface SubjectService {

    public SubjectValueObject getSubject(Long projectId, Long subjectId)
            throws NotLoggedInException;

    public Collection<Property> suggestProperties()
        throws NotLoggedInException;

    public Collection<PropertyValue> suggestValues(Property property, SuggestionContext suggestionContext)
            throws NotLoggedInException;

    public List<PhenotypeSummaryValueObject> getPhenotypeSummaries(List<Long> subjectIds, Collection<Long> projectIds)
            throws NotLoggedInException, ExternalDependencyException;
    
    public List<SubjectValueObject> getSubjectsWithPhenotypesBySubjectIds(List<Long> subjectIds)
            throws NotLoggedInException;

    public LabelValueObject addLabel(Collection<Long> subjectIds, LabelValueObject label) throws NotLoggedInException;

    public void removeLabel(Collection<Long> subjectIds, LabelValueObject label)
            throws NotLoggedInException;

    public void removeLabel(Long subjectId, LabelValueObject label)
            throws NotLoggedInException;

    public List<LabelValueObject> suggestLabels(SuggestionContext suggestionContext) throws NotLoggedInException;
}
