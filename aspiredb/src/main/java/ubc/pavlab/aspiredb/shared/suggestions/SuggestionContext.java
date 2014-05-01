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
package ubc.pavlab.aspiredb.shared.suggestions;

import java.io.Serializable;
import java.util.Collection;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * author: anton date: 02/05/13
 */
@DataTransferObject(javascript = "SuggestionContext")
public class SuggestionContext implements Serializable {
    private static final long serialVersionUID = 6089979800051229965L;

    private Collection<Long> activeProjectIds;
    private String valuePrefix;

    public String getValuePrefix() {
        return valuePrefix;
    }

    public void setValuePrefix( String valuePrefix ) {
        this.valuePrefix = valuePrefix;
    }

    public Collection<Long> getActiveProjectIds() {
        return activeProjectIds;
    }

    public void setActiveProjectIds( Collection<Long> activeProjectIds ) {
        this.activeProjectIds = activeProjectIds;
    }

}
