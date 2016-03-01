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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.QueryTestUtils;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.service.VariantService;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;

/**
 * author: anton date: 21/05/13
 */
public class VariantDaoTest extends BaseSpringContextTest {

    @Autowired
    private VariantDao variantDao;

    @Autowired
    private LabelDao labelDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private VariantService variantService;

    @Autowired
    private PersistentTestObjectHelper testObjectHelper;

    private CNV cnv;
    private Subject subject;
    private Label label;
    private Project project;

    @After
    public void cleanup() {
        new InlineTransaction() {
            @Override
            public void instructions() {
                variantDao.remove( cnv );
                subjectDao.remove( subject );
                labelDao.remove( label );
                projectDao.remove( project );
            }
        }.execute();
    }

    @Before
    public void setup() {
        new InlineTransaction() {
            @Override
            public void instructions() {
                Project p = new Project();
                p.setName( "VariantDaoTestProject" );
                project = testObjectHelper.createPersistentProject( p );
                subject = testObjectHelper.createPersistentTestIndividualObject( "testPatientId" );
                subject = testObjectHelper.addSubjectToProject( subject, project );
                cnv = testObjectHelper.createPersistentTestCNVObject();
                cnv.setSubject( subject );
                label = labelDao.findOrCreate( new LabelValueObject( "CNV_TEST_LABEL" ) );

                variantService.addLabel( cnv.getId(), new LabelValueObject( label.getId(), "CNV_TEST_LABEL" ) );

            }
        }.execute();
    }

    @Test
    public void testFindByGenomicLocation() throws Exception {
        GenomicRange range = new GenomicRange( "8", 37885255, 37890000 );
        // new GenomicLocation( "X", 56650362, 56729961 );
        Collection<Variant> results = variantDao.findByGenomicLocation( range,
                Collections.singletonList( project.getId() ) );

        assertEquals( 0, results.size() );

        range = new GenomicRange( "X", 56650000, 56730000 );

        results = variantDao.findByGenomicLocation( range, Collections.singletonList( project.getId() ) );
        assertEquals( 1, results.size() );
    }

    @Test
    public void testFindByGenomicLocationQuick() throws Exception {
        GenomicRange range = new GenomicRange( "8", 37885255, 37890000 );
        // new GenomicLocation( "X", 56650362, 56729961 );
        Collection<VariantValueObject> results = variantDao.findByGenomicLocationQuick( range,
                Collections.singletonList( project.getId() ) );

        assertEquals( 0, results.size() );

        range = new GenomicRange( "X", 56650000, 56730000 );

        results = variantDao.findByGenomicLocationQuick( range, Collections.singletonList( project.getId() ) );
        assertEquals( 1, results.size() );
    }

    @Test
    public void testFindBySubjectPatientId() throws Exception {
        Collection<Variant> results = variantDao.findBySubjectPatientId( project.getId(), subject.getPatientId() );
        assertEquals( 1, results.size() );
    }

    @Test
    public void testLoad() throws Exception {
        final Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
        filters.add( new VariantFilterConfig( QueryTestUtils.makeTestVariantRestrictionExpression( label.getId() ) ) );
        new InlineTransaction() {
            @Override
            public void instructions() {
                Collection<? extends Variant> variants = null;
                try {
                    variants = variantDao.load( filters );
                    assertEquals( 1, variants.size() );

                } catch ( BioMartServiceException e ) {
                } catch ( NeurocartaServiceException e ) {
                }

            }
        }.execute();

        filters.clear();
        filters.add( new VariantFilterConfig( QueryTestUtils.makeTestVariantRestrictionExpressionWithSets( label
                .getId() ) ) );
        new InlineTransaction() {
            @Override
            public void instructions() {
                Collection<? extends Variant> variants = null;

                try {
                    variants = variantDao.load( filters );
                    assertEquals( 1, variants.size() );

                } catch ( BioMartServiceException e ) {
                } catch ( NeurocartaServiceException e ) {
                }

            }
        }.execute();
    }

    // @Test
    public void testSuggestValuesForEntityProperty() throws Exception {
        // final Property cnvTypeProperty = new CNVTypeProperty();
        // final SuggestionContext suggestionContext = new SuggestionContext();
        // suggestionContext.setValuePrefix("LOS");
        //
        // new InlineTransaction() {
        // @Override
        // public void instructions() {
        // Collection<String> values = variantDao.suggestValuesForEntityProperty(cnvTypeProperty, suggestionContext);
        // assertEquals(values.size(), 1);
        // assertEquals(values.iterator().next(), "LOSS");
        // }
        // }.execute();
    }
}
