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
import static org.junit.Assert.fail;

import java.util.ArrayList;
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
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
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
import ubc.pavlab.aspiredb.shared.query.ProjectOverlapFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

public class QueryServiceProjectOverlapFilterTest extends BaseSpringContextTest {

    @Autowired
    private ProjectManager projectManager;

    @Autowired
    private QueryService queryService;

    @Autowired
    private Variant2SpecialVariantOverlapDao variant2SpecialVariantOverlapDao;

    private @Autowired
    ProjectDao projectDao;

    @Autowired
    private VariantDao variantDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private PersistentTestObjectHelper helper;

    private final String patientId = RandomStringUtils.randomAlphabetic( 5 );
    private final String projectName = RandomStringUtils.randomAlphabetic( 5 );

    private final String userVariantId = RandomStringUtils.randomAlphabetic( 5 );

    private final String userVariantId2 = RandomStringUtils.randomAlphabetic( 5 );

    // private final String userVariantIdToTestOverlapPercentage = RandomStringUtils.randomAlphabetic( 5 );

    private final String overlapVariantId1 = RandomStringUtils.randomAlphabetic( 5 );
    private final String overlapVariantId2 = RandomStringUtils.randomAlphabetic( 5 );
    private final String overlapVariantId3 = RandomStringUtils.randomAlphabetic( 5 );
    private final String overlapVariantId4 = RandomStringUtils.randomAlphabetic( 5 );
    private final String overlapVariantId5 = RandomStringUtils.randomAlphabetic( 5 );

    private final String patientIdWithOverlap = RandomStringUtils.randomAlphabetic( 5 );
    private final String projectNameWithOverlap = RandomStringUtils.randomAlphabetic( 5 );

    @Before
    public void setup() throws Exception {

        super.runAsAdmin();

        ArrayList<VariantValueObject> cnvList = new ArrayList<VariantValueObject>();
        // v1
        cnvList.add( getCNV( "X", 3, 234, userVariantId, patientId ) );

        // v2
        cnvList.add( getCNV( "7", 1, 5, userVariantId2, patientId ) );

        // these next two will not overlap
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

        // will overlap v1 by 229
        cnvListWithOverlap.add( getCNV( "X", 5, 237, overlapVariantId2, patientIdWithOverlap ) );

        // will overlap v2 by 4
        cnvListWithOverlap.add( getCNV( "7", 1, 5, overlapVariantId3, patientIdWithOverlap ) );

        // will overlap v2 by 1
        cnvListWithOverlap.add( getCNV( "7", 4, 7, overlapVariantId4, patientIdWithOverlap ) );

        // will overlap v1 by 134, we will use this one to test the percentage overlap
        cnvListWithOverlap.add( getCNV( "X", 100, 900, overlapVariantId5, patientIdWithOverlap ) );

        // will not overlap
        cnvListWithOverlap.add( getCNV( "X", 900, 1600, null, patientIdWithOverlap ) );

        // will not overlap
        cnvListWithOverlap.add( getCNV( "Y", 3, 234, null, patientIdWithOverlap ) );

        try {

            projectManager.addSubjectVariantsToProject( projectNameWithOverlap, true, cnvListWithOverlap );

        } catch ( Exception e ) {

            fail( "projectManager.addSubjectVariantsToProject threw an exception" );

        }

        try {

            Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

            variant2SpecialVariantOverlapDao.deleteByOverlapProjectId( projectWithOverlap.getId() );

            projectManager.populateProjectToProjectOverlap( projectName, projectNameWithOverlap );

        } catch ( Exception e ) {

            fail( "projectManager.populateSpecialProjectOverlap threw an exception:" + e.toString() );

        }

    }

    @After
    public void tearDown() throws Exception {
        // helper.deleteProject( projectName );
        // helper.deleteProject( projectNameWithOverlap );

    }

    @Test
    public void testProjectOverlapFilterWithOverlap() throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        Set<AspireDbFilterConfig> set = new HashSet<>();

        set.add( overlapFilter );

        BoundedList<VariantValueObject> result = queryService.queryVariants( set );

