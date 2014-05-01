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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

/**
 * author: anton date: 10/06/13
 */
@Component("labelService")
@Service("labelService")
@RemoteProxy(name = "LabelService")
public class LabelServiceImpl implements LabelService {

    @Autowired
    private LabelDao labelDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private VariantDao variantDao;

    @Override
    @Transactional
    @RemoteMethod
    public void updateLabel( LabelValueObject label ) {
        Label labelEntity = labelDao.load( label.getId() );
        labelEntity.setName( label.getName() );
        labelEntity.setColour( label.getColour() );
        labelEntity.setIsShown( label.getIsShown() );
        labelDao.update( labelEntity );
    }

    @Override
    @Transactional
    @RemoteMethod
    public void deleteSubjectLabels( Collection<LabelValueObject> labels ) {
        for ( LabelValueObject lvo : labels ) {
            deleteSubjectLabel( lvo );
        }
    }

    @Override
    @Transactional
    @RemoteMethod
    public void removeLabelsFromSubjects( Collection<LabelValueObject> labels, Collection<Long> subjectIds ) {
        // Collection<Label labelEntity = labelDao.load( labelIds );
        Collection<Subject> subjects = subjectDao.load( subjectIds );
        for ( Subject subject : subjects ) {
            for ( LabelValueObject label : labels ) {
                subject.getLabels().remove( labelDao.findOrCreate( label ) );
                subjectDao.update( subject );
            }
        }
    }

    @Override
    @Transactional
    @RemoteMethod
    public void deleteVariantLabel( LabelValueObject label ) {
        Collection<Variant> variants = variantDao.findByLabel( label );
        Collection<Long> variantIds = new ArrayList<>();
        for ( Variant v : variants ) {
            variantIds.add( v.getId() );
        }
        Collection<LabelValueObject> labels = new ArrayList<>();
        labels.add( label );
        removeLabelsFromVariants( labels, variantIds );
        Label labelEntity = labelDao.load( label.getId() );
        labelDao.remove( labelEntity );
    }

    @Override
    @Transactional
    @RemoteMethod
    public void deleteVariantLabels( Collection<LabelValueObject> labels ) {
        for ( LabelValueObject lvo : labels ) {
            deleteVariantLabel( lvo );
        }
    }

    @Override
    @Transactional
    @RemoteMethod
    public void removeLabelsFromVariants( Collection<LabelValueObject> labels, Collection<Long> variantIds ) {
        Collection<Variant> variants = variantDao.load( variantIds );
        // Collection<Label> labels = labelDao.load( labelIds );
        for ( Variant variant : variants ) {
            for ( LabelValueObject label : labels ) {
                variant.getLabels().remove( labelDao.findOrCreate( label ) );
                variantDao.update( variant );
            }
        }
    }

    @Override
    @Transactional
    @RemoteMethod
    public void deleteSubjectLabel( LabelValueObject label ) {
        if ( label == null || label.getId() == null ) {
            return;
        }
        Collection<Subject> subjects = subjectDao.findByLabel( label );
        Collection<Long> subjectIds = new ArrayList<>();
        for ( Subject s : subjects ) {
            subjectIds.add( s.getId() );
        }
        Collection<LabelValueObject> labels = new ArrayList<>();
        labels.add( label );
        removeLabelsFromSubjects( labels, subjectIds );
        Label labelEntity = labelDao.load( label.getId() );
        labelDao.remove( labelEntity );
    }
}
