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
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.Variant2SpecialVariantInfoDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantInfo;
import ubc.pavlab.aspiredb.server.service.QueryService;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;

public class Project2SpecialProjectOverlapTest extends BaseSpringContextTest {
            
    @Autowired
    private ProjectManager projectManager;
    
    @Autowired
    private QueryService queryService;
    
    @Autowired
    private Variant2SpecialVariantInfoDao variant2SpecialVariantInfoDao;
    
    @Autowired
    ProjectDao projectDao;
    
    @Autowired
    VariantDao variantDao;
    
    
    final String patientId = RandomStringUtils.randomAlphabetic( 5 );
    final String projectId = RandomStringUtils.randomAlphabetic( 5 );
    
    final String userVariantId = RandomStringUtils.randomAlphabetic( 5 );
    
    final String overlapVariantId1 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId2 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId3 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId4 = RandomStringUtils.randomAlphabetic( 5 );
    
    final String patientIdSpecial = RandomStringUtils.randomAlphabetic( 5 );
    final String projectIdSpecial = RandomStringUtils.randomAlphabetic( 5 );
    
    
    @Before
    public void setup() throws Exception {   
        
        super.runAsAdmin();
        
        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        cnvList.add( getCNV("X", 3, 234, userVariantId,patientId) );
        
        try{
        
            projectManager.addSubjectVariantsToProject( projectId, true, cnvList );
        
        }catch(Exception e){
            
            fail("projectManager.addSubjectVariantsToProject threw an exception");
            
        }        
        
        ArrayList<VariantValueObject> cnvListSpecial = new ArrayList<VariantValueObject>();
        
        
        
        //will overlap, by 231
        cnvListSpecial.add( getCNV("X", 3, 234, overlapVariantId1,patientIdSpecial) );
        
        //will overlap by 229
        cnvListSpecial.add( getCNV("X", 5, 237, overlapVariantId2,patientIdSpecial) );
        
      //will overlap by2
        cnvListSpecial.add( getCNV("X", 1, 5,overlapVariantId3,patientIdSpecial) );
        
      //will overlap by 2
        cnvListSpecial.add( getCNV("X", 5, 7, overlapVariantId4,patientIdSpecial) );
        
        cnvListSpecial.add( getCNV("X", 400, 500, null,patientIdSpecial) );
        
        cnvListSpecial.add( getCNV("Y", 3, 234, null,patientIdSpecial) );        
        
        try{
        
            projectManager.addSubjectVariantsToProject( projectIdSpecial, true, cnvListSpecial );
        
        }catch(Exception e){
            
            fail("projectManager.addSubjectVariantsToProject threw an exception");
            
        }
        
        
        
       
        
            

    }
    
    @Test
    public void testPopulateSpecialProjectOverlap(){
        
        super.runAsAdmin();
        
        try{
        
        projectManager.populateSpecialProjectOverlap( projectId, projectIdSpecial );
        
        }catch(Exception e){
            
            fail("projectManager.populateSpecialProjectOverlap threw an exception");
            
        }
        
        Project projectToPopulate = projectDao.findByProjectName( projectId );
        
        ProjectFilterConfig projectToPopulateFilterConfig = getProjectFilterConfigById(projectToPopulate);
        
       
        
        HashSet<AspireDbFilterConfig> projSet = new HashSet<AspireDbFilterConfig>();
        projSet.add( projectToPopulateFilterConfig );
        
        BoundedList<VariantValueObject> projToPopulateVvos=null;
        
        try{
        
        //this should only be one variant in this project        
            projToPopulateVvos = queryService.queryVariants( projSet );
        }catch(Exception e){
            
            fail("queryService.queryVariants threw an exception");
            
        }
        
        assertEquals(projToPopulateVvos.getItems().size(),1);
        
        //the test variant
        VariantValueObject vvo = projToPopulateVvos.getItems().iterator().next();
        
        Collection<Variant2SpecialVariantInfo> infos = variant2SpecialVariantInfoDao.loadByVariantId( vvo.getId() );
        
        Variant v= variantDao.load( vvo.getId() );
        
        
        assertEquals(v.getUserVariantId(), userVariantId);
        
        assertEquals(v.getLocation().getChromosome(), "X");
        
        assertEquals(infos.size(),4);
        
        for (Variant2SpecialVariantInfo vInfo: infos){
            
            Variant specialVariant = variantDao.load( vInfo.getOverlapSpecialVariantId() );
            
            assertTrue(doesOverlap(v,specialVariant));
            
            //i just arbitrarily gave the overlapping variants userVariantIds and set the nonoverlapping ones to null, only for the purposes of this test
            if (specialVariant.getUserVariantId()==null){
                fail("overlap special variant has null userVariantId, test failed ");
            }else  if(specialVariant.getUserVariantId().equals( overlapVariantId1 )){//check the accuracy of the overlap length
                
                assertEquals(vInfo.getOverlap().intValue(),231);
                
            }else  if(specialVariant.getUserVariantId().equals( overlapVariantId2 )){
                
                assertEquals(vInfo.getOverlap().intValue(),229);
                
            }else  if(specialVariant.getUserVariantId().equals( overlapVariantId3 )){
                assertEquals(vInfo.getOverlap().intValue(),2);
            }else  if(specialVariant.getUserVariantId().equals( overlapVariantId4 )){
                assertEquals(vInfo.getOverlap().intValue(),2);
            }
            
        }
        
    }
    
    private boolean doesOverlap(Variant variant, Variant specialVariant){
        
        if (!variant.getLocation().getChromosome().equals( specialVariant.getLocation().getChromosome() )){
            return false;
        }
        
        int start = Math.max( variant.getLocation().getStart(), specialVariant.getLocation().getStart() );
        int end = Math.min( variant.getLocation().getEnd(), specialVariant.getLocation().getEnd() );
       
       //genius 
       if (start< end){
           return true;
       }else{                   
           return false;
       }
        
    }
    
    private ProjectFilterConfig getProjectFilterConfigById(Project p){
        
        ProjectFilterConfig projectFilterConfig = new ProjectFilterConfig();
        
        ArrayList<Long> projectIds = new ArrayList<Long>();
        
        projectIds.add(p.getId() );
        
        projectFilterConfig.setProjectIds( projectIds );        
        
        return projectFilterConfig;        
        
    }
    
    private CNVValueObject getCNV(String chrom, int baseStart, int baseEnd, String userVariantId,String patientId){
        
        CharacteristicValueObject cvo = new CharacteristicValueObject();
        
        cvo.setKey( "testChar" );
        cvo.setValue( "testcharvalue" );
        
        Map<String, CharacteristicValueObject> charMap = new HashMap<String,CharacteristicValueObject>();
        charMap.put(cvo.getKey(), cvo);
        
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