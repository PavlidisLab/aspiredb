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

package ubc.pavlab.aspiredb.server.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadService;
import ubc.pavlab.aspiredb.server.fileupload.VariantUploadServiceResult;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.IndelValueObject;
import ubc.pavlab.aspiredb.shared.SNVValueObject;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

public class VariantUploadServiceTest extends BaseSpringContextTest {

    @Test
    public void testMakeValueObjectsFromCNVFile() {

        super.runAsAdmin();

        try {

            Class.forName( "org.relique.jdbc.csv.CsvDriver" );
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:src/test/resources/data" );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM testcnv" );

            VariantUploadServiceResult result = VariantUploadService.makeVariantValueObjectsFromResultSet( results,
                    VariantType.CNV );

            results.close();
            stmt.close();
            conn.close();

            assertEquals( result.getErrorMessages().size(), 0 );
            assertEquals( result.getVariantsToAdd().size(), 3 );

            VariantValueObject temp1vo = result.getVariantsToAdd().get( 0 );

            assertTrue( temp1vo instanceof CNVValueObject );

            CNVValueObject temp1cnv = ( CNVValueObject ) temp1vo;

            assertEquals( temp1cnv.getPatientId(), "temp1" );
            assertEquals( temp1cnv.getGenomicRange().getChromosome(), "1" );
            assertEquals( temp1cnv.getGenomicRange().getBaseStart(), 1000 );
            assertEquals( temp1cnv.getGenomicRange().getBaseEnd(), 2000 );
            assertEquals( temp1cnv.getType(), "loss" );

            Map<String, CharacteristicValueObject> charMap = temp1cnv.getCharacteristics();

            assertEquals( charMap.size(), 3 );

            assertEquals( charMap.get( "Inheritance" ).getValue(), "unclassified" );

            VariantValueObject temp3vo = result.getVariantsToAdd().get( 2 );

            assertTrue( temp3vo instanceof CNVValueObject );

            CNVValueObject temp3cnv = ( CNVValueObject ) temp3vo;

            assertEquals( temp3cnv.getPatientId(), "temp3" );
            assertEquals( temp3cnv.getGenomicRange().getChromosome(), "Y" );
            assertEquals( temp3cnv.getGenomicRange().getBaseStart(), 60000 );
            assertEquals( temp3cnv.getGenomicRange().getBaseEnd(), 700000 );
            assertEquals( temp3cnv.getType(), "gain" );

            charMap = temp3cnv.getCharacteristics();

            assertEquals( charMap.size(), 3 );

            assertEquals( charMap.get( "Inheritance" ).getValue(), "blah" );

        } catch ( Exception e ) {

            fail();
        }

    }

    @Test
    public void testMakeValueObjectsFromIndelFile() {

        super.runAsAdmin();

        try {

            Class.forName( "org.relique.jdbc.csv.CsvDriver" );
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:src/test/resources/data" );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM testindel" );

            VariantUploadServiceResult result = VariantUploadService.makeVariantValueObjectsFromResultSet( results,
                    VariantType.INDEL );

            results.close();
            stmt.close();
            conn.close();

            assertEquals( result.getErrorMessages().size(), 0 );
            assertEquals( result.getVariantsToAdd().size(), 4 );

            VariantValueObject temp1vo = result.getVariantsToAdd().get( 0 );

            assertTrue( temp1vo instanceof IndelValueObject );

            IndelValueObject temp1indel = ( IndelValueObject ) temp1vo;

            assertEquals( temp1indel.getPatientId(), "ind1" );
            assertEquals( temp1indel.getGenomicRange().getChromosome(), "10" );
            assertEquals( temp1indel.getGenomicRange().getBaseStart(), 1 );
            assertEquals( temp1indel.getGenomicRange().getBaseEnd(), 5 );
            assertEquals( temp1indel.getLength(), 200 );

            Map<String, CharacteristicValueObject> charMap = temp1indel.getCharacteristics();

            assertEquals( charMap.size(), 3 );

            assertEquals( charMap.get( "exonic_function" ).getValue(), "frameshift" );

            VariantValueObject temp3vo = result.getVariantsToAdd().get( 3 );

            assertTrue( temp3vo instanceof IndelValueObject );

            IndelValueObject temp3indel = ( IndelValueObject ) temp3vo;

            assertEquals( temp3indel.getPatientId(), "ind4" );
            assertEquals( temp3indel.getGenomicRange().getChromosome(), "17" );
            assertEquals( temp3indel.getGenomicRange().getBaseStart(), 4 );
            assertEquals( temp3indel.getGenomicRange().getBaseEnd(), 8 );
            assertEquals( temp3indel.getLength(), 0 );

            charMap = temp3indel.getCharacteristics();

            assertEquals( charMap.size(), 3 );

            assertEquals( charMap.get( "function_prediction" ).getValue(), "bla" );

        } catch ( Exception e ) {

            fail();
        }

    }

    @Test
    public void testMakeValueObjectsFromSNVFile() {

        super.runAsAdmin();

        try {

            Class.forName( "org.relique.jdbc.csv.CsvDriver" );
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:src/test/resources/data" );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM testsnp" );

            VariantUploadServiceResult result = VariantUploadService.makeVariantValueObjectsFromResultSet( results,
                    VariantType.SNV );

            results.close();
            stmt.close();
            conn.close();

            assertEquals( result.getErrorMessages().size(), 0 );
            assertEquals( result.getVariantsToAdd().size(), 3 );

            VariantValueObject temp1vo = result.getVariantsToAdd().get( 0 );

            assertTrue( temp1vo instanceof SNVValueObject );

            SNVValueObject temp1snv = ( SNVValueObject ) temp1vo;

            assertEquals( temp1snv.getPatientId(), "snv1" );
            assertEquals( temp1snv.getGenomicRange().getChromosome(), "1" );
            assertEquals( temp1snv.getGenomicRange().getBaseStart(), 1 );
            assertEquals( temp1snv.getGenomicRange().getBaseEnd(), 2 );
            assertEquals( temp1snv.getReferenceBase(), "C" );
            assertEquals( temp1snv.getObservedBase(), "T" );

            Map<String, CharacteristicValueObject> charMap = temp1snv.getCharacteristics();

            assertEquals( charMap.size(), 3 );

            assertEquals( charMap.get( "genotype_quality" ).getValue(), "67" );

            VariantValueObject temp3vo = result.getVariantsToAdd().get( 2 );

            assertTrue( temp3vo instanceof SNVValueObject );

            SNVValueObject temp3snv = ( SNVValueObject ) temp3vo;

            assertEquals( temp3snv.getPatientId(), "snv3" );
            assertEquals( temp3snv.getGenomicRange().getChromosome(), "15" );
            assertEquals( temp3snv.getGenomicRange().getBaseStart(), 10 );
            assertEquals( temp3snv.getGenomicRange().getBaseEnd(), 15 );
            assertEquals( temp3snv.getReferenceBase(), "G" );
            assertEquals( temp3snv.getObservedBase(), "A" );

            charMap = temp3snv.getCharacteristics();

            assertEquals( charMap.size(), 3 );

            assertEquals( charMap.get( "damaging" ).getValue(), "NA" );

        } catch ( Exception e ) {

            fail();
        }

    }

    @Test
    public void testPredictSNVFunction() {
        try {
            Class.forName( "org.relique.jdbc.csv.CsvDriver" );

            // query variants
            Connection qconn = DriverManager.getConnection( "jdbc:relique:csv:src/test/resources/data" );
            Statement qstmt = qconn.createStatement();
            ResultSet qresults = qstmt.executeQuery( "SELECT * FROM testsnp" );
            VariantUploadServiceResult result = VariantUploadService.makeVariantValueObjectsFromResultSet( qresults,
                    VariantType.SNV );
            qresults.close();
            qstmt.close();
            qconn.close();
            List<VariantValueObject> vos = result.getVariantsToAdd();
            assertEquals( vos.size(), 3 );
            Map<String, Map<Integer, Collection<SNVValueObject>>> chrMap = VariantUploadService
                    .constructQuerySNVMap( vos );
            assertEquals( chrMap.keySet().size(), 3 );

            // db / target variants
            final String chr = "1";
            Connection dbconn = DriverManager.getConnection( "jdbc:relique:csv:src/test/resources/data" + "?"
                    + "separator=" + URLEncoder.encode( "\t", "UTF-8" ) + "&" + "fileExtension=" + ".chr" + chr );
            Statement dbstmt = dbconn.createStatement();
            ResultSet dbResults = dbstmt.executeQuery( "SELECT * FROM testdbNSFP2.4_variant" );
            // snv1,1,1,2,C,T

            String dbPredColname = "LR_pred";

            Collection<SNVValueObject> matched = VariantUploadService.predictSNVFunction( chrMap.get( chr ), dbResults,
                    dbPredColname );

            dbResults.close();
            dbstmt.close();
            dbconn.close();

            assertEquals( matched.size(), 1 );
            assertEquals( matched.iterator().next().getCharacteristics().get( dbPredColname ).getValue(), "D" );

        } catch ( Exception e ) {
            fail();
        }
    }

}