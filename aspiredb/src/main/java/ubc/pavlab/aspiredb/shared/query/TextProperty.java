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
 * date: 07/05/13
 */
@DataTransferObject(javascript = "TextProperty")
public class TextProperty extends Property<TextDataType> {
    private static final long serialVersionUID = 3655463679776859811L;

    public TextProperty() {
        this.dataType = new TextDataType();
        this.operators = Arrays.asList(Operator.IS_IN_SET, Operator.IS_NOT_IN_SET);
        this.supportsSuggestions = true;
    }

    public TextProperty (String displayName, String name) {
        this();
        this.displayName = displayName;
        this.name = name;
    }

    public TextProperty (String displayName, String name, Collection<String> allowedValues) {
        this();
        this.displayName = displayName;
        this.name = name;
        this.dataType.addAllowedValues( allowedValues );
    }


}
