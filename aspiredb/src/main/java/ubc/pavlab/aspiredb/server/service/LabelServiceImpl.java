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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ubc.pavlab.aspiredb.client.service.LabelService;
import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

import java.util.Collection;

/**
 * author: anton
 * date: 10/06/13
 */
@Component("labelService")
public class LabelServiceImpl implements LabelService {

    @Autowired
    private LabelDao labelDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private VariantDao variantDao;

    @Override
    @Transactional
    public void deleteSubjectLabel(LabelValueObject label) {
        Label labelEntity = labelDao.load(label.getId());
        Collection<Subject> subjects = subjectDao.findByLabel(label);
        for (Subject subject : subjects) {
            subject.getLabels().remove(labelEntity);
            subjectDao.update(subject);
        }
        labelDao.remove(labelEntity);
    }

    @Override
    @Transactional
    public void deleteVariantLabel(LabelValueObject label) {
        Label labelEntity = labelDao.load(label.getId());
        Collection<Variant> variants = variantDao.findByLabel(label);
        for (Variant variant : variants) {
            variant.getLabels().remove(labelEntity);
            variantDao.update(variant);
        }
        labelDao.remove(labelEntity);
    }
}
