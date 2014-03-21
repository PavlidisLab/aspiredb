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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.GenomeCoordinateConverter;
import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.dao.Page;
import ubc.pavlab.aspiredb.server.dao.UserGeneSetDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.Query;
import ubc.pavlab.aspiredb.server.model.UserGeneSet;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.server.util.ConfigUtils;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.OntologyTermValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.GeneProperty;
import ubc.pavlab.aspiredb.shared.query.GenomicLocationProperty;
import ubc.pavlab.aspiredb.shared.query.NeurocartaPhenotypeProperty;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.Junction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.SetRestriction;
import ubc.pavlab.aspiredb.shared.suggestions.PhenotypeSuggestion;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.providers.HumanPhenotypeOntologyService;

import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

/**
 * @author anton
 */
@Service("userGeneSetService")
@RemoteProxy(name = "UserGeneSetService")
public class UserGeneSetServiceImpl implements UserGeneSetService {

    private static Logger log = LoggerFactory.getLogger( UserGeneSetServiceImpl.class );

    @Autowired 
    private UserGeneSetDao userGeneSetDao;

	 @Autowired
    private BioMartQueryService bioMartQueryService;
	 
	/**
	 * saving the user selected gene listed in the phenotype and variant window
	 * @param geneSetName
	 * @param Gene value objects holding only the symbol, name and type of a gene
	 * @return saved gene set id 
	 */
    @Override
    @Transactional
    @RemoteMethod
    public Long saveUserGeneSet(String geneSetName,List<GeneValueObject> genes)throws BioMartServiceException {
        final List<UserGeneSet> geneSet = userGeneSetDao.findByName(geneSetName);        
        
        
        List<String> geneSymbols=new ArrayList<String>();
      //storing the gene symbols
        for (GeneValueObject gvo: genes){
        	geneSymbols.add(gvo.getSymbol());        	
        }
        //getting the actual gene value objects. Gene value object will return null unless the gene value object id is specified. so we need to do this workaround to obtain the complete gene value object 
        List<GeneValueObject> geneValueObjects= bioMartQueryService.getGenes(geneSymbols);   
        
        UserGeneSet savedUserGeneSet=null;
        if ( geneSet.isEmpty() ) {
        	UserGeneSet userGeneSet = new UserGeneSet(geneSetName, ( Serializable ) geneValueObjects);
        	savedUserGeneSet = userGeneSetDao.create( userGeneSet );
        } else if ( geneSet.size() == 1 ) {
        	UserGeneSet userGeneSet = geneSet.iterator().next();
        	userGeneSet.setObject( ( Serializable ) geneValueObjects );
            userGeneSetDao.update( userGeneSet );
            savedUserGeneSet = userGeneSet;
        } else {
            throw new IllegalStateException( "Found more than one saved gene sets with same name belonging to one user." );
        }
        return savedUserGeneSet.getId();
    }
    
    @Override
    @Transactional
    @RemoteMethod
    public List<GeneValueObject> getGenes(String geneSymbol)throws BioMartServiceException {
    	List<String> geneSymbols=new ArrayList<String>();
    	geneSymbols.add(geneSymbol);
    	return bioMartQueryService.getGenes(geneSymbols); 
    }
    
    
    /**
     * Check weather the geneset name exist
     * @param Gene Set Name
     * @return true or false 
     */
    @Override
    @RemoteMethod
    public boolean isGeneSetName(String name) {

    	List<UserGeneSet> geneSet = userGeneSetDao.findByName( name );
        
        if (geneSet.size() >0){
                return true;
            }
        
        
        return false;              
             
    }
    @SuppressWarnings("unchecked")
    @Override
    @RemoteMethod
    public List<GeneValueObject> loadUserGeneSet( String name ) {
    	 List<UserGeneSet> genesets = userGeneSetDao.findByName( name );
    	 
    	 List<GeneValueObject> geneValueObjects=new ArrayList<GeneValueObject>();
    	 
    	 if (genesets.size() > 0) {  
    		 geneValueObjects = (List<GeneValueObject>)genesets.iterator().next().getObject();
    	 }
    	 else geneValueObjects=null;
    	 
    	 	  
         // should only be one for one user
         return geneValueObjects;
       
    }
    
    @Override
    @RemoteMethod
    public Collection<String> getSavedUserGeneSetNames(){
    	Collection<String> geneSetNames = new ArrayList<String>();
        Collection<UserGeneSet> genesets = userGeneSetDao.loadAll();

        for ( UserGeneSet geneset : genesets ) {
        	geneSetNames.add( geneset.getName() );
        }
        return geneSetNames;
    	
    }
    
    @Override
    @RemoteMethod
    public void deleteUserGeneSet( String name ){
    	List<UserGeneSet> genesets = userGeneSetDao.findByName( name );
    	userGeneSetDao.remove( genesets.iterator().next() );
    }
    
}
