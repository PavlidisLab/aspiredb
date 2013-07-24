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

package ubc.pavlab.aspiredb.server.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

@RunWith(SpringJUnit4ClassRunner.class)
public class CNVDaoTest extends BaseSpringContextTest {

    @Autowired
    private CNVDao cnvDao;
    @Autowired
    private SubjectDao individualDao;
    @Autowired
    PersistentTestObjectHelper testObjectHelper;

    @Autowired
    UserManager userManager;

    @Autowired
    private ProjectDao projectDao;

    private Subject individual;
    private CNV cnv1;

    String authorizedUsername = RandomStringUtils.randomAlphabetic( 6 );

    String aDifferentUsername = RandomStringUtils.randomAlphabetic( 5 );

    String projectName = RandomStringUtils.randomAlphabetic( 4 );
    String groupName = RandomStringUtils.randomAlphabetic( 4 );

    Long projectId;

    @Before
    public void setup() throws Exception {

        Project detachedProject = new Project();

        detachedProject.setName( projectName );

        Project p1 = projectDao.create( detachedProject );

        projectId = p1.getId();

        try {
            userManager.loadUserByUsername( authorizedUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", authorizedUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        List<GrantedAuthority> authos = new ArrayList<GrantedAuthority>();
        authos.add( new GrantedAuthorityImpl( groupName ) );

        userManager.createGroup( groupName, authos );

        userManager.addUserToGroup( authorizedUsername, groupName );

        try {
            userManager.loadUserByUsername( aDifferentUsername );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "foo", aDifferentUsername, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

    }

    @Test
    public void testCreate() {
        TransactionTemplate tt = new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {

                // Just a stub to test the plumbing.
                CNV cnv = new CNV();
                cnvDao.create( cnv );

            }
        } );
    }

    @Test
    public void testUpdateLoad() {

        // this method creates a LOSS cnv
        CNV cnv = testObjectHelper.createPersistentTestCNVObject();
        Long id = cnv.getId();
        assertEquals( cnv.getType(), CnvType.LOSS );

        cnv.setType( CnvType.GAIN );

        cnvDao.update( cnv );

        CNV updatedCnv = cnvDao.load( id );

        assertEquals( updatedCnv.getType(), CnvType.GAIN );

    }

//    @Test
//    public void testMakeQueryAndFind() {
//        super.runAsAdmin();
//
//        // these are added
//        ComparisonExpression ce1 = new ComparisonExpression( "Chromosome", OperatorType.EQ, "40" );
//        ComparisonExpression ce2 = new ComparisonExpression( "Start", OperatorType.GTE, "50000000" );
//        ComparisonExpression ce3 = new ComparisonExpression( "End", OperatorType.LTE, "55000000" );
//
//        List<ComparisonExpression> ceList1 = new ArrayList<ComparisonExpression>();
//        ceList1.add( ce1 );
//        ceList1.add( ce2 );
//        ceList1.add( ce3 );
//
//        // this should not exist
//        ComparisonExpression ce4 = new ComparisonExpression( "Chromosome", OperatorType.EQ, "50" );
//        ComparisonExpression ce5 = new ComparisonExpression( "Start", OperatorType.GTE, "50000000" );
//        ComparisonExpression ce6 = new ComparisonExpression( "End", OperatorType.LTE, "55000000" );
//
//        List<ComparisonExpression> ceList2 = new ArrayList<ComparisonExpression>();
//        ceList2.add( ce4 );
//        ceList2.add( ce5 );
//        ceList2.add( ce6 );
//
//        Collection<CNV> cnvs1 = cnvDao.makeQueryAndFind( ceList1 );
//        Collection<CNV> cnvs2 = cnvDao.makeQueryAndFind( ceList2 );
//
//        for ( CNV cnv : cnvs1 ) {
//            assertEquals( cnv1.getLocation().getChromosome(), cnv.getLocation().getChromosome() );
//            assertTrue( cnv1.getLocation().getStart() >= 50000000 );
//            assertTrue( cnv1.getLocation().getEnd() <= 55000000 );
//        }
//
//        assertTrue( cnvs2.isEmpty() );
//
//    }

    @Before
    public void createIndividualAndCNVs() {

        TransactionTemplate tt = new TransactionTemplate( transactionManager );
        tt.execute( new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult( TransactionStatus status ) {
                individual = new Subject();

                String patientId = "test_patient";
                individual.setPatientId( patientId );

                GenomicLocation genomicLocation1 = new GenomicLocation();
                genomicLocation1.setChromosome( "40" );
                genomicLocation1.setStart( 50500000 );
                genomicLocation1.setEnd( 54500000 );                

                cnv1 = new CNV();
                cnv1.setLocation( genomicLocation1 );
                cnv1.setCopyNumber( 1 );
                cnv1.setType( CnvType.valueOf( "LOSS" ) );                
                
                cnv1.setSubject( individual );

                individual.addVariant( cnv1 );

                Phenotype ph = new Phenotype();
                ph.setName( "Test Phenotype" );
                ph.setValue( "1234" );
                individual.addPhenotype( ph );

                individualDao.create( individual );
            }
        } );
    }

    

}
