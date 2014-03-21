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

package ubc.pavlab.aspiredb.server.project;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.Variant2SpecialVariantOverlapDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantOverlap;
import ubc.pavlab.aspiredb.server.service.QueryService;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;

public class Project2SpecialProjectOverlapTest{// extends BaseSpringContextTest {

    @Autowired
    private ProjectManager projectManager;

    @Autowired
    private QueryService queryService;

    @Autowired
    private Variant2SpecialVariantOverlapDao variant2SpecialVariantOverlapDao;

    @Autowired
    ProjectDao projectDao;

    @Autowired
    VariantDao variantDao;
    
    @Autowired
    SubjectDao subjectDao;
    
    @Autowired
    PersistentTestObjectHelper helper;

    final String patientId = RandomStringUtils.randomAlphabetic( 5 );
    final String projectName = RandomStringUtils.randomAlphabetic( 7 );

    final String userVariantId = RandomStringUtils.randomAlphabetic( 5 );
    
    final String userVariantId2 = RandomStringUtils.randomAlphabetic( 5 );
    
    final String userVariantIdToTestOverlapPercentage = RandomStringUtils.randomAlphabetic( 5 );

    final String overlapVariantId1 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId2 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId3 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId4 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId5 = RandomStringUtils.randomAlphabetic( 5 );

