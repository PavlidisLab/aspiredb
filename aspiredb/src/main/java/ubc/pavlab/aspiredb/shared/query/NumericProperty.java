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
 * author: anton date: 07/05/13
 */
@DataTransferObject(javascript = "NumericProperty")
public class NumericProperty extends Property<NumericalDataType> {

    /**
     * 
     */
    private static final long serialVersionUID = 5128521922049745550L;

    public NumericProperty() {
        this.dataType = new NumericalDataType();
        this.operators = Arrays.asList( Operator.NUMERIC_EQUAL, Operator.NUMERIC_GREATER, Operator.NUMERIC_LESS,
                Operator.NUMERIC_NOT_EQUAL );
    }

    public NumericProperty( String displayName, String name ) {
        this();
        this.displayName = displayName;
        this.name = name;
    }

}
