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
@DataTransferObject(javascript = "CharacteristicProperty")
public class CharacteristicProperty extends Property<DataType> {

    private static final long serialVersionUID = 1L;

    public CharacteristicProperty() {
        this.operators = Arrays.asList( Operator.IS_IN_SET, Operator.IS_NOT_IN_SET );
        this.supportsSuggestions = true;
        this.isCharacteristic = true;
    }

    public CharacteristicProperty( String name ) {
        this();
        this.name = name;
        this.displayName = name;
        this.dataType = new TextDataType();
        this.isCharacteristic = true;
    }

    public CharacteristicProperty( String name, DataType dataType ) {
        this();
        this.name = name;
        this.displayName = name;
        this.dataType = dataType;
        this.isCharacteristic = true;
    }
}
