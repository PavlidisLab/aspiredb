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

import java.util.Collection;
import java.util.List;

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.model.UserGeneSet;
import ubc.pavlab.aspiredb.shared.GeneSetValueObject;
import ubc.pavlab.aspiredb.shared.GeneValueObject;

/**
 * @author Gaya
 */
public interface UserGeneSetService {

    public List<GeneValueObject> loadUserGeneSet( String name );

    public Collection<String> getSavedUserGeneSetNames();

    public void addGenes( String geneName, String geneSymbol ) throws BioMartServiceException;

    public void deleteUserGeneSet( String name );
    

    public void deleteGene( String geneSetName, String geneSymbol ) throws BioMartServiceException;

    public Long saveUserGeneSet( String geneName, List<GeneValueObject> genes ) throws BioMartServiceException;

    public boolean isGeneSetName( String name );

    public boolean isGeneInGeneSet( String genSetName, String geneSymbol );

    public List<GeneValueObject> getGenes( String geneSymbol ) throws BioMartServiceException;

    List<GeneSetValueObject> suggestUserGeneSet( String query );

    void addGenesToGeneSet( String geneSetName, List<String> geneSymbol ) throws BioMartServiceException;

    public void updateUserGeneSet( GeneSetValueObject geneset ) ;

    GeneSetValueObject findUserGeneSet( String geneSetName );

    List<GeneSetValueObject> getSavedUserGeneSets();

}