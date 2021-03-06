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
import java.util.List;

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.shared.VariantValueObject;

@DataTransferObject
public class VariantUploadServiceResult {

    List<VariantValueObject> variantsToAdd;

    List<String> errorMessages;

    public VariantUploadServiceResult( ArrayList<VariantValueObject> variants, List<String> errors ) {

        variantsToAdd = variants;
        errorMessages = errors;

    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public List<VariantValueObject> getVariantsToAdd() {
        return variantsToAdd;
    }

    public void setErrorMessages( List<String> errorMessages ) {
        this.errorMessages = errorMessages;
    }

    public void setVariantsToAdd( ArrayList<VariantValueObject> variantsToAdd ) {
        this.variantsToAdd = variantsToAdd;
    }
}