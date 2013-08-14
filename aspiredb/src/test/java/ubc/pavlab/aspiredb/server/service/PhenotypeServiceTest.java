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


import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import ubc.pavlab.aspiredb.server.BaseSpringContextTest;


import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.shared.PhenotypeEnrichmentValueObject;
import ubic.basecode.math.SpecFunc;


public class PhenotypeServiceTest extends BaseSpringContextTest {

    @Autowired
    private PersistentTestObjectHelper testObjectHelper;
    
    
    @Autowired
    private PhenotypeService phenotypeService;
    
    private Subject subject1;
    private Subject subject2;
    private Subject subject3;
    private Subject subject4;
    private Subject subject5;
    private Subject subject6;
    private Subject subject7;
    private Subject subject8;
    private Subject subject9;
    private Subject subjectNoPhenotypes;

    Project p1;

    @Before
    public void setup() throws Exception {
        
        new InlineTransaction() {
            @Override
            public void instructions() {
               
                subject1 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","1" );
                subject2 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","0" );
                subject3 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","1" );
                subject4 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","0" );
                subject5 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","0" );
                subject6 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","1" );
                subject7 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","1" );
                subject8 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","1" );
                subject9 = testObjectHelper.createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( RandomStringUtils.randomAlphabetic(5),"Abnormality of abnormalities", "uri1","1" );
                                
                subjectNoPhenotypes = testObjectHelper.createPersistentTestIndividualObject(RandomStringUtils.randomAlphabetic(5));
                
                p1 = new Project();
                p1.setName(RandomStringUtils.randomAlphabetic(5));
                p1.getSubjects().add(subject1);
                p1.getSubjects().add(subject2);
                p1.getSubjects().add(subject3);
                p1.getSubjects().add(subject4);
                p1.getSubjects().add(subject5);
                p1.getSubjects().add(subject6);
                p1.getSubjects().add(subject7);
                p1.getSubjects().add(subject8);
                p1.getSubjects().add(subject9);
                p1.getSubjects().add( subjectNoPhenotypes );

                p1 = testObjectHelper.createPersistentProject(p1);
                
            }
        }.execute();
        
    }

    @Test
    public void testGetPhenotypeEnrichment() throws Exception {
        
        Collection<Long> projectIdList = new ArrayList<Long>();
        projectIdList.add( p1.getId() );
        
        Collection<Long> subjectIdList = new ArrayList<Long>();
        
        subjectIdList.add(subject1.getId());
        subjectIdList.add(subject2.getId());
        subjectIdList.add(subject3.getId());
        
        List<PhenotypeEnrichmentValueObject> pevoList= phenotypeService.getPhenotypeEnrichmentValueObjects( projectIdList, subjectIdList );
        
        PhenotypeEnrichmentValueObject vo = pevoList.iterator().next();
        
        /* @param x - number of reds retrieved == successes
                * @param NR - number of reds in the urn. == positives
                * @param NB - number of blacks in the urn == negatives
                * @param n - the total number of objects drawn == successes + failures
                * @param lowerTail
                * @return cumulative hypergeometric distribution.
                * public static double phyper( int x, int NR, int NB, int n, boolean lowerTail )
                */               
        //values based on subjects1-10 defined in setup()
        //note lower.tail: logical; if TRUE (default), probabilities are P[X <= x],
        //otherwise, P[X > x].
        //Since we want P[X >= x], we want to set x = x - 1
        final double DELTA = 1e-15;
        assertEquals(vo.getPValue().doubleValue(), SpecFunc.phyper( 1, 6, 4, 3, false ),DELTA);
        
        assertEquals(vo.getUri(),"uri1");
        
        assertEquals(vo.getName(),"Abnormality of abnormalities");
        
        assertEquals(vo.getOutGroupTotal().intValue(),4);
        assertEquals(vo.getInGroupTotal().intValue(),2);
       
    }
    
    


}
