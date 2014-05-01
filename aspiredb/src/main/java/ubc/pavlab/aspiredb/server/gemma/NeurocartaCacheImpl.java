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
package ubc.pavlab.aspiredb.server.gemma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.expression.Criteria;

import org.springframework.stereotype.Component;

import ubc.pavlab.aspiredb.server.util.SearchableEhcache;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;

/**
 * Neurocarta cache implementation
 * 
 * @author frances
 * @version $Id: NeurocartaCacheImpl.java,v 1.3 2013/06/11 22:30:46 anton Exp $
 */
@Component
public class NeurocartaCacheImpl extends SearchableEhcache<NeurocartaPhenotypeValueObject> implements NeurocartaCache {
    // These constants are used in ehcache.xml. If they are changed, ehcache.xml must be modified.
    private static final String CACHE_NAME = "NeurocartaPhenotypeCache";
    private static final String PHENOTYPE_NAME_SEARCH_ATTRIBUTE_NAME = "name";

    @Override
    public Object getKey( NeurocartaPhenotypeValueObject neurocartaPhenotype ) {
        return neurocartaPhenotype.getUri();
    }

    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    @Override
    public boolean hasPhenotype( String phenotypeUri ) {
        return isKeyInCache( phenotypeUri );
    }

    @Override
    public Collection<NeurocartaPhenotypeValueObject> findPhenotypes( String queryString ) {
        String regexQueryString = "*" + queryString + "*";

        Criteria nameCriteria = getSearchAttribute( PHENOTYPE_NAME_SEARCH_ATTRIBUTE_NAME ).ilike( regexQueryString );

        return fetchByCriteria( nameCriteria );
    }

    @Override
    public List<NeurocartaPhenotypeValueObject> getPhenotypes( List<String> names ) {
        List<NeurocartaPhenotypeValueObject> phenotypes = new ArrayList<NeurocartaPhenotypeValueObject>( names.size() );

        Attribute<Object> phenotypeNameAttribute = getSearchAttribute( PHENOTYPE_NAME_SEARCH_ATTRIBUTE_NAME );

        for ( String name : names ) {
            Criteria nameCriteria = phenotypeNameAttribute.ilike( name );
            Collection<NeurocartaPhenotypeValueObject> fetchedPhenotypes = fetchByCriteria( nameCriteria );
            if ( fetchedPhenotypes.size() > 0 ) {
                // Only use the first phenotype.
                phenotypes.add( fetchedPhenotypes.iterator().next() );
            } else {
                phenotypes.add( null );
            }

        }

        return phenotypes;
    }
}
