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

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

/**
 * author: gaya date: 12/03/14
 */
public class UserGeneServiceTest extends BaseSpringContextTest {

    

    @Autowired
    private UserGeneSetService userGeneSetService;
    
    @Autowired
    private PhenotypeDao phenotypeDao;
    
    @Autowired
    private PersistentTestObjectHelper persistentTestObjectHelper;

    @Autowired
    UserManager userManager;
    
    private Project project;
    private Collection<Long> activeProjectIds;

    private static Log log = LogFactory.getLog( UserGeneServiceTest.class.getName() );
    String username = "jimmy";
    String testname = RandomStringUtils.randomAlphabetic( 6 );

    @Before
    public void setUp() {
        try {
            userManager.loadUserByUsername( username );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", username, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        // run save query as administrator in filter window
        super.runAsAdmin();
    }

    @Test
    public void testIsQueryName() throws Exception {
    	/**
    	 Collection<Long> projectIds = new HashSet<Long>();
         projectIds.add( project.getId() );
         ProjectFilterConfig projConfig = new ProjectFilterConfig();
         projConfig.setProjectIds( projectIds );
    	
    	List<GeneValueObject> genes =new ArrayList<GeneValueObject>();
    	GeneValueObject gvo = new GeneValueObject();
    	gvo.setEnsemblId("1");
    	gvo.setSymbol("HAIRCH");
    	gvo.setName("Hairy chest gene.");
    	gvo.setGeneBioType("");
    	gvo.setTaxon("human");
    	gvo.setKey("HAIRCH:human");
      
    	genes.add(gvo);
        userGeneSetService.saveUserGeneSet( testname, genes);

        // run as user to check wheather the admin created query is accessble by the user
        super.runAsUser( this.username );
        boolean returnvalue = userGeneSetService.isGeneSetName( testname );
        assertFalse( returnvalue );*/

    }

   

    @Test
    public void testSaveUserGeneSet() throws Exception {/**
    	 Collection<Long> projectIds = new HashSet<Long>();
         projectIds.add( project.getId() );
         ProjectFilterConfig projConfig = new ProjectFilterConfig();
         projConfig.setProjectIds( projectIds );

    	List<GeneValueObject> genes =new ArrayList<GeneValueObject>();
    	GeneValueObject gvo = new GeneValueObject();
    	gvo.setEnsemblId("1");
    	gvo.setSymbol("HAIRCH");
    	gvo.setName("Hairy chest gene.");
    	gvo.setGeneBioType("");
    	gvo.setTaxon("human");
    	gvo.setKey("HAIRCH:human");
      
    	genes.add(gvo);
    	
    	 // run as user to check wheather the admin created query is accessble by the user
        super.runAsUser( this.username );
        Long geneSetId = userGeneSetService.saveUserGeneSet( testname, genes);
        assertFalse( geneSetId == null );*/
    }

   
}
