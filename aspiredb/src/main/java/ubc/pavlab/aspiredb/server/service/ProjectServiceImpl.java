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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadService;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadServiceResult;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadService;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadServiceResult;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;
import ubc.pavlab.aspiredb.shared.ProjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantType;

@Service
@RemoteProxy(name="ProjectService")
public class ProjectServiceImpl implements ProjectService{

    private static Logger log = LoggerFactory.getLogger( ProjectService.class );

    @Autowired
    ProjectDao projectDao;

    @Autowired
    ProjectManager projectManager;

    @Autowired
    UserManager userManager;

    @Autowired
    PhenotypeUploadService phenotypeUploadService;

    @RemoteMethod 
    public List<ProjectValueObject> getProjects(){
        

        Collection<Project> projects = projectDao.loadAll();
        List<ProjectValueObject> vos = new ArrayList<ProjectValueObject>();

        for ( Project p : projects ) {
            vos.add( Project.convertToValueObject( p ) );
        }

        return vos;
    }
    
    @RemoteMethod 
    public List<ProjectValueObject> getOverlapProjects(Collection<Long> ids){        

        Collection<Project> projects = projectDao.getOverlapProjects(ids);
        List<ProjectValueObject> vos = new ArrayList<ProjectValueObject>();

        for ( Project p : projects ) {
                vos.add( Project.convertToValueObject( p ) );           
        }

        return vos;
    }
    
    //Hard code these special project's access for clarity
    @RemoteMethod
    public ProjectValueObject getDgvProject( ){
        
        ProjectValueObject pvo = new ProjectValueObject();
        
        Collection<Project> projects = projectDao.getSpecialOverlapProjects();
        
        for ( Project p : projects ) {
            
                if (p.getName().equals( "DGV" )){
                    return Project.convertToValueObject( p );
                }
                        
        }

       
        return pvo;       
        
        
    }
    
   //Hard code these special project's access for clarity
    @RemoteMethod
    public ProjectValueObject getDecipherProject( ){
        
        ProjectValueObject pvo = new ProjectValueObject();
        
        Collection<Project> projects = projectDao.getSpecialOverlapProjects();
        
        for ( Project p : projects ) {
            
                if (p.getName().equals( "DECIPHER" )){
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
    @RemoteMethod
    public Integer numVariants( Collection<Long> projectIds ) {


        Collection<Long> projectCollection = new ArrayList<Long>();
        projectCollection.add( projectIds.iterator().next() );

        return this.projectDao.getVariantCountForProjects( projectCollection );
    }

    
    // TODO change return type to some object that can contain more relevant information, handle other exceptions
    public String processUploadedFile( String projectName, String filename, VariantType v )  {

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

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM " + filename );

            log.info( " Making vvos" );

            VariantUploadServiceResult result = VariantUploadService.makeVariantValueObjectsFromResultSet( results, v );

            results.close();
            stmt.close();
            conn.close();

            StringBuffer errors = new StringBuffer();

            if ( result.getErrorMessages().isEmpty() ) {

                projectManager.addSubjectVariantsToProjectForceCreate( projectName, result.getVariantsToAdd() );
                log.info( " success" );
                return "Success";

            } else {
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
    public String processUploadedPhenotypeFile( String projectName, String filename )  {

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

    public String deleteProject( String projectName )  {

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

    public String createUserAndAssignToGroup( String userName, String password, String groupName )
            throws NotLoggedInException {

        log.info( " In createUserAndAssignToGroup userName:" + userName + " group name: " + groupName );

        return projectManager.createUserAndAssignToGroup( userName, password, groupName );

    }

}