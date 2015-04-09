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

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.MutableAclService;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

/**
 * Tests of ACL management: adding and removing from objects during CRUD operations. (AclAdvice) *
 * 
 * @author paul
 * @version $Id: AclAdviceTest.java,v 1.8 2013/06/12 20:18:48 cmcdonald Exp $
 */
public class AclAdviceTest extends BaseSpringContextTest {

    @Autowired
    MutableAclService aclService;

    @Autowired
    private AclTestUtils aclTestUtils;

    @Autowired
    private PersistentTestObjectHelper testObjectHelper;

    @Autowired
    SubjectDao indDao;

    @Autowired
    VariantDao variantDao;

    @Autowired
    PhenotypeDao phenotypeDao;

    /**
     * @throws Exception
     */
    @Test
    public void testCNVAcls() throws Exception {

        CNV cnv = testObjectHelper.createPersistentTestCNVObject();

        aclTestUtils.checkHasAcl( cnv );

        testObjectHelper.removeVariant( cnv );

        aclTestUtils.checkDeletedAcl( cnv );

    }

    @Test
    public void testIndividualAcls() throws Exception {
        String patientId = RandomStringUtils.randomAlphabetic( 4 );

        Subject ind = testObjectHelper.createPersistentTestIndividualObject( patientId );

        aclTestUtils.checkHasAcl( ind );

        testObjectHelper.removeSubject( ind );

        aclTestUtils.checkDeletedAcl( ind );

    }

    // Bug 4230. Removed Phenotype ACLs because they're too slow.
    // @Test
    // public void testPhenotypeAcls() throws Exception {
    // Collection<Phenotype> phenos = new HashSet<Phenotype>();
    // Phenotype pheno = testObjectHelper.createPersistentTestPhenotypeObject(
    // RandomStringUtils.randomAlphabetic( 4 ), RandomStringUtils.randomAlphabetic( 4 ),
    // RandomStringUtils.randomAlphabetic( 1 ), RandomStringUtils.randomAlphabetic( 4 ) );
    // phenos.add( pheno );
    //
    // pheno = testObjectHelper.createPersistentTestPhenotypeObject( RandomStringUtils.randomAlphabetic( 4 ),
    // RandomStringUtils.randomAlphabetic( 4 ), RandomStringUtils.randomAlphabetic( 1 ),
    // RandomStringUtils.randomAlphabetic( 4 ) );
    // phenos.add( pheno );
    //
    // // TODO figure out why removing a collection throws an error
    // // phenotypeDao.remove( phenos );
    //
    // for ( Phenotype apheno : phenos ) {
    // aclTestUtils.checkHasAcl( apheno );
    // testObjectHelper.removePhenotype( apheno );
    // aclTestUtils.checkDeletedAcl( apheno );
    // }
    //
    // }
}