        assertEquals( 2, result.getItems().size() );

        assertEquals( result.getItems().iterator().next().getUserVariantId(), userVariantId );

    }

    @Test
    public void testProjectOverlapFilterWithSecondaryGreaterThan() throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        SimpleRestriction overlapRestriction = new SimpleRestriction();

        overlapRestriction.setProperty( new DoesOverlapWithXProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 10000001 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction2( overlapRestriction );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        result = queryService.queryVariants( set );
        assertEquals( 0, result.getItems().size() );

    }

    @Test
    public void testProjectOverlapFilterWithSecondaryOverlapLessThan() throws Exception {

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

        overlapRestriction.setOperator( Operator.NUMERIC_LESS_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 9999999 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction2( overlapRestriction );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        result = queryService.queryVariants( set );

        assertEquals( 4, result.getItems().size() );

    }

    @Test
    public void testProjectOverlapFilterWithSpecificLessThanPlusSecondaryOverlapLessThan() throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        // Note that the logic for this filter restriction is that ALL overlaps must be greater than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();

        overlapRestriction.setProperty( new OverlapBasesProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_LESS_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 0 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction1( overlapRestriction );

        SimpleRestriction overlapRestriction2 = new SimpleRestriction();

        overlapRestriction2.setProperty( new DoesOverlapWithXProperty() );

        overlapRestriction2.setOperator( Operator.NUMERIC_LESS_OR_EQUAL );

        NumericValue numericValue2 = new NumericValue();
        numericValue2.setValue( 0 );

        overlapRestriction2.setValue( numericValue2 );

        overlapFilter.setRestriction2( overlapRestriction2 );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        result = queryService.queryVariants( set );

        assertEquals( 2, result.getItems().size() );

    }

    @Test
    public void testProjectOverlapFilterWithSpecificOverlapGreaterThan() throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        // Note that the logic for this filter restriction is that ALL overlaps must be greater than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();

        overlapRestriction.setProperty( new OverlapBasesProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 101 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction1( overlapRestriction );

        Set<AspireDbFilterConfig> set = new HashSet<>();

        set.add( overlapFilter );

        try {

            BoundedList<VariantValueObject> result = queryService.queryVariants( set );
            assertEquals( 1, result.getItems().size() );

            assertEquals( result.getItems().iterator().next().getUserVariantId(), userVariantId );

        } catch ( Exception e ) {
            fail( e.toString() );
        }

    }

    @Test
    public void testProjectOverlapFilterWithSpecificOverlapGreaterThanAgain() throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        SimpleRestriction overlapRestriction = new SimpleRestriction();

        overlapRestriction.setProperty( new OverlapBasesProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 201 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction1( overlapRestriction );

        Set<AspireDbFilterConfig> set = new HashSet<>();

        set.add( overlapFilter );

        BoundedList<VariantValueObject> result = null;

        result = queryService.queryVariants( set );
        assertEquals( 1, result.getItems().size() );

    }

    @Test
    public void testProjectOverlapFilterWithSpecificOverlapGreaterThanPlusSecondaryOverlapGreaterAgain()
            throws Exception {

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

        overlapRestriction.setProperty( new OverlapBasesProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 2 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction1( overlapRestriction );

        SimpleRestriction overlapRestriction2 = new SimpleRestriction();

        overlapRestriction2.setProperty( new DoesOverlapWithXProperty() );

        overlapRestriction2.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue2 = new NumericValue();
        numericValue2.setValue( 3 );

        overlapRestriction2.setValue( numericValue2 );

        overlapFilter.setRestriction2( overlapRestriction2 );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        result = queryService.queryVariants( set );

        assertEquals( 1, result.getItems().size() );

    }

    @Test
    // great method name
    public void testProjectOverlapFilterWithSpecificOverlapGreaterThanPlusSecondaryOverlapGreaterAgainOneMoreTime()
            throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        // Note that the logic for this filter restriction is that ALL overlaps must be greater than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();

        overlapRestriction.setProperty( new OverlapBasesProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 1 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction1( overlapRestriction );

        SimpleRestriction overlapRestriction2 = new SimpleRestriction();

        overlapRestriction2.setProperty( new DoesOverlapWithXProperty() );

        overlapRestriction2.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue2 = new NumericValue();
        numericValue2.setValue( 2 );

        overlapRestriction2.setValue( numericValue2 );

        overlapFilter.setRestriction2( overlapRestriction2 );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        result = queryService.queryVariants( set );

        assertEquals( 2, result.getItems().size() );

    }

    @Test
    public void testProjectOverlapFilterWithSpecificOverlapGreaterThanPlusSecondaryOverlapGreaterThan()
            throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<Long>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<Long>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        // Note that the logic for this filter restriction is that ALL overlaps must be greater than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();

        overlapRestriction.setProperty( new OverlapBasesProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 0 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction1( overlapRestriction );

        SimpleRestriction overlapRestriction2 = new SimpleRestriction();

        overlapRestriction2.setProperty( new DoesOverlapWithXProperty() );

        overlapRestriction2.setOperator( Operator.NUMERIC_GREATER_OR_EQUAL );

        NumericValue numericValue2 = new NumericValue();
        numericValue2.setValue( 3 );

        overlapRestriction2.setValue( numericValue2 );

        overlapFilter.setRestriction2( overlapRestriction2 );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        result = queryService.queryVariants( set );

        assertEquals( 1, result.getItems().size() );

    }

    @Test
    public void testProjectOverlapFilterWithSpecificOverlapLessThan() throws Exception {

        Project project = projectDao.findByProjectName( projectName );

        Project projectWithOverlap = projectDao.findByProjectName( projectNameWithOverlap );

        List<Long> projectList = new ArrayList<>();
        projectList.add( project.getId() );

        List<Long> projectListWithOverlap = new ArrayList<>();
        projectListWithOverlap.add( projectWithOverlap.getId() );

        ProjectOverlapFilterConfig overlapFilter = new ProjectOverlapFilterConfig();

        overlapFilter.setProjectIds( projectList );
        overlapFilter.setOverlapProjectIds( projectListWithOverlap );

        // Note that the logic for this filter restriction is that ALL overlaps must be less than the value specified
        SimpleRestriction overlapRestriction = new SimpleRestriction();

        overlapRestriction.setProperty( new OverlapBasesProperty() );

        overlapRestriction.setOperator( Operator.NUMERIC_LESS_OR_EQUAL );

        NumericValue numericValue = new NumericValue();
        numericValue.setValue( 4 );

        overlapRestriction.setValue( numericValue );

        overlapFilter.setRestriction1( overlapRestriction );

        BoundedList<VariantValueObject> result = null;

        Set<AspireDbFilterConfig> set = new HashSet<AspireDbFilterConfig>();

        set.add( overlapFilter );

        result = queryService.queryVariants( set );

        assertEquals( 3, result.getItems().size() );

    }

    private CNVValueObject getCNV( String chrom, int baseStart, int baseEnd, String varId, String patId ) {

        CharacteristicValueObject cvo = new CharacteristicValueObject();

        cvo.setKey( "testChar" );
        cvo.setValue( "testcharvalue" );

        Map<String, CharacteristicValueObject> charMap = new HashMap<String, CharacteristicValueObject>();
        charMap.put( cvo.getKey(), cvo );

        CNVValueObject cnv = new CNVValueObject();

        cnv.setCharacteristics( charMap );
        cnv.setType( "GAIN" );

        cnv.setUserVariantId( varId );

        GenomicRange gr = new GenomicRange( chrom, baseStart, baseEnd );

        cnv.setGenomicRange( gr );

        cnv.setPatientId( patId );

        return cnv;

    }

    // private ProjectFilterConfig getProjectFilterConfigById( Project p ) {
    //
    // ProjectFilterConfig projectFilterConfig = new ProjectFilterConfig();
    //
    // ArrayList<Long> projectIds = new ArrayList<Long>();
    //
    // projectIds.add( p.getId() );
    //
    // projectFilterConfig.setProjectIds( projectIds );
    //
    // return projectFilterConfig;
    //
    // }

}