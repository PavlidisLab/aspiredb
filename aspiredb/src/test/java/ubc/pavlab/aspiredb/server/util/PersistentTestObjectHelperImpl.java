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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.CNVDao;
import ubc.pavlab.aspiredb.server.dao.IndelDao;
import ubc.pavlab.aspiredb.server.dao.InversionDao;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SNVDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.TranslocationDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Indel;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.SNV;
import ubc.pavlab.aspiredb.server.model.Subject;

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
    IndelDao indelDao;

    @Autowired
    TranslocationDao translocationDao;

    @Autowired
    CNVDao cnvDao;

    @Autowired
    SNVDao snvDao;

    @Autowired
    InversionDao inversionDao;

    @Autowired
    SubjectDao individualDao;

    @Autowired
    ProjectDao projectDao;

    @Autowired
    PhenotypeDao phenotypeDao;

    @Autowired
    PhenotypeUtil phenotypeUtil;

    public PersistentTestObjectHelperImpl() {
    }


    public SNV createDetachedTestSNVObject() {

        GenomicLocation genomicLocation = new GenomicLocation();
        genomicLocation.setChromosome( "X" );
        genomicLocation.setStart( 56650362 );
        genomicLocation.setEnd( 56729961 );
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

    @Transactional
    public SNV createPersistentTestSNVObject() {

        return snvDao.create( createDetachedTestSNVObject() );
    }

    public Indel createDetachedTestIndelObject() {

        GenomicLocation genomicLocation = new GenomicLocation();
        genomicLocation.setChromosome( "X" );
        genomicLocation.setStart( 56650362 );
        genomicLocation.setEnd( 56729961 );
        Indel indel = new Indel();
        indel.setLocation( genomicLocation );

        indel.setIndelLength( 13214124 );

        List<Characteristic> characteristics = new ArrayList<Characteristic>();
        characteristics.add( new Characteristic( "BENIGN", "YES" ) );

        indel.setCharacteristics( characteristics );

        return indel;
    }

    @Transactional
    public Indel createPersistentTestIndelObject() {

        return indelDao.create( createDetachedTestIndelObject() );
    }

    public CNV createDetachedTestCNVObject() {

        GenomicLocation genomicLocation = new GenomicLocation();
        genomicLocation.setChromosome( "X" );
        genomicLocation.setStart( 56650362 );
        genomicLocation.setEnd( 56729961 );
        CNV cnv = new CNV();
        cnv.setLocation( genomicLocation );
        cnv.setCopyNumber( 2 );

        List<GenomicLocation> targetLocations = new ArrayList<GenomicLocation>();

        GenomicLocation targLocation = new GenomicLocation();
        targLocation.setChromosome( "11" );
        targLocation.setStart( 1123232 );
        targLocation.setEnd( 23433443 );

        GenomicLocation targLocation2 = new GenomicLocation();
        targLocation.setChromosome( "12" );
        targLocation.setStart( 12232 );
        targLocation.setEnd( 134456 );

        targetLocations.add( targLocation );
        targetLocations.add( targLocation2 );

        cnv.setTargetLocations( targetLocations );

        cnv.setType( CnvType.valueOf( "LOSS" ) );

        List<Characteristic> characteristics = new ArrayList<Characteristic>();
        characteristics.add( new Characteristic( "BENIGN", "YES" ) );

        cnv.setCharacteristics( characteristics );

        return cnv;
    }
    
    public Phenotype createDetachedPhenotypeObject(String name, String uri, String valueType, String value){
        
        Phenotype p = new Phenotype();
        
        p.setName( name );
        p.setValueType(valueType );
        p.setUri( uri );
        p.setValue( value );
        
        return p;
        
    }

    @Transactional
    public CNV createPersistentTestCNVObject() {
        return cnvDao.create( createDetachedTestCNVObject() );
    }

    public CNV createDetachedCNVObject() {

        GenomicLocation genomicLocation = new GenomicLocation();
        genomicLocation.setChromosome( "X" );
        genomicLocation.setStart( 56650362 );
        genomicLocation.setEnd( 56729961 );

        CNV cnv = new CNV();
        cnv.setLocation( genomicLocation );
        cnv.setCopyNumber( 2 );

        List<GenomicLocation> targetLocations = new ArrayList<GenomicLocation>();

        GenomicLocation targLocation = new GenomicLocation();
        targLocation.setChromosome( "11" );
        targLocation.setStart( 56650362 );
        targLocation.setEnd( 56729961 );

        targetLocations.add( targLocation );

        cnv.setTargetLocations( targetLocations );

        cnv.setType( CnvType.valueOf( "LOSS" ) );

        List<Characteristic> characteristics = new ArrayList<Characteristic>();
        characteristics.add( new Characteristic( "BENIGN", "YES" ) );

        cnv.setCharacteristics( characteristics );

        return cnv;
    }

    public Subject createDetachedIndividualObject( String patientId ) {

        Subject individual = new Subject();

        individual.setPatientId( patientId );

        return individual;
    }

    @Transactional
    public Subject createPersistentTestIndividualObject( String patientId ) {

        Subject individual = createDetachedIndividualObject( patientId );

        return individualDao.create( individual );
    }
    
    @Transactional
    public Phenotype createPersistentTestPhenotypeObject( String name, String uri, String valueType, String value ) {

        Phenotype p = createDetachedPhenotypeObject( name,uri, valueType, value );

        return phenotypeDao.create( p );
    }

    @Transactional
    public Subject createPersistentTestSubjectObjectWithCNV( String patientId ) {

        Subject individual = createPersistentTestIndividualObject( patientId );
        
        individual.addVariant( createPersistentTestCNVObject() );

        return individual;
    }
    
    @Transactional
    public Subject createPersistentTestSubjectObjectWithHPOntologyPhenotypes( String patientId ){
        
        Subject subject = createPersistentTestIndividualObject( patientId );
        
        subject.addPhenotype( createPersistentTestPhenotypeObject("Abnormality of abnormalities", "uri1","HPONTOLOGY","somevalue") );
        subject.addPhenotype( createPersistentTestPhenotypeObject("Abnormality of the mind", "uri2","HPONTOLOGY","somevalue") );
        subject.addPhenotype( createPersistentTestPhenotypeObject("Abnormality of society", "uri3","HPONTOLOGY","somevalue") );
        subject.addPhenotype( createPersistentTestPhenotypeObject("CustomPhenotype", "uri4","CUSTOM","somevalue") );
        

        return subject;
        
    }
    
    @Transactional
    public Subject createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( String patientId, String phenName, String phenUri, String phenValue ){
        
        Subject subject = createPersistentTestIndividualObject( patientId );
        
        subject.addPhenotype( createPersistentTestPhenotypeObject(phenName, phenUri,"HPONTOLOGY",phenValue) );

        return subject;
        
    }

    @Transactional
    public Project createPersistentProject( Project p ) {
        return projectDao.create( p );
    }
}