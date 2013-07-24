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

/**
 * author: anton
 * date: 24/05/13
 */
public enum SetOperator implements Operator {
    IS_IN("is in"),
    IS_NOT_IN("is not in");

    private String displayLabel;

    SetOperator(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    @Override
    public String getDisplayLabel() {
        return displayLabel;
    }
}
