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
package ubc.pavlab.aspiredb.server.util;

import gemma.gsec.SecurityService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.CNVDao;
import ubc.pavlab.aspiredb.server.dao.IndelDao;
import ubc.pavlab.aspiredb.server.dao.InversionDao;
import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SNVDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.TranslocationDao;
import ubc.pavlab.aspiredb.server.dao.Variant2SpecialVariantOverlapDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Indel;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.SNV;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

/**
 * Class for tests to use to create and remove persistent objects This class will become unnecessary one we have
 * services that do these operations. Using this class in the meantime to get some basic test coverage.
 * 
 * @author cmcdonald
 * @version $Id: PersistentTestObjectHelperImpl.java,v 1.18 2013/07/09 21:25:44 cmcdonald Exp $
 */
@Service
public class PersistentTestObjectHelperImpl implements PersistentTestObjectHelper {

    @Autowired
    private IndelDao indelDao;

    @Autowired
    private LabelDao labelDao;

    @Autowired
    private TranslocationDao translocationDao;

    @Autowired
    private CNVDao cnvDao;

    @Autowired
    private SNVDao snvDao;

    @Autowired
    private InversionDao inversionDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private Variant2SpecialVariantOverlapDao variant2SpecialVariantOverlapDao;

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    private PhenotypeUtil phenotypeUtil;

    @Autowired
    private VariantDao variantDao;

    public PersistentTestObjectHelperImpl() {
    }

    @Override
    public SNV createDetachedTestSNVObject() {

        GenomicLocation genomicLocation = new GenomicLocation( "X", 56650362, 56729961 );

        SNV snv = new SNV();
        snv.setLocation( genomicLocation );

        snv.setReferenceBase( "referenceBase" );
        snv.setObservedBase( "observedBase" );
        snv.setDbSNPID( "567id" );

        List<Characteristic> characteristics = new ArrayList<Characteristic>();
        characteristics.add( new Characteristic( "BENIGN", "YES" ) );

        snv.setCharacteristics( characteristics );

        return snv;
    }

    @Override
    @Transactional
    public SNV createPersistentTestSNVObject() {

        return snvDao.create( createDetachedTestSNVObject() );
    }

    @Override
    public Indel createDetachedTestIndelObject() {

        GenomicLocation genomicLocation = new GenomicLocation( "X", 56650362, 56729961 );

        Indel indel = new Indel();
        indel.setLocation( genomicLocation );

        indel.setIndelLength( 13214124 );

        List<Characteristic> characteristics = new ArrayList<Characteristic>();
        characteristics.add( new Characteristic( "BENIGN", "YES" ) );

        indel.setCharacteristics( characteristics );

        return indel;
    }

    @Override
    @Transactional
    public Indel createPersistentTestIndelObject() {

        return indelDao.create( createDetachedTestIndelObject() );
    }

    @Override
    @Transactional
    public void removeSubject( Subject subject ) {
        subjectDao.remove( subject );
    }

    @Override
    @Transactional
    public void removeVariant( Variant variant ) {
        variantDao.remove( variant );
    }

    @Override
    @Transactional
    public void removeLabel( Label label ) {
        labelDao.remove( label );
    }

    @Override
    @Transactional
    public void removePhenotype( Phenotype phenotype ) {
        phenotypeDao.remove( phenotype );
    }

    @Override
    public CNV createDetachedTestCNVObject() {

        GenomicLocation genomicLocation = new GenomicLocation( "X", 56650362, 56729961 );

        CNV cnv = new CNV();
        cnv.setLocation( genomicLocation );
        cnv.setCopyNumber( 2 );

        List<GenomicLocation> targetLocations = new ArrayList<GenomicLocation>();

        GenomicLocation targLocation = new GenomicLocation( "11", 1123232, 23433443 );

        GenomicLocation targLocation2 = new GenomicLocation( "12", 12232, 134456 );

        targetLocations.add( targLocation );
        targetLocations.add( targLocation2 );

        cnv.setTargetLocations( targetLocations );

        cnv.setType( CnvType.valueOf( "LOSS" ) );

        List<Characteristic> characteristics = new ArrayList<Characteristic>();
        characteristics.add( new Characteristic( "BENIGN", "YES" ) );

        cnv.setCharacteristics( characteristics );

        return cnv;
    }

    public Phenotype createDetachedPhenotypeObject( String name, String uri, String valueType, String value ) {

        Phenotype p = new Phenotype();

        p.setName( name );
        p.setValueType( valueType );
        p.setUri( uri );
        p.setValue( value );

        return p;

    }

    @Override
    @Transactional
    public CNV createPersistentTestCNVObject() {
        return cnvDao.create( createDetachedTestCNVObject() );
    }

    public CNV createDetachedCNVObject() {

        GenomicLocation genomicLocation = new GenomicLocation( "X", 56650362, 56729961 );

        CNV cnv = new CNV();
        cnv.setLocation( genomicLocation );
        cnv.setCopyNumber( 2 );

        List<GenomicLocation> targetLocations = new ArrayList<GenomicLocation>();

        GenomicLocation targLocation = new GenomicLocation( "11", 56650362, 56729961 );

        targetLocations.add( targLocation );

        cnv.setTargetLocations( targetLocations );

        cnv.setType( CnvType.valueOf( "LOSS" ) );

        List<Characteristic> characteristics = new ArrayList<Characteristic>();
        characteristics.add( new Characteristic( "BENIGN", "YES" ) );

        cnv.setCharacteristics( characteristics );

        return cnv;
    }

    @Override
    public Subject createDetachedIndividualObject( String patientId ) {

        Subject individual = new Subject();

        individual.setPatientId( patientId );

        return individual;
    }

    @Override
    @Transactional
    public Subject createPersistentTestIndividualObject( String patientId ) {

        Subject individual = createDetachedIndividualObject( patientId );

        return subjectDao.create( individual );
    }

    @Override
    @Transactional
    public Phenotype createPersistentTestPhenotypeObject( String name, String uri, String valueType, String value ) {

        Phenotype p = createDetachedPhenotypeObject( name, uri, valueType, value );

        return phenotypeDao.create( p );
    }

    @Override
    @Transactional
    public Subject createPersistentTestSubjectObjectWithCNV( String patientId ) {

        Subject individual = createPersistentTestIndividualObject( patientId );

        individual.addVariant( createPersistentTestCNVObject() );

        return individual;
    }

    @Override
    @Transactional
    public Subject createPersistentTestSubjectObjectWithHPOntologyPhenotypes( String patientId ) {

        Subject subject = createPersistentTestIndividualObject( patientId );

        subject.addPhenotype( createPersistentTestPhenotypeObject( "Abnormality of abnormalities", "uri1",
                "HPONTOLOGY", "somevalue" ) );
        subject.addPhenotype( createPersistentTestPhenotypeObject( "Abnormality of the mind", "uri2", "HPONTOLOGY",
                "somevalue" ) );
        subject.addPhenotype( createPersistentTestPhenotypeObject( "Abnormality of society", "uri3", "HPONTOLOGY",
                "somevalue" ) );
        subject.addPhenotype( createPersistentTestPhenotypeObject( "CustomPhenotype", "uri4", "CUSTOM", "somevalue" ) );

        return subject;

    }

    @Override
    @Transactional
    public Subject addSubjectToProject( Subject s, Project p ) {

        s.getProjects().add( p );

        subjectDao.update( s );

        return s;

    }

    @Override
    @Transactional
    public Subject createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( String patientId,
            String phenName, String phenUri, String phenValue ) {

        Subject subject = createPersistentTestIndividualObject( patientId );

        subject.addPhenotype( createPersistentTestPhenotypeObject( phenName, phenUri, "HPONTOLOGY", phenValue ) );

        return subject;

    }

    @Override
    @Transactional
    public Project createPersistentProject( Project p ) {
        Project pp = projectDao.create( p );
        securityService.makePrivate( pp );
        return pp;
    }

    @Override
    @Transactional
    public Label createPersistentLabel( Label label ) {
        return labelDao.create( label );
    }

    @Override
    @Transactional
    public List<Subject> getSubjectsForProject( Project p ) {
        return p.getSubjects();
    }

    @Override
    @Transactional
    public Collection<LabelValueObject> getLabelsForSubject( Long subjectId ) {

        return Label.toValueObjects( labelDao.getSubjectLabelsBySubjectId( subjectId ) );
    }

    @Override
    @Transactional
    public Collection<LabelValueObject> getLabelsForVariant( Long variantId ) {

        return Label.toValueObjects( labelDao.getVariantLabelsByVariantId( variantId ) );
    }

    @Override
    @Transactional
    public void deleteProject( String projectName ) {

        Project project = projectDao.findByProjectName( projectName );

        if ( project == null ) {
            return;
        }

        variant2SpecialVariantOverlapDao.deleteByOverlapProjectId( project.getId() );
        for ( Subject s : project.getSubjects() ) {
            try {

                for ( Phenotype p : s.getPhenotypes() ) {
                    p.setSubject( null );
                }

                for ( Variant v : s.getVariants() ) {
                    v.setSubject( null );
                }

                subjectDao.remove( s );

            } catch ( Exception e ) {
                // e.printStackTrace();
            }
        }

        try {
            projectDao.remove( project );
        } catch ( Exception e ) {
            // noop
        }
    }

}