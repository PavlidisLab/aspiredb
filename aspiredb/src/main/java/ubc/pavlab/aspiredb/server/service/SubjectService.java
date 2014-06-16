/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.server.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.StringMatrix;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: SubjectService.java,v 1.16 2013/06/24 23:26:40 cmcdonald Exp $
 */
public interface SubjectService {

    public LabelValueObject addLabel( Collection<Long> subjectIds, LabelValueObject label );

    /**
     * Rownames are Subject.patientId (e.g. 03_01) Colnames are PhenotypeValueUri:Name (e.g. HP_0001000:Abnormality of
     * skin pigmentation)
     * 
     * @param subjectIds
     * @param removeEmpty should Subjects with no phenotypes be excluded?
     * @return
     */
    public StringMatrix<String, String> getPhenotypeBySubjectIds( Collection<Long> subjectIds, boolean removeEmpty );

    public List<PhenotypeSummaryValueObject> getPhenotypeSummaries( List<Long> subjectIds, Collection<Long> projectIds )
            throws ExternalDependencyException;

    public Map<String, PhenotypeSummaryValueObject> getPhenotypeSummaryValueObjects( List<Long> subjectIds,
            Collection<Long> projectIds ) throws NeurocartaServiceException;

    public String getPhenotypeTextDownloadBySubjectIds( List<Long> subjectIds );

    public SubjectValueObject getSubject( Long projectId, Long subjectId );

    public Collection<SubjectValueObject> getSubjects( Long projectId, List<Long> subjectId );

    public List<Long> getVariantsSubjects( List<String> patientIds );

    public void removeLabel( Collection<Long> subjectIds, LabelValueObject label );

    public void removeLabel( Long subjectId, LabelValueObject label );

    public List<LabelValueObject> suggestLabels( SuggestionContext suggestionContext );

    public Collection<Property> suggestProperties();

    public Collection<PropertyValue> suggestValues( Property property, SuggestionContext suggestionContext );

    Collection<Label> getSubjectLabels( Collection<Long> subjectIds );
}
