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

import ubc.pavlab.aspiredb.shared.LabelValueObject;

/**
 * author: anton date: 10/06/13
 */
public interface LabelService {

    public void deleteSubjectLabel( LabelValueObject label );

    public void deleteSubjectLabels( Collection<LabelValueObject> labels );

    public void deleteVariantLabel( LabelValueObject label );

    public void deleteVariantLabels( Collection<LabelValueObject> labels );

    public void removeLabelsFromSubjects( Collection<LabelValueObject> label, Collection<Long> subjectIds );

    public void removeLabelsFromVariants( Collection<LabelValueObject> label, Collection<Long> variantIds );

    public void updateLabel( LabelValueObject label );

    public void updateSubjectLabel( LabelValueObject label );

}
