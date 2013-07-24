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

package ubc.pavlab.aspiredb.server.security.authorization.acl;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.CNVDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.security.SecurityService;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;



/**
 * 
 * @version $Id: AclAuthorizationTest.java,v 1.9 2013/06/11 23:01:33 cmcdonald Exp $
 */
public class AclAuthorizationTest extends BaseSpringContextTest {
    
    @Autowired
    private UserManager userManager;

    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private SubjectDao subjectDao;
    
    @Autowired
    private CNVDao cnvDao;   
    
    
    @Autowired
    private PersistentTestObjectHelper testObjectHelper;
    

    String ownerUsername = RandomStringUtils.randomAlphabetic( 6 );
    
    String aDifferentUsername = RandomStringUtils.randomAlphabetic( 5 );
    
    String patientId = RandomStringUtils.randomAlphabetic( 4 );
    
    
    
    
    @Before
    public void setup() throws Exception {   
        
        try {
            userManager.loadUserByUsername( ownerUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", ownerUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 )
                    + "@gmail.com", "key", new Date() ) );
        }
        
        super.runAsUser( this.ownerUsername );
        
        testObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId );
        
        try {
            userManager.loadUserByUsername( aDifferentUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "foo", aDifferentUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 )
                    + "@gmail.com", "key", new Date() ) );
        }

    }

    /**
     * Tests getting composite sequences (target objects) with correct privileges on domain object.
     * 
     * @throws Exception
     */
    @Test
    public void testGetIndividual() throws Exception {
        
        
        
        super.runAsUser( this.aDifferentUsername );
        try {
            Subject ind2 = subjectDao.findByPatientId( patientId );
            ind2.getPatientId();
            fail( "Should have gotten an access denied" );
        } catch ( AccessDeniedException ok ) {

        }

        
        super.runAsUser( this.ownerUsername );
        
        Subject ind = subjectDao.findByPatientId( patientId );
        
        assertTrue("User should own the individual" , securityService.isOwnedByCurrentUser( ind ));
        
        super.runAsUser( this.aDifferentUsername );
        
        assertFalse("User shouldn't own the individual" , securityService.isOwnedByCurrentUser( ind ));
        
        
    }

    
    @Test
    public void testUserOwnsIndividualAndIndividualCNVs() throws Exception {
        
        
        super.runAsUser( this.ownerUsername );
                
        CNV cnv = cnvDao.findBySubjectId( patientId ).iterator().next();
        
        assertTrue("User should own the individual's cnvs" , securityService.isOwnedByCurrentUser(cnv ));
        
        super.runAsUser( this.aDifferentUsername );
        
        assertFalse("User shouldn't own the individual's cnvs" , securityService.isOwnedByCurrentUser(cnv ));
        
        
    }
    
    @Test
    public void testIndividualSecured() throws Exception {
        
        super.runAsUser( this.aDifferentUsername );
        

        try {
            subjectDao.findByPatientId( patientId );
            fail( "Should have gotten an access denied exception, acl was: " );
        } catch ( AccessDeniedException e ) {

        }
        
    }
    
    @Test
    public void testEditCNV(){
        
        super.runAsUser( this.ownerUsername );
        
        Collection<CNV> ownedCNVs = cnvDao.findBySubjectId( patientId );
        
        CNV cnv = ownedCNVs.iterator().next();
        
        super.runAsUser( this.aDifferentUsername );
         
        cnv.setCopyNumber( 234 );
        
        try{
            
            
            cnvDao.update( cnv );
            fail( "Should have gotten an access denied exception, acl was: " );
        }
        catch ( AccessDeniedException e ) {
            

        }
    }
    
   
    
}
