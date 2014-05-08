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

package ubc.pavlab.aspiredb.server.ontology;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubic.basecode.ontology.model.OntologyResource;
import ubic.basecode.ontology.model.OntologyTerm;

/**
 * @author paul
 * @version $Id: OntologyServiceTest.java,v 1.6 2013/06/21 05:10:42 paul Exp $
 */
public class OntologyServiceTest extends BaseSpringContextTest {

    @Autowired
    OntologyService os;

    @Test
    public void testObsolete() throws Exception {
        os.getDiseaseOntologyService().startInitializationThread( true );
        int c = 0;

        while ( !os.getDiseaseOntologyService().isOntologyLoaded() ) {
            Thread.sleep( 10000 );
            log.info( "Waiting for DiseaseOntology to load" );
            if ( ++c > 10 ) {
                fail( "Ontology load timeout" );
            }
        }

        OntologyTerm t1 = os.getTerm( "http://purl.obolibrary.org/obo/DOID_0050001" );
        assertNotNull( t1 );

        // Actinomadura madurae infectious disease
        assertTrue( os.isObsolete( "http://purl.obolibrary.org/obo/DOID_0050001" ) );

        // inflammatory diarrhea, not obsolete as of May 2012.
       // assertNotNull( os.getTerm( "http://purl.obolibrary.org/obo/DOID_0050132" ) );
      //  assertTrue( !os.isObsolete( "http://purl.obolibrary.org/obo/DOID_0050132" ) );

    }

    @Test
    public void testHumanPhenotypeOntology() throws Exception {
        os.getHumanPhenotypeOntologyService().startInitializationThread( true );
        int c = 0;

        while ( !os.getHumanPhenotypeOntologyService().isOntologyLoaded() ) {
            Thread.sleep( 10000 );
            log.info( "Waiting for HumanPhenotypeOntology to load" );
            if ( ++c > 10 ) {
                fail( "Ontology load timeout" );
            }
        }

        // e.g. http://purl.org/obo/owl/HP#HP_0001748, http://purl.org/obo/owl/HP#HP_0004950,
        // http://purl.org/obo/owl/HP#HP_0001746

        OntologyResource t1 = os.getTerm( "http://purl.obolibrary.org/obo/HP_0004209" );

        assertNotNull( t1 );

    }

}
