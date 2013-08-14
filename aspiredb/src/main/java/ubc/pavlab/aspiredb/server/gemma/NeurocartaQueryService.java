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

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;

import java.util.Collection;
import java.util.List;

/**
 * Neurocarta query service
 * 
 * @author frances
 * @version $Id: NeurocartaQueryService.java,v 1.6 2013/06/11 22:30:46 anton Exp $
 */
public interface NeurocartaQueryService {

	public boolean isNeurocartaPhenotype( String phenotypeUri ) throws NeurocartaServiceException;
	
	public Collection<NeurocartaPhenotypeValueObject> findPhenotypes( String queryString )
            throws NeurocartaServiceException;

	public List<NeurocartaPhenotypeValueObject> getPhenotypes( List<String> names )
            throws NeurocartaServiceException;

	public Collection<GeneValueObject> fetchGenesAssociatedWithPhenotype( String phenotypeUri )
            throws NeurocartaServiceException, BioMartServiceException;

}