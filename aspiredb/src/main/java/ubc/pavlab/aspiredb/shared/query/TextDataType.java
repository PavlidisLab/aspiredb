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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * author: anton
 * date: 07/05/13
 */
@DataTransferObject(javascript = "TextDataType")
public class TextDataType extends DataType  {
    private static final long serialVersionUID = -6610724309760895447L;

    protected Collection<String> allowedValues = new ArrayList<String>();

    public TextDataType() {}

    public Collection<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(Collection<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public void addAllowedValues(Collection<String> allowedValues) {
        this.allowedValues.addAll( allowedValues );
    }
}
