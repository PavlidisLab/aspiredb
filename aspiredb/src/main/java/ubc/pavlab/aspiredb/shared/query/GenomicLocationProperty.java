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

import org.directwebremoting.annotations.DataTransferObject;

import java.util.Arrays;
import java.util.Collection;

/**
 * author: anton
 * date: 23/05/13
 */
@DataTransferObject
public class GenomicLocationProperty extends Property<GenomicRangeDataType> {
    private static final long serialVersionUID = -1272375656961236482L;

    public GenomicLocationProperty() {
        this.dataType = new GenomicRangeDataType();
        this.displayName = "Location";
        this.exampleValues = "Examples: <b>1</b>, <b>2</b>, <b>2p25.1</b>, <b>2p25.1-p11.1</b>, <b>1:103956050-104113271</b>";
        this.operators = Arrays.asList( (Operator[]) SetOperator.values() );
    }
}
