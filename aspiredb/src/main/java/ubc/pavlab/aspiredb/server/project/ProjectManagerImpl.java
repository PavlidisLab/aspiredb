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

import gemma.gsec.SecurityService;
import gemma.gsec.acl.domain.AclService;
import gemma.gsec.authentication.UserDetailsImpl;
import gemma.gsec.authentication.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.dao.CharacteristicDao;
import ubc.pavlab.aspiredb.server.dao.GenomicLocationDao;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
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
import ubc.pavlab.aspiredb.server.model.Variant2VariantOverlap;
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
 * Class for project operations such as create, update, and delete. Project creation includes addition of subjects and
 * variants.
 * 
 * @author cmcdonald
 * @version $Id: ProjectManagerImpl.java,v 1.33 2013/06/24 20:24:44 cmcdonald Exp $
 */
@Service("projectManager")
public class ProjectManagerImpl implements ProjectManager {

    protected static Log log = LogFactory.getLog( ProjectManagerImpl.class );

    @Autowired
    private GenomicLocationDao genomicLocDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private VariantDao variantDao;

    @Autowired
    private CharacteristicDao characteristicDao;

    @Autowired
    private Variant2SpecialVariantOverlapDao variant2SpecialVariantOverlapDao;

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    private PhenotypeUtil phenotypeService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserManager userManager;

    @Autowired
    private QueryService queryService;

    @Autowired
    private AclService aclService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    // private ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

    public static final String DGV_SUPPORT_CHARACTERISTIC_KEY = "pubmedid";

    public enum SpecialProject {
        DGV, DECIPHER
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

        StopWatch timer = new StopWatch();
        timer.start();
        createSubjectPhenotypesFromPhenotypeValueObjects( proj, voList );
        log.info( "Added " + voList.size() + " phenotypes to " + proj.getName() + " which took " + timer.getTime()
                + " ms" );

    }

    @Override
    @Transactional
    public void addSubjectPhenotypesToSpecialProject( String projectName, boolean deleteProject,
            List<PhenotypeValueObject> voList ) throws Exception {

        Project proj = createSpecialProject( projectName, deleteProject );

        createSubjectPhenotypesFromPhenotypeValueObjects( proj, voList );

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

    public static boolean isSpecialProject( String name ) {
        return name.equals( SpecialProject.DECIPHER.toString() ) || name.equals( SpecialProject.DGV.toString() );
    }

    @Transactional
    public Project createSpecialProject( String name, boolean deleteProject ) throws Exception {

        name = name.toUpperCase();

        if ( !isSpecialProject( name ) ) {
            throw new Exception( "Special project names limited to 'DECIPHER' or 'DGV'" );
        }

        if ( projectDao.findByProjectName( name ) != null ) {

            if ( deleteProject ) {
            	log.info("Deleting project: " + name);
                deleteProject( name );

            } else {
                return projectDao.findByProjectName( name );
            }

        }

        Project p = new Project();
        p.setName( name );
        p.setSpecialData( true );

        if ( name.equals( SpecialProject.DGV.toString() ) ) {
            p.setVariantSupportCharacteristicKey( DGV_SUPPORT_CHARACTERISTIC_KEY );
        }

        return projectDao.create( p );
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

            // String encodedPassword = passwordEncoder.encodePassword( password, userName );
            String encodedPassword = passwordEncoder.encode( password );
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
    @Transactional
    public void deleteProject( String name ) throws Exception {

        StopWatch timer = new StopWatch();
        timer.start();

        Project project = projectDao.findByProjectName( name );

        if ( project == null ) {
            log.warn( "Project " + name + " doesn't exist" );
            return;
        }

        List<Subject> subjects = project.getSubjects();

        Collection<Characteristic> characteristics = new HashSet<>();
        Collection<Variant> variants = new HashSet<>();
        Collection<Phenotype> phenotypes = new HashSet<>();

        for ( Subject s : subjects ) {
            for ( Phenotype p : s.getPhenotypes() ) {
                phenotypes.add( p );
            }
            for ( Variant v : s.getVariants() ) {
                variants.add( v );
                // not required, removed by cascade
                for ( Characteristic c : v.getCharacteristics() ) {
                    characteristics.add( c );
                }
                v.getCharacteristics().removeAll( characteristics );
            }
        }

        variant2SpecialVariantOverlapDao.deleteByProjectId( project.getId() );
        variant2SpecialVariantOverlapDao.deleteByOverlapProjectId( project.getId() );

        for ( Subject s : subjects ) {
            s.getPhenotypes().removeAll( phenotypes );
            s.getVariants().removeAll( variants );
            s.getProjects().remove( project );
        }

        // We don't need to do this anymore because we use
        // the hibernate annotation "orphanRemoval=true"
        // characteristicDao.remove( characteristics );
        // variantDao.remove( variants );
        // phenotypeDao.remove( phenotypes );

        try {
            subjectDao.remove( subjects );
        } catch ( Exception e ) {
            log.error( e );
        }

        try {
            projectDao.remove( project );
        } catch ( Exception e ) {
            log.error( e );
        }

        log.info( project + " has been deleted which took " + timer.getTime() + " ms" );
    }

    @Override
    @Transactional
    public Project findProject( String projectName ) throws Exception {
        Project project = projectDao.findByProjectName( projectName );
        if ( project != null ) {
            return project;
        }
        return null;

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
    public String isProjectHasSubjectPhenotypes( String projectName ) throws Exception {

        String returnString = "";

        Project proj = projectDao.findByProjectName( projectName );
        if ( proj == null ) {
            returnString = "Project does not exist";
        }
        Collection<Long> projectIds = new ArrayList<>();
        // FIXME proj can be null!
        projectIds.add( proj.getId() );

        Collection<String> phenotypes = phenotypeDao.getExistingNames( projectIds );
        if ( phenotypes.size() > 0 ) {
            returnString = "Phenotype exist";
        }
        return returnString;

    }

    @Override
    @Transactional
    public Collection<Variant2VariantOverlap> populateProjectToProjectOverlap( String projectName,
            String overlappingProjectName ) throws ExternalDependencyException, NotLoggedInException {

        Project projectToPopulate = projectDao.findByProjectName( projectName );

        ProjectFilterConfig projectToPopulateFilterConfig = getProjectFilterConfigById( projectToPopulate );

        Set<AspireDbFilterConfig> projSet = new HashSet<>();
        projSet.add( projectToPopulateFilterConfig );

        BoundedList<VariantValueObject> projToPopulateVvos = queryService.queryVariants( projSet );

        return populateProjectToProjectOverlap( projectName, overlappingProjectName, projToPopulateVvos.getItems() );
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public Collection<Variant2VariantOverlap> populateProjectToProjectOverlap( String projectName,
            String overlappingProjectName, Collection<VariantValueObject> projToPopulateVvos )
            throws ExternalDependencyException, NotLoggedInException {

        if ( projectName.equals( overlappingProjectName ) ) {
            log.warn( "Project name is the same as the overlapping project name! No overlaps computed." );
            return new ArrayList<>();
        }

        StopWatch timer = new StopWatch();
        timer.start();

        Project specialProject = projectDao.findByProjectName( overlappingProjectName );

        Project projectToPopulate = projectDao.findByProjectName( projectName );

        ProjectFilterConfig projectToPopulateFilterConfig = getProjectFilterConfigById( projectToPopulate );

        Set<AspireDbFilterConfig> projSet = new HashSet<>();
        projSet.add( projectToPopulateFilterConfig );

        Collection<Variant2VariantOverlap> overlapVos = new HashSet<>();

        // This probably won't work for all variant types
        int i = 0;
        for ( VariantValueObject vvo : projToPopulateVvos ) {

            Collection<VariantValueObject> overlappedVvos = variantDao.findByGenomicLocationQuick(
                    vvo.getGenomicRange(), Collections.singletonList( specialProject.getId() ) );
            for ( VariantValueObject vvoOverlapped : overlappedVvos ) {
                overlapVos.add( new Variant2VariantOverlap( vvo, vvoOverlapped, projectToPopulate.getId(),
                        specialProject.getId() ) );
            }

            if ( ( ++i % 100 ) == 0 ) {
                log.info( " Processed " + i + "/" + projToPopulateVvos.size() + " ("
                        + String.format( "%.2f", ( 100.0 * i / projToPopulateVvos.size() ) ) + " %) variants in "
                        + projectName + " and found " + overlapVos.size() + " overlaps with " + overlappingProjectName );
            }
        }

        overlapVos = ( Collection<Variant2VariantOverlap> ) variant2SpecialVariantOverlapDao.create( overlapVos );

        // variantDao.printCacheStatistics();

        log.info( "Found " + overlapVos.size() + " new variant overlaps between " + projectName + " and "
                + overlappingProjectName + " which took " + timer.getTime() + " ms" );

        return overlapVos;
    }

    private void addCommonVariantData( Variant v, VariantValueObject vvo ) {
        v.setLocation( getGenomicLocation( vvo ) );
        v.setCharacteristics( getCharacteristics( vvo ) );
        v.setDescription( vvo.getDescription() );
        v.setExternalId( vvo.getExternalId() );
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

        for ( Subject s : subjects ) {

            if ( grant ) {
                securityService.makeWriteableByGroup( s, groupName );
            } else {
                securityService.makeUnreadableByGroup( s, groupName );
            }

        }
    }

    // The PhenotypeValueObject class doesn't really fit well with this, using it anyway
    @Transactional
    public void createSubjectPhenotypesFromPhenotypeValueObjects( Project project, List<PhenotypeValueObject> voList )
            throws InvalidDataException {

        // gather all patient IDs and create new subjects first in bulk
        Collection<String> patientIds = new HashSet<>();
        for ( PhenotypeValueObject vo : voList ) {
            patientIds.add( vo.getExternalSubjectId() );
        }
        HashMap<String, Subject> subjects = findOrCreateByPatientIds( project, patientIds );

        Collection<Phenotype> phenotypes = new HashSet<>();

        log.info( "Adding " + voList.size() + " valueobjects" );
        for ( PhenotypeValueObject vo : voList ) {

            Phenotype p = new Phenotype();

            p.setName( vo.getName() );
            p.setValue( vo.getDbValue() );
            p.setValueType( vo.getValueType() );
            p.setUri( vo.getUri() );

            // phenotypeDao.create( p );
            phenotypes.add( p );

            // Subject subject = subjectDao.findByPatientId( project, vo.getExternalSubjectId() );
            Subject subject = subjects.get( vo.getExternalSubjectId() );

            if ( subject == null ) {
                subject = new Subject();
                subject.setPatientId( vo.getExternalSubjectId() );
                subject = subjectDao.create( subject );
                subject.addPhenotype( p );
                subject.getProjects().add( project );
            } else {
                log.debug( "Adding phenotype to existing subject " + p.getUri() );
                subject.addPhenotype( p );
            }
        }

        StopWatch timer = new StopWatch();
        timer.start();
        phenotypeDao.create( phenotypes );
        log.info( "phenotypeDao.create took " + timer.getTime() + " ms for " + phenotypes.size() + " phenotypes" );
    }

    private void writeIndelValueObjects( Project project, Collection<VariantValueObject> vos,
            Map<String, Subject> patientIdEntityMap, Boolean specialProject ) {

        for ( VariantValueObject vo : vos ) {
            IndelValueObject indel = ( IndelValueObject ) vo;
            Indel indelEntity = ( Indel ) getVariant( indel );

            if ( specialProject ) {
                indelEntity = ( Indel ) getSpecialProjectVariant( indel );
            } else {
                indelEntity = ( Indel ) getVariant( indel );
            }

            indelEntity.setIndelLength( indel.getIndelLength() );

            indelEntity.setSubject( patientIdEntityMap.get( vo.getPatientId() ) );

            patientIdEntityMap.get( vo.getPatientId() ).addVariant( indelEntity );

            vo.setId( variantDao.create( indelEntity ).getId() );
        }
    }

    @Transactional
    private void writeInversionValueObjects( Project project, Collection<VariantValueObject> vos,
            Map<String, Subject> patientIdEntityMap, Boolean specialProject ) {

        for ( VariantValueObject vo : vos ) {
            InversionValueObject inversion = ( InversionValueObject ) vo;
            Inversion inversionEntity = ( Inversion ) getVariant( inversion );

            if ( specialProject ) {
                inversionEntity = ( Inversion ) getSpecialProjectVariant( inversion );
            } else {
                inversionEntity = ( Inversion ) getVariant( inversion );
            }
            patientIdEntityMap.get( vo.getPatientId() ).addVariant( inversionEntity );

            vo.setId( variantDao.create( inversionEntity ).getId() );
        }
    }

    private void writeSNVValueObjects( Project project, Collection<VariantValueObject> vos,
            Map<String, Subject> patientIdEntityMap, Boolean specialProject ) {

        for ( VariantValueObject vo : vos ) {
            SNVValueObject snv = ( SNVValueObject ) vo;
            SNV snvEntity = null;
            if ( specialProject ) {
                snvEntity = ( SNV ) getSpecialProjectVariant( snv );
            } else {
                snvEntity = ( SNV ) getVariant( snv );
            }
            snvEntity.setReferenceBase( snv.getReferenceBase() );
            snvEntity.setObservedBase( snv.getObservedBase() );
            snvEntity.setDbSNPID( snv.getDbSNPID() );

            snvEntity.setSubject( patientIdEntityMap.get( vo.getPatientId() ) );

            patientIdEntityMap.get( vo.getPatientId() ).addVariant( snvEntity );

            vo.setId( variantDao.create( snvEntity ).getId() );
        }
    }

    public void createSubjectVariantsFromVariantValueObjects( Project project, List<VariantValueObject> voList ) {
        createSubjectVariantsFromVariantValueObjects( project, voList, false );
    }

    @Transactional
    public HashMap<String, Subject> findOrCreateByPatientIds( Project project, Collection<String> patientIds ) {
        Collection<Subject> allEntities = new HashSet<>();
        Collection<Subject> newEntities = new HashSet<>();
        Collection<Subject> foundEntities = subjectDao.findByPatientIds( project, patientIds );
        Collection<String> foundIds = new HashSet<>();
        for ( Subject s : foundEntities ) {
            foundIds.add( s.getPatientId() );
        }
        for ( String id : patientIds ) {
            if ( !foundIds.contains( id ) ) {
                Subject s = new Subject();
                s.setPatientId( id );
                s.getProjects().add( project );
                newEntities.add( s );
            }
        }
        allEntities.addAll( foundEntities );
        StopWatch timer = new StopWatch();
        timer.start();
        allEntities.addAll( subjectDao.create( newEntities ) );

        HashMap<String, Subject> map = new HashMap<>();
        for ( Subject s : allEntities ) {
            map.put( s.getPatientId(), s );
        }

        log.info( "subjectDao create took " + timer.getTime() + " ms" );
        return map;
    }

    @Transactional
    public void createSubjectVariantsFromVariantValueObjects( Project project, List<VariantValueObject> voList,
            Boolean specialProject ) {

        log.info( "Adding " + voList.size() + " value objects" );
        Collection<String> patientIds = extractPatientIds( voList );
        Map<String, Subject> patientIdEntityMap = findOrCreateByPatientIds( project, patientIds );

        if ( voList.size() == 0 ) {
            log.warn( "No variants to add!" );
            return;
        }

        if ( voList.iterator().next() instanceof CNVValueObject ) {
            writeCNVValueObjects( project, voList, patientIdEntityMap, specialProject );
        } else if ( voList.iterator().next() instanceof SNVValueObject ) {
            writeSNVValueObjects( project, voList, patientIdEntityMap, specialProject );
        } else if ( voList.iterator().next() instanceof IndelValueObject ) {
            writeIndelValueObjects( project, voList, patientIdEntityMap, specialProject );
        } else if ( voList.iterator().next() instanceof InversionValueObject ) {
            writeInversionValueObjects( project, voList, patientIdEntityMap, specialProject );
        } else {
            log.warn( "TODO Not implemented yet!" );
        }
    }

    private void writeCNVValueObjects( Project project, Collection<VariantValueObject> vos,
            Map<String, Subject> patientIdEntityMap, Boolean specialProject ) {

        for ( VariantValueObject vo : vos ) {
            CNVValueObject cnv = ( CNVValueObject ) vo;
            CNV cnvEntity = null;
            if ( specialProject ) {
                cnvEntity = ( CNV ) getSpecialProjectVariant( cnv );
            } else {
                cnvEntity = ( CNV ) getVariant( cnv );
            }
            cnvEntity.setType( CnvType.valueOf( cnv.getType().toUpperCase() ) );
            cnvEntity.setCopyNumber( cnv.getCopyNumber() );
            cnvEntity.setCnvLength( cnv.getCnvLength() );

            cnvEntity.setSubject( patientIdEntityMap.get( vo.getPatientId() ) );

            patientIdEntityMap.get( vo.getPatientId() ).addVariant( cnvEntity );

            vo.setId( variantDao.create( cnvEntity ).getId() );
        }

    }

    private Collection<String> extractPatientIds( List<VariantValueObject> voList ) {
        Collection<String> results = new HashSet<>();
        for ( VariantValueObject vo : voList ) {
            results.add( vo.getPatientId() );
        }
        return results;
    }

    private List<Characteristic> getCharacteristics( VariantValueObject v ) {

        List<Characteristic> characteristics = new ArrayList<Characteristic>();

        for ( CharacteristicValueObject cvo : v.getCharacteristics().values() ) {
            characteristics.add( new Characteristic( cvo.getKey(), cvo.getValue() ) );
        }

        return characteristics;
    }

    private GenomicLocation getGenomicLocation( VariantValueObject v ) {

        GenomicLocation genomicLocation = new GenomicLocation( v.getGenomicRange().getChromosome(), v.getGenomicRange()
                .getBaseStart(), v.getGenomicRange().getBaseEnd() );

        return genomicLocation;

    }

    private ProjectFilterConfig getProjectFilterConfigById( Project p ) {

        ProjectFilterConfig projectFilterConfig = new ProjectFilterConfig();

        ArrayList<Long> projectIds = new ArrayList<Long>();

        projectIds.add( p.getId() );

        projectFilterConfig.setProjectIds( projectIds );

        return projectFilterConfig;

    }

    private Variant getSpecialProjectVariant( VariantValueObject vvo ) {

        Variant entity = getVariantEntity( vvo );

        addCommonVariantData( entity, vvo );

        return entity;

    }

    private Variant getVariant( VariantValueObject vvo ) {

        Variant entity = variantDao.findByUserVariantId( vvo.getUserVariantId(), vvo.getPatientId() );
        if ( entity == null ) {
            entity = getVariantEntity( vvo );
        }

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

    private VariantFilterConfig getVariantFilterConfigForSingleVariant( VariantValueObject v ) {

        SetRestriction genomicRangeRestriction = new SetRestriction();

        GenomicLocationProperty genomicLocationProperty = new GenomicLocationProperty();

        genomicLocationProperty.setDataType( new GenomicRangeDataType() );

        genomicRangeRestriction.setProperty( genomicLocationProperty );

        genomicRangeRestriction.setOperator( Operator.IS_IN_SET );

        GenomicRange gr = v.getGenomicRange();

        Set genomicRangeSet = Collections.singleton( gr );

        genomicRangeRestriction.setValues( genomicRangeSet );

        VariantFilterConfig variantFilterConfig = new VariantFilterConfig();

        variantFilterConfig.setRestriction( genomicRangeRestriction );

        return variantFilterConfig;
    }
}
