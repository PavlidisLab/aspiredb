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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gemma.gsec.authentication.UserDetailsImpl;
import gemma.gsec.authentication.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.CNVDao;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Query;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.security.authorization.acl.AclTestUtils;
import ubc.pavlab.aspiredb.server.util.GenomeBin;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.AspireDbPagingLoadConfig;
import ubc.pavlab.aspiredb.shared.AspireDbPagingLoadConfigBean;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ExternalSubjectIdProperty;
import ubc.pavlab.aspiredb.shared.query.GenomicLocationProperty;
import ubc.pavlab.aspiredb.shared.query.NeurocartaPhenotypeProperty;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.SubjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

/**
 * author: anton date: 22/05/13
 */
public class QueryServiceTest extends BaseSpringContextTest {

    @Autowired
    private NeurocartaQueryService neurocartaQueryService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private PersistentTestObjectHelper persistentTestObjectHelper;

    @Autowired
    private CNVDao cnvDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    private PhenotypeUtil phenotypeUtil;

    @Autowired
    private AclTestUtils aclUtils;

    @Autowired
    UserManager userManager;

    private Project project;

    private String HP_HEAD = "Abnormality of the head";
    private String HP_FACE = "Abnormality of the face";
    private String HP_MOUTH = "Abnormality of the mouth";
    private String HP_NERVOUS = "Abnormality of the nervous system";

    private Collection<Long> activeProjectIds;

    private static Log log = LogFactory.getLog( QueryServiceTest.class.getName() );
    String username = "jimmy";
    String testname = RandomStringUtils.randomAlphabetic( 6 );

    @Before
    public void setUp() {
        try {
            userManager.loadUserByUsername( username );
        } catch ( UsernameNotFoundException e ) {
            userManager.createUser( new UserDetailsImpl( "jimmy", username, true, null, RandomStringUtils
                    .randomAlphabetic( 10 ) + "@gmail.com", "key", new Date() ) );
        }

        // run save query as administrator in filter window
        super.runAsAdmin();
    }

    public void setUpPhenotypes() {
        createSubjectWithPhenotypes( "1", "0", "0", "0" );
        createSubjectWithPhenotypes( "0", "1", "0", "0" );
        createSubjectWithPhenotypes( "0", "0", "1", "0" );
        createSubjectWithPhenotypes( "0", "0", "0", "1" );
        createSubjectWithPhenotypes( "1", "0", "1", "0" );
    }

    public void tearDownPhenotypes() throws Exception {
        // run save query as administrator in filter window
        super.runAsAdmin();

        // for ( Subject s : project.getSubjects() ) {
        // try {
        //
        // for ( Phenotype p : s.getPhenotypes() ) {
        // phenotypeDao.remove( p );
        // }
        //
        // subjectDao.remove( s );
        //
        // } catch ( Exception e ) {
        // e.printStackTrace();
        // }
        // }
        // projectDao.remove( project );
    }

    @Test
    public void testIsQueryName() throws Exception {

        setUpPhenotypes();

        Collection<Long> projectIds = new HashSet<Long>();
        projectIds.add( project.getId() );
        ProjectFilterConfig projConfig = new ProjectFilterConfig();
        projConfig.setProjectIds( projectIds );

        PhenotypeRestriction restriction = new PhenotypeRestriction();
        restriction.setName( "Abnormality of the head" );
        restriction.setValue( "1" );

        PhenotypeFilterConfig phenoConfig = new PhenotypeFilterConfig();
        phenoConfig.setRestriction( restriction );
        phenoConfig.setActiveProjectIds( activeProjectIds );

        Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
        filters.add( projConfig );
        filters.add( phenoConfig );

        // Remove any existing query
        queryService.deleteQuery( testname );
        boolean returnvalue = queryService.isQueryName( testname );
        assertFalse( returnvalue );

        // Admin creates query
        super.runAsAdmin();
        Long queryId = queryService.saveQuery( testname, filters );
        Query queryObj = queryService.getQuery( queryId );
        assertNotNull( queryObj );
        returnvalue = queryService.isQueryName( testname );
        assertTrue( returnvalue );
        log.debug( "Query acl is " + aclUtils.getAcl( queryObj ) );

        // run as user to check whether the admin created query is accessble by the user
        super.runAsUser( this.username );
        returnvalue = queryService.isQueryName( testname );
        assertFalse( returnvalue );

        tearDownPhenotypes();
    }

    private Subject createSubjectWithPhenotypes( String headPhenoValue, String facePhenoValue, String mouthPhenoValue,
            String nervousPhenoValue ) {

        if ( project == null ) {
            project = new Project();
            project.setName( RandomStringUtils.randomAlphabetic( 4 ) );
            project = persistentTestObjectHelper.createPersistentProject( project );
        }

        List<Project> plist = new ArrayList<Project>();
        plist.add( project );
        Collection<Long> projectIds = new ArrayList<Long>();
        projectIds.add( project.getId() );
        activeProjectIds = projectIds;

        Phenotype phenoHead = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_HEAD, "HP_0000234",
                "HPONTOLOGY", headPhenoValue );
        Phenotype phenoFace = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_FACE, "HP_0000271",
                "HPONTOLOGY", facePhenoValue );
        Phenotype phenoMouth = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_MOUTH, "HP_0000153",
                "HPONTOLOGY", mouthPhenoValue );
        Phenotype phenoNervous = persistentTestObjectHelper.createPersistentTestPhenotypeObject( HP_NERVOUS,
                "HP_0000707", "HPONTOLOGY", nervousPhenoValue );

        List<Subject> subjectList = project.getSubjects();
        log.debug( "subjectList.size=" + subjectList.size() );
        String patientId = "" + subjectList.size();

        if ( headPhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_HEAD + "=" + headPhenoValue;
        }

        if ( facePhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_FACE + "=" + facePhenoValue;
        }

        if ( mouthPhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_MOUTH + "=" + mouthPhenoValue;
        }

        if ( nervousPhenoValue.equals( "1" ) ) {
            patientId += "_" + HP_NERVOUS + "=" + nervousPhenoValue;
        }

        Subject subject = persistentTestObjectHelper.createPersistentTestIndividualObject( patientId );
        subject.setProjects( plist );
        subject.addPhenotype( phenoHead );
        subject.addPhenotype( phenoFace );
        subject.addPhenotype( phenoMouth );
        subject.addPhenotype( phenoNervous );
        subjectList.add( subject );
        subjectDao.update( subject );
        phenotypeDao.update( phenoHead );
        phenotypeDao.update( phenoFace );
        phenotypeDao.update( phenoMouth );
        phenotypeDao.update( phenoNervous );

        projectDao.update( project );

        return subject;
    }

    /**
     * Counts the number of Subjects that satisfies "restriction"
     * 
     * @param restriction
     * @return
     * @throws NotLoggedInException
     * @throws ExternalDependencyException
     */
    private List<SubjectValueObject> querySubjects( PhenotypeRestriction restriction ) throws NotLoggedInException,
            ExternalDependencyException {
        Collection<Long> projectIds = new HashSet<Long>();
        projectIds.add( project.getId() );
        ProjectFilterConfig projConfig = new ProjectFilterConfig();
        projConfig.setProjectIds( projectIds );

        PhenotypeFilterConfig phenoConfig = new PhenotypeFilterConfig();
        phenoConfig.setRestriction( restriction );
        phenoConfig.setActiveProjectIds( activeProjectIds );

        Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
        filters.add( projConfig );
        filters.add( phenoConfig );
        AspireDbPagingLoadConfig loadConfig = new AspireDbPagingLoadConfigBean();
        loadConfig.setFilters( filters );
        return queryService.querySubjects( filters ).getItems();
    }

    /**
     * Tests for absence of Phenotypes
     * 
     * @throws Exception
     */
    @Test
    public void testPhenoHasNoValueSubjects() throws Exception {

        setUpPhenotypes();

        try {
            int found = 0;

            assertEquals( 5, project.getSubjects().size() );

            Collection<String> phenoNames = phenotypeDao.getExistingNames( activeProjectIds );
            assertEquals( 4, phenoNames.size() );

            PhenotypeRestriction restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the head" );
            restriction.setValue( "0" );
            List<SubjectValueObject> result = querySubjects( restriction );
            log.debug( "head 0 size=" + result.size() );
            for ( SubjectValueObject svo : result ) {
                log.debug( "head 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_NERVOUS ) != -1 ) {
                    found++;
                }
            }
            assertEquals( 1, result.size() );
            assertEquals( 1, found );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the face" );
            restriction.setValue( "0" );
            result = querySubjects( restriction );
            found = 0;
            for ( SubjectValueObject svo : result ) {
                log.debug( "face 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_HEAD ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_NERVOUS ) != -1 ) {
                    found++;
                }
            }
            log.debug( "face 0 result.size=" + result.size() );
            assertEquals( 2, result.size() );
            assertEquals( 2, found );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the mouth" );
            restriction.setValue( "0" );
            result = querySubjects( restriction );
            found = 0;
            for ( SubjectValueObject svo : result ) {
                log.debug( "mouth 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_HEAD ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_FACE ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_NERVOUS ) != -1 ) {
                    found++;
                }
            }
            log.debug( "mouth 0 result.size=" + result.size() );
            assertEquals( 3, result.size() );
            assertEquals( 3, found );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the nervous system" );
            restriction.setValue( "0" );
            result = querySubjects( restriction );
            found = 0;
            for ( SubjectValueObject svo : result ) {
                log.debug( "nervous 0 svo " + svo.getPatientId() );
                if ( svo.getPatientId().indexOf( HP_HEAD ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_FACE ) != -1 ) {
                    found++;
                } else if ( svo.getPatientId().indexOf( HP_MOUTH ) != -1 ) {
                    found++;
                }
            }
            log.debug( "nervous 0 result.size=" + result.size() );
            assertEquals( 4, result.size() );
            assertEquals( 4, found );

        } catch ( Exception e ) {
            fail( e.getMessage() );
        }

        tearDownPhenotypes();
    }

    /**
     * Tests for presence of Phenotypes
     * 
     * @throws Exception
     */
    @Test
    public void testPhenoHasValueSubjects() throws Exception {

        setUpPhenotypes();

        try {
            Collection<String> phenoNames = phenotypeDao.getExistingNames( activeProjectIds );
            assertEquals( 4, phenoNames.size() );

            PhenotypeRestriction restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the head" );
            restriction.setValue( "1" );
            List<SubjectValueObject> result = querySubjects( restriction );
            assertEquals( 4, result.size() );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the face" );
            restriction.setValue( "1" );
            result = querySubjects( restriction );
            for ( SubjectValueObject svo : result ) {
                log.debug( "face 1 svo " + svo.getPatientId() );
            }
            assertEquals( 3, result.size() );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the mouth" );
            restriction.setValue( "1" );
            result = querySubjects( restriction );
            for ( SubjectValueObject svo : result ) {
                log.debug( "mouth 1 svo " + svo.getPatientId() );
            }
            assertEquals( 2, result.size() );

            restriction = new PhenotypeRestriction();
            restriction.setName( "Abnormality of the nervous system" );
            restriction.setValue( "1" );
            result = querySubjects( restriction );
            for ( SubjectValueObject svo : result ) {
                log.debug( "nervous 1 svo " + svo.getPatientId() );
            }
            assertEquals( 1, result.size() );

        } catch ( Exception e ) {
            fail( e.getMessage() );
        }

        tearDownPhenotypes();
    }

    private Map<Integer, Integer> getSubjectVariantCountForPatientId( String patientId ) throws NotLoggedInException,
            ExternalDependencyException {
        SimpleRestriction simpleRe = new SimpleRestriction();
        simpleRe.setOperator( Operator.TEXT_EQUAL );
        simpleRe.setProperty( new ExternalSubjectIdProperty() );
        simpleRe.setValue( new TextValue( patientId ) );

        Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
        filters.add( new SubjectFilterConfig( simpleRe ) );

        // System.out.println("getSubjectCount=" + queryService.getSubjectCount( filters ));

        Map<Integer, Integer> ret = queryService.getSubjectVariantCounts( filters );
        return ret;
    }

    private Map<Integer, Integer> getSubjectVariantCountForChromosome( String chromosome ) throws NotLoggedInException,
            ExternalDependencyException {
        SimpleRestriction simpleRe = new SimpleRestriction();
        simpleRe.setOperator( Operator.IS_IN_SET );
        simpleRe.setProperty( new GenomicLocationProperty() );
        simpleRe.setValue( new GenomicRange( chromosome ) );

        Set<AspireDbFilterConfig> filters = new HashSet<>();
        filters.add( new VariantFilterConfig( simpleRe ) );
        Map<Integer, Integer> ret = queryService.getSubjectVariantCounts( filters );
        return ret;
    }

    /**
     * @param phenotypeURI
     * @return
     * @throws NotLoggedInException
     * @throws ExternalDependencyException
     */
    private Map<Integer, Integer> getSubjectVariantCountForPhenocarta( String phenotypeURI )
            throws NotLoggedInException, ExternalDependencyException {
        SimpleRestriction simpleRe = new SimpleRestriction();
        simpleRe.setOperator( Operator.IS_IN_SET );
        simpleRe.setProperty( new NeurocartaPhenotypeProperty() );
        NeurocartaPhenotypeValueObject vo = new NeurocartaPhenotypeValueObject();
        Collection<GeneValueObject> gvo = neurocartaQueryService.fetchGenesAssociatedWithPhenotype( phenotypeURI );
        // System.out.println(gvo.size() + " genes found for URI " + phenotypeURI);
        vo.setGenes( gvo );
        simpleRe.setValue( vo );

        Set<AspireDbFilterConfig> filters = new HashSet<>();
        filters.add( new VariantFilterConfig( simpleRe ) );
        Map<Integer, Integer> ret = queryService.getSubjectVariantCounts( filters );
        return ret;
    }

    /**
     * @throws ExternalDependencyException
     * @throws NotLoggedInException
     */
    @Test
    public void testGetSubjectVariantCountsForLocation() throws NotLoggedInException, ExternalDependencyException {

        String chr = "1";
        String patientId = "testGetSubjectVariantCountsForLocation";

        // look at how many there are currently in the database
        Map<Integer, Integer> ret = getSubjectVariantCountForChromosome( chr );
        int subjectCount = ret.get( VariantDao.SUBJECT_IDS_KEY );
        int variantCount = ret.get( VariantDao.VARIANT_IDS_KEY );

        // add a variant in Chr 1
        Subject s = persistentTestObjectHelper.createPersistentTestSubjectObjectWithCNV( patientId );
        CNV cnv = ( CNV ) s.getVariants().iterator().next();
        cnv.getLocation().setChromosome( chr );
        cnv.getLocation().setStart( 1 );
        cnv.getLocation().setEnd( 100 );
        cnvDao.update( cnv );
        subjectDao.update( s );

        // now there should be one more
        ret = getSubjectVariantCountForChromosome( chr );
        int addedSubjectCount = ret.get( VariantDao.SUBJECT_IDS_KEY );
        int addedVariantCount = ret.get( VariantDao.VARIANT_IDS_KEY );

        assertEquals( subjectCount + 1, addedSubjectCount );
        assertEquals( variantCount + 1, addedVariantCount );

        // cleanup
        s.getVariants().remove( cnv );
        subjectDao.update( s );
        persistentTestObjectHelper.removeVariant( cnv );
        persistentTestObjectHelper.removeSubject( s );
    }

    @Test
    public void testGetSubjectVariantCountsForPatientId() throws NotLoggedInException, ExternalDependencyException {

        String patientId = "testGetSubjectVariantCountsForPatientId";

        // look at how many there are currently in the database
        Map<Integer, Integer> ret = getSubjectVariantCountForPatientId( patientId );
        int subjectCount = ret.get( VariantDao.SUBJECT_IDS_KEY );
        int variantCount = ret.get( VariantDao.VARIANT_IDS_KEY );
        // add a subject
        Subject s = persistentTestObjectHelper.createPersistentTestIndividualObject( patientId );

        assertEquals( patientId, s.getPatientId() );

        // now there should be one more
        ret = getSubjectVariantCountForPatientId( patientId );
        int addedSubjectCount = ret.get( VariantDao.SUBJECT_IDS_KEY );
        int addedVariantCount = ret.get( VariantDao.VARIANT_IDS_KEY );

        assertEquals( subjectCount + 1, addedSubjectCount ); // one subject was added
        assertEquals( variantCount, addedVariantCount ); // no new variants were added!

        // cleanup
        persistentTestObjectHelper.removeSubject( s );
    }

    @Test
    public void testGetSubjectVariantCountsForPhenocarta() throws NotLoggedInException, ExternalDependencyException {

        String patientId = "testGetSubjectVariantCountsForLocation";

       String phenotypeURI = "http://purl.obolibrary.org/obo/DOID_219"; // colon cancer (~321 genes)
       //  String phenotypeURI = "http://purl.obolibrary.org/obo/DOID_0060041"; // autism spectrum disorder (~853 genes)

        // look at how many there are currently in the database
        Map<Integer, Integer> ret = getSubjectVariantCountForPhenocarta( phenotypeURI );
        int subjectCount = ret.get( VariantDao.SUBJECT_IDS_KEY );
        int variantCount = ret.get( VariantDao.VARIANT_IDS_KEY );

        // add some variants the would overlap
        // 17:37885247-37885647, bin
        // 4:72247-5545043, bin
        Subject s = persistentTestObjectHelper.createPersistentTestIndividualObject( patientId );
        CNV cnv1 = persistentTestObjectHelper.createPersistentTestCNVObject();
        cnv1.setSubject( s );
        cnv1.getLocation().setChromosome( "17" );
        cnv1.getLocation().setStart( 37885247 );
        cnv1.getLocation().setEnd( 37885647 );
        cnv1.getLocation()
                .setBin( GenomeBin.binFromRange( cnv1.getLocation().getStart(), cnv1.getLocation().getEnd() ) );
        cnvDao.update( cnv1 );
        CNV cnv2 = persistentTestObjectHelper.createPersistentTestCNVObject();
        cnv2.setSubject( s );
        cnv2.getLocation().setChromosome( "4" );
        cnv2.getLocation().setStart( 72247 );
        cnv2.getLocation().setEnd( 5545043 );
        cnv2.getLocation()
                .setBin( GenomeBin.binFromRange( cnv2.getLocation().getStart(), cnv2.getLocation().getEnd() ) );

        cnvDao.update( cnv2 );
        CNV cnv3 = persistentTestObjectHelper.createPersistentTestCNVObject(); // this one doesn't count
        cnv3.setSubject( s );
        cnv3.getLocation().setChromosome( "4" );
        cnv3.getLocation().setStart( 1 );
        cnv3.getLocation().setEnd( 2 );
        cnv3.getLocation()
                .setBin( GenomeBin.binFromRange( cnv3.getLocation().getStart(), cnv3.getLocation().getEnd() ) );

        cnvDao.update( cnv3 );
        s.addVariant( cnv1 );
        s.addVariant( cnv2 );
        s.addVariant( cnv3 );
        subjectDao.update( s );

        assertEquals( patientId, s.getPatientId() );
        assertEquals( 3, s.getVariants().size() );

        // now there should be one more
        StopWatch watch = new StopWatch();
        watch.start();
        ret = getSubjectVariantCountForPhenocarta( phenotypeURI );
        watch.stop();
        // System.out.println("time to get variants = " + watch );
        int addedSubjectCount = ret.get( VariantDao.SUBJECT_IDS_KEY );
        int addedVariantCount = ret.get( VariantDao.VARIANT_IDS_KEY );

        assertEquals( subjectCount + 1, addedSubjectCount ); // one subject was added
       // assertEquals( variantCount + s.getVariants().size() - 1, addedVariantCount ); // no new variants were added!

        // cleanup
        List<Variant> sv = s.getVariants();
        s.setVariants( null ); 
        subjectDao.update( s );
        for ( Variant v : sv ) {
            CNV c = ( CNV ) v;
            c.setSubject( null );
            cnvDao.update( c );
            persistentTestObjectHelper.removeVariant( c );
        }
        persistentTestObjectHelper.removeSubject( s );

    }
}
