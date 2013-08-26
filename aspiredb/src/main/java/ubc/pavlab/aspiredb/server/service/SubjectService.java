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

import java.util.ArrayList;
import java.util.Collection;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.CNVDao;
import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.valueobjects.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

/**
 * TODO Document Me
 * 
 * @author ptan
 * @version $Id$
 *
 * TODO: delete this and drop 'Old' from *ServiceOld
 *
 */
@Deprecated
public class SubjectService {

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private CNVDao cnvDao;

    @Autowired
    private PhenotypeBrowserService phenotypeBrowserService;

    @Autowired
    private LabelDao labelDao;

    @Transactional(readOnly = true)
    public Collection<SubjectValueObject> getSubjects() {

        Collection<Subject> subjects = subjectDao.loadAll();
        Collection<SubjectValueObject> vos = new ArrayList<SubjectValueObject>();

        for ( Subject s : subjects ) {
            SubjectValueObject vo = Subject.convertToValueObject( s );
            vos.add( vo );
        }

        return vos;
    }

    @Transactional(readOnly = true)
    public SubjectValueObject getSubject( Long projectId, Long subjectId ) {
        Subject subject = subjectDao.load( subjectId );
        if ( subject == null ) return null;

        SubjectValueObject vo = Subject.convertToValueObject( subject );

        // TODO add variants
        // Integer numVariants = cnvDao.findBySubjectId( subject.getPatientId() ).size();
        // vo.setVariants( numVariants != null ? numVariants : 0 );

        return vo;
    }

    @RemoteMethod
    @Transactional
    public LabelValueObject addLabel(Collection<Long> subjectIds, LabelValueObject labelVO) {
        Collection<Subject> subjects = subjectDao.load(subjectIds);
        Label label = labelDao.findOrCreate( labelVO );
        for (Subject subject : subjects) {
            subject.addLabel( label );
            subjectDao.update( subject );
        }
        return label.toValueObject();
    }
    
}
