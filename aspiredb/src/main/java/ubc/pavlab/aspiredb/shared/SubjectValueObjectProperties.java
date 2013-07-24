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
package ubc.pavlab.aspiredb.shared;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import java.util.Collection;
import java.util.Map;

/**
 * Provides access to SubjectValueObject properties.
 * 
 * @author frances
 * @version $Id: SubjectValueObjectProperties.java,v 1.2 2013/06/11 22:19:45 anton Exp $
 */
public interface SubjectValueObjectProperties extends PropertyAccess<SubjectValueObject> {
    public ModelKeyProvider<SubjectValueObject> id();

    public ValueProvider<SubjectValueObject, String> patientId();

    public ValueProvider<SubjectValueObject, Map<String,PhenotypeValueObject>> phenotypes();

    public ValueProvider<SubjectValueObject, Integer> variants();

    public ValueProvider<SubjectValueObject, Collection<LabelValueObject>> labels();
}
