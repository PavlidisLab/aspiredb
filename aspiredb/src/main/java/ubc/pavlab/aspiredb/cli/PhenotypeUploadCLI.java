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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.beans.factory.BeanFactory;

import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadService;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadServiceResult;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.server.project.ProjectManager;

/**
 * First pass phenotype data uploader, missing a bunch of requirements
 * 
 * @author cmcdonald
 * @version $Id: PhenotypeUploadCLI.java,v 1.8 2013/07/19 17:03:49 ptan Exp $
 */
public class PhenotypeUploadCLI extends AbstractCLI {

    private static ProjectDao projectDao;

    private static ProjectManager projectManager;

    private static OntologyService os;

    private static PhenotypeUploadService phenotypeUploadService;

    private String directory = "";
    private String columns = "";
    private String filename = "";
    private String projectName = "";

    private boolean createProject = false;

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
        projectDao = ( ProjectDao ) applicationContext.getBean( "projectDao" );
        projectManager = ( ProjectManager ) applicationContext.getBean( "projectManager" );
        phenotypeUploadService = ( PhenotypeUploadService ) applicationContext.getBean( "phenotypeUploadService" );
        os = ( OntologyService ) applicationContext.getBean( "ontologyService" );

        PhenotypeUploadCLI p = new PhenotypeUploadCLI();
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

    @Override
    public String getShortDesc() {
        return "Upload a phenotype data file";
    }

    @Override
    protected void buildOptions() {
        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "Directory" );
        OptionBuilder.withDescription( "Directory containing csv files" );
        OptionBuilder.withLongOpt( "directory" );
        Option d = OptionBuilder.create( 'd' );

        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "File name" );
        OptionBuilder.withDescription( "The file to parse" );
        OptionBuilder.withLongOpt( "filename" );
        Option f = OptionBuilder.create( 'f' );

        OptionBuilder.isRequired();
        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "Project name" );
        OptionBuilder
                .withDescription( "The project where this data will reside. Project will be created if does not exist,"
                        + "If the project does exist use the existingproject option to add to an existing project" );
        Option project = OptionBuilder.create( "project" );

        addOption( "existingproject", false, "You must use this option if you are adding to an existing project" );

        addOption( d );
        addOption( f );
        addOption( project );
    }

    @Override
    protected Exception doWork( String[] args ) {
        Exception err = processCommandLine( "Parse CVS", args );
        authenticate( applicationContext );
        if ( err != null ) {
            return err;
        }

        if ( directory == null || filename == null || columns == null ) {
            return err;
        }

        try {

            os.getHumanPhenotypeOntologyService().startInitializationThread( true, true );
            int c = 0;

            while ( !os.getHumanPhenotypeOntologyService().isOntologyLoaded() ) {
                Thread.sleep( 10000 );
                log.info( "Waiting for HumanPhenotypeOntology to load" );
                if ( ++c > 10 ) {
                    throw new Exception( "Ontology load timeout" );
                }
            }

            Class.forName( "org.relique.jdbc.csv.CsvDriver" );

            // create a connection
            // arg[0] is the directory in which the .csv files are held
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:" + directory );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM " + filename );

            if ( createProject ) {
                if ( projectDao.findByProjectName( projectName ) != null ) {
                    log.warn( "Project name already exists, choose a different project name or use existingproject option to add to this project." );

                    bail( ErrorCode.MISSING_OPTION );
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
                    System.out.println( errorMessage );
                }

            } else {
                System.out.println( "no errors" );

            }

        } catch ( Exception e ) {
            return e;
        }

        return null;
    }

    @Override
    protected void processOptions() {
        if ( this.hasOption( 'd' ) ) {
            directory = this.getOptionValue( 'd' );
        }
        if ( this.hasOption( 'f' ) ) {
            filename = this.getOptionValue( 'f' );
        }
        if ( this.hasOption( 't' ) ) {
            projectName = this.getOptionValue( 't' );
        }

        if ( this.hasOption( "project" ) ) {
            projectName = this.getOptionValue( "project" );
        }

        if ( this.hasOption( "existingproject" ) ) {
            createProject = false;
        } else {
            createProject = true;
        }
    }

}
