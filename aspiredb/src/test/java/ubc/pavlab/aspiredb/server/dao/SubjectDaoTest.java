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


import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.*;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.service.SubjectService;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.query.*;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.SetRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

/**
 *
 * @author anton
 * @version $Id: SubjectDaoTest.java,v 1.9 2013/07/02 18:20:21 anton Exp $
 */
public class SubjectDaoTest extends BaseSpringContextTest {

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private LabelDao labelDao;

    @Autowired
    private PersistentTestObjectHelper testObjectHelper;

    String project1Name = RandomStringUtils.randomAlphabetic(5);
    String project2Name = RandomStringUtils.randomAlphabetic(5);
    String patientId = RandomStringUtils.randomAlphabetic(5);

    private Subject subject;
    private Label label;

    @Before
    public void setup() throws NotLoggedInException {
        new InlineTransaction() {
            @Override
            public void instructions() {
                subject = testObjectHelper.createPersistentTestIndividualObject("testPatientId");
                label = labelDao.findOrCreate(new LabelValueObject("SUBJECT_TEST_LABEL"));
                Collection<Long> ids = new ArrayList<Long>(Arrays.asList(subject.getId()));
                try{
                    subjectService.addLabel(ids, new LabelValueObject(label.getId(), "SUBJECT_TEST_LABEL"));
                }catch(Exception e){
                    
                }
                
            }
        }.execute();
    }

    @After
    public void cleanup() {
        new InlineTransaction() {
            @Override
            public void instructions() {
                subjectDao.remove(subject);
                labelDao.remove(label);
            }
        }.execute();
    }


    @Test
    public void testLoad() {
        final Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
        filters.add(new SubjectFilterConfig( makeTestRestrictionExpression() ) );
        new InlineTransaction() {
            @Override
            public void instructions() {
                Collection<? extends Subject> subjects = null;
                try {
                    subjects = subjectDao.load(filters);
                } catch (BioMartServiceException e) {
                } catch (NeurocartaServiceException e) {
                }

                assertTrue(subjects.size() == 1);
            }
        }.execute();

        filters.clear();
        filters.add(new SubjectFilterConfig( makeTestRestrictionExpressionWithSets() ));
        new InlineTransaction() {
            @Override
            public void instructions() {
                Collection<? extends Subject> subjects = null;
                try {
                    subjects = subjectDao.load(filters);
                } catch (BioMartServiceException e) {
                } catch (NeurocartaServiceException e) {
                }

                assertTrue(subjects.size() == 1);
            }
        }.execute();
    }

    public RestrictionExpression makeTestRestrictionExpression() {
        RestrictionExpression labelRestriction = new SimpleRestriction(new SubjectLabelProperty(),
                Operator.TEXT_EQUAL, new LabelValueObject(label.getId(), "SUBJECT_TEST_LABEL"));

        RestrictionExpression patientIdRestriction =
                new SimpleRestriction(new ExternalSubjectIdProperty(), Operator.TEXT_EQUAL, new TextValue("testPatientId"));

        Conjunction restriction = new Conjunction();
        restriction.add(patientIdRestriction);
        restriction.add(labelRestriction);

        return restriction;
    }

    public RestrictionExpression makeTestRestrictionExpressionWithSets() {
        final Set<LabelValueObject> values = new HashSet<LabelValueObject>();
        values.add( new LabelValueObject( label.getId(), "SUBJECT_TEST_LABEL" ) );
        RestrictionExpression labelRestriction = new SetRestriction( new SubjectLabelProperty(), Operator.IS_IN_SET,
                values );

        Set <TextValue> textValues = new HashSet<TextValue>();
        textValues.add(new TextValue("testPatientId"));
        
        RestrictionExpression patientIdRestriction = new SetRestriction( new ExternalSubjectIdProperty(),
                Operator.IS_IN_SET, textValues );

        Conjunction restriction = new Conjunction();
        restriction.add(patientIdRestriction);
        restriction.add(labelRestriction);

        return restriction;
    }

    @Test
    public void testFindByPatientId() throws Exception {
        Project p1 = new Project();
        p1.setName(project1Name);
        p1.getSubjects().add(testObjectHelper.createPersistentTestSubjectObjectWithCNV(patientId));

        p1 = testObjectHelper.createPersistentProject(p1);

        Project p2 = new Project();
        p2.setName(project2Name);

        p2 = testObjectHelper.createPersistentProject(p2);

        Subject s = subjectDao.findByPatientId(p2, patientId);

        assertNull(s);

        Subject s2 = subjectDao.findByPatientId(p1, patientId);

        assertEquals(patientId, s2.getPatientId());
    }
}
