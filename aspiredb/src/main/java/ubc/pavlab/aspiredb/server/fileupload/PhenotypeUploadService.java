/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
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
package ubc.pavlab.aspiredb.server.fileupload;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadService.CommonVariantColumn;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;

@Service("phenotypeUploadService")
public class PhenotypeUploadService {

    @Autowired
    PhenotypeUtil phenotypeUtil;

    protected static Log log = LogFactory.getLog( PhenotypeUploadService.class );

    // cut and pasted and modified to handle decipher file to quickly get it into the system
    // no point making this pretty because some guy at decipher is going to break this code next time he uploads a file
    // to the decipher ftp server
    public PhenotypeUploadServiceResult getPhenotypeValueObjectsFromDecipherResultSet( ResultSet results )
            throws Exception {

        ArrayList<String> errorMessages = new ArrayList<String>();

        ArrayList<PhenotypeValueObject> voList = new ArrayList<PhenotypeValueObject>();

        HashSet<String> unmatched = new HashSet<String>();
        
        String regex = "\\bHP:\\d+\\b";
        Pattern pattern = Pattern.compile(regex);

        int lineNumber = 1;
        while ( results.next() ) {

            String html = results.getString( "html" );
            if ( html.contains( "Phenotypes" ) ) {
            	Matcher matcher = pattern.matcher(html);
            	while (matcher.find()) {
            		String s = matcher.group();
            		s = s.replaceAll( ":", "_" );
            		
                    PhenotypeValueObject vo = new PhenotypeValueObject();
                    vo.setExternalSubjectId( results.getString( CommonVariantColumn.SUBJECTID.key ) );
            		
                    try {

                        phenotypeUtil.setNameUriValueType( vo, s );
                        phenotypeUtil.setValue( vo, "1" );
                        voList.add( vo );

                    } catch ( InvalidDataException e ) {
                        errorMessages.add( "Error on line number:" + lineNumber + " phenotype string:" + s );

                        unmatched.add( s );
                    }
            	}
            } else {
                errorMessages.add( "Error on line number:" + lineNumber + "  \nFull entry:" + html );
            }

            lineNumber++;

        }

        return new PhenotypeUploadServiceResult( voList, errorMessages, unmatched );

    }

    public PhenotypeUploadServiceResult getPhenotypeValueObjectsFromResultSet( ResultSet results ) throws Exception {

        ArrayList<String> phenotypeFileColumns = new ArrayList<String>();

        ResultSetMetaData rsmd = results.getMetaData();
        int numColumns = rsmd.getColumnCount();

        // Get the column names; column indices start from 1
        for ( int i = 1; i < numColumns + 1; i++ ) {
            String columnName = rsmd.getColumnName( i );
            phenotypeFileColumns.add( columnName.trim() );
        }

        int lineNumber = 1;

        ArrayList<String> errorMessages = new ArrayList<String>();

        ArrayList<PhenotypeValueObject> voList = new ArrayList<PhenotypeValueObject>();

        while ( results.next() ) {

            // check if required fields exist
            if ( lineNumber <= 1 ) {
                String requiredColumn = CommonVariantColumn.SUBJECTID.key;
                try {
                    results.getString( requiredColumn );
                } catch ( SQLException e ) {
                    String errorMessage = "Required column '" + requiredColumn + "' not found.";
                    log.error( errorMessage, e );
                    errorMessages.add( errorMessage );
                    break;
                }
            }

            for ( String p : phenotypeFileColumns ) {

                try {

                    if ( !p.equals( CommonVariantColumn.SUBJECTID.key ) && results.getString( p ) != null
                            && !results.getString( p ).trim().isEmpty() ) {

                        // No data means unobserved. Change later if people change their minds again.
                        if ( results.getString( p ) != null && !results.getString( p ).trim().isEmpty() ) {

                            PhenotypeValueObject vo = new PhenotypeValueObject();

                            vo.setExternalSubjectId( results.getString( CommonVariantColumn.SUBJECTID.key ) );

                            phenotypeUtil.setNameUriValueType( vo, p );
                            phenotypeUtil.setValue( vo, results.getString( p ).trim() );

                            voList.add( vo );

                        }
                    }
                } catch ( InvalidDataException e ) {

                    errorMessages.add( "Invalid data on line number: " + lineNumber + " error message:"
                            + e.getMessage() );

                }

                lineNumber++;
            }

        }

        return new PhenotypeUploadServiceResult( voList, errorMessages, new HashSet<String>() );

    }

}