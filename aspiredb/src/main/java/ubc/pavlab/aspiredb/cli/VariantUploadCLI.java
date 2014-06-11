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
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadService;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadServiceResult;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.project.ProjectManager;
import ubc.pavlab.aspiredb.shared.VariantType;

/**
 * @version $Id: VariantUploadCLI.java,v 1.12 2013/07/19 17:03:49 ptan Exp $
 */
public class VariantUploadCLI extends AbstractCLI {

    private static ProjectManager projectManager;
    private static ProjectDao projectDao;

    private String directory = "";

    private String filename = "";
    private String projectName = "";
    private VariantType variantType;

    private boolean createProject = false;

    private boolean dryRun = false;
    private boolean predictSNVfunction = false;

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

        VariantUploadCLI p = new VariantUploadCLI();
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
        Option d = OptionBuilder.isRequired().hasArg().withArgName( "Directory" )
                .withDescription( "Directory containing csv files" ).withLongOpt( "directory" ).create( 'd' );

        Option f = OptionBuilder.isRequired().hasArg().withArgName( "File name" ).withDescription( "The file to parse" )
                .withLongOpt( "filename" ).create( 'f' );

        Option variantType = OptionBuilder.isRequired().hasArg().withArgName( "Variant Type" )
                .withDescription( "The type of variant in this file, one of: CNV, Indel, SNV, Inversion" )
                .create( "variant" );

        Option project = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName( "Project name" )
                .withDescription(
                        "The project where this data will reside. Project will be created if does not exist,"
                                + "If the project does exist use the existingproject option to add to an existing project" )
                .create( "project" );

        addOption( "existingproject", false, "You must use this option if you are adding to an existing project" );

        addOption( "dryrun", false, "Use this option to validate your data before uploading" );

        addOption( "predictSNVfunction", true,
                "Set argument to true (default) to predict SNV function and false otherwise. Variant Type must be SNV." );

        addOption( d );
        addOption( f );
        addOption( variantType );

        addOption( project );

    }

    @Override
    protected void processOptions() {
        if ( this.hasOption( 'd' ) ) {
            directory = this.getOptionValue( 'd' );
        }
        if ( this.hasOption( 'f' ) ) {
            filename = this.getOptionValue( 'f' );
        }

        if ( this.hasOption( "variant" ) ) {
            String variant = this.getOptionValue( "variant" );
            if ( VariantType.findByName( variant ) == null ) {
                bail( ErrorCode.INVALID_OPTION );
            }

            variantType = VariantType.findByName( variant );

        }

        if ( variantType.equals( VariantType.SNV ) ) {
            predictSNVfunction = true;
            if ( this.hasOption( "predictSNVfunction" ) ) {
                predictSNVfunction = Boolean.parseBoolean( this.getOptionValue( "predictSNVfunction" ) );
            }
        }

        if ( this.hasOption( "project" ) ) {
            projectName = this.getOptionValue( "project" );
        }

        if ( this.hasOption( "existingproject" ) ) {
            createProject = false;
        } else {
            createProject = true;
        }

        if ( this.hasOption( "dryrun" ) ) {
            dryRun = true;
        }

    }

    @Override
    protected Exception doWork( String[] args ) {
        Exception err = processCommandLine( "Upload Variant file", args );
        authenticate( applicationContext );
        if ( err != null ) {
            return err;
        }

        if ( directory == null || filename == null ) {
            return err;
        }

        try {
            Class.forName( "org.relique.jdbc.csv.CsvDriver" );

            // create a connection
            // arg[0] is the directory in which the .csv files are held
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:" + directory );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM " + filename );

            if ( createProject ) {
                Project proj = projectDao.findByProjectName( projectName );

                if ( proj != null ) {
                    log.error( "Project with that name already exists.  Use -existingproject option to add to an existing project" );
                    bail( ErrorCode.FATAL_ERROR );
                }
            }

            VariantUploadServiceResult result = VariantUploadService.makeVariantValueObjectsFromResultSet( results,
                    variantType );

            results.close();
            stmt.close();
            conn.close();

            if ( predictSNVfunction ) {
                VariantUploadService.predictDbNsfpSNVFunction( result.getVariantsToAdd() );
            }

            if ( result.getErrorMessages().isEmpty() && !dryRun ) {
                projectManager.addSubjectVariantsToProject( projectName, createProject, result.getVariantsToAdd() );
            } else if ( result.getErrorMessages().isEmpty() ) {
                System.out.println( "No errors are detected in your data file" );

            } else {
                for ( String errorMessage : result.getErrorMessages() ) {
                    System.out.println( errorMessage );
                }

            }

        } catch ( Exception e ) {
            return e;
        }

        return null;
    }

    @Override
    public String getShortDesc() {
        return "Upload a variant data file and create / assign it to a project";
    }

}
