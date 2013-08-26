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
package ubc.pavlab.aspiredb.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.CNVDao;
import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.query.*;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * TODO Document Me
 * TODO: Sorting needs some thought.
 * 
 * @author Paul
 * @version $Id: SubjectServiceImpl.java,v 1.36 2013/06/24 23:26:39 cmcdonald Exp $
 */
@Service("subjectServiceOld")
@RemoteProxy(name="SubjectService")
public class SubjectServiceOldImpl extends GwtService implements SubjectServiceOld {
	protected static Log log = LogFactory.getLog( SubjectServiceOldImpl.class );

    @Autowired private SubjectDao subjectDao;
    
    @Autowired private CNVDao cnvDao;
    @Autowired private PhenotypeBrowserService phenotypeBrowserService;
    @Autowired private LabelDao labelDao;

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public SubjectValueObject getSubject(Long projectId, Long subjectId ) throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();
        Subject subject = subjectDao.load( subjectId );
        if ( subject == null ) return null;

        SubjectValueObject vo = subject.convertToValueObject();
        Integer numVariants = cnvDao.findBySubjectId( subject.getPatientId() ).size();
        vo.setVariants( numVariants != null ? numVariants : 0 );

        return vo;
    }

    @Override
    @RemoteMethod
    public Collection<Property> suggestProperties() {
        Collection<Property> properties = new ArrayList<Property>();
        properties.add( new ExternalSubjectIdProperty() );
        properties.add( new SubjectLabelProperty() );
        return properties;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<PropertyValue> suggestValues(Property property, SuggestionContext suggestionContext) throws NotLoggedInException {
        List<PropertyValue> values = new ArrayList<PropertyValue>();
        if (property instanceof LabelProperty) {
            List<LabelValueObject> labels = suggestLabels(suggestionContext);
            for (LabelValueObject label : labels) {
                values.add( new PropertyValue<LabelValueObject>(label) );
            }
        } else if (property instanceof TextProperty) {
            Collection<String> stringValues = ((TextProperty) property).getDataType().getAllowedValues();
            if (stringValues.isEmpty()) {
                stringValues = subjectDao.suggestValuesForEntityProperty(property, suggestionContext);
            }
            for (String stringValue : stringValues) {
                values.add( new PropertyValue<TextValue>(new TextValue(stringValue)) );
            }
        }
        return values;
    }

    @Override
    @RemoteMethod
    @Transactional
	public List<PhenotypeSummaryValueObject> getPhenotypeSummaries( List<Long> subjectIds, Collection<Long> projectIds )
            throws NotLoggedInException, NeurocartaServiceException {
        throwGwtExceptionIfNotLoggedIn();
        // This should throw AccessDenied exception if user isn't allowed to view the subjects
        // (there is a better way to test security, this method is probably going to disappear)
        Collection<Subject> subjects = subjectDao.load(subjectIds);
        
        StopWatch timer = new StopWatch();
        timer.start();

        log.info( "loading phenotypeSummaries for "+subjectIds.size()+" subjects" );
        List<PhenotypeSummaryValueObject> phenotypeSummaries =
                phenotypeBrowserService.getPhenotypesBySubjectIds(subjectIds, projectIds);
        log.info( "processing phenotypeSummaries for "+subjectIds.size()+" subjects took " + timer.getTime() + "ms" );
        
        return phenotypeSummaries;
	}
    
    @Override
    @RemoteMethod
    @Transactional
    public List<SubjectValueObject> getSubjectsWithPhenotypesBySubjectIds( List<Long> subjectIds)
            throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();        
        
        StopWatch timer = new StopWatch();
        timer.start();
        
        List<SubjectValueObject> svoList = new ArrayList<SubjectValueObject>();
        
        Collection<Subject> subjectList = subjectDao.load( subjectIds );
        
        for (Subject s: subjectList){            
            SubjectValueObject svo = s.convertToValueObjectWithPhenotypes();
            svoList.add( svo );            
        }
        
        return svoList;
    }

    @Override
    @RemoteMethod
    @Transactional
    public LabelValueObject addLabel(Collection<Long> subjectIds, LabelValueObject labelVO) throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();
        Collection<Subject> subjects = subjectDao.load(subjectIds);
        Label label = labelDao.findOrCreate( labelVO );
        for (Subject subject : subjects) {
            subject.addLabel( label );
            subjectDao.update( subject );
        }
        return label.toValueObject();
    }

    @Override
    @RemoteMethod
    @Transactional
    public void removeLabel(Long id, LabelValueObject label) throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();
        Subject subject = subjectDao.load(id);
        Label labelEntity = labelDao.load(label.getId());
        subject.removeLabel(labelEntity);
        subjectDao.update( subject );
    }

    @Override
    @RemoteMethod
    @Transactional
    public void removeLabel(Collection<Long> subjectIds, LabelValueObject label) throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();
        for (Long subjectId : subjectIds) {
            removeLabel(subjectId, label);
        }
    }

    @Override
    @RemoteMethod
    @Transactional
    public List<LabelValueObject> suggestLabels(SuggestionContext suggestionContext) {
        // TODO: filter out labels non-applicable to subjects
        // labelDao.getLabelsMatching(partialName);
        Collection<Label> labels = labelDao.getSubjectLabels();
        List<LabelValueObject> vos = new ArrayList<LabelValueObject>();
        for (Label label : labels) {
            vos.add( label.toValueObject() );
        }
        return vos;
    }
}