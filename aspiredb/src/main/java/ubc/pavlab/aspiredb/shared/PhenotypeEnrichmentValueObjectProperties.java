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

/**
 * TODO Document Me
 * 
 * @author ?
 * @version $Id: PhenotypeEnrichmentValueObjectProperties.java,v 1.1 2013/07/09 21:25:08 cmcdonald Exp $
 */
public interface PhenotypeEnrichmentValueObjectProperties extends PropertyAccess<PhenotypeEnrichmentValueObject> {

    ModelKeyProvider<PhenotypeEnrichmentValueObject> id();

    ValueProvider<PhenotypeEnrichmentValueObject, String> inGroupTotalString();
    ValueProvider<PhenotypeEnrichmentValueObject, String> outGroupTotalString();
    ValueProvider<PhenotypeEnrichmentValueObject, String> name();    
    ValueProvider<PhenotypeEnrichmentValueObject, String> pValueCorrectedString();
    ValueProvider<PhenotypeEnrichmentValueObject, String> pValueString();    
    
}
