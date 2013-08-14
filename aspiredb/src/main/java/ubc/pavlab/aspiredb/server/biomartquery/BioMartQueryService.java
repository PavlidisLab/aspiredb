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

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;

import java.util.Collection;
import java.util.List;

/**
 * Biomart query service
 * 
 * @author frances
 * @version $Id: BioMartQueryService.java,v 1.15 2013/06/11 22:30:47 anton Exp $
 */
public interface BioMartQueryService {

	public Collection<GeneValueObject> findGenes(String queryString) throws BioMartServiceException;

	/**
	 * Get a list of genes using the given gene symbols or ensembl ids. The order of the returned list of genes is preserved. 
	 * If a gene symbol or ensembl id is not valid, the returned gene will be null.
	 * 
	 * @param geneStrings gene symbols or ensembl ids
	 * @return a list of GeneValueObjects
	 * @throws BioMartServiceException 
	 */
	public List<GeneValueObject> getGenes(List<String> geneStrings) throws BioMartServiceException;
	
	/**
	 * Find genes that are inside the specified region of the genome.
	 * 
	 * @param chromosomeName - 1,2,3,X, etc
	 * @param start -
	 * @param end -
	 * @return collection of genes
	 * @throws BioMartServiceException
	 */
	public Collection<GeneValueObject> fetchGenesByLocation(String chromosomeName, Long start, Long end) throws BioMartServiceException;

	/**
	 * Find genomic ranges by gene symbols.
	 * 
	 * @param geneSymbols
	 * @return collection of genomic ranges
	 * @throws BioMartServiceException 
	 */
	public Collection<GenomicRange> fetchGenomicRangesByGeneSymbols(Collection<String> geneSymbols) throws BioMartServiceException;

	/**
	 * Find genes by gene symbols.
	 * 
	 * @param geneSymbols
	 * @return collection of genes
	 * @throws BioMartServiceException
	 */
	public Collection<GeneValueObject> fetchGenesByGeneSymbols(Collection<String> geneSymbols) throws BioMartServiceException;
}