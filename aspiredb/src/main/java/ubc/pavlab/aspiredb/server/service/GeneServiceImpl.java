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
package ubc.pavlab.aspiredb.server.service;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.GeneValueObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * author: anton
 * date: 01/05/13
 */
@Service("geneService")
@RemoteProxy(name="GeneService")
public class GeneServiceImpl implements GeneService {

    @Autowired private VariantDao variantDao;
    @Autowired private BioMartQueryService bioMartQueryService;
    @Autowired private NeurocartaQueryService neurocartaQueryService;
    
    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public List<GeneValueObject> getGenesInsideVariants(Collection<Long> ids)
            throws NotLoggedInException, BioMartServiceException {
        
        // Used to remove duplicates
        HashMap<String,GeneValueObject> genes = new HashMap<String, GeneValueObject>();
        for (Long id: ids) {
            Variant variant = variantDao.load( id );
            GenomicLocation location = variant.getLocation();
            Collection<GeneValueObject> genesInsideRange = this.bioMartQueryService.fetchGenesByLocation(
                    String.valueOf(location.getChromosome()),
                    (long) location.getStart(), (long) location.getEnd());
            for (GeneValueObject geneValueObject : genesInsideRange) {
                genes.put(geneValueObject.getEnsemblId(), geneValueObject);
            }
        }
        List<GeneValueObject> results = new ArrayList<GeneValueObject>(genes.values());
        return results;
    }

    @Override
    @RemoteMethod
    public Collection<GeneValueObject> findGenesWithNeurocartaPhenotype(String phenotypeValueUri)
            throws NotLoggedInException, ExternalDependencyException {
        
        return this.neurocartaQueryService.fetchGenesAssociatedWithPhenotype(phenotypeValueUri);
    }
}
