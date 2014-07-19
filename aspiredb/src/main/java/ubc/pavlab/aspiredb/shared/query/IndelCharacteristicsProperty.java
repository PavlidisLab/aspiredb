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
 * author: gaya date: 07/18/14 Class is concrete because I couldn't get DWR to see it when it is abstract.
 */
@DataTransferObject(javascript = "IndelCharacteristicsProperty")
public class IndelCharacteristicsProperty extends TextProperty {

    /**
     * generated the serializable id
     */
    private static final long serialVersionUID = -2124660825928667197L;

    public IndelCharacteristicsProperty() {
        super( "Indel Characteristics", "Indel", Arrays.asList( "Indel Length", "characteristics" ) );
    }

}