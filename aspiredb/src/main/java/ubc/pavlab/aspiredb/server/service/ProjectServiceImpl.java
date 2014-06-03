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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.io.StringWriter;
import java.lang.reflect.Array;

import javax.ws.rs.core.Variant;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.gwt.http.client.URL;

import ubc.pavlab.aspiredb.cli.AbstractCLI.ErrorCode;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadService;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadServiceResult;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadService;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadServiceResult;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.Securable;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.User;
import ubc.pavlab.aspiredb.server.model.common.auditAndSecurity.UserGroup;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.server.security.SecurityService;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.server.security.authentication.UserService;
import ubc.pavlab.aspiredb.shared.ProjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

@Service
@RemoteProxy(name = "ProjectService")
public class ProjectServiceImpl implements ProjectService {

    private static Logger log = LoggerFactory.getLogger( ProjectService.class );

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
        
        userObject =null;
        Collection<String> userNames = new ArrayList<String>();

        Project proj = projectDao.findByProjectName( projectName );
        userNames = projectReadableBy( proj );

        for ( String userName : userNames ) {
            User user = userService.findByUserName( userName );
            userObject.add( user.getFirstName() );
        }

        return userObject;
    }
    
    @Override
    @RemoteMethod
    public Collection<User> getProjectUsers( String projectName ) {
        Collection<User> userObject = new ArrayList<User>();
        
        Collection<String> userNames =new ArrayList<String>();
                        
        Project proj = projectDao.findByProjectName( projectName );
        userNames= securityService.readableBy( proj );
        
        for (String userName : userNames){
            User user = userManager.findByUserName( userName );
            userObject.add( user);
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
    public Map<String, Collection<UserGroup>> getProjectUserGroups( String projectName ) {
        Map<String, Collection<UserGroup>> userGroupObject = new HashMap<String, Collection<UserGroup>>();

        Collection<String> userNames = new ArrayList<String>();

        Project proj = projectDao.findByProjectName( projectName );
        userNames = securityService.readableBy( proj );

        for ( String userName : userNames ) {

            User user = userManager.findByUserName( userName );
            Collection<UserGroup> usergroups = userService.findGroupsForUser( user );
            userGroupObject.put( userName, usergroups );
        }

        return userGroupObject;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public User getCurrentUserName() {
        return userManager.getCurrentUser();
    }

    @Override
    @RemoteMethod
    public String createUserProject( String projectName, String projectDescription ) throws NotLoggedInException {

        log.info( " In createProject projectName:" + projectName );

        try {
            projectManager.createProject( projectName, projectDescription );
        } catch ( Exception e ) {
            log.error( e.getMessage() );
            return e.getMessage();
        }

        return "Success";

    }

    /**
     * public ProjectValueObject findUserProject(String projectName) throws NotLoggedInException{ ProjectValueObject
     * pvo=null; log.info( " finding projectName:" + projectName ); try { pvo.equals( projectManager.findProject(
     * projectName )); } catch ( Exception e ) { log.error( e.getMessage() ); //return e.getMessage(); } return pvo; }
     */

    /**
     * @author gaya
     * @param fileContent
     * @param projectName
     * @param variantType
     * @return Error String
     */
    @Override
    @RemoteMethod
    public String addSubjectVariantsToExistingProject( String fileContent, boolean createProject, String projectName,
            String variantType ) {

        String returnString = "Success";

        try {

            String csv = "uploadFile/variantFile.csv";
            CSVWriter writer = new CSVWriter( new FileWriter( csv ) );

            // Object[] objectArray = resultsList.toArray();
            String[] Outresults = fileContent.split( "\n" );

            for ( int i = 0; i < Outresults.length; i++ ) {
                String[] passedCSVFile = Outresults[i].toString().split( "," );
                writer.writeNext( passedCSVFile );
            }

            writer.close();

            Class.forName( "org.relique.jdbc.csv.CsvDriver" );

            // create a connection
            // arg[0] is the directory in which the .csv files are held
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:uploadFile/" );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECt * from variantFile" );

            // check weather the project exist
            if ( createProject ) {
                if ( projectDao.findByProjectName( projectName ) != null ) {
                    returnString = "Project name already exists, choose a different project name or use existingproject option to add to this project.";
                }
            }

            VariantType VariantType = null;
            VariantUploadServiceResult result = null;

            if ( variantType.equalsIgnoreCase( "CNV" ) ) {
                result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.CNV );
            } else if ( variantType.equalsIgnoreCase( "SNV" ) ) {
                result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.SNV );
            } else if ( variantType.equalsIgnoreCase( "INDEL" ) ) {
                result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.INDEL );
            } else if ( variantType.equalsIgnoreCase( "INVERSION" ) ) {
                result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.INVERSION );
            } else if ( variantType.equalsIgnoreCase( "DECIPHER" ) ) {
                result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.DECIPHER );
            } else if ( variantType.equalsIgnoreCase( "DGV" ) ) {
                result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, VariantType.DGV );
            }

            if ( result.getErrorMessages().isEmpty() ) {
                projectManager.addSubjectVariantsToProject( projectName, false, result.getVariantsToAdd() );
            } else if ( result.getErrorMessages().isEmpty() ) {
                returnString = "Success";

            } else {
                for ( String errorMessage : result.getErrorMessages() ) {
                    returnString = errorMessage;
                }

            }

        } catch ( Exception e ) {
            return e.toString();
        }
        return returnString;
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
    public String addSubjectPhenotypeToExistingProject( String fileContent, boolean createProject, String projectName,
            String variantType ) {

        String returnString = "Success";

        try {

            os.getHumanPhenotypeOntologyService().startInitializationThread( true );
            int c = 0;

            while ( !os.getHumanPhenotypeOntologyService().isOntologyLoaded() ) {
                Thread.sleep( 10000 );
                log.info( "Waiting for HumanPhenotypeOntology to load" );
                if ( ++c > 10 ) {
                    throw new Exception( "Ontology load timeout" );
                }
            }

            String csv = "uploadFile/phenotypeFile.csv";
            CSVWriter writer = new CSVWriter( new FileWriter( csv ) );

            // Object[] objectArray = resultsList.toArray();
            String[] Outresults = fileContent.split( "\n" );

            for ( int i = 0; i < Outresults.length; i++ ) {
                String[] passedCSVFile = Outresults[i].toString().split( "," );
                writer.writeNext( passedCSVFile );
            }

            writer.close();

            Class.forName( "org.relique.jdbc.csv.CsvDriver" );

            // create a connection
            // arg[0] is the directory in which the .csv files are held
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:uploadFile/" );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM phenotypeFile" );

            if ( createProject ) {
                if ( projectDao.findByProjectName( projectName ) != null ) {
                    returnString = "Project name already exists, choose a different project name or use existingproject option to add to this project.";
                }
            }

            PhenotypeUploadServiceResult phenResult = phenotypeUploadService
                    .getPhenotypeValueObjectsFromResultSet( results );

            // clean up
            results.close();
            stmt.close();
            conn.close();

            projectManager.addSubjectPhenotypesToProject( projectName, createProject, phenResult.getPhenotypesToAdd() );

            if ( !phenResult.getErrorMessages().isEmpty() ) {
                for ( String errorMessage : phenResult.getErrorMessages() ) {
                    returnString = errorMessage;
                }

            } else {
                returnString = "Success";
            }

        } catch ( Exception e ) {
            return e.toString();
        }

        return returnString;
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

    /*
     * TODO eventually we want this to work with a collection of projectIds
     */
    @Override
    @RemoteMethod
    public Integer numSubjects( Collection<Long> projectIds ) {

        return this.projectDao.getSubjectCountForProjects( projectIds );
    }

    /*
     * TODO eventually we want this to work with a collection of projectIds
     */
    @Override
    @RemoteMethod
    public Integer numVariants( Collection<Long> projectIds ) {

        Collection<Long> projectCollection = new ArrayList<Long>();
        projectCollection.add( projectIds.iterator().next() );

        return this.projectDao.getVariantCountForProjects( projectCollection );
    }

    // TODO change return type to some object that can contain more relevant information, handle other exceptions
    @Override
    public String processUploadedFile( String projectName, String filename, VariantType v ) {

        log.info( " In processUploadedFile projectName:" + projectName + " filename:" + filename + " varianttype:"
                + v.name() );

        try {
            Class.forName( "org.relique.jdbc.csv.CsvDriver" );
            Connection conn = null;
            // Connection conn = DriverManager.getConnection( "jdbc:relique:csv:" + FileUploadUtil.getUploadPath() );

            if ( filename.endsWith( ".csv" ) ) {
                filename = filename.substring( 0, filename.length() - 4 );
            } else {
                return "File not processed, file name must end with .csv";
            }

            log.info( " executing SELECT * FROM " + filename );

            try (ResultSet results = conn.createStatement().executeQuery( "SELECT * FROM " + filename );) {

                log.info( " Making vvos" );

                VariantUploadServiceResult result = VariantUploadService.makeVariantValueObjectsFromResultSet( results,
                        v );

                StringBuffer errors = new StringBuffer();

                if ( result.getErrorMessages().isEmpty() ) {

                    projectManager.addSubjectVariantsToProjectForceCreate( projectName, result.getVariantsToAdd() );
                    log.info( " success" );
                    return "Success";

                }
                for ( String errorMessage : result.getErrorMessages() ) {
                    errors.append( errorMessage + "\n" );
                }
                log.info( " there are errors" );

                String errorString = errors.toString();

                // TODO handle better than this, just substringing currently because gwt takes a dump when the string is
                // too long
                if ( errorString.length() > 4000 ) {
                    errorString = errorString.substring( 0, 4000 );
                }
                return errorString;
            }
        } catch ( Exception e ) {
            return e.getMessage();
        }

    }

    // TODO change return type to some object that can contain more relevant information, handle other exceptions
    @Override
    public String processUploadedPhenotypeFile( String projectName, String filename ) {

        log.info( " In processUploadedPhenotypeFile projectName:" + projectName + " filename:" + filename );

        try {
            Class.forName( "org.relique.jdbc.csv.CsvDriver" );

            // create a connection
            // arg[0] is the directory in which the .csv files are held
            Connection conn = null;
            // Connection conn = DriverManager.getConnection( "jdbc:relique:csv:" + FileUploadUtil.getUploadPath() );

            if ( filename.endsWith( ".csv" ) ) {
                filename = filename.substring( 0, filename.length() - 4 );
            } else {
                return "File not processed, file name must end with .csv";
            }

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM " + filename );

            boolean createProject = true;
            if ( projectDao.findByProjectName( projectName ) != null ) {
                log.info( "Project name already exists,  adding to existing project" );

                createProject = false;
            }

            PhenotypeUploadServiceResult phenResult = phenotypeUploadService
                    .getPhenotypeValueObjectsFromResultSet( results );

            // clean up
            results.close();
            stmt.close();
            conn.close();

            if ( phenResult.getErrorMessages().isEmpty() ) {
                projectManager.addSubjectPhenotypesToProject( projectName, createProject,
                        phenResult.getPhenotypesToAdd() );
            } else {
                for ( String errorMessage : phenResult.getErrorMessages() ) {
                    System.out.println( errorMessage );
                }

            }

            return "Success";
        } catch ( Exception e ) {

            return e.getMessage();

        }

    }

    @Override
    public String deleteProject( String projectName ) {

        log.info( " In deleteProject projectName:" + projectName );

        Project proj = projectDao.findByProjectName( projectName );

        if ( proj == null ) {
            log.error( "Project does not exist" );
            return "Project does not exist";
        }

        try {
            projectManager.deleteProject( projectName );
        } catch ( Exception e ) {
            log.error( e.getMessage() );
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

}