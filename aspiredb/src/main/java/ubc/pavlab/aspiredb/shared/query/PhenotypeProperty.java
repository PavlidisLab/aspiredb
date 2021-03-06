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

/**
 * author: anton date: 07/06/13
 */
@DataTransferObject(javascript = "PhenotypeProperty")
public class PhenotypeProperty extends Property {
    private static final long serialVersionUID = -8724559011217298269L;

    private boolean isOntologyTerm;
    private String uri;
    private boolean existInDatabase;

    public PhenotypeProperty() {
    }

    public PhenotypeProperty( String name, DataType datatype ) {
        this.name = name;
        this.displayName = name;
        this.dataType = datatype;
        this.supportsSuggestions = true;
    }

    public String getUri() {
        return uri;
    }

    public boolean isExistInDatabase() {
        return existInDatabase;
    }

    public boolean isOntologyTerm() {
        return isOntologyTerm;
    }

    public void setExistInDatabase( boolean existInDatabase ) {
        this.existInDatabase = existInDatabase;
    }

    public void setOntologyTerm( boolean ontologyTerm ) {
        isOntologyTerm = ontologyTerm;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }
}
