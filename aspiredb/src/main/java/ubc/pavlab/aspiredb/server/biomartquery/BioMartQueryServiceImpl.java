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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ubc.pavlab.aspiredb.client.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Simple wrapper that calls BioMart REST query service. 
 * 
 * @author anton
 * @version $Id: BioMartQueryServiceImpl.java,v 1.13 2013/07/15 16:01:54 anton Exp $
 */
@Service
public class BioMartQueryServiceImpl implements BioMartQueryService {
    private static final String BIO_MART_URL = "http://www.biomart.org/biomart/martservice/results";
    
    private static Log log = LogFactory.getLog( BioMartQueryServiceImpl.class.getName() );
    
	@Autowired
	private BioMartCache bioMartCache;
	
    private static String sendRequest( String xmlQueryString ) throws BioMartServiceException {
        Client client = Client.create();

        MultivaluedMap<String, String> queryData = new MultivaluedMapImpl();
        queryData.add( "query", xmlQueryString );

        WebResource resource = client.resource( BIO_MART_URL ).queryParams( queryData );

        ClientResponse response = resource.type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                .get( ClientResponse.class );

        // Check return code
        if ( Response.Status.fromStatusCode( response.getStatus() ).getFamily() != Response.Status.Family.SUCCESSFUL ) {
			String errorMessage = "Error occurred when accessing BioMart web service: " + response.getEntity( String.class );
			log.error(errorMessage);

			throw new BioMartServiceException(errorMessage);
        }

        return response.getEntity( String.class );
    }

	@SuppressWarnings("unused")
	@PostConstruct
	private void initialize() throws BioMartServiceException {
		//updateCacheIfExpired();
	}
	
	private void updateCacheIfExpired() throws BioMartServiceException  {
		if (this.bioMartCache.hasExpired()) {
			Dataset dataset = new Dataset( "hsapiens_gene_ensembl" );
			
			dataset.Filter.add( new Filter( "chromosome_name", "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X,Y" ) );

			dataset.Attribute.add( new Attribute( "ensembl_gene_id" ) );
			dataset.Attribute.add( new Attribute( "hgnc_symbol" ) );
			dataset.Attribute.add( new Attribute( "description") );
			dataset.Attribute.add( new Attribute( "gene_biotype") );
			dataset.Attribute.add( new Attribute( "chromosome_name") );
			dataset.Attribute.add( new Attribute( "start") );
			dataset.Attribute.add( new Attribute( "end") );
			
			Query query = new Query();
			query.Dataset = dataset;
			
			StringWriter xmlQueryWriter = null;
			
			try {
			    JAXBContext jaxbContext = JAXBContext.newInstance( Query.class, Dataset.class, Filter.class, Attribute.class );
			    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			
			    xmlQueryWriter = new StringWriter();
			    jaxbMarshaller.marshal( query, xmlQueryWriter );
			} catch (JAXBException e) {
				String errorMessage = "Cannot initialize genes from BioMart"; 
				log.error(errorMessage, e);
	
				throw new BioMartServiceException(errorMessage);
			}
			
			String response = sendRequest( xmlQueryWriter.toString() );
			
			String[] rows = StringUtils.split( response, "\n" );

			Collection<GeneValueObject> genes = new HashSet<GeneValueObject>();

			int rowsLength = rows.length;
			if (rowsLength <= 1) {
				String errorMessage = "Error: retrieved only " + rowsLength + " row of gene data from BioMart" 
						+ (rowsLength == 1
							? "(Error message from BioMart: " + rows[0] + ")" 
							: ""); 
				log.error(errorMessage);
				
				throw new BioMartServiceException(errorMessage);
			}
			
			for ( String row : rows ) {
			    String[] fields = row.split( "\t" );
			
				int index = 0;
				String ensemblId = fields[index++];
				String symbol = fields[index++];
				String name = fields[index++];
				String geneBiotype = fields[index++];
				String chromosome = fields[index++];
				String start = fields[index++];
				String end = fields[index++];
				
				// Ignore results that do not have required attributes. 
				if (ensemblId.equals("") || chromosome.equals("") || start.equals("") || end.equals("")) {
					continue;
				}
				
				int sourceIndex = name.indexOf(" [Source:");
				name = sourceIndex >= 0 ? name.substring(0, sourceIndex) : name;            
			    
				GeneValueObject gene = new GeneValueObject( ensemblId, symbol, name, geneBiotype, "human" );
				gene.setGenomicRange(new GenomicRange(chromosome, Integer.valueOf(start), Integer.valueOf(end)));
	  
				genes.add(gene);
			}

			this.bioMartCache.putAll(genes);			
		}
	}
	
	@Override
	public Collection<GeneValueObject> findGenes(String queryString) throws BioMartServiceException {
		updateCacheIfExpired();

		return bioMartCache.findGenes(queryString);
	}

	@Override
	public List<GeneValueObject> getGenes(List<String> geneStrings) throws BioMartServiceException {
		updateCacheIfExpired();

		return bioMartCache.getGenes(geneStrings);
	}
    
	@Override
	public Collection<GeneValueObject> fetchGenesByLocation( String chromosomeName, Long start, Long end ) throws BioMartServiceException {
		updateCacheIfExpired();

		return bioMartCache.fetchGenesByLocation(chromosomeName, start, end);
	}

    @Override
	public Collection<GenomicRange> fetchGenomicRangesByGeneSymbols( Collection<String> geneSymbols ) throws BioMartServiceException {
    	Collection<GeneValueObject> genes = fetchGenesByGeneSymbols(geneSymbols);
    	Collection<GenomicRange> genomicRanges = new HashSet<GenomicRange>(genes.size());

    	for (GeneValueObject gene: genes) {
    		genomicRanges.add(gene.getGenomicRange());		
    	}

    	return genomicRanges;
    }

	@Override
	public Collection<GeneValueObject> fetchGenesByGeneSymbols(Collection<String> geneSymbols) throws BioMartServiceException {
		updateCacheIfExpired();

    	return bioMartCache.fetchGenesByGeneSymbols(geneSymbols);
	}
}
