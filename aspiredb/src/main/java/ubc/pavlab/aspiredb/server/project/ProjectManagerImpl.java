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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SecurableDaoBase;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.Variant2SpecialVariantOverlapDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Indel;
import ubc.pavlab.aspiredb.server.model.Inversion;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.SNV;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantOverlap;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.server.security.SecurityService;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.service.QueryService;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.BoundedList;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.IndelValueObject;
import ubc.pavlab.aspiredb.shared.InversionValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.SNVValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.GenomicLocationProperty;
import ubc.pavlab.aspiredb.shared.query.GenomicRangeDataType;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.SetRestriction;

/**
 * TODO Document Me
 * 
 * @author cmcdonald
 * @version $Id: ProjectManagerImpl.java,v 1.33 2013/06/24 20:24:44 cmcdonald Exp $
 */
@Service("projectManager")
public class ProjectManagerImpl implements ProjectManager {

    protected static Log log = LogFactory.getLog( ProjectManagerImpl.class );

    @Autowired
    ProjectDao projectDao;

    @Autowired
    SubjectDao subjectDao;

    @Autowired
    VariantDao variantDao;

    SecurityService securityservice;

    @Autowired
    Variant2SpecialVariantOverlapDao variant2SpecialVariantOverlapDao;

    @Autowired
    PhenotypeDao phenotypeDao;

    @Autowired
    PhenotypeUtil phenotypeService;

    @Autowired
    SecurityService securityService;

    @Autowired
    UserManager userManager;

    @Autowired
    QueryService queryService;

    ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

    public static final String DGV_SUPPORT_CHARACTERISTIC_KEY = "pubmedid";

    @Override
    @Transactional
    public Project createProject( String name, String description ) throws Exception {
        if ( projectDao.findByProjectName( name ) != null ) {
            throw new Exception( "project with that name already exists" );

        }

        Project p = new Project();
        p.setName( name );
        p.setDescription( description );

        return projectDao.create( p );
    }

    @Override
    @Transactional
    public Project findProject( String projectName ) throws Exception {
        Project project = projectDao.findByProjectName( projectName );
        if ( project != null ) {
            return project;
        } else
            return null;

    }

    @Transactional
    public Project createSpecialProject( String name, boolean deleteProject ) throws Exception {

        name = name.toUpperCase();

        if ( !name.equals( "DECIPHER" ) && !name.equals( "DGV" ) ) {
            throw new Exception( "Special project names limited to 'DECIPHER' or 'DGV'" );
        }

        if ( projectDao.findByProjectName( name ) != null ) {

            if ( deleteProject ) {

                deleteProject( name );

            } else {
                return projectDao.findByProjectName( name );
            }

        }

        Project p = new Project();
        p.setName( name );
        p.setSpecialData( true );

        if ( name.equals( "DGV" ) ) {
            p.setVariantSupportCharacteristicKey( DGV_SUPPORT_CHARACTERISTIC_KEY );
        }

        return projectDao.create( p );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubc.pavlab.aspiredb.server.project.ProjectManager#addSubjectVariantsToProject(java.lang.String, boolean,
     * java.util.List) createProject parameter is included to make accidental overwriting via cli's more difficult
     */
    @Override
    @Transactional
    public void addSubjectVariantsToProject( String projectName, boolean createProject, List<VariantValueObject> voList )
            throws Exception {

        Project proj;
        if ( createProject ) {
            proj = createProject( projectName, "" );
        } else {
            proj = projectDao.findByProjectName( projectName );
            if ( proj == null ) {
                throw new Exception( "Project does not exist" );
            }
        }

        createSubjectVariantsFromVariantValueObjects( proj, voList );

    }

    @Override
    @Transactional
    public void addSubjectVariantsToSpecialProject( String projectName, boolean deleteProject,
            List<VariantValueObject> voList, boolean existingProject ) throws Exception {

        Project proj = null;

        if ( !existingProject ) {
            proj = createSpecialProject( projectName, deleteProject );
        } else {
            proj = projectDao.findByProjectName( projectName );
        }

        createSubjectVariantsFromVariantValueObjects( proj, voList, true );

    }

    /**
     * @param projectName
     * @param voList
     * @throws Exception
     */
    @Override
    @Transactional
    public void addSubjectVariantsToProjectForceCreate( String projectName, List<VariantValueObject> voList )
            throws Exception {

        Project proj;

        proj = projectDao.findByProjectName( projectName );
        if ( proj == null ) {
            proj = createProject( projectName, "" );
        }

        createSubjectVariantsFromVariantValueObjects( proj, voList );

    }

    private void createSubjectVariantsFromVariantValueObjects( Project project, List<VariantValueObject> voList ) {
        createSubjectVariantsFromVariantValueObjects( project, voList, false );
    }

    @Transactional
    private void createSubjectVariantsFromVariantValueObjects( Project project, List<VariantValueObject> voList,
            Boolean specialProject ) {
        log.info( "Adding " + voList.size() + " value objects" );
        int counter = 0;
        for ( VariantValueObject vo : voList ) {

            if ( vo instanceof CNVValueObject ) {
                createSubjectVariantFromCNVValueObject( project, ( CNVValueObject ) vo, specialProject );
            } else if ( vo instanceof SNVValueObject ) {
                createSubjectVariantFromSNVValueObject( project, ( SNVValueObject ) vo, specialProject );
            } else if ( vo instanceof IndelValueObject ) {
                createSubjectVariantFromIndelValueObject( project, ( IndelValueObject ) vo, specialProject );
            } else if ( vo instanceof InversionValueObject ) {
                createSubjectVariantFromInversionValueObject( project, ( InversionValueObject ) vo, specialProject );
            } else {
                log.error( "unsupported VariantValueObject" );
            }

            counter++;

            if ( counter % 500 == 0 ) {
                log.info( "Added " + counter + " variant" );
            }
        }

    }

    @Transactional
    private void createSubjectVariantFromCNVValueObject( Project project, CNVValueObject cnv, Boolean specialProject ) {

        CNV cnvEntity;

        if ( specialProject ) {
            cnvEntity = ( CNV ) getSpecialProjectVariant( cnv );
        } else {
            cnvEntity = ( CNV ) getVariant( cnv );
        }
        cnvEntity.setType( CnvType.valueOf( cnv.getType().toUpperCase() ) );
        cnvEntity.setCopyNumber( cnv.getCopyNumber() );
        cnvEntity.setCnvLength( cnv.getCnvLength() );

        if ( cnvEntity.getId() == null ) {
            addSubjectVariantToProject( project, cnv.getPatientId(), cnvEntity, specialProject );
        }
    }

    @Transactional
    private void createSubjectVariantFromSNVValueObject( Project project, SNVValueObject snv, Boolean specialProject ) {
        SNV snvEntity;

        if ( specialProject ) {
            snvEntity = ( SNV ) getSpecialProjectVariant( snv );
        } else {
            snvEntity = ( SNV ) getVariant( snv );
        }

        snvEntity.setReferenceBase( snv.getReferenceBase() );
        snvEntity.setObservedBase( snv.getObservedBase() );

        snvEntity.setDbSNPID( snv.getDbSNPID() );

        if ( snvEntity.getId() == null ) {
            addSubjectVariantToProject( project, snv.getPatientId(), snvEntity, specialProject );
        }
    }

    @Transactional
    private void createSubjectVariantFromIndelValueObject( Project project, IndelValueObject indel,
            Boolean specialProject ) {

        Indel indelEntity = ( Indel ) getVariant( indel );

        if ( specialProject ) {
            indelEntity = ( Indel ) getSpecialProjectVariant( indel );
        } else {
            indelEntity = ( Indel ) getVariant( indel );
        }

        indelEntity.setIndelLength( indel.getLength() );

        if ( indelEntity.getId() == null ) {
            addSubjectVariantToProject( project, indel.getPatientId(), indelEntity, specialProject );
        }
    }

    @Transactional
    private void createSubjectVariantFromInversionValueObject( Project project, InversionValueObject inversion,
            Boolean specialProject ) {

        Inversion inversionEntity = ( Inversion ) getVariant( inversion );

        if ( specialProject ) {
            inversionEntity = ( Inversion ) getSpecialProjectVariant( inversion );
        } else {
            inversionEntity = ( Inversion ) getVariant( inversion );
        }

        if ( inversionEntity.getId() == null ) {
            addSubjectVariantToProject( project, inversion.getPatientId(), inversionEntity, specialProject );
        }

    }

    private Variant getVariant( VariantValueObject vvo ) {

        Variant entity = variantDao.findByUserVariantId( vvo.getUserVariantId(), vvo.getPatientId() );
        if ( entity == null ) {
            entity = getVariantEntity( vvo );
        }

        addCommonVariantData( entity, vvo );

        return entity;

    }

    private Variant getSpecialProjectVariant( VariantValueObject vvo ) {

        Variant entity = getVariantEntity( vvo );

        addCommonVariantData( entity, vvo );

        return entity;

    }

    private Variant getVariantEntity( VariantValueObject vvo ) {
        Variant entity;
        if ( vvo instanceof CNVValueObject ) {
            entity = new CNV();
            entity.setUserVariantId( vvo.getUserVariantId() );
        } else if ( vvo instanceof SNVValueObject ) {
            entity = new SNV();
            entity.setUserVariantId( vvo.getUserVariantId() );
        } else if ( vvo instanceof IndelValueObject ) {
            entity = new Indel();
            entity.setUserVariantId( vvo.getUserVariantId() );
        } else if ( vvo instanceof InversionValueObject ) {
            entity = new Inversion();
            entity.setUserVariantId( vvo.getUserVariantId() );
        } else {
            throw new RuntimeException();
        }

        return entity;

    }

    @Override
    @Transactional
    public void addSubjectPhenotypesToProject( String projectName, boolean createProject,
            List<PhenotypeValueObject> voList ) throws Exception {

        Project proj;
        if ( createProject ) {

            proj = new Project();
            proj.setName( projectName );
            proj = createProject( projectName, "" );

        } else {
            proj = projectDao.findByProjectName( projectName );
            if ( proj == null ) {
                throw new Exception( "Project does not exist" );
            }
        }

        createSubjectPhenotypesFromPhenotypeValueObjects( proj, voList );

    }

    @Override
    @Transactional
    public String isProjectHasSubjectPhenotypes( String projectName ) throws Exception {

        String returnString = "";

        Project proj = projectDao.findByProjectName( projectName );
        if ( proj == null ) {
            returnString = "Project does not exist";
        }
        Collection<Long> projectIds=new ArrayList<Long>();
        projectIds.add( proj.getId() );
        
       Collection<String> phenotypes = phenotypeDao.getExistingNames( projectIds ) ;
       if (phenotypes.size() > 0 ){
           returnString ="Phenotype exist";
       }
        return returnString;

    }

    @Override
    @Transactional
    public void addSubjectPhenotypesToSpecialProject( String projectName, boolean deleteProject,
            List<PhenotypeValueObject> voList ) throws Exception {

        Project proj = createSpecialProject( projectName, deleteProject );

        createSubjectPhenotypesFromPhenotypeValueObjects( proj, voList );

    }

    // The PhenotypeValueObject class doesn't really fit well with this, using it anyway
    private void createSubjectPhenotypesFromPhenotypeValueObjects( Project project, List<PhenotypeValueObject> voList )
            throws InvalidDataException {
        log.info( "Adding " + voList.size() + " valueobjects" );
        for ( PhenotypeValueObject vo : voList ) {

            Phenotype p = new Phenotype();

            p.setName( vo.getName() );
            p.setValue( vo.getDbValue() );
            p.setValueType( vo.getValueType() );
            p.setUri( vo.getUri() );

            phenotypeDao.create( p );

            Subject subject = subjectDao.findByPatientId( project, vo.getExternalSubjectId() );

            if ( subject == null ) {
                log.info( "Adding new Subject and phenotype" );
                subject = new Subject();
                subject.setPatientId( vo.getExternalSubjectId() );
                subject = subjectDao.create( subject );
                subject.addPhenotype( p );
                subject.getProjects().add( project );

            } else {
                log.info( "Adding phenotype to existing subject " + p.getUri() );
                subject.addPhenotype( p );
            }
        }
    }

    private GenomicLocation getGenomicLocation( VariantValueObject v ) {

        GenomicLocation genomicLocation = new GenomicLocation();
        genomicLocation.setChromosome( v.getGenomicRange().getChromosome() );
        genomicLocation.setStart( v.getGenomicRange().getBaseStart() );
        genomicLocation.setEnd( v.getGenomicRange().getBaseEnd() );

        return genomicLocation;

    }

    private List<Characteristic> getCharacteristics( VariantValueObject v ) {

        List<Characteristic> characteristics = new ArrayList<Characteristic>();

        for ( CharacteristicValueObject cvo : v.getCharacteristics().values() ) {
            characteristics.add( new Characteristic( cvo.getKey(), cvo.getValue() ) );
        }

        return characteristics;
    }

    private String getNewVariantId( String patientId ) {

        String userVariantId;
        int ridiculousnessCount = 0;
        do {
            userVariantId = RandomStringUtils.randomAlphanumeric( 6 );

            ridiculousnessCount++;
            if ( ridiculousnessCount > 1000 ) {
                log.error( "This is ridiculous" );
                break;
            }
        } while ( variantDao.findByUserVariantId( userVariantId, patientId ) != null );

        return userVariantId;

    }

    private void addSubjectVariantToProject( Project project, String patientId, Variant v, Boolean specialProject ) {

        Subject subject = subjectDao.findByPatientId( project, patientId );

        boolean newSubject = false;
        if ( subject == null ) {
            newSubject = true;
            subject = new Subject();
            subject.setPatientId( patientId );
            subject = subjectDao.create( subject );
        }

        if ( !specialProject && ( v.getUserVariantId() == null || v.getUserVariantId().trim().isEmpty() ) ) {
            v.setUserVariantId( getNewVariantId( patientId ) );
        }

        v = variantDao.create( v );
        subject.addVariant( v );

        if ( newSubject ) {
            subject.getProjects().add( project );
        }
    }

    private void addCommonVariantData( Variant v, VariantValueObject vvo ) {
        v.setLocation( getGenomicLocation( vvo ) );
        v.setCharacteristics( getCharacteristics( vvo ) );
        v.setDescription( vvo.getDescription() );
        v.setExternalId( vvo.getExternalId() );
    }

    @Override
    @Transactional
    public void alterGroupWritePermissions( String projectName, String groupName, boolean grant ) {
        log.info( ( grant ? "Granting" : "Removing" ) + " write permissions for group:" + groupName + " on project:"
                + projectName );

        Project proj = projectDao.findByProjectName( projectName );

        if ( proj == null ) {
            log.error( "This project doesn't exist" );
            return;
        }

        alterWritePermissionForGroup( proj, groupName, grant );
        log.info( "FINISHED " + ( grant ? "Granting" : "Removing" ) + " write permissions for group:" + groupName
                + " on project:" + projectName );
    }

    /**
     * public Collection<String> getUsersForProject(String projectName){ Project proj = projectDao.findByProjectName(
     * projectName ); return securityservice.readableBy(proj ); }
     */

    private void alterWritePermissionForGroup( Project project, String groupName, boolean grant ) {

        List<Subject> subjects = project.getSubjects();

        if ( grant ) {
            securityService.makeWriteableByGroup( project, groupName );
        } else {
            securityService.makeUnreadableByGroup( project, groupName );
        }

        // TODO labels? other stuff that has been added?
        for ( Subject s : subjects ) {

            if ( grant ) {
                securityService.makeWriteableByGroup( s, groupName );
            } else {
                securityService.makeUnreadableByGroup( s, groupName );
            }

            for ( Variant v : s.getVariants() ) {

                if ( grant ) {
                    securityService.makeWriteableByGroup( v, groupName );
                } else {
                    securityService.makeUnreadableByGroup( v, groupName );
                }

                for ( Characteristic c : v.getCharacteristics() ) {
                    if ( grant ) {
                        securityService.makeWriteableByGroup( c, groupName );
                    } else {
                        securityService.makeUnreadableByGroup( c, groupName );
                    }

                }

            }

            for ( Phenotype phen : s.getPhenotypes() ) {

                if ( grant ) {
                    securityService.makeWriteableByGroup( phen, groupName );
                } else {
                    securityService.makeUnreadableByGroup( phen, groupName );
                }

            }

        }
    }

    @Override
    @Transactional
    public void deleteProject( String name ) throws Exception {

        Project project = projectDao.findByProjectName( name );

        if ( project == null ) {
            log.error( "That project doesn't exist" );
        }

        List<Subject> subjectsToRemove = new ArrayList<Subject>();
        List<Subject> subjects = project.getSubjects();

        for ( Subject s : subjects ) {
            s.getProjects().remove( project );
            if ( s.getProjects().size() == 1 ) {
                subjectsToRemove.add( s );
            } else {
                // this is when a subject has more than one project
            }
        }

        // might have to individually remove variants/labels/phenotypes etc. to get rid of acls
        for ( Subject s : subjectsToRemove ) {

            subjectDao.remove( s );
        }

        projectDao.remove( project );

    }

    @Override
    @Transactional
    public String createUserAndAssignToGroup( String userName, String password, String groupName ) {

        if ( password == null || userName == null ) {
            log.error( "missing username or password options" );
            return "missing username or password options";
        }

        try {

            userManager.loadUserByUsername( userName );

            log.info( "User already exists" );

        } catch ( UsernameNotFoundException e ) {

            String encodedPassword = passwordEncoder.encodePassword( password, userName );
            UserDetailsImpl u = new UserDetailsImpl( encodedPassword, userName, true, null, null, null, new Date() );

            userManager.createUser( u );

        }

        if ( userManager.groupExists( groupName ) ) {
            log.info( "Group already exists" );

        } else {

            List<GrantedAuthority> authos = new ArrayList<GrantedAuthority>();
            authos.add( new GrantedAuthorityImpl( groupName ) );
            userManager.createGroup( groupName, authos );

        }

        userManager.addUserToGroup( userName, groupName );

        return "success";

    }

    @Override
    public List<String> getVariantUploadWarnings( String projectName, List<VariantValueObject> valueObjects ) {

        // TODO we need to warn the user when we are modifying a subject that exists in another project
        // this is currently impossible because the upload code will always make a new subject if the patientId doesn't
        // exist in the current project.

        return null;

    }

    @Override
    @Transactional
    public void populateProjectToProjectOverlap( String projectName, String overlappingProjectName )
            throws ExternalDependencyException, NotLoggedInException {

        Project specialProject = projectDao.findByProjectName( overlappingProjectName );

        Project projectToPopulate = projectDao.findByProjectName( projectName );

        ProjectFilterConfig specialProjectFilterConfig = getProjectFilterConfigById( specialProject );

        ProjectFilterConfig projectToPopulateFilterConfig = getProjectFilterConfigById( projectToPopulate );

        HashSet<AspireDbFilterConfig> projSet = new HashSet<AspireDbFilterConfig>();
        projSet.add( projectToPopulateFilterConfig );

        BoundedList<VariantValueObject> projToPopulateVvos = queryService.queryVariants( projSet );

        // This probably won't work for all variant types
        for ( VariantValueObject vvo : projToPopulateVvos.getItems() ) {

            Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();

            filters.add( specialProjectFilterConfig );
            filters.add( getVariantFilterConfigForSingleVariant( vvo ) );

            BoundedList<VariantValueObject> overLappedVvos = queryService.queryVariants( filters );

            for ( VariantValueObject vvoOverlapped : overLappedVvos.getItems() ) {

                if ( !vvo.getGenomicRange().getChromosome().equals( vvoOverlapped.getGenomicRange().getChromosome() ) ) {
                    continue;
                }

                Variant2SpecialVariantOverlap overlapInfo = new Variant2SpecialVariantOverlap();

                int start = Math.max( vvo.getGenomicRange().getBaseStart(), vvoOverlapped.getGenomicRange()
                        .getBaseStart() );
                int end = Math.min( vvo.getGenomicRange().getBaseEnd(), vvoOverlapped.getGenomicRange().getBaseEnd() );

                // genius
                if ( start < end ) {

                    int overlap = end - start;

                    float vvoSize = vvo.getGenomicRange().getBaseEnd() - vvo.getGenomicRange().getBaseStart();
                    float vvoOverlappedSize = vvoOverlapped.getGenomicRange().getBaseEnd()
                            - vvoOverlapped.getGenomicRange().getBaseStart();

                    float vvoPercentageOverlap = overlap / vvoSize * 100;
                    float vvoOverlappedPercentageOverlap = overlap / vvoOverlappedSize * 100;

                    overlapInfo.setOverlap( overlap );
                    overlapInfo.setOverlapPercentage( Math.round( vvoPercentageOverlap ) );
                    // set the percentage overlap of the OverlapSpecialVariantId-variant, I realize that these method
                    // and variable names kind of suck
                    overlapInfo.setOverlappedOverlapPercentage( Math.round( vvoOverlappedPercentageOverlap ) );

                    overlapInfo.setVariantId( vvo.getId() );
                    overlapInfo.setOverlapSpecialVariantId( vvoOverlapped.getId() );
                    overlapInfo.setOverlapProjectId( specialProject.getId() );
                    overlapInfo.setProjectId( projectToPopulate.getId() );

                    variant2SpecialVariantOverlapDao.create( overlapInfo );

                } else {
                    log.info( "No Overlap" );
                }

            }

        }

    }

    private ProjectFilterConfig getProjectFilterConfigById( Project p ) {

        ProjectFilterConfig projectFilterConfig = new ProjectFilterConfig();

        ArrayList<Long> projectIds = new ArrayList<Long>();

        projectIds.add( p.getId() );

        projectFilterConfig.setProjectIds( projectIds );

        return projectFilterConfig;

    }

    private VariantFilterConfig getVariantFilterConfigForSingleVariant( VariantValueObject v ) {

        SetRestriction genomicRangeRestriction = new SetRestriction();

        GenomicLocationProperty genomicLocationProperty = new GenomicLocationProperty();

        genomicLocationProperty.setDataType( new GenomicRangeDataType() );

        genomicRangeRestriction.setProperty( genomicLocationProperty );

        genomicRangeRestriction.setOperator( Operator.IS_IN_SET );

        GenomicRange gr = v.getGenomicRange();

        Set genomicRangeSet = new HashSet();

        genomicRangeSet.add( gr );

        genomicRangeRestriction.setValues( genomicRangeSet );

        VariantFilterConfig variantFilterConfig = new VariantFilterConfig();

        variantFilterConfig.setRestriction( genomicRangeRestriction );

        return variantFilterConfig;
    }

}