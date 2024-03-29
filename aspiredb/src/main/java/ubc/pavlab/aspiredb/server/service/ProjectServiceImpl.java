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

import gemma.gsec.SecurityService;
import gemma.gsec.authentication.UserManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.RandomStringUtils;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadService;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadServiceResult;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadService;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadServiceResult;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant2VariantOverlap;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.server.project.ProjectManagerImpl.SpecialProject;
import ubc.pavlab.aspiredb.server.security.authentication.UserService;
import ubc.pavlab.aspiredb.server.util.ConfigUtils;
import ubc.pavlab.aspiredb.server.util.MailEngine;
import ubc.pavlab.aspiredb.server.util.MessageUtil;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.ProjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

/**
 * Services for project related tasks such as adding, deleting projects, altering user permissions, adding subjects,
 * variants and phenotypes.
 */
@Service
@RemoteProxy(name = "ProjectService")
public class ProjectServiceImpl implements ProjectService {

    private static Logger log = LoggerFactory.getLogger( ProjectService.class );

    @Autowired
    protected MailEngine mailEngine = null;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    private MessageUtil messageUtil;

    @Autowired
    ProjectDao projectDao;

    @Autowired
    ProjectManager projectManager;

    @Autowired
    OntologyService os;

    @Autowired
    SecurityService securityService;

    @Autowired
    UserManager userManager;

    @Autowired
    UserService userService;

    @Autowired
    PhenotypeUploadService phenotypeUploadService;

    @Override
    @RemoteMethod
    public List<ProjectValueObject> getProjects() {

        Collection<Project> projects = projectDao.loadAll();
        List<ProjectValueObject> vos = new ArrayList<ProjectValueObject>();

        for ( Project p : projects ) {
            vos.add( Project.convertToValueObject( p ) );
        }

        return vos;
    }

    @Override
    @RemoteMethod
    public Collection<String> getProjectUserNames( String projectName ) {
        Collection<String> userObject = new ArrayList<String>();

        Collection<String> userNames = new ArrayList<String>();

        Project proj = projectDao.findByProjectName( projectName );
        userNames = projectReadableBy( proj );

        for ( String userName : userNames ) {
            User user = ( User ) userService.findByUserName( userName );
            userObject.add( user.getUserName() );
        }

        return userObject;
    }

    @Override
    @RemoteMethod
    public Collection<User> getProjectUsers( String projectName ) {
        Collection<User> userObject = new ArrayList<User>();

        Collection<String> userNames = new ArrayList<String>();

        Project proj = projectDao.findByProjectName( projectName );
        userNames = securityService.readableBy( proj );

        for ( String userName : userNames ) {
            User user = ( ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User ) userManager
                    .findByUserName( userName );
            userObject.add( user );
        }

        return userObject;
    }

    @Override
    @RemoteMethod
    public Collection<String> projectReadableBy( Project project ) {
        Collection<String> allUsers = userManager.findAllUsers();

        Collection<String> result = new HashSet<String>();

        for ( String u : allUsers ) {
            if ( securityService.isViewableByUser( project, u ) ) {
                result.add( u );
            }
        }

        return result;
    }

    @Override
    @RemoteMethod
    public Map<String, String> getProjectUserGroups( String projectName ) {
        Map<String, String> userGroupObject = new HashMap<String, String>();

        Collection<String> userNames = new ArrayList<String>();

        Project proj = projectDao.findByProjectName( projectName );
        userNames = securityService.readableBy( proj );

        for ( String userName : userNames ) {
            User user = ( User ) userManager.findByUserName( userName );
            Collection<gemma.gsec.model.UserGroup> usergroups = new ArrayList<>();
            for ( gemma.gsec.model.UserGroup group : userService.findGroupsForUser( user ) ) {
                usergroups.add( group );
            }
            String groupNames = "";
            final String SEPARATOR = ", ";
            for ( gemma.gsec.model.UserGroup usergroup : usergroups ) {
                groupNames = groupNames + usergroup.getName() + SEPARATOR;

            }
            groupNames = groupNames.substring( 0, groupNames.lastIndexOf( SEPARATOR ) );
            userGroupObject.put( userName, groupNames );
        }

        return userGroupObject;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public User getCurrentUserName() {
        return ( User ) userManager.getCurrentUser();
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public void deleteUser( String userName ) {
        userService.deleteByUserName( userName );
    }

    @Override
    @RemoteMethod
    public String createUserProject( String projectName, String projectDescription ) throws NotLoggedInException {

        log.info( " In createProject projectName:" + projectName );

        try {
            projectManager.createProject( projectName, projectDescription );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            return e.getMessage();
        }

        return "Success";

    }

    @Override
    @RemoteMethod
    public VariantUploadServiceResult addSubjectVariantsToProject( String filepath, boolean createProject,
            String projectName, String variantType ) throws Exception {
        return addSubjectVariantsToProject( filepath, createProject, projectName, variantType, false );
    }

    /**
     * @author gaya
     * @param fileContent
     * @param projectName
     * @param variantType
     * @return Error String
     */
    @Override
    @RemoteMethod
    public VariantUploadServiceResult addSubjectVariantsToProject( String filepath, boolean createProject,
            String projectName, String variantType, final boolean dryRun ) throws Exception {

        Class.forName( "org.relique.jdbc.csv.CsvDriver" );

        File file = new File( filepath );
        String filename = file.getName();

        if ( !file.exists() ) {
            throw new IllegalArgumentException( "File " + filepath + " not found" );
        }

        if ( filename.endsWith( ".csv" ) ) {
            filename = filename.substring( 0, file.getName().length() - 4 );
        } else {
            throw new IllegalArgumentException( "File not processed, file name must end with .csv" );
        }

        // create a connection
        // arg[0] is the directory in which the .csv files are held
        Properties props = new Properties();
        props.put( "trimHeaders", "true" );
        props.put( "trimValues", "true" );
        Connection conn = DriverManager.getConnection( "jdbc:relique:csv:" + file.getParentFile().getAbsolutePath(),
                props );

        Statement stmt = conn.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY );
        ResultSet results = stmt.executeQuery( "SELECt * from " + filename );

        // check weather the project exist
        if ( createProject ) {
            if ( projectDao.findByProjectName( projectName ) != null ) {
                throw new IllegalArgumentException(
                        "Project "
                                + projectName
                                + " already exists, choose a different project name or use existingproject option to add to this project." );
            } else {
                projectManager.createProject( projectName, "" );
            }
        }

        VariantUploadServiceResult result = null;

        if ( variantType.equalsIgnoreCase( "CNV" ) ) {
            result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.CNV );
        } else if ( variantType.equalsIgnoreCase( "SNV" ) ) {
            result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.SNV );
        } else if ( variantType.equalsIgnoreCase( "INDEL" ) ) {
            result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.INDEL );
        } else if ( variantType.equalsIgnoreCase( "INVERSION" ) ) {
            result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.INVERSION );
        } else if ( variantType.equalsIgnoreCase( SpecialProject.DECIPHER.toString() ) ) {
            result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.DECIPHER );
        } else if ( variantType.equalsIgnoreCase( SpecialProject.DGV.toString() ) ) {
            result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.DGV );
        }

        if ( !result.getErrorMessages().isEmpty() ) {
            return result;
        }

        // check if the user is trying to upload too much data
        boolean isSpecialProject = false;
        for ( SpecialProject sp : SpecialProject.values() ) {
            if ( sp.toString().equals( projectName ) ) {
                isSpecialProject = true;
                break;
            }
        }
        int limit = ConfigUtils.getInt( "aspiredb.upload.maxRecords", 10000 );
        if ( !isSpecialProject && result.getVariantsToAdd().size() > limit ) {
            List<String> errorMessages = Collections.singletonList( "Upload is limited to " + limit + " variants" );
            ArrayList<VariantValueObject> variantsToAdd = new ArrayList<>();
            return new VariantUploadServiceResult( variantsToAdd, errorMessages );
        }

        StopWatch timer = new StopWatch();
        timer.start();

        if ( !dryRun ) {
            projectManager.addSubjectVariantsToProject( projectName, false, result.getVariantsToAdd() );

            log.info( "Adding " + result.getVariantsToAdd().size() + " variants to project " + projectName + " took "
                    + timer.getTime() + " ms" );
        }

        return result;
    }

    @Override
    @RemoteMethod
    public PhenotypeUploadServiceResult addSubjectPhenotypeToProject( String filepath, boolean createProject,
            String projectName ) throws Exception {
        return addSubjectPhenotypeToProject( filepath, createProject, projectName, false );
    }

    /**
     * @author gaya
     * @param fileContent
     * @param projectName
     * @param variantType
     * @return Error String
     */
    @Override
    @RemoteMethod
    public PhenotypeUploadServiceResult addSubjectPhenotypeToProject( String filepath, boolean createProject,
            String projectName, final boolean dryRun ) throws Exception {

        if ( !os.getHumanPhenotypeOntologyService().isOntologyLoaded() ) {
            os.getHumanPhenotypeOntologyService().startInitializationThread( true, true );
        }

        int c = 0;

        while ( !os.getHumanPhenotypeOntologyService().isOntologyLoaded() ) {
            Thread.sleep( 10000 );
            log.info( "Waiting for HumanPhenotypeOntology to load" );
            if ( ++c > 10 ) {
                throw new Exception( "Ontology load timeout" );
            }
        }

        File f = new File( filepath );
        String directory = f.getParent();
        String filename = f.getName();

        Class.forName( "org.relique.jdbc.csv.CsvDriver" );

        if ( filename.endsWith( ".csv" ) && f.exists() ) {
            filename = filename.substring( 0, filename.length() - 4 );
        } else {
            throw new IllegalArgumentException( "File not processed, file name must end with .csv" );
        }

        // create a connection
        // arg[0] is the directory in which the .csv files are held
        Connection conn = DriverManager.getConnection( "jdbc:relique:csv:" + directory );

        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery( "SELECT * FROM " + filename );

        if ( createProject ) {
            if ( projectDao.findByProjectName( projectName ) != null ) {
                throw new IllegalArgumentException(
                        "Project name already exists, choose a different project name or use existingproject option to add to this project." );

            }
        }

        PhenotypeUploadServiceResult phenResult = phenotypeUploadService
                .getPhenotypeValueObjectsFromResultSet( results );

        // clean up
        results.close();
        stmt.close();
        conn.close();

        // check if the user is trying to upload too much data
        boolean isSpecialProject = false;
        for ( SpecialProject sp : SpecialProject.values() ) {
            if ( sp.toString().equals( projectName ) ) {
                isSpecialProject = true;
                break;
            }
        }
        int limit = ConfigUtils.getInt( "aspiredb.upload.maxRecords", 10000 );
        if ( !isSpecialProject && phenResult.getPhenotypesToAdd().size() > limit ) {
            List<String> errorMessages = Collections.singletonList( "Upload is limited to " + limit + " phenotypes" );
            ArrayList<PhenotypeValueObject> phenosToAdd = new ArrayList<>();
            HashSet<String> unmatchedStrings = new HashSet<>();
            return new PhenotypeUploadServiceResult( phenosToAdd, errorMessages, unmatchedStrings );
        }

        if ( !dryRun ) {
            projectManager.addSubjectPhenotypesToProject( projectName, createProject, phenResult.getPhenotypesToAdd() );

            log.info( "Added " + phenResult.getPhenotypesToAdd().size() + " phenotypes to project " + projectName );
        }

        return phenResult;
    }

    @Override
    @RemoteMethod
    public List<ProjectValueObject> getOverlapProjects( Collection<Long> ids ) {

        Collection<Project> projects = projectDao.getOverlapProjects( ids );
        List<ProjectValueObject> vos = new ArrayList<ProjectValueObject>();

        for ( Project p : projects ) {
            vos.add( Project.convertToValueObject( p ) );
        }

        return vos;
    }

    // Hard code these special project's access for clarity
    @Override
    @RemoteMethod
    public ProjectValueObject getDgvProject() {

        ProjectValueObject pvo = new ProjectValueObject();
        Collection<Project> projects = projectDao.getSpecialOverlapProjects();

        for ( Project p : projects ) {
            if ( p.getName().equals( "DGV" ) ) {
                return Project.convertToValueObject( p );
            }
        }
        return pvo;

    }

    // Hard code these special project's access for clarity
    @Override
    @RemoteMethod
    public ProjectValueObject getDecipherProject() {

        ProjectValueObject pvo = new ProjectValueObject();
        Collection<Project> projects = projectDao.getSpecialOverlapProjects();

        for ( Project p : projects ) {
            if ( p.getName().equals( "DECIPHER" ) ) {
                return Project.convertToValueObject( p );
            }
        }
        return pvo;

    }

    @Override
    @RemoteMethod
    public Integer numSubjects( Collection<Long> projectIds ) {

        return this.projectDao.getSubjectCountForProjects( projectIds );
    }

    @Override
    @RemoteMethod
    public UserGroup findGroupByName( String name ) {
        return ( ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup ) userService
                .findGroupByName( name );
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<String> suggestUsers( SuggestionContext suggestionContext ) throws NotLoggedInException {
        Collection<String> userNames = new ArrayList<String>();
        Collection<User> users = userService.suggestUser( suggestionContext.getValuePrefix() );
        for ( User user : users ) {
            userNames.add( user.getFirstName() + " " + user.getLastName() );
        }
        return userNames;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public boolean isUser( String userName ) throws NotLoggedInException {

        User user = ( ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User ) userService
                .findByUserName( userName );
        if ( user != null ) return true;
        return false;
    }

    @Override
    @RemoteMethod
    public Integer numVariants( Collection<Long> projectIds ) {

        Collection<Long> projectCollection = new ArrayList<Long>();
        projectCollection.add( projectIds.iterator().next() );

        return this.projectDao.getVariantCountForProjects( projectCollection );
    }

    @Override
    @RemoteMethod
    public String deleteProject( String projectName ) {

        log.info( " Deleting project " + projectName );

        Project proj = projectDao.findByProjectName( projectName );

        if ( proj == null ) {
            log.warn( "Project " + projectName + " does not exist" );
            return "Project " + projectName + " does not exist";
        }

        try {
            projectManager.deleteProject( projectName );
        } catch ( Exception e ) {
            log.warn( e.getMessage() );
            return e.getMessage();
        }

        return "Success";

    }

    @Override
    public String alterGroupPermissions( String projectName, String groupName, Boolean grant )
            throws NotLoggedInException {
        log.info( " In alterGroupPermissions projectName:" + projectName + " group name: " + groupName + " grant:"
                + grant );

        if ( projectName == null || groupName == null ) {
            log.error( "null projectName or groupName options" );
            return "missing project name or group name";
        }

        if ( !userManager.groupExists( groupName ) ) {
            log.error( "Group does not exist" );
            return "Group does not exist";
        }

        Project proj = projectDao.findByProjectName( projectName );

        if ( proj == null ) {
            log.error( "Project does not exist" );
            return "Project does not exist";
        }

        projectManager.alterGroupWritePermissions( projectName, groupName, grant );

        return "Success";

    }

    @Override
    public String createUserAndAssignToGroup( String userName, String password, String groupName )
            throws NotLoggedInException {

        log.info( " In createUserAndAssignToGroup userName:" + userName + " group name: " + groupName );

        return projectManager.createUserAndAssignToGroup( userName, password, groupName );

    }

    /**
     * Send an e-mail to the user that their upload has finished.
     */
    private void emailUploadFinished( String projectName, String resultMessage ) {
        gemma.gsec.model.User user = userManager.getCurrentUser();

        Map<String, Object> model = new HashMap<>();
        model.put( "username", user.getUserName() );
        model.put( "siteurl", ConfigUtils.getBaseUrl() + "home.html" );
        model.put( "projectName", projectName );
        model.put( "resultMessage", resultMessage );

        sendEmail( user.getUserName(), user.getEmail(), model );
    }

    private void sendEmail( String username, String email, Map<String, Object> model ) {
        String subject = this.messageUtil.getText( "projectUpload.email.subject", Locale.getDefault() );
        String templateName = "projectUploaded.vm";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom( ConfigUtils.getAdminEmailAddress() );
        mailMessage.setSubject( subject );
        mailMessage.setTo( username + "<" + email + ">" );
        mailEngine.sendMessage( mailMessage, templateName, model );
    }

    @Override
    @RemoteMethod
    public String addSubjectVariantsPhenotypeToProject( final String variantFilename, final String phenotypeFilename,
            final boolean createProject, final String projectName, final String variantType ) {
        return addSubjectVariantsPhenotypeToProject( variantFilename, phenotypeFilename, createProject, projectName,
                variantType, false );
    }

    @Override
    @RemoteMethod
    public String addSubjectVariantsPhenotypeToProject( final String variantFilename, final String phenotypeFilename,
            final boolean createProject, final String projectName, final String variantType, final boolean dryRun ) {

        // Run the upload task on a separate thread so we don't block the current thread and return a proxy error on the
        // front end
        class ProjectUploadThread extends Thread {

            SecurityContext context;

            public ProjectUploadThread( SecurityContext context ) {
                this.context = context;
            }

            @Override
            public void run() {

                SecurityContextHolder.setContext( this.context );

                StopWatch timer = new StopWatch();
                timer.start();

                StringBuffer returnMsg = new StringBuffer();
                StringBuffer errMsg = new StringBuffer();

                VariantUploadServiceResult variantResult = null;
                Collection<Variant2VariantOverlap> overlap = null;
                PhenotypeUploadServiceResult phenResult = null;

                final String STR_FMT = "%35s: %5s\n";

                returnMsg.append( String.format( STR_FMT, "Project", projectName ) + "\n" );

                if ( variantFilename.length() > 0 ) {

                    try {
                        variantResult = addSubjectVariantsToProject( variantFilename, createProject, projectName,
                                variantType, dryRun );
                        returnMsg.append( String.format( STR_FMT, "Number of Subjects", getSubjects( projectName )
                                .size() ) );
                        returnMsg.append( String.format( STR_FMT, "Number of Variants", variantResult
                                .getVariantsToAdd().size() ) );
                        for ( String err : variantResult.getErrorMessages() ) {
                            errMsg.append( err + "\n" );
                        }

                        if ( !dryRun ) {
                            // only run SpecialProject overlap if projectName is not a special project
                            try {
                                SpecialProject.valueOf( projectName );
                            } catch ( IllegalArgumentException argEx ) {
                                for ( Project specialProject : projectDao.getSpecialOverlapProjects() ) {
                                    try {
                                        overlap = projectManager.populateProjectToProjectOverlap( projectName,
                                                specialProject.getName(), variantResult.getVariantsToAdd() );
                                        returnMsg.append( String.format( STR_FMT, "Number of Overlaps with "
                                                + specialProject.getName(), overlap.size() ) );
                                    } catch ( Exception e ) {
                                        log.error( e.getLocalizedMessage(), e );
                                        errMsg.append( e.getLocalizedMessage() + "\n" );
                                    }
                                }
                            }
                        }

                    } catch ( Exception e ) {
                        log.error( e.getLocalizedMessage(), e );
                        errMsg.append( e.getLocalizedMessage() + "\n" );
                    }
                }

                if ( phenotypeFilename.length() > 0 ) {
                    try {
                        phenResult = addSubjectPhenotypeToProject( phenotypeFilename, createProject, projectName,
                                dryRun );
                        if ( returnMsg.indexOf( "Number of Subjects" ) == -1 ) {
                            returnMsg.append( String.format( STR_FMT, "Number of Subjects", getSubjects( projectName )
                                    .size() ) );
                        }
                        returnMsg.append( String.format( STR_FMT, "Number of Phenotypes", phenResult
                                .getPhenotypesToAdd().size() ) );
                        for ( String err : phenResult.getErrorMessages() ) {
                            errMsg.append( err + "\n" );
                        }
                    } catch ( Exception e ) {
                        log.error( e.getLocalizedMessage(), e );
                        errMsg.append( e.getLocalizedMessage() + "\n" );
                    }
                }

                log.info( "Uploading took " + timer.getTime() + " ms" );

                // trim errorMssage
                final int ERRMSG_LIMIT = 500;
                if ( errMsg.length() > ERRMSG_LIMIT ) {
                    errMsg.delete( ERRMSG_LIMIT, errMsg.length() - 1 );
                    errMsg.append( "\n...\n" );
                }

                String returnStr = returnMsg.toString();
                returnStr += errMsg.length() > 0 ? "\nExceptions\n" + errMsg : "";

                log.info( returnStr );

                if ( !dryRun ) {
                    // email users that the upload has finished

                    // if ( timer.getTime() > ConfigUtils.getLong( "aspiredb.uploadTimeThreshold", 60000 ) ) {
                    emailUploadFinished( projectName, returnStr );
                    // }
                }

            }
        }

        ProjectUploadThread thread = new ProjectUploadThread( SecurityContextHolder.getContext() );
        thread.setName( ProjectUploadThread.class.getName() + "_" + projectName + "_load_thread_"
                + RandomStringUtils.randomAlphanumeric( 5 ) );
        // To prevent VM from waiting on this thread to shutdown (if shutting down).
        thread.setDaemon( true );
        thread.start();

        return thread.getName();
    }

    @Override
    public Collection<Subject> getSubjects( String projectName ) throws Exception {
        Project project = projectManager.findProject( projectName );
        if ( project == null ) {
            log.error( "Project " + projectName + " not found" );
            return new HashSet<>();
        }
        return projectDao.getSubjects( project.getId() );
    }
}