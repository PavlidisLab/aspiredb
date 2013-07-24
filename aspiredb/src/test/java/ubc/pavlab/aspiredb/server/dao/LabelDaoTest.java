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
package ubc.pavlab.aspiredb.server.dao;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class LabelDaoTest extends BaseSpringContextTest {
    @Autowired
    private LabelDao labelDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private VariantDao variantDao;

    @Autowired
    PersistentTestObjectHelper testObjectHelper;

    @Autowired
    HibernateTransactionManager txManager;

    String testSubjectId;
    Subject testSubject;
    Variant testVariant;

    @Before
    public void init() {
        testSubjectId = RandomStringUtils.randomAlphanumeric( 5 );
        testSubject = testObjectHelper.createPersistentTestSubjectObjectWithCNV(testSubjectId);
        testVariant = testSubject.getVariants().iterator().next();
    }

    @Test
    public void addSubjectLabel() {
        String name = RandomStringUtils.randomAlphabetic( 4 );
        Label label = new Label( name, "FF0000" );
        labelDao.create( label );
        testSubject.addLabel( label );
        subjectDao.update( testSubject );

        new InlineTransaction() {
            @Override
            public void instructions() {
                Subject s = subjectDao.findByPatientId( testSubjectId );
                assertTrue(s.getLabels().size() > 0);
            }
        }.execute();
    }

    @Test
    public void addVariantLabel() {
        String name = RandomStringUtils.randomAlphabetic( 4 );
        Label label = new Label( name, "FF0000" );
        labelDao.create( label );
        testVariant.addLabel( label );
        variantDao.update( testVariant );

        new InlineTransaction() {
            @Override
            public void instructions() {
                Subject s = subjectDao.findByPatientId( testSubjectId );
                Variant v = s.getVariants().iterator().next();
                assertTrue(v.getLabels().size() > 0);
            }
        }.execute();
     }

    @Test
    public void labelSuggestion() {
        String name = RandomStringUtils.randomAlphabetic( 4 );
        Label label = new Label( name, "FF0000" );
        labelDao.create( label );
        testSubject.addLabel( label );
        subjectDao.update( testSubject );

        Collection<Label> labels = labelDao.getLabelsMatching( name.substring(0, 3) );
        Label l = labels.iterator().next();
        assertEquals( name, l.getName() );
    }
}
