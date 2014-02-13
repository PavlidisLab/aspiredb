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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ubc.pavlab.aspiredb.shared.PhenotypeSummary;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.query.ExternalSubjectIdProperty;
import ubc.pavlab.aspiredb.shared.query.LabelProperty;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.query.SubjectLabelProperty;
import ubc.pavlab.aspiredb.shared.query.TextProperty;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * TODO Document Me
 * TODO: Sorting needs some thought.
 * 
 * @author Paul
 * @version $Id: SubjectServiceImpl.java,v 1.36 2013/06/24 23:26:39 cmcdonald Exp $
 */
@Service("subjectService")
@RemoteProxy(name="SubjectService")
public class SubjectServiceImpl implements SubjectService {
	protected static Log log = LogFactory.getLog( SubjectServiceImpl.class );

    @Autowired private SubjectDao subjectDao;
    
    @Autowired private CNVDao cnvDao;
    @Autowired private PhenotypeBrowserService phenotypeBrowserService;
    @Autowired private LabelDao labelDao;

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public SubjectValueObject getSubject(Long projectId, Long subjectId ){
        //throwGwtExceptionIfNotLoggedIn();
        Subject subject = subjectDao.load( subjectId );
        if ( subject == null ) return null;

        SubjectValueObject vo = subject.convertToValueObject();
        Integer numVariants = cnvDao.findBySubjectPatientId( subject.getPatientId() ).size();
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
    public Collection<PropertyValue> suggestValues(Property property, SuggestionContext suggestionContext){
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
	public List<PhenotypeSummaryValueObject> getPhenotypeSummaries( List<Long> subjectIds, Collection<Long> projectIds ) throws NeurocartaServiceException{
        
        Collection<Subject> subjects = subjectDao.load(subjectIds);
        
        StopWatch timer = new StopWatch();
        timer.start();

        log.info( "loading phenotypeSummaries for "+subjectIds.size()+" subjects" );
        List<PhenotypeSummary> phenotypeSummaries =
                phenotypeBrowserService.getPhenotypesBySubjectIds(subjectIds, projectIds);
        log.info( "processing"+ phenotypeSummaries.size() + " phenotypeSummaries for "+subjectIds.size()+" subjects took " + timer.getTime() + "ms" );
        
        
        
        List<PhenotypeSummaryValueObject> valueObjects = new ArrayList<PhenotypeSummaryValueObject>();        
        
        //convert PhenotypeSummaries to lighter PhenotypeValueObjects
        
        for (PhenotypeSummary sum: phenotypeSummaries){
            
            String displaySummary = "";
            
            Set<String> keyArray = sum.getDbValueToSubjectSet().keySet();
            
            for (String key: keyArray){
                
                Integer size = sum.getDbValueToSubjectSet().get( key ).size();
                
                if (sum.getValueType().equals( "HPONTOLOGY")) {
                    if (key.equals( "1")) {
                        displaySummary = displaySummary + " Present(" + size + ')';
                        displaySummary = "<span " + "style='color: red'" + ">" + displaySummary + "</span>";
                    }else if (key.equals( "0")) {
                        displaySummary = displaySummary + " Absent(" + size + ')';
                        displaySummary = "<span " + "style='color: green'" + ">" + displaySummary + "</span>";
                    } else {
                        displaySummary = displaySummary + ' ' + key + " (" + size + ')';
                        displaySummary = "<span " + "style='color: black'" + ">" + displaySummary + "</span>";
                    }
                } else {
                    displaySummary = displaySummary + ' ' + key + " (" + size + ')';
                    displaySummary = "<span " + "style='color: black'" + ">" + displaySummary + "</span>";
                }
                
                
                
                
            }
            
            PhenotypeSummaryValueObject pvo = new PhenotypeSummaryValueObject();
            
            pvo.setName( sum.getName() );
            pvo.setUri( sum.getUri() );
            pvo.setValueType( sum.getValueType() );
            pvo.setNeurocartaPhenotype( sum.isNeurocartaPhenotype() );
            
            pvo.setDisplaySummary( displaySummary );
            
            valueObjects.add( pvo );
            
        }
        
        
        return valueObjects;
	}
    
    @Override
    @RemoteMethod
    @Transactional
    public String getPhenotypeTextDownloadBySubjectIds( List<Long> subjectIds){
        
        
        StopWatch timer = new StopWatch();
        timer.start();
        
        List<SubjectValueObject> svoList = new ArrayList<SubjectValueObject>();
        
        Collection<Subject> subjectList = subjectDao.load( subjectIds );
        
        for (Subject s: subjectList){            
            SubjectValueObject svo = s.convertToValueObjectWithPhenotypes();
            svoList.add( svo );            
        }
        
        StringBuffer text = new StringBuffer();

        LinkedHashMap<String, String> phenotypeFileColumnsMap = new LinkedHashMap<String, String>();

        for ( SubjectValueObject svo : svoList ) {

            for ( PhenotypeValueObject pvo : svo.getPhenotypes().values() ) {

                String columnName = pvo.getUri()!=null ? (pvo.getUri() +":"+pvo.getName() ): pvo
                        .getName();

                if ( !phenotypeFileColumnsMap.containsKey( columnName ) ) {
                    phenotypeFileColumnsMap.put( columnName, pvo.getName() );
                }

            }

        }

        text.append( "Subject Id\t" );

        for ( String columnName : phenotypeFileColumnsMap.keySet() ) {
            text.append( columnName + "\t" );
        }

        text.append( "\n" );
        for ( SubjectValueObject svo : svoList ) {

            text.append( svo.getPatientId() + "\t" );
            Map<String, PhenotypeValueObject> phenotypeMap = svo.getPhenotypes();

            for ( String columnName : phenotypeFileColumnsMap.keySet() ) {
                
                PhenotypeValueObject vo = phenotypeMap.get( phenotypeFileColumnsMap.get( columnName ) );
                
                if (vo !=null){                
                    text.append( vo.getDbValue() + "\t" );
                }
            }

            text.append( "\n" );
        }
        
        return text.toString();
    }

    @Override
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

    @Override
    @RemoteMethod
    @Transactional
    public void removeLabel(Long id, LabelValueObject label){
       
        Subject subject = subjectDao.load(id);
        Label labelEntity = labelDao.load(label.getId());
        subject.removeLabel(labelEntity);
        subjectDao.update( subject );
    }

    @Override
    @RemoteMethod
    @Transactional
    public void removeLabel(Collection<Long> subjectIds, LabelValueObject label){
       
        for (Long subjectId : subjectIds) {
            removeLabel(subjectId, label);
        }
    }

    @Override
    @RemoteMethod
    @Transactional
    public List<LabelValueObject> suggestLabels(SuggestionContext suggestionContext) {
        Collection<Label> labels;
        if ( suggestionContext == null || suggestionContext.getActiveProjectIds().size() == 0 )
            labels = labelDao.getSubjectLabels();
        else 
            labels = labelDao.getSubjectLabelsByProjectId( suggestionContext.getActiveProjectIds().iterator().next() );
        List<LabelValueObject> vos = new ArrayList<LabelValueObject>();
        for (Label label : labels) {
            vos.add( label.toValueObject() );
        }
        return vos;
    }
}