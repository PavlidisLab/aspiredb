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
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
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
    AclTestUtils aclTestUtils;

    @Autowired
    PersistentTestObjectHelper testObjectHelper;

    @Autowired
    SubjectDao indDao;
    
    @Autowired
    VariantDao variantDao;

    /**
     * @throws Exception
     */
    @Test
    public void testCNVAcls() throws Exception {

        CNV cnv = testObjectHelper.createPersistentTestCNVObject();

        aclTestUtils.checkHasAcl( cnv );

        variantDao.remove( (Variant)cnv );

        aclTestUtils.checkDeletedAcl( cnv );

    }

    @Test
    public void testIndividualAcls() throws Exception {
        String patientId = RandomStringUtils.randomAlphabetic( 4 );

        Subject ind = testObjectHelper.createPersistentTestIndividualObject( patientId );

        aclTestUtils.checkHasAcl( ind );

        indDao.remove( ind );

        aclTestUtils.checkDeletedAcl( ind );
        
        
    }

}
