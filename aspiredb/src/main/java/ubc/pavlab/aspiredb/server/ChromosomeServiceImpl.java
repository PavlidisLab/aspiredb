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
package ubc.pavlab.aspiredb.server;

import java.util.HashMap;
import java.util.Map;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ubc.pavlab.aspiredb.server.service.ChromosomeService;
import ubc.pavlab.aspiredb.shared.ChromosomeValueObject;

/**
 * author: anton date: 19/02/13
 */
@RemoteProxy(name = "ChromosomeService")
@Service("chromosomeService")
public class ChromosomeServiceImpl implements ChromosomeService {

    @Autowired
    private GenomeCoordinateConverter genomeCoordinateConverter;

    @RemoteMethod
    @Override
    public Map<String, ChromosomeValueObject> getChromosomes() {
        Map<String, Chromosome> chromosomes = genomeCoordinateConverter.getChromosomes();
        Map<String, ChromosomeValueObject> chromosomeVOs = new HashMap<String, ChromosomeValueObject>();
        for ( Chromosome chromosome : chromosomes.values() ) {
            ChromosomeValueObject chromosomeValueObject = convert( chromosome );
            chromosomeVOs.put( chromosomeValueObject.getName(), chromosomeValueObject );
        }
        return chromosomeVOs;
    }

    private ChromosomeValueObject convert( Chromosome chromosome ) {
        return new ChromosomeValueObject( chromosome.getName(), chromosome.getBands(), chromosome.getSize(),
                chromosome.getCentromereLocation() );
    }
}
