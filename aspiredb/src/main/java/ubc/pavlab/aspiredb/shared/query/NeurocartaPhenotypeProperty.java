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
package ubc.pavlab.aspiredb.shared.query;

import java.util.Arrays;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * author: anton date: 24/05/13
 */
@DataTransferObject(javascript = "NeurocartaPhenotypeProperty")
public class NeurocartaPhenotypeProperty extends Property<GenomicRangeDataType> {
    private static final long serialVersionUID = 718213962710902188L;

    public NeurocartaPhenotypeProperty() {
        this.dataType = new GenomicRangeDataType();
        this.displayName = "Phenocarta Phenotype";
        this.exampleValues = "Examples: <b>autism spectrum disorder</b>, <b>colon cancer</b>";
        this.operators = Arrays.asList( Operator.IS_IN_SET, Operator.IS_NOT_IN_SET );
        this.supportsSuggestions = true;
    }
}
