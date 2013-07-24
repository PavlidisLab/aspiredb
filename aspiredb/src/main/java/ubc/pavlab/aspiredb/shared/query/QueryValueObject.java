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

import java.io.Serializable;
import java.util.Set;

/**
 * author: anton
 * date: 22/05/13
 */
public class QueryValueObject implements Serializable {
    private static final long serialVersionUID = 2761322439040578263L;

    private Long id;
    private String name;
    private Set<RestrictionFilterConfig> query;

    public QueryValueObject(String name, Set<RestrictionFilterConfig> query) {
        this.name = name;
        this.query = query;
    }

    public QueryValueObject() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<RestrictionFilterConfig> getQuery() {
        return query;
    }

    public void setQuery(Set<RestrictionFilterConfig> query) {
        this.query = query;
    }
}
