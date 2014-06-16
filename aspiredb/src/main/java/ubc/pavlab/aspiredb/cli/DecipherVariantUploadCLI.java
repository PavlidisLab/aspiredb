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
import ubc.pavlab.aspiredb.server.project.ProjectManager;

/**
 * modified VariantUploadCLI hack for unfortunately formatted file(since modified) that we got from DECIPHER not
 * intended to be used for anything other than that file
 * 
 * @version $Id: DecipherVariantUploadCLI.java
 */
public class DecipherVariantUploadCLI extends AbstractCLI {

    private static ProjectManager projectManager;
    private static ProjectDao projectDao;

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

        DecipherVariantUploadCLI p = new DecipherVariantUploadCLI();
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

    private String directory = "";
    private String filename = "";

    private String projectName = "";

    private boolean deleteProject = true;

    private boolean dryRun = false;

    private static BeanFactory applicationContext;

    @Override
    public String getLogger() {
        return "ubc.pavlab.aspiredb.cli.DecipherVariantUploadCLI";
    }

    @Override
    public String getShortDesc() {
        return "Upload a variant data file and create / assign it to a project";
    }

    @SuppressWarnings("static-access")
    @Override
    protected void buildOptions() {
        OptionBuilder.isRequired();
        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "Directory" );
        OptionBuilder.withDescription( "Directory containing csv files" );
        OptionBuilder.withLongOpt( "directory" );
        Option d = OptionBuilder.create( 'd' );

        OptionBuilder.isRequired();
        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "File name" );
        OptionBuilder.withDescription( "The file to parse" );
        OptionBuilder.withLongOpt( "filename" );
        Option f = OptionBuilder.create( 'f' );

        OptionBuilder.isRequired();
        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "Project name" );
        OptionBuilder
                .withDescription( "The project where this data will reside. Project will be deleted if existingproject option is not specified,"
                        + "Acceptable values = 'DECIPHER" );
        // Decipher will reside in a 'Special project' and are all CNVs
        /*
         * Option variantType = OptionBuilder.isRequired().hasArg().withArgName( "Variant Type" ) .withDescription(
         * "The type of variant in this file, one of: CNV, Indel, SNV, Inversion" ) .create( "variant" );
         */
        Option project = OptionBuilder.create( "project" );

        addOption( "existingproject", false, "You must use this option if you are adding to an existing project" );

        addOption( "dryrun", false, "Use this option to validate your data before uploading" );

        addOption( d );
        addOption( f );

        addOption( project );

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

            System.out.println( "getting value objects" );
            VariantUploadServiceResult result = VariantUploadService
                    .makeVariantValueObjectsFromDecipherResultSet( results );

            results.close();
            stmt.close();
            conn.close();

            if ( result.getErrorMessages().isEmpty() && !dryRun ) {

                System.out.println( "inserting " + result.getVariantsToAdd().size() + " value objects into database" );

                // should probably batch this up to speed it up a bit

                projectManager.addSubjectVariantsToSpecialProject( projectName, deleteProject,
                        result.getVariantsToAdd(), false );

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
    protected void processOptions() {
        if ( this.hasOption( 'd' ) ) {
            directory = this.getOptionValue( 'd' );
        }
        if ( this.hasOption( 'f' ) ) {
            filename = this.getOptionValue( 'f' );
        }

        if ( this.hasOption( "project" ) ) {
            projectName = this.getOptionValue( "project" );
        }

        if ( this.hasOption( "existingproject" ) ) {
            deleteProject = false;
        } else {
            deleteProject = true;
        }

        if ( this.hasOption( "dryrun" ) ) {
            dryRun = true;
        }

    }

}
