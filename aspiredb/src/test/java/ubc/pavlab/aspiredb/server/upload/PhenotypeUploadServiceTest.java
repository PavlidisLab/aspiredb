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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadService;
import ubc.pavlab.aspiredb.server.fileupload.PhenotypeUploadServiceResult;
import ubc.pavlab.aspiredb.server.model.PhenotypeValueType;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;

public class PhenotypeUploadServiceTest extends BaseSpringContextTest {

    @Autowired
    PhenotypeUploadService phenotypeUploadService;

    @Autowired
    OntologyService os;

    @Test
    public void testMakeValueObjectsFromPhenotypeFile() {

        super.runAsAdmin();

        try {

            os.getHumanPhenotypeOntologyService().startInitializationThread( true, true );
            int c = 0;

            while ( !os.getHumanPhenotypeOntologyService().isOntologyLoaded() ) {
                Thread.sleep( 10000 );
                log.info( "Waiting for HumanPhenotypeOntology to load" );
                if ( ++c > 10 ) {
                    fail( "Ontology load timeout" );
                }
            }

            Class.forName( "org.relique.jdbc.csv.CsvDriver" );
            Connection conn = DriverManager.getConnection( "jdbc:relique:csv:src/test/resources/data" );

            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery( "SELECT * FROM testphenotype" );

            PhenotypeUploadServiceResult phenResult = phenotypeUploadService
                    .getPhenotypeValueObjectsFromResultSet( results );

            results.close();
            stmt.close();
            conn.close();

            assertEquals( 15, phenResult.getPhenotypesToAdd().size() );

            PhenotypeValueObject temp1 = phenResult.getPhenotypesToAdd().get( 0 );

            assertEquals( temp1.getExternalSubjectId(), "temp1" );

            HashSet<String> phenotypeNameSet = new HashSet<String>();

            boolean fail = true;

            for ( PhenotypeValueObject pvo : phenResult.getPhenotypesToAdd() ) {

                phenotypeNameSet.add( pvo.getName() );

                if ( pvo.getExternalSubjectId().equals( "temp1" ) && pvo.getUri() != null
                        && pvo.getUri().equals( "HP_0000077" ) ) {
                    assertEquals( pvo.getDbValue(), "1" );
                    assertEquals( pvo.getValueType(), PhenotypeValueType.HPONTOLOGY.toString() );
                    fail = false;
                }

            }

            if ( fail ) {
                fail( " Record not present" );
            }

            assertEquals( 5, phenotypeNameSet.size() );

            assertTrue( phenotypeNameSet.contains( "Family history" ) );
            assertTrue( phenotypeNameSet.contains( "Gender" ) );
            assertTrue( phenotypeNameSet.contains( "Abnormality of the bladder" ) );// "HP_0000014"
            assertTrue( phenotypeNameSet.contains( "Abnormality of the ureter" ) );// "HP_0000069"
            assertTrue( phenotypeNameSet.contains( "Abnormality of the kidney" ) );// "HP_0000077"

        } catch ( Exception e ) {

            fail( "An exception was thrown while processing the file" + e.getMessage() );
        }

    }

}