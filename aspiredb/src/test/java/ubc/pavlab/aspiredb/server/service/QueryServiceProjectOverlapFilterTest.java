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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantOverlap;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.server.service.QueryService;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.NumericValue;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.DoesOverlapWithXProperty;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.OverlapBasesProperty;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectOverlapFilterConfig;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

public class QueryServiceProjectOverlapFilterTest extends BaseSpringContextTest {

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
    final String projectName = RandomStringUtils.randomAlphabetic( 5 );

    final String userVariantId = RandomStringUtils.randomAlphabetic( 5 );
    
    final String userVariantId2 = RandomStringUtils.randomAlphabetic( 5 );
    
    final String userVariantIdToTestOverlapPercentage = RandomStringUtils.randomAlphabetic( 5 );

    final String overlapVariantId1 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId2 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId3 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId4 = RandomStringUtils.randomAlphabetic( 5 );
    final String overlapVariantId5 = RandomStringUtils.randomAlphabetic( 5 );

    final String patientIdWithOverlap = RandomStringUtils.randomAlphabetic( 5 );
    final String projectNameWithOverlap = RandomStringUtils.randomAlphabetic( 5 );

    

    @Before
    public void setup() throws Exception {

        super.runAsAdmin();

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        //v1
        cnvList.add( getCNV( "X", 3, 234, userVariantId, patientId ) );
        
        //v2
        cnvList.add( getCNV( "7", 1, 5, userVariantId2, patientId ) );        
        
        
        //these next two will not overlap
        cnvList.add( getCNV( "2", 123, 235, "XXXXXXX", patientId ) );
        cnvList.add( getCNV( "3", 12, 236, "XXXXXXX2", patientId ) );

        try {

            projectManager.addSubjectVariantsToProject( projectName, true, cnvList );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        ArrayList<VariantValueObject> cnvListWithOverlap = new ArrayList<VariantValueObject>();

        // will overlap v1 by 231
        cnvListWithOverlap.add( getCNV( "X", 3, 234, overlapVariantId1, patientIdWithOverlap ) );

        // will overlap v1 by  229
        cnvListWithOverlap.add( getCNV( "X", 5, 237, overlapVariantId2, patientIdWithOverlap ) );

        // will overlap v2 by 4
        cnvListWithOverlap.add( getCNV( "7", 1, 5, overlapVariantId3, patientIdWithOverlap ) );

        // will overlap v2 by 1
        cnvListWithOverlap.add( getCNV( "7", 4, 7, overlapVariantId4, patientIdWithOverlap ) );
        
        //will overlap v1 by 134, we will use this one to test the percentage overlap
        cnvListWithOverlap.add( getCNV( "X", 100, 900, overlapVariantId5, patientIdWithOverlap ) );

        //will not overlap
        cnvListWithOverlap.add( getCNV( "X", 900, 1600, null, patientIdWithOverlap ) );

        //will not overlap
        cnvListWithOverlap.add( getCNV( "Y", 3, 234, null, patientIdWithOverlap ) );

        try {

            projectManager.addSubjectVariantsToProject( projectNameWithOverlap, true, cnvListWithOverlap );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        

        try {
            
            Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );
            
            variant2SpecialVariantOverlapDao.deleteByOverlapProjectId( projectWithOverlap.getId() );

            projectManager.populateSpecialProjectOverlap( projectName, projectNameWithOverlap );
       

        } catch ( Exception e ) {

            fail( "projectManager.populateSpecialProjectOverlap threw an exception:" + e.toString() );

        }
        
        

    }
    
    
    @After
    public void tearDown() throws Exception {
        helper.deleteProject(projectName);
        helper.deleteProject(projectNameWithOverlap);
        
    }
    
   
   

    @Test
    public void testProjectOverlapFilterWithOverlap() {
        

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );
        
        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();
        
        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );
        

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        try {

            result = queryService.queryVariants( set );

        } catch ( Exception e ) {
            fail(e.toString());
        }

        assertEquals(2, result.getItems().size());

        assertEquals( result.getItems().iterator().next().getUserVariantId(), userVariantId );
        
        
    }
    
        
    
    @Test
    public void testProjectOverlapFilterWithSpecificOverlapGreaterThan() {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );
        
        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();
        
        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );
        
      
        //Note that the logic for this filter restriction is that ALL overlaps must be greater than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();
        
        overlapRestriction.setProperty( new OverlapBasesProperty() );
                
        overlapRestriction.setOperator( Operator.NUMERIC_GREATER );
        
        
        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 100 );
        
        overlapRestriction.setValue( numericValue );
        
        overlapFilter.setRestriction1( overlapRestriction );
        

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        try {

            result = queryService.queryVariants( set );

        } catch ( Exception e ) {
            fail(e.toString());
        }

        assertEquals( 1,result.getItems().size() );

        assertEquals( result.getItems().iterator().next().getUserVariantId(), userVariantId );
        
        
    }
    
    @Test
    public void testProjectOverlapFilterWithSpecificOverlapGreaterThanAgain() {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );
        
        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();
        
        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );
        
      
        //Note that the logic for this filter restriction is that ALL overlaps must be greater than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();
        
        overlapRestriction.setProperty( new OverlapBasesProperty() );
                
        overlapRestriction.setOperator( Operator.NUMERIC_GREATER );
        
        
        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 200 );
        
        overlapRestriction.setValue( numericValue );
        
        overlapFilter.setRestriction1( overlapRestriction );
        

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        try {

            result = queryService.queryVariants( set );

        } catch ( Exception e ) {
            fail(e.toString());
        }

        assertEquals( 0,result.getItems().size() );

        
        
    }
 
    @Test
    public void testProjectOverlapFilterWithSpecificOverlapLessThan() {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );
        
        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();
        
        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );
        
      //Note that the logic for this filter restriction is that ALL overlaps must be less than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();
        
        overlapRestriction.setProperty( new OverlapBasesProperty() );
                
        overlapRestriction.setOperator( Operator.NUMERIC_LESS );
        
        
        NumericValue numericValue = new NumericValue();
        numericValue.setValue(5 );
        
        overlapRestriction.setValue( numericValue );
        
        overlapFilter.setRestriction1( overlapRestriction );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        try {

            result = queryService.queryVariants( set );

        } catch ( Exception e ) {
            
            fail(e.toString());
        }

        assertEquals(3, result.getItems().size());

       
        
        
    }
    
    @Test
    public void testProjectOverlapFilterWithSecondaryOverlapLessThan() {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );
        
        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();
        
        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );
        
      
        
        SimpleRestriction overlapRestriction = new SimpleRestriction();
        
        overlapRestriction.setProperty( new DoesOverlapWithXProperty() );
                
        overlapRestriction.setOperator( Operator.NUMERIC_LESS);
        
        
        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 10000000 );
        
        overlapRestriction.setValue( numericValue );
        
        overlapFilter.setRestriction2( overlapRestriction );
        

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        try {

            result = queryService.queryVariants( set );

        } catch ( Exception e ) {
            fail(e.toString());
        }

        assertEquals( 4,result.getItems().size() );

        
        
    }
    
    @Test
    public void testProjectOverlapFilterWithSecondaryGreaterThan() {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );
        
        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();
        
        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );
        
      
        
        SimpleRestriction overlapRestriction = new SimpleRestriction();
        
        overlapRestriction.setProperty( new DoesOverlapWithXProperty() );
                
        overlapRestriction.setOperator( Operator.NUMERIC_GREATER);
        
        
        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 10000000 );
        
        overlapRestriction.setValue( numericValue );
        
        overlapFilter.setRestriction2( overlapRestriction );
        

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        try {

            result = queryService.queryVariants( set );

        } catch ( Exception e ) {
            fail(e.toString());
        }

        assertEquals( 0,result.getItems().size() );

        
        
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