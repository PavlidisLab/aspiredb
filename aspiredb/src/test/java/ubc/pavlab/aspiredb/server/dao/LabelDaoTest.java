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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;

@RunWith(SpringJUnit4ClassRunner.class)
public class LabelDaoTest extends BaseSpringContextTest {
    @Autowired
    private LabelDao labelDao;

    @Autowired
    private ProjectDao projectDao;

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
    Project testProject;

    @Before
    public void init() {
        testSubjectId = RandomStringUtils.randomAlphanumeric( 5 );
        testSubject = testObjectHelper.createPersistentTestSubjectObjectWithCNV( testSubjectId );
        testVariant = testSubject.getVariants().iterator().next();

        testProject = new Project();
        testProject.setName( RandomStringUtils.randomAlphabetic( 4 ) );

        testProject = testObjectHelper.createPersistentProject( testProject );

        testObjectHelper.addSubjectToProject( testSubject, testProject );

    }

    @Test
    public void addSubjectLabel() {

        new InlineTransaction() {
            @Override
            public void instructions() {
                String name = RandomStringUtils.randomAlphabetic( 4 );
                Label label = new Label( name, "FF0000" );
                labelDao.create( label );
                testSubject.addLabel( label );
                subjectDao.update( testSubject );

                Subject s = subjectDao.findByPatientId( testSubjectId );
                assertTrue( s.getLabels().size() > 0 );
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
                assertTrue( v.getLabels().size() > 0 );
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

        Collection<Label> labels = labelDao.getLabelsMatching( name.substring( 0, 3 ) );
        Label l = labels.iterator().next();
        assertEquals( name, l.getName() );
    }

    @Test
    public void labelSuggestionByContext() {
        String name = RandomStringUtils.randomAlphabetic( 4 );
        Label label = new Label( name, "FF0000" );
        labelDao.create( label );
        testSubject.addLabel( label );
        subjectDao.update( testSubject );

        Collection<Label> labels = labelDao.getSubjectLabelsByProjectId( 0L );
        assertEquals( 0, labels.size() );

        labels = labelDao.getSubjectLabelsByProjectId( testProject.getId() );
        assertNotNull( labels );
        assertEquals( 1, labels.size() );
        assertEquals( name, labels.iterator().next().getName() );
    }
}
