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
package ubc.pavlab.aspiredb.server.biomartquery;

import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.expression.Criteria;
import org.springframework.stereotype.Component;
import ubc.pavlab.aspiredb.server.util.SearchableEhcache;
import ubc.pavlab.aspiredb.shared.GeneValueObject;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * BioMart cache imlementation
 * 
 * @author frances
 * @version $Id: BioMartCacheImpl.java,v 1.8 2013/07/15 16:01:53 anton Exp $
 */
@Component
public class BioMartCacheImpl extends SearchableEhcache<GeneValueObject> implements BioMartCache {
	// These constants are used in ehcache.xml. If they are changed, ehcache.xml must be modified. 
    private static final String CACHE_NAME = "BioMartCache";
    private static final String GENE_ENSEMBL_ID_SEARCH_ATTRIBUTE_NAME = "ensemblId";
    private static final String GENE_NAME_SEARCH_ATTRIBUTE_NAME = "name";
    private static final String GENE_SYMBOL_SEARCH_ATTRIBUTE_NAME = "symbol";
    private static final String CHROMOSOME_SEARCH_ATTRIBUTE_NAME = "genomicRangeChromosome";
    private static final String START_SEARCH_ATTRIBUTE_NAME = "genomicRangeStart";
    private static final String END_SEARCH_ATTRIBUTE_NAME = "genomicRangeEnd";

    private Attribute<Object> geneEnsemblIdAttribute;
	private Attribute<Object> geneNameAttribute;
	private Attribute<Object> geneSymbolAttribute;
	private Attribute<Object> chromosomeAttribute;
	private Attribute<Object> startAttribute;
	private Attribute<Object> endAttribute;

	@SuppressWarnings("unused")
	@PostConstruct
	private void initialize() {
		geneEnsemblIdAttribute = getSearchAttribute(GENE_ENSEMBL_ID_SEARCH_ATTRIBUTE_NAME);
		geneNameAttribute = getSearchAttribute(GENE_NAME_SEARCH_ATTRIBUTE_NAME);
		geneSymbolAttribute = getSearchAttribute(GENE_SYMBOL_SEARCH_ATTRIBUTE_NAME);
		chromosomeAttribute = getSearchAttribute(CHROMOSOME_SEARCH_ATTRIBUTE_NAME);
		startAttribute = getSearchAttribute(START_SEARCH_ATTRIBUTE_NAME);
		endAttribute = getSearchAttribute(END_SEARCH_ATTRIBUTE_NAME);
	}

	@Override
	public Object getKey(GeneValueObject gene) {
		return gene.getEnsemblId();
	}

	@Override
	public String getCacheName() {
		return CACHE_NAME;
	}
	
	@Override
	public Collection<GeneValueObject> findGenes(String queryString) {
		String regexQueryString = "*" + queryString + "*";
	
		Criteria nameCriteria = geneNameAttribute.ilike(regexQueryString);
		Criteria symbolCriteria = geneSymbolAttribute.ilike(regexQueryString);
		
		return fetchByCriteria(nameCriteria.or(symbolCriteria));
	}

	@Override
	public List<GeneValueObject> getGenes(List<String> geneStrings) {
		List<GeneValueObject> genes = new ArrayList<GeneValueObject>(geneStrings.size());
		
		for (String geneString: geneStrings) {
			Criteria symbolCriteria = geneSymbolAttribute.ilike(geneString);
			Criteria ensemblIdCriteria = geneEnsemblIdAttribute.ilike(geneString);
			Collection<GeneValueObject> fetchedGenes = fetchByCriteria(symbolCriteria.or(ensemblIdCriteria));
			if (fetchedGenes.size() > 0) {
				// Only use the first gene.
				genes.add(fetchedGenes.iterator().next());
			} else {
				genes.add(null);
			}
		}
		
		return genes;
	}

	@Override
	public Collection<GeneValueObject> fetchGenesByLocation( String chromosomeName, Long start, Long end ) {
		Criteria chromosomeCriteria = chromosomeAttribute.eq(chromosomeName);
        Criteria insideVariant = startAttribute.between(start.intValue(), end.intValue())
                .or(endAttribute.between(start.intValue(), end.intValue()));
        Criteria overlapsStart = startAttribute.le(start.intValue()).and(endAttribute.ge(start.intValue()));
        Criteria overlapsEnd = startAttribute.le(end.intValue()).and(endAttribute.ge(end.intValue()));

        Criteria hasName = geneSymbolAttribute.ne("");

        final Collection<GeneValueObject> geneValueObjects = fetchByCriteria(hasName.and(
                                                                chromosomeCriteria
                                                                    .and( insideVariant
                                                                        .or( overlapsStart )
                                                                        .or( overlapsEnd ))));

        return geneValueObjects;
    }

	@Override
	public Collection<GeneValueObject> fetchGenesByGeneSymbols(Collection<String> geneSymbols) {
    	Criteria symbolCriteria = geneSymbolAttribute.in(geneSymbols);

    	return fetchByCriteria(symbolCriteria);
	}
}
