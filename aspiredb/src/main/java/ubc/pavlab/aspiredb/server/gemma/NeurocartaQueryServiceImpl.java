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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.util.GemmaURLUtils;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Simple wrapper that calls Neurocarta REST web service.
 * 
 * @author frances
 * @version $Id: NeurocartaQueryServiceImpl.java,v 1.8 2013/06/11 22:30:46 anton Exp $
 */
@Service
public class NeurocartaQueryServiceImpl implements NeurocartaQueryService {
	private static final String LOAD_PHENOTYPES_URL_SUFFIX = "/phenotype/load-all-phenotypes";
	private static final String FIND_GENES_URL_SUFFIX = "/phenotype/find-candidate-genes";

    private static Log log = LogFactory.getLog( NeurocartaQueryServiceImpl.class.getName() );

	@Autowired 
	private BioMartQueryService bioMartQueryService;	

	@Autowired
	private NeurocartaCache neurocartaCache;
	
    private static String sendRequest( String urlSuffix, MultivaluedMap<String, String> queryParams ) throws NeurocartaServiceException {
        Client client = Client.create();

        WebResource resource = client.resource( GemmaURLUtils.makeWebServiceUrl(urlSuffix) ).queryParams( queryParams );

        ClientResponse response = resource.type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                .get( ClientResponse.class );

        // Check return code
        if ( Response.Status.fromStatusCode( response.getStatus() ).getFamily() != Response.Status.Family.SUCCESSFUL ) {
			String errorMessage = "Error occurred when accessing Neurocarta web service: " + response.getEntity( String.class );
			log.error(errorMessage);

			throw new NeurocartaServiceException(errorMessage);
        }

        return response.getEntity( String.class );
    }

	@SuppressWarnings("unused")
	@PostConstruct
	private void initialize() throws NeurocartaServiceException {
		updateCacheIfExpired();
	}
	
	private void updateCacheIfExpired() throws NeurocartaServiceException  {
		if (this.neurocartaCache.hasExpired()) {		
	    	
	        String result = sendRequest(LOAD_PHENOTYPES_URL_SUFFIX, new MultivaluedMapImpl());
	        
	        Collection<NeurocartaPhenotypeValueObject> neurocartaPhenotypes = new HashSet<NeurocartaPhenotypeValueObject>();

			try {
				JSONArray jsonArray = new JSONArray(new JSONTokener(result));
				
		    	for (int i = 0 ; i < jsonArray.length() ; i++) {
		    		JSONObject json = jsonArray.getJSONObject(i);

		    		NeurocartaPhenotypeValueObject neurocartaPhenotype = new NeurocartaPhenotypeValueObject();
		    		neurocartaPhenotype.setName(json.getString("value"));
		    		neurocartaPhenotype.setUri(json.getString("valueUri"));
		    		neurocartaPhenotype.setGeneCount(json.getInt("publicGeneCount"));
		    				
		    		neurocartaPhenotypes.add(neurocartaPhenotype);  		
		    	}
			} catch (JSONException e) {
				String errorMessage = "Cannot initialize phenotypes from Neurocarta";
				log.error(errorMessage, e);
	
				throw new NeurocartaServiceException(errorMessage);
			}

			this.neurocartaCache.putAll(neurocartaPhenotypes);
		}
	}

	@Override
	public boolean isNeurocartaPhenotype(String phenotypeUri) throws NeurocartaServiceException {
		updateCacheIfExpired();
		
		return this.neurocartaCache.hasPhenotype(phenotypeUri);
	}

	@Override
	public Collection<NeurocartaPhenotypeValueObject> findPhenotypes(String queryString) throws NeurocartaServiceException {
		updateCacheIfExpired();
	
		return this.neurocartaCache.findPhenotypes(queryString);
	}

	@Override
	public List<NeurocartaPhenotypeValueObject> getPhenotypes(List<String> names) throws NeurocartaServiceException {
		updateCacheIfExpired();
		
		return this.neurocartaCache.getPhenotypes(names);
	}
    
	@Override
	public Collection<GeneValueObject> fetchGenesAssociatedWithPhenotype(String phenotypeUri) throws NeurocartaServiceException, BioMartServiceException {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add( "taxonId", "1" ); // Human
        queryParams.add( "showOnlyEditable", "false" );
        queryParams.add( "phenotypeValueUris", phenotypeUri );

        String result = sendRequest(FIND_GENES_URL_SUFFIX, queryParams);
        
        Collection<String> geneSymbols;

        try {
			JSONArray jsonArray = new JSONArray(new JSONTokener(result));
	    	
			geneSymbols = new HashSet<String>(jsonArray.length());

	    	for (int i = 0 ; i < jsonArray.length() ; i++) {
	    		JSONObject json = jsonArray.getJSONObject(i);
	    		geneSymbols.add(json.getString("officialSymbol"));
	    	}
		} catch (JSONException e) {
			String errorMessage = "Cannot get genes from Neurocarta";
			log.error(errorMessage, e);

			throw new NeurocartaServiceException(errorMessage);
		}

        return this.bioMartQueryService.fetchGenesByGeneSymbols(geneSymbols);
	}
}
