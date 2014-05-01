/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
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
package ubc.pavlab.aspiredb.server.fileupload;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;

public class PhenotypeUploadServiceResult {

    public PhenotypeUploadServiceResult( ArrayList<PhenotypeValueObject> phenotypes, List<String> errors,
            HashSet<String> unmatchedStrings ) {

        phenotypesToAdd = phenotypes;
        errorMessages = errors;
        unmatched = unmatchedStrings;

    }

    HashSet<String> unmatched = new HashSet<String>();

    ArrayList<PhenotypeValueObject> phenotypesToAdd;

    List<String> errorMessages;

    public ArrayList<PhenotypeValueObject> getPhenotypesToAdd() {
        return phenotypesToAdd;
    }

    public void setPhenotypesToAdd( ArrayList<PhenotypeValueObject> phenotypesToAdd ) {
        this.phenotypesToAdd = phenotypesToAdd;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages( List<String> errorMessages ) {
        this.errorMessages = errorMessages;
    }

    public HashSet<String> getUnmatched() {
        return unmatched;
    }

    public void setUnmatched( HashSet<String> unmatched ) {
        this.unmatched = unmatched;
    }
}