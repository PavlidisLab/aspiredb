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
import java.util.HashMap;
import java.util.HashSet;
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
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeSummary;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.StringMatrix;
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
 * TODO Document Me TODO: Sorting needs some thought.
 * 
 * @author Paul
 * @version $Id: SubjectServiceImpl.java,v 1.36 2013/06/24 23:26:39 cmcdonald Exp $
 */
@Service("subjectService")
@RemoteProxy(name = "SubjectService")
public class SubjectServiceImpl implements SubjectService {
    protected static Log log = LogFactory.getLog( SubjectServiceImpl.class );

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private CNVDao cnvDao;

    @Autowired
    private PhenotypeBrowserService phenotypeBrowserService;

    @Autowired
    private LabelDao labelDao;

    @Autowired
    private VariantDao variantDao;

    /**
     * Get the Subject value Object of the given subject Id
     * 
     * @param projectId, subjectId
     * @return SubjectValueObject
     */
    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public SubjectValueObject getSubject( Long projectId, Long subjectId ) {
        // throwGwtExceptionIfNotLoggedIn();
        Subject subject = subjectDao.load( subjectId );
        if ( subject == null ) {
            return null;
        }

        SubjectValueObject vo = subject.convertToValueObject();
        Integer numVariants = cnvDao.findBySubjectPatientId( projectId, subject.getPatientId() ).size();
        vo.setVariants( numVariants != null ? numVariants : 0 );

        return vo;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public List<Long> getVariantsSubjects( List<String> patientIds ) {
        List<Long> subjectIds = new ArrayList<Long>();

        for ( String patientId : patientIds ) {
            Subject variantSubject = subjectDao.findByPatientId( patientId );
            subjectIds.add( variantSubject.getId() );
        }

        return subjectIds;
    }

    /**
     * Get the list of Subject value Objects of the given subject Ids
     * 
     * @param projectId, subjectId
     * @return SubjectValueObject
     */
    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<SubjectValueObject> getSubjects( Long projectId, List<Long> subjectIds ) {
        // throwGwtExceptionIfNotLoggedIn();
        Collection<Subject> subjects = subjectDao.load( subjectIds );
        if ( subjects.isEmpty() ) {
            return null;
        }

        List<SubjectValueObject> vos = new ArrayList<>();

        for ( Subject subject : subjects ) {

            SubjectValueObject vo = subject.convertToValueObject();
            Integer numVariants = cnvDao.findBySubjectPatientId( projectId, subject.getPatientId() ).size();
            vo.setVariants( numVariants != null ? numVariants : 0 );
            vos.add( vo );
        }
        return vos;
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
    public Collection<PropertyValue> suggestValues( Property property, SuggestionContext suggestionContext ) {
        List<PropertyValue> values = new ArrayList<PropertyValue>();
        if ( property instanceof LabelProperty ) {
            List<LabelValueObject> labels = suggestLabels( suggestionContext );
            for ( LabelValueObject label : labels ) {
                values.add( new PropertyValue<LabelValueObject>( label ) );
            }
        } else if ( property instanceof TextProperty ) {
            Collection<String> stringValues = ( ( TextProperty ) property ).getDataType().getAllowedValues();
            if ( stringValues.isEmpty() ) {
                stringValues = subjectDao.suggestValuesForEntityProperty( property, suggestionContext );
            }
            for ( String stringValue : stringValues ) {
                values.add( new PropertyValue<TextValue>( new TextValue( stringValue ) ) );
            }
        }
        return values;
    }

    /**
     * Get the Phenotype summary value objects for the given list of subject Ids and Project Ids.
     * 
     * @exception NeurocartaServiceException
     * @param List of subjectIds, Collection of projectIds
     * @return List of Phenotype summary value objects
     */
    @Override
    @RemoteMethod
    @Transactional
    public List<PhenotypeSummaryValueObject> getPhenotypeSummaries( List<Long> subjectIds, Collection<Long> projectIds )
            throws NeurocartaServiceException {

        subjectDao.load( subjectIds );

        StopWatch timer = new StopWatch();
        timer.start();

        log.info( "loading phenotypeSummaries for " + subjectIds.size() + " subjects" );
        List<PhenotypeSummary> phenotypeSummaries = phenotypeBrowserService.getPhenotypesBySubjectIds( subjectIds,
                projectIds );
        log.info( "processing" + phenotypeSummaries.size() + " phenotypeSummaries for " + subjectIds.size()
                + " subjects took " + timer.getTime() + "ms" );

        List<PhenotypeSummaryValueObject> valueObjects = new ArrayList<PhenotypeSummaryValueObject>();

        // convert PhenotypeSummaries to lighter PhenotypeValueObjects

        for ( PhenotypeSummary sum : phenotypeSummaries ) {

            String displaySummary = "";
            HashMap<String, Integer> phenoSummaryMap = new HashMap<String, Integer>();

            Set<String> keyArray = sum.getDbValueToSubjectSet().keySet();
            /**
             * Used the Color Brewer 2.0 system for coloring the chart Thanks for Cynthia Brewer, Mark Harrower and The
             * Pennsylvania State University
             */
            String[] colors = { "#b35806", "#31a354", "#636363", "#d8b365", "#2c7fb8", "#addd8e", "#7570b3", "#a6bddb" };
            int j = 3;
            int unknown = 0;
            int present = 0;
            int denominator = 0;
            for ( String key : keyArray ) {

                Integer size = sum.getDbValueToSubjectSet().get( key ).size();

                if ( sum.getValueType().equals( "HPONTOLOGY" ) ) {
                    if ( key.equals( "1" ) || key.equals( "Y" ) ) {
                        phenoSummaryMap.put( "Present", size );
                        present = size;
                        denominator = denominator + size;
                        displaySummary = displaySummary + " Present(" + size + ')';
                        displaySummary = "<span " + "style='color: " + colors[0] + "'" + ">" + displaySummary
                                + "</span>";
                    } else if ( key.equals( "0" ) || key.equals( "N" ) ) {
                        phenoSummaryMap.put( "Absent", size );
                        denominator = denominator + size;
                        displaySummary = displaySummary + " Absent(" + size + ')';
                        displaySummary = "<span " + "style='color: " + colors[1] + "'" + ">" + displaySummary
                                + "</span>";
                    } else if ( key.equals( "Unknown" ) ) {
                        unknown = size;
                    }
                } else if ( key.equals( "Unknown" ) ) {
                    unknown = size;
                } else {
                    phenoSummaryMap.put( key, size );
                    displaySummary = displaySummary + ' ' + key + " (" + size + ')';

                    // special case for phenotype with many values
                    if ( j < colors.length ) {
                        displaySummary = "<span " + "style='color:" + colors[j] + "'" + ">" + displaySummary
                                + "</span>";
                    } else {
                        // log.warn( "There are more '" + sum.getName() + "' phenotype values than there are colors" );
                        displaySummary = "<span " + "style='color:" + colors[colors.length - 1] + "'" + ">"
                                + displaySummary + "</span>";
                    }

                    j++;
                }

            }
            if ( unknown != 0 ) {
                phenoSummaryMap.put( "Unknown", unknown );
                displaySummary = displaySummary + " Unknown(" + unknown + ')';
                displaySummary = "<span " + "style='color: " + colors[2] + "'" + ">" + displaySummary + "</span>";
            } else {
                phenoSummaryMap.put( "Unknown", 0 );
                displaySummary = displaySummary + " Unknown(" + 0 + ')';
                displaySummary = "<span " + "style='color: " + colors[2] + "'" + ">" + displaySummary + "</span>";
            }
            PhenotypeSummaryValueObject pvo = new PhenotypeSummaryValueObject();

            pvo.setName( sum.getName() );
            pvo.setUri( sum.getUri() );
            pvo.setValueType( sum.getValueType() );
            pvo.setNeurocartaPhenotype( sum.isNeurocartaPhenotype() );
            pvo.setSubjects( sum.getDbValueToSubjectSet() );
            pvo.setDisplaySummary( displaySummary );
            pvo.setPhenoSummaryMap( phenoSummaryMap );
            pvo.setPhenoSet( phenoSummaryMap.keySet() );
            // if (denominator!=0)
            pvo.setSortValue( present );
            valueObjects.add( pvo );

        }

        return valueObjects;
    }

    /**
     * Get the map of Phenotype Name and Phenotype Summary Value Objects for the given list of subject Ids and list of
     * Project Ids.
     * 
     * @exception NeurocartaServiceException
     * @param List of subjectIds, Collection of projectIds
     * @return Map of Phenotype Name and Phenotype summary value objects
     */
    @Override
    @RemoteMethod
    @Transactional
    public Map<String, PhenotypeSummaryValueObject> getPhenotypeSummaryValueObjects( List<Long> subjectIds,
            Collection<Long> projectIds ) throws NeurocartaServiceException {

        subjectDao.load( subjectIds );

        StopWatch timer = new StopWatch();
        timer.start();

        log.info( "loading phenotypeSummaries for " + subjectIds.size() + " subjects" );
        List<PhenotypeSummary> phenotypeSummaries = phenotypeBrowserService.getPhenotypesBySubjectIds( subjectIds,
                projectIds );
        log.info( "processing" + phenotypeSummaries.size() + " phenotypeSummaries for " + subjectIds.size()
                + " subjects took " + timer.getTime() + "ms" );

        // List<PhenotypeSummaryValueObject> valueObjects = new ArrayList<PhenotypeSummaryValueObject>();
        Map<String, PhenotypeSummaryValueObject> summaryValueObjectsMap = new HashMap<String, PhenotypeSummaryValueObject>();
        new HashMap<String, Integer>();

        // convert PhenotypeSummaries to lighter PhenotypeValueObjects

        for ( PhenotypeSummary sum : phenotypeSummaries ) {

            String displaySummary = "";
            HashMap<String, Integer> phenoSummaryMap = new HashMap<String, Integer>();

            Set<String> keyArray = sum.getDbValueToSubjectSet().keySet();
            /**
             * Used the Color Brewer 2.0 system for coloring the chart Thanks for Cynthia Brewer, Mark Harrower and The
             * Pennsylvania State University
             */
            String[] colors = { "#b35806", "#31a354", "#636363", "#d8b365", "#2c7fb8", "#addd8e", "#7570b3", "#a6bddb" };
            int j = 3;
            int unknown = 0;
            int present = 0;
            int denominator = 0;
            for ( String key : keyArray ) {

                Integer size = sum.getDbValueToSubjectSet().get( key ).size();

                if ( sum.getValueType().equals( "HPONTOLOGY" ) ) {
                    if ( key.equals( "1" ) || key.equals( "Y" ) ) {
                        phenoSummaryMap.put( "Present", size );
                        present = size;
                        denominator = denominator + size;
                        displaySummary = displaySummary + " Present(" + size + ')';
                        displaySummary = "<span " + "style='color: " + colors[0] + "'" + ">" + displaySummary
                                + "</span>";
                    } else if ( key.equals( "0" ) || key.equals( "N" ) ) {
                        phenoSummaryMap.put( "Absent", size );
                        denominator = denominator + size;
                        displaySummary = displaySummary + " Absent(" + size + ')';
                        displaySummary = "<span " + "style='color: " + colors[1] + "'" + ">" + displaySummary
                                + "</span>";
                    } else if ( key.equals( "Unknown" ) ) {
                        unknown = size;
                    }
                } else if ( key.equals( "Unknown" ) ) {
                    unknown = size;
                } else {
                    phenoSummaryMap.put( key, size );
                    displaySummary = displaySummary + ' ' + key + " (" + size + ')';

                    // special case for phenotype with many values
                    if ( j < colors.length ) {
                        displaySummary = "<span " + "style='color:" + colors[j] + "'" + ">" + displaySummary
                                + "</span>";
                    } else {
                        // log.warn( "There are more '" + sum.getName() + "' phenotype values than there are colors" );
                        displaySummary = "<span " + "style='color:" + colors[colors.length - 1] + "'" + ">"
                                + displaySummary + "</span>";
                    }

                    j++;
                }

            }
            if ( unknown != 0 ) {
                phenoSummaryMap.put( "Unknown", unknown );
                displaySummary = displaySummary + " Unknown(" + unknown + ')';
                displaySummary = "<span " + "style='color: " + colors[2] + "'" + ">" + displaySummary + "</span>";
            } else {
                phenoSummaryMap.put( "Unknown", 0 );
                displaySummary = displaySummary + " Unknown(" + 0 + ')';
                displaySummary = "<span " + "style='color: " + colors[2] + "'" + ">" + displaySummary + "</span>";
            }

            PhenotypeSummaryValueObject pvo = new PhenotypeSummaryValueObject();

            pvo.setName( sum.getName() );
            pvo.setUri( sum.getUri() );
            pvo.setValueType( sum.getValueType() );
            pvo.setNeurocartaPhenotype( sum.isNeurocartaPhenotype() );

            pvo.setDisplaySummary( displaySummary );
            pvo.setPhenoSummaryMap( phenoSummaryMap );
            pvo.setPhenoSet( phenoSummaryMap.keySet() );
            // if (denominator!=0)
            pvo.setSortValue( present );
            summaryValueObjectsMap.put( sum.getName(), pvo );

        }

        return summaryValueObjectsMap;
    }

    /**
     * Create the Text to download the phenotype property values of given subject Ids
     * 
     * @param List of subject Ids
     * @return Long text of given subjects phenotype summary
     */
    @Override
    @RemoteMethod
    @Transactional
    public String getPhenotypeTextDownloadBySubjectIds( List<Long> subjectIds ) {

        StopWatch timer = new StopWatch();
        timer.start();

        List<SubjectValueObject> svoList = new ArrayList<SubjectValueObject>();

        Collection<Subject> subjectList = subjectDao.load( subjectIds );

        for ( Subject s : subjectList ) {
            SubjectValueObject svo = s.convertToValueObjectWithPhenotypes();
            svoList.add( svo );
        }

        StringBuffer text = new StringBuffer();

        HashMap<String, String> phenotypeFileColumnsMap = new HashMap<String, String>();

        for ( SubjectValueObject svo : svoList ) {

            for ( PhenotypeValueObject pvo : svo.getPhenotypes().values() ) {

                String columnName = pvo.getUri() != null ? ( pvo.getUri() + ":" + pvo.getName() ) : pvo.getName();

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

                if ( vo != null ) {
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
    public LabelValueObject addLabel( Collection<Long> subjectIds, LabelValueObject labelVO ) {
        LabelValueObject ret = null;
        Collection<Subject> subjects = subjectDao.load( subjectIds );
        Label label = labelDao.findOrCreate( labelVO );
        if ( label == null ) {
            return ret;
        }
        for ( Subject subject : subjects ) {
            subject.addLabel( label );
            subjectDao.update( subject );
        }
        ret = label.toValueObject();
        return ret;
    }

    @Override
    @RemoteMethod
    @Transactional
    public void removeLabel( Long id, LabelValueObject label ) {

        Subject subject = subjectDao.load( id );
        Label labelEntity = labelDao.load( label.getId() );
        subject.removeLabel( labelEntity );
        subjectDao.update( subject );
    }

    @Override
    @RemoteMethod
    @Transactional
    public void removeLabel( Collection<Long> subjectIds, LabelValueObject label ) {

        for ( Long subjectId : subjectIds ) {
            removeLabel( subjectId, label );
        }
    }

    @Override
    @RemoteMethod
    @Transactional
    public List<LabelValueObject> suggestLabels( SuggestionContext suggestionContext ) {
        Collection<Label> labels;
        if ( suggestionContext == null || suggestionContext.getActiveProjectIds().size() == 0 ) {
            labels = labelDao.getSubjectLabels();
        } else {
            labels = labelDao.getSubjectLabelsByProjectId( suggestionContext.getActiveProjectIds().iterator().next() );
        }
        List<LabelValueObject> vos = new ArrayList<LabelValueObject>();
        for ( Label label : labels ) {
            vos.add( label.toValueObject() );
        }
        return vos;
    }

    @Override
    @Transactional
    @RemoteMethod
    public Collection<Label> getSubjectLabels( Collection<Long> subjectIds ) {
        // Collection<Label labelEntity = labelDao.load( labelIds );
        Collection<Label> labels = new ArrayList<Label>();

        for ( Long subjectId : subjectIds ) {
            Collection<Label> subjectLabels = labelDao.getSubjectLabelsBySubjectId( subjectId );
            if ( subjectLabels == null ) {
                //
            } else {
                for ( Label subjectLabel : subjectLabels ) {
                    labels.add( subjectLabel );
                }
            }
        }
        return labels;
    }

    /**
     * @param variants
     * @return Map<Label.name, Collection<Subject.patientID>>
     */
    @Override
    public Map<String, Collection<String>> groupPatientIdsBySubjectLabel( Collection<Subject> subjects ) {
        Map<String, Collection<String>> labelPatientId = new HashMap<>();
        for ( Subject subject : subjects ) {

            // organize labels
            for ( Label label : subject.getLabels() ) {
                if ( !labelPatientId.containsKey( label.getName() ) ) {
                    labelPatientId.put( label.getName(), new HashSet<String>() );
                }
                labelPatientId.get( label.getName() ).add( subject.getPatientId() );
            }

            // create a fake label to capture those Subjects with no labels
            if ( subject.getLabels().size() == 0 ) {
                String labelName = "NO_LABEL";
                if ( !labelPatientId.containsKey( labelName ) ) {
                    labelPatientId.put( labelName, new HashSet<String>() );
                }
                labelPatientId.get( labelName ).add( subject.getPatientId() );
            }
        }
        return labelPatientId;
    }

    /**
     * @param variants
     * @return Map<Label.name, Collection<Subject.patientID>>
     */
    @Override
    public Map<String, Collection<Long>> groupSubjectIdsBySubjectLabel( Collection<Subject> subjects ) {
        Map<String, Collection<Long>> labelSubjectId = new HashMap<>();
        for ( Subject subject : subjects ) {

            // organize labels
            for ( Label label : subject.getLabels() ) {
                if ( !labelSubjectId.containsKey( label.getName() ) ) {
                    labelSubjectId.put( label.getName(), new HashSet<Long>() );
                }
                labelSubjectId.get( label.getName() ).add( subject.getId() );
            }

            // create a fake label to capture those Subjects with no labels
            if ( subject.getLabels().size() == 0 ) {
                String labelName = "NO_LABEL";
                if ( !labelSubjectId.containsKey( labelName ) ) {
                    labelSubjectId.put( labelName, new HashSet<Long>() );
                }
                labelSubjectId.get( labelName ).add( subject.getId() );
            }
        }
        return labelSubjectId;
    }

    @Override
    @RemoteMethod
    @Transactional
    public StringMatrix<String, String> getPhenotypeBySubjectIds( Collection<Long> subjectIds, boolean removeEmpty ) {

        List<String> columnNames = new ArrayList<>();
        List<String> rowNames = new ArrayList<>();

        List<SubjectValueObject> svoList = new ArrayList<SubjectValueObject>();

        Collection<Subject> subjectList = subjectDao.load( subjectIds );

        HashMap<String, String> phenotypeFileColumnsMap = new HashMap<String, String>();

        for ( Subject s : subjectList ) {
            SubjectValueObject svo = s.convertToValueObjectWithPhenotypes();
            if ( removeEmpty && svo.getPhenotypes().size() == 0 ) {
                continue;
            }

            svoList.add( svo );
            rowNames.add( svo.getPatientId() );

            for ( PhenotypeValueObject pvo : svo.getPhenotypes().values() ) {

                String columnName = pvo.getUri() != null ? ( pvo.getUri() + ":" + pvo.getName() ) : pvo.getName();

                if ( !phenotypeFileColumnsMap.containsKey( columnName ) ) {
                    phenotypeFileColumnsMap.put( columnName, pvo.getName() );
                    columnNames.add( columnName );
                }

            }

        }

        StringMatrix<String, String> matrix = new StringMatrix<String, String>( rowNames.size(), columnNames.size() );
        matrix.setColumnNames( columnNames );
        matrix.setRowNames( rowNames );

        int i = 0;
        for ( SubjectValueObject svo : svoList ) {
            Map<String, PhenotypeValueObject> phenotypeMap = svo.getPhenotypes();

            int j = 0;
            for ( String columnName : columnNames ) {

                PhenotypeValueObject vo = phenotypeMap.get( phenotypeFileColumnsMap.get( columnName ) );

                if ( vo != null ) {
                    matrix.set( i, j, vo.getDbValue() );
                } else {
                    matrix.set( i, j, "" );
                }

                j++;
            }

            i++;
        }

        return matrix;
    }

    @Override
    @Transactional
    public boolean hasLabel( Long subjectId, Long labelId ) {
        Subject subject = subjectDao.load( subjectId );
        for ( Label label : subject.getLabels() ) {
            if ( label.getId() == labelId ) {
                return true;
            }
        }
        return false;
    }
}
