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
import java.util.Date;
import java.util.List;

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
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
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
import ubc.pavlab.aspiredb.server.security.SecurityService;
import ubc.pavlab.aspiredb.server.security.authentication.UserDetailsImpl;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.IndelValueObject;
import ubc.pavlab.aspiredb.shared.InversionValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.SNVValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

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

    @Autowired
    PhenotypeDao phenotypeDao;

    @Autowired
    PhenotypeUtil phenotypeService;

    @Autowired
    SecurityService securityService;
    
    @Autowired
    UserManager userManager;
    
    ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

    @Transactional
    public Project createProject( String name ) throws Exception {
        if ( projectDao.findByProjectName( name ) != null ) {
            throw new Exception( "project with that name already exists" );

        }

        Project p = new Project();
        p.setName( name );

        return projectDao.create( p );
    }

    
    /* (non-Javadoc)
     * @see ubc.pavlab.aspiredb.server.project.ProjectManager#addSubjectVariantsToProject(java.lang.String, boolean, java.util.List)
     * createProject parameter is included to make accidental overwriting via cli's more difficult
     */
    @Transactional
    public void addSubjectVariantsToProject( String projectName, boolean createProject, List<VariantValueObject> voList )
            throws Exception {

        Project proj;
        if ( createProject ) {
            proj = createProject( projectName );
        } else {
            proj = projectDao.findByProjectName( projectName );
            if ( proj == null ) throw new Exception( "Project does not exist" );
        }

        createSubjectVariantsFromVariantValueObjects( proj, voList );

    }
    
    /**
     * @param projectName
     * @param voList
     * @throws Exception
     */
    @Transactional
    public void addSubjectVariantsToProjectForceCreate( String projectName, List<VariantValueObject> voList )
            throws Exception {

        Project proj;
        
        proj = projectDao.findByProjectName( projectName );
        if ( proj == null ) {
            proj = createProject( projectName );
        } 

        createSubjectVariantsFromVariantValueObjects( proj, voList );

    }

    @Transactional
    private void createSubjectVariantsFromVariantValueObjects( Project project, List<VariantValueObject> voList ) {

        for ( VariantValueObject vo : voList ) {
            if ( vo instanceof CNVValueObject ) {
                createSubjectVariantFromCNVValueObject( project, ( CNVValueObject ) vo );
            } else if ( vo instanceof SNVValueObject ) {
                createSubjectVariantFromSNVValueObject( project, ( SNVValueObject ) vo );
            } else if ( vo instanceof IndelValueObject ) {
                createSubjectVariantFromIndelValueObject( project, ( IndelValueObject ) vo );
            } else if ( vo instanceof InversionValueObject ) {
                createSubjectVariantFromInversionValueObject( project, ( InversionValueObject ) vo );
            } else {
                log.error( "unsupported VariantValueObject" );
            }
        }

    }

    @Transactional
    private void createSubjectVariantFromCNVValueObject( Project project, CNVValueObject cnv ) {

        CNV cnvEntity = ( CNV ) getVariant(cnv);
        
        cnvEntity.setType( CnvType.valueOf( cnv.getType().toUpperCase() ) );
        cnvEntity.setCopyNumber( cnv.getCopyNumber() );
        cnvEntity.setCnvLength( cnv.getCnvLength() );
        
        
        if ( cnvEntity.getId()==null ) {
            addSubjectVariantToProject( project, cnv.getPatientId(), cnvEntity );
        }
    }

    @Transactional
    private void createSubjectVariantFromSNVValueObject( Project project, SNVValueObject snv ) {
        SNV snvEntity = ( SNV ) getVariant(snv);
        
        snvEntity.setReferenceBase( snv.getReferenceBase() );
        snvEntity.setObservedBase( snv.getObservedBase() );
        
        snvEntity.setDbSNPID( snv.getDbSNPID() );        
        
        if ( snvEntity.getId()==null ) {
            addSubjectVariantToProject( project, snv.getPatientId(), snvEntity );
        }
    }

    @Transactional
    private void createSubjectVariantFromIndelValueObject( Project project, IndelValueObject indel ) {
        
        Indel indelEntity = ( Indel ) getVariant(indel);
        
        indelEntity.setIndelLength( indel.getLength() );

        if (indelEntity.getId()==null){
            addSubjectVariantToProject( project, indel.getPatientId(), indelEntity );
        }
    }

    @Transactional
    private void createSubjectVariantFromInversionValueObject( Project project, InversionValueObject inversion ) {

        Inversion inversionEntity = (Inversion) getVariant(inversion);
        
        if (inversionEntity.getId()==null){
            addSubjectVariantToProject( project, inversion.getPatientId(), inversionEntity );
        }

    }
    
    private Variant getVariant(VariantValueObject vvo){
        
        Variant entity = variantDao.findByUserVariantId( vvo.getUserVariantId(), vvo.getPatientId() );
        if ( entity == null ) {            
            if (vvo instanceof CNVValueObject){
                entity = new CNV();
                entity.setUserVariantId( vvo.getUserVariantId() );
            }else if (vvo instanceof SNVValueObject){
                entity = new SNV();
                entity.setUserVariantId( vvo.getUserVariantId() );
            }else if (vvo instanceof IndelValueObject){
                entity = new Indel();
                entity.setUserVariantId( vvo.getUserVariantId() );
            }else if (vvo instanceof InversionValueObject){
                entity = new Inversion();
                entity.setUserVariantId( vvo.getUserVariantId() );
            }else{
                throw new RuntimeException();
            }
            
        }
        
                
        addCommonVariantData( entity, vvo );
        
        return entity;
        
    }

    @Transactional
    public void addSubjectPhenotypesToProject( String projectName, boolean createProject,
            List<PhenotypeValueObject> voList ) throws Exception {

        Project proj;
        if ( createProject ) {

            proj = new Project();
            proj.setName( projectName );
            proj = createProject( projectName );

        } else {
            proj = projectDao.findByProjectName( projectName );
            if ( proj == null ) throw new Exception( "Project does not exist" );
        }

        createSubjectPhenotypesFromPhenotypeValueObjects( proj, voList );

    }

    // The PhenotypeValueObject class doesn't really fit well with this, using it anyway    
    private void createSubjectPhenotypesFromPhenotypeValueObjects( Project project, List<PhenotypeValueObject> voList ) throws InvalidDataException {

        for ( PhenotypeValueObject vo : voList ) {

            Phenotype p = new Phenotype();
            
            p.setName( vo.getName() );
            p.setValue( vo.getDbValue() );
            p.setValueType( vo.getValueType() );
            p.setUri( vo.getUri() );

            phenotypeDao.create( p );

            Subject subject = subjectDao.findByPatientId( project, vo.getExternalSubjectId() );

            if ( subject == null ) {
                subject = new Subject();
                subject.setPatientId( vo.getExternalSubjectId() );
                subject = subjectDao.create( subject );
                subject.addPhenotype( p );
                projectDao.addSubjectToProject( project, subject );
            } else {
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
        int ridiculousnessCount=0;
        do{           
           userVariantId = RandomStringUtils.randomAlphanumeric( 6 );
           
           ridiculousnessCount++;           
           if (ridiculousnessCount>1000){
               log.error( "This is ridiculous" );
               break;
           }
        } while (variantDao.findByUserVariantId( userVariantId, patientId )!=null);
        
        return userVariantId;        

    }

    private void addSubjectVariantToProject( Project project, String patientId, Variant v ) {

        Subject subject = subjectDao.findByPatientId( project, patientId );

        boolean newSubject = false;
        if ( subject == null ) {
            newSubject = true;
            subject = new Subject();
            subject.setPatientId ( patientId );
            subject = subjectDao.create( subject );
        }

        if ( v.getUserVariantId() == null || v.getUserVariantId().trim().isEmpty() ) {
            v.setUserVariantId( getNewVariantId(patientId) );
        }
        
        v = variantDao.create( v );
        subject.addVariant( v );

        if ( newSubject ) {
            projectDao.addSubjectToProject( project, subject );
        }
    }

    private void addCommonVariantData( Variant v, VariantValueObject vvo ) {
        v.setLocation( getGenomicLocation( vvo ) );
        v.setCharacteristics( getCharacteristics( vvo ) );
        v.setDescription( vvo.getDescription() );
        v.setExternalId( vvo.getExternalId() );
    }
    
    @Transactional
    public void alterGroupWritePermissions( String projectName, String groupName, boolean grant ) {
        log.info( (grant?"Granting":"Removing") + " write permissions for group:"+groupName+" on project:"+projectName );
        
        Project proj = projectDao.findByProjectName( projectName );
        
        if (proj==null){
            log.error( "This project doesn't exist");
            return;
        }
        
        alterWritePermissionForGroup( proj,  groupName, grant );
        log.info( "FINISHED "+ (grant?"Granting":"Removing") + " write permissions for group:"+groupName+" on project:"+projectName );
    }
    
    
    private void alterWritePermissionForGroup( Project project, String groupName, boolean grant ) {

        List<Subject> subjects = project.getSubjects();
                
        if (grant){
            securityService.makeWriteableByGroup( project, groupName );
        }else{
            securityService.makeUnreadableByGroup( project, groupName );
        }        

        //TODO labels? other stuff that has been added?
        for ( Subject s : subjects ) {
               
            if (grant){
                securityService.makeWriteableByGroup( s, groupName );
            }else{
                securityService.makeUnreadableByGroup( s, groupName );
            }
           
            for ( Variant v : s.getVariants() ) {
                
                if (grant){
                    securityService.makeWriteableByGroup( v, groupName );
                }else{
                    securityService.makeUnreadableByGroup( v, groupName );
                }
                
                for (Characteristic c: v.getCharacteristics()){
                    if (grant){
                        securityService.makeWriteableByGroup( c, groupName );
                    }else{
                        securityService.makeUnreadableByGroup( c, groupName );
                    }
                    
                }
                
            }

            for ( Phenotype phen : s.getPhenotypes() ) {

                if (grant){
                    securityService.makeWriteableByGroup( phen, groupName );
                }else{
                    securityService.makeUnreadableByGroup( phen, groupName );
                }
                
            }
            
            
        }
    }
    
    
    @Transactional
    public void deleteProject( String name ) throws Exception {
        
        Project project = projectDao.findByProjectName( name );
        
        if ( project == null ) {
            log.error( "That project doesn't exist" );
        }
        
        List<Subject> subjectsToRemove = new ArrayList<Subject>();
        List<Subject> subjects = project.getSubjects();
        
        for (Subject s: subjects){            
            if (s.getProjects().size()>1){
                s.getProjects().remove( project );
            }else{
                subjectsToRemove.add( s );
            }            
        }
        
        //might have to individually remove variants/labels/phenotypes etc. to get rid of acls
        for (Subject s: subjectsToRemove){
            subjectDao.remove( s );
        }
        
        
        projectDao.remove( project );
    }
    
    @Override
    @Transactional    
    public String createUserAndAssignToGroup(String userName, String password, String groupName){
        
        if ( password == null || userName == null ) {
            log.error( "missing username or password options" );
            return "missing username or password options";
        }

        try {

            userManager.loadUserByUsername( userName );
            
            log.info( "User already exists" );
            
        } catch ( UsernameNotFoundException e ) {

            String encodedPassword = passwordEncoder.encodePassword( password, userName );
            UserDetailsImpl u = new UserDetailsImpl( encodedPassword, userName, true, null, null, null,
                    new Date() );

            userManager.createUser( u );

        }
        
        
        if ( userManager.groupExists( groupName ) ) {
            log.info( "Group already exists" );
            
        }else{

            List<GrantedAuthority> authos = new ArrayList<GrantedAuthority>();
            authos.add( new GrantedAuthorityImpl( groupName ) );
            userManager.createGroup( groupName, authos );
        
        }
        
        userManager.addUserToGroup( userName, groupName );
        
        return "success";
        
    }
    
    @Override
    public List<String> getVariantUploadWarnings(String projectName, List<VariantValueObject> valueObjects){
            
            //TODO we need to warn the user when we are modifying a subject that exists in another project
        //this is currently impossible because the upload code will always make a new subject if the patientId doesn't
        //exist in the current project.
        
        return null;
        
        
        
    }

}