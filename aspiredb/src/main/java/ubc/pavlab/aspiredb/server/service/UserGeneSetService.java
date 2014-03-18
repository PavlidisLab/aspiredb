/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
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
package ubc.pavlab.aspiredb.server.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.UserGeneSet;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.GeneValueObject;


import com.sencha.gxt.data.shared.loader.PagingLoadResult;

/**
 * @author anton
 */
public interface UserGeneSetService {

    public List<GeneValueObject> loadUserGeneSet( String name );

    public Collection<String> getSavedUserGeneSetNames();

    public void deleteUserGeneSet( String name );
        
    public Long saveUserGeneSet(String geneName,List<GeneValueObject> genes);
    
    public boolean isGeneSetName(String name);

  
}