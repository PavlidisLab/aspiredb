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

import java.util.Collection;
import java.util.List;

import ubc.pavlab.aspiredb.shared.GeneValueObject;

/**
 * BioMart cache
 * 
 * @author frances
 * @version $Id: BioMartCache.java,v 1.3 2013/06/11 22:30:47 anton Exp $
 */
public interface BioMartCache {
    public Collection<GeneValueObject> fetchGenesByGeneSymbols( Collection<String> geneSymbols );

    public Collection<GeneValueObject> fetchGenesByLocation( String chromosomeName, Long start, Long end );

    public Collection<GeneValueObject> findGenes( String queryString );

    /**
     * Get a list of genes using the given gene symbols or ensembl ids. The order of the returned list of genes is
     * preserved. If a gene symbol or ensembl id is not valid, the returned gene will be null.
     * 
     * @param geneStrings gene symbols or ensembl ids
     * @return a list of GeneValueObjects
     */
    public List<GeneValueObject> getGenes( List<String> geneStrings );

    public boolean hasExpired();

    public void putAll( Collection<GeneValueObject> genes );

    public Collection<GeneValueObject> fetchGenesByBin( int bin );
}