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
package ubc.pavlab.aspiredb.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.beans.factory.BeanFactory;
import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.server.security.authentication.UserManager;

/**
 * CLI to give groups read permissions to projects and delete projects
 * 
 * @author cmcdonald
 * @version $Id: ProjectManagerCLI.java,v 1.7 2013/07/19 17:03:49 ptan Exp $
 */
public class ProjectManagerCLI extends AbstractCLI {

    private static ProjectManager projectManager;
    private static ProjectDao projectDao;

    private static UserManager userManager;

    private String projectName = "";
    private String groupName = "";
    private boolean grant = false;
    private boolean restrict = false;
    private boolean delete = false;

    private static BeanFactory applicationContext;

    /**
     * @param args
     */
    public static void main( String[] args ) {
        ConsoleAppender console = new ConsoleAppender(); // create appender
        // configure the appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout( new PatternLayout( PATTERN ) );
        console.setThreshold( Level.DEBUG );
        console.activateOptions();
        Logger.getRootLogger().addAppender( console );

        applicationContext = SpringContextUtil.getApplicationContext( false );

        projectManager = ( ProjectManager ) applicationContext.getBean( "projectManager" );

        projectDao = ( ProjectDao ) applicationContext.getBean( "projectDao" );

        userManager = ( UserManager ) applicationContext.getBean( "userManager" );

        ProjectManagerCLI p = new ProjectManagerCLI();
        try {
            Exception ex = p.doWork( args );
            if ( ex != null ) {
                ex.printStackTrace();
            }
            System.exit( 0 );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings("static-access")
    @Override
    protected void buildOptions() {

        Option groupname = OptionBuilder.hasArg().withArgName( "group name" )
                .withDescription( "The group to give permissions to" ).create( "groupname" );

        Option project = OptionBuilder.isRequired().hasArg().withArgName( "Project name" )
                .withDescription( "The project that will be affected by these operations" ).create( "project" );

        addOption( "delete", false, "Using this option will delete the project" );
        addOption( "grant", false, "Using this option will grant the group read permissions on the project" );
        addOption( "restrict", false, "Using this option will remove the group's read permissions on the project" );

        addOption( groupname );
        addOption( project );
    }

    @Override
    protected Exception doWork( String[] args ) {
        Exception err = processCommandLine( "Project ", args );
        authenticate( applicationContext );
        if ( err != null ) return err;

        if ( ( grant == true || restrict == true ) && delete == true ) {
            log.error( "can't have -grant/-restrict and -delete options together" );
            bail( AbstractCLI.ErrorCode.INVALID_OPTION );
        }
        if ( grant == true && restrict == true ) {
            log.error( "can't have -grant and -restrict options together" );
            bail( AbstractCLI.ErrorCode.INVALID_OPTION );
        }

        if ( grant == true ) {

            if ( projectName == null || groupName == null ) {
                log.error( "missing -project or -groupname options" );
                bail( AbstractCLI.ErrorCode.MISSING_OPTION );
            }

            if ( !userManager.groupExists( groupName ) ) {
                log.error( "Group does not exist, create group first with -creategroup option" );
                bail( AbstractCLI.ErrorCode.INVALID_OPTION );
            }

            Project proj = projectDao.findByProjectName( projectName );

            if ( proj == null ) {
                log.error( "Project does not exist" );
                bail( ErrorCode.INVALID_OPTION );
            }

            projectManager.alterGroupWritePermissions( projectName, groupName, true );

        } else if ( restrict == true ) {
            if ( projectName == null || groupName == null ) {
                log.error( "missing -project or -groupname options" );
                bail( AbstractCLI.ErrorCode.MISSING_OPTION );
            }

            projectManager.alterGroupWritePermissions( projectName, groupName, false );

        } else if ( delete == true ) {

            if ( projectName == null ) {
                log.error( "missing -project option" );
                bail( AbstractCLI.ErrorCode.MISSING_OPTION );
            }

            Project proj = projectDao.findByProjectName( projectName );

            if ( proj == null ) {
                log.error( "Project does not exist" );
                bail( ErrorCode.INVALID_OPTION );
            }

            try {
                projectManager.deleteProject( projectName );
            } catch ( Exception e ) {
                log.error( e.getMessage() );
                return e;
            }
        }

        return null;
    }

    @Override
    public String getShortDesc() {
        return "Allow users access to projects";
    }

    @Override
    protected void processOptions() {

        if ( this.hasOption( "groupname" ) ) {
            groupName = this.getOptionValue( "groupname" );
        }
        if ( this.hasOption( "project" ) ) {
            projectName = this.getOptionValue( "project" );
        }
        if ( this.hasOption( "grant" ) ) {
            grant = true;
        }
        if ( this.hasOption( "restrict" ) ) {
            restrict = true;
        }
        if ( this.hasOption( "delete" ) ) {
            delete = true;
        }
    }

}
