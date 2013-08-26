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
package ubc.pavlab.aspiredb.server;

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.Page;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.service.SubjectServiceOld;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.query.*;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.VariantTypeRestriction;

@RunWith(SpringJUnit4ClassRunner.class)
public class SubjectServiceTest extends BaseSpringContextTest {
    @Autowired
    private LabelDao labelDao;

    @Autowired
    private SubjectServiceOld subjectService;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    PersistentTestObjectHelper testObjectHelper;

    String testSubjectId;
    Subject testSubject;
    Variant testVariant;

    @Before
    public void init() {
        testSubjectId = RandomStringUtils.randomAlphanumeric(5);
        testSubject = testObjectHelper.createPersistentTestSubjectObjectWithCNV(testSubjectId);
        testVariant = testSubject.getVariants().iterator().next();
    }

    @Test
    public void findSubjectWithVariantFilter() throws NeurocartaServiceException, BioMartServiceException {
        Conjunction restriction = new Conjunction();
        restriction.add(new VariantTypeRestriction(VariantType.CNV));
        restriction.add(new SimpleRestriction(new CNVTypeProperty(), Operator.TEXT_EQUAL, new TextValue("LOSS")));

        VariantFilterConfig filter = new VariantFilterConfig();
        filter.setRestriction(restriction);
        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();
        set.add(filter);

        Page<? extends Subject> subjects = subjectDao.loadPage(0, 100, "", "", set);
        assertTrue(subjects.size() > 0);
    }

    @Test
    public void addSubjectLabel() {
        Collection<Long> ids = new ArrayList<Long>();
        ids.add(testSubject.getId());

        String name = RandomStringUtils.randomAlphabetic( 4 );
        
        try{
            subjectService.addLabel(ids, new LabelValueObject(name));
        }catch(Exception e){
            
        }

        Collection<Label> labels = labelDao.getLabelsMatching(name);
        assertTrue(labels.size() > 0);
    }
}