    final String patientIdWithOverlap = RandomStringUtils.randomAlphabetic( 5 );
    final String projectNameWithOverlap = "DGV";

   

   
   //@Test
    public void testPopulateSpecialProjectOverlap() {

       // super.runAsAdmin();
        
        
        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( getCNV( "X", 3, 234, userVariantId, patientId ) );
        
        cnvList.add( getCNV( "X", 1, 5, userVariantId2, patientId ) );        
        
        cnvList.add( getCNV( "2", 123, 235, RandomStringUtils.randomAlphabetic( 5 ), patientId ) );
        cnvList.add( getCNV( "3", 12, 236, RandomStringUtils.randomAlphabetic( 5 ), patientId ) );

        try {

            projectManager.addSubjectVariantsToProject( projectName, true, cnvList );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        ArrayList<VariantValueObject> cnvListWithOverlap = new ArrayList<VariantValueObject>();

        // will overlap, by 231
        cnvListWithOverlap.add( getCNV( "X", 3, 234, overlapVariantId1, patientIdWithOverlap ) );

        // will overlap by 229
        cnvListWithOverlap.add( getCNV( "X", 5, 237, overlapVariantId2, patientIdWithOverlap ) );

        // will overlap by2
        cnvListWithOverlap.add( getCNV( "X", 1, 5, overlapVariantId3, patientIdWithOverlap ) );

        // will overlap by 2
        cnvListWithOverlap.add( getCNV( "X", 5, 7, overlapVariantId4, patientIdWithOverlap ) );
        
        //will overlap by 134, we will use this one to test the percentage overlap
        cnvListWithOverlap.add( getCNV( "X", 100, 900, overlapVariantId5, patientIdWithOverlap ) );

        cnvListWithOverlap.add( getCNV( "X", 900, 1600, null, patientIdWithOverlap ) );

        cnvListWithOverlap.add( getCNV( "Y", 3, 234, null, patientIdWithOverlap ) );

        try {
            
            helper.deleteProject( "DGV" );
            
            projectManager.addSubjectVariantsToSpecialProject( projectNameWithOverlap, true, cnvListWithOverlap, false );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        

        Collection<Project> overlapProjects = projectDao.getSpecialOverlapProjects();
        Collection<Long> overlapProjectIds = new ArrayList<Long>();
        
        for (Project p: overlapProjects){
            
            overlapProjectIds.add( p.getId() );            
            
        }
        
                
        try {
            
            projectManager.populateProjectToProjectOverlap( projectName, projectNameWithOverlap );
       

        } catch ( Exception e ) {

            fail( "projectManager.populateSpecialProjectOverlap threw an exception:" + e.toString() );

        }
        
       

        Project projectToPopulate = projectDao.findByProjectName( projectName );

        Project specialProject = projectDao.findByProjectName( projectNameWithOverlap );

        ProjectFilterConfig projectToPopulateFilterConfig = getProjectFilterConfigById( projectToPopulate );

        HashSet<AspireDbFilterConfig> projSet = new HashSet<AspireDbFilterConfig>();
        projSet.add( projectToPopulateFilterConfig );

        BoundedList<VariantValueObject> projToPopulateVvos = null;

        try {

            projToPopulateVvos = queryService.queryVariants( projSet );
        } catch ( Exception e ) {

            fail( "queryService.queryVariants threw an exception" );

        }

        assertEquals(4, projToPopulateVvos.getItems().size() );

        
        List<VariantValueObject> vvos = projToPopulateVvos.getItems();

        VariantValueObject vvo = new VariantValueObject();

        for ( VariantValueObject v : vvos ) {

            if ( v.getUserVariantId() != null && v.getUserVariantId().equals( userVariantId ) ) {
                vvo = v;
                break;
            }

        }

        List<Long> specialProjectList = new ArrayList<Long>();

        specialProjectList.add( specialProject.getId() );

        Collection<Variant2SpecialVariantOverlap> infos = variant2SpecialVariantOverlapDao.loadByVariantId( vvo.getId(),
                specialProjectList );

        Variant v = variantDao.load( vvo.getId() );

        assertEquals( v.getUserVariantId(), userVariantId );

        assertEquals( v.getLocation().getChromosome(), "X" );

        assertEquals( 5, infos.size() );

        for ( Variant2SpecialVariantOverlap vInfo : infos ) {

            Variant specialVariant = variantDao.load( vInfo.getOverlapSpecialVariantId() );

            assertTrue( doesOverlap( v, specialVariant ) );

            // i just arbitrarily gave the overlapping variants userVariantIds and set the nonoverlapping ones to null,
            // only for the purposes of this test
            if ( specialVariant.getUserVariantId() == null ) {
                fail( "overlap special variant has null userVariantId, test failed " );
            } else if ( specialVariant.getUserVariantId().equals( overlapVariantId1 ) ) {// check the accuracy of the
                                                                                         // overlap length

                assertEquals(231, vInfo.getOverlap().intValue());
                assertEquals( vInfo.getOverlapProjectId(), specialProject.getId() );
                
                

            } else if ( specialVariant.getUserVariantId().equals( overlapVariantId2 ) ) {

                assertEquals(229, vInfo.getOverlap().intValue());
                assertEquals( vInfo.getOverlapProjectId(), specialProject.getId() );

            } else if ( specialVariant.getUserVariantId().equals( overlapVariantId3 ) ) {
                assertEquals(2, vInfo.getOverlap().intValue());
                assertEquals( vInfo.getOverlapProjectId(), specialProject.getId() );
            } else if ( specialVariant.getUserVariantId().equals( overlapVariantId4 ) ) {
                assertEquals(2, vInfo.getOverlap().intValue());
                assertEquals( vInfo.getOverlapProjectId(), specialProject.getId() );
            } else if ( specialVariant.getUserVariantId().equals( overlapVariantId5 ) ) {
                assertEquals(58,vInfo.getOverlapPercentage().intValue());
                
                assertEquals(17,vInfo.getOverlappedOverlapPercentage().intValue());
            }

        }
        
        helper.deleteProject(projectName);
        helper.deleteProject(projectNameWithOverlap);
        

    }

    

    private boolean doesOverlap( Variant variant, Variant specialVariant ) {

        if ( !variant.getLocation().getChromosome().equals( specialVariant.getLocation().getChromosome() ) ) {
            return false;
        }

        int start = Math.max( variant.getLocation().getStart(), specialVariant.getLocation().getStart() );
        int end = Math.min( variant.getLocation().getEnd(), specialVariant.getLocation().getEnd() );

        // genius
        if ( start < end ) {
            return true;
        } else {
            return false;
        }

    }

    private ProjectFilterConfig getProjectFilterConfigById( Project p ) {

        ProjectFilterConfig projectFilterConfig = new ProjectFilterConfig();

        ArrayList<Long> projectIds = new ArrayList<Long>();

        projectIds.add( p.getId() );

        projectFilterConfig.setProjectIds( projectIds );

        return projectFilterConfig;

    }

    private CNVValueObject getCNV( String chrom, int baseStart, int baseEnd, String userVariantId, String patientId ) {

        CharacteristicValueObject cvo = new CharacteristicValueObject();

        cvo.setKey( "testChar" );
        cvo.setValue( "testcharvalue" );

        Map<String, CharacteristicValueObject> charMap = new HashMap<String, CharacteristicValueObject>();
        charMap.put( cvo.getKey(), cvo );

        CNVValueObject cnv = new CNVValueObject();

        cnv.setCharacteristics( charMap );
        cnv.setType( "GAIN" );

        cnv.setUserVariantId( userVariantId );

        GenomicRange gr = new GenomicRange();
        gr.setChromosome( chrom );
        gr.setBaseStart( baseStart );
        gr.setBaseEnd( baseEnd );
        cnv.setGenomicRange( gr );

        cnv.setPatientId( patientId );

        return cnv;

    }

}