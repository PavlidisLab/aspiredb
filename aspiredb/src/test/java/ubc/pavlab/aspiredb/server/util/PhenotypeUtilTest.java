/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ubc.pavlab.aspiredb.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.dao.PhenotypeDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.ontology.OntologyService;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.Junction;
import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;

/**
 * @author ptan
 * @version $Id$
 */
public class PhenotypeUtilTest extends BaseSpringContextTest {

    @Autowired
    private PhenotypeUtil phenotypeUtil;

    @Autowired
    private OntologyService ontologyService;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private PhenotypeDao phenotypeDao;

    @Autowired
    PersistentTestObjectHelper persistentTestObjectHelper;

    private Collection<Phenotype> phenolist = new HashSet<Phenotype>();

    private static Log log = LogFactory.getLog( PhenotypeUtilTest.class.getName() );

    @Before
    public void setup() {
        phenolist.add( persistentTestObjectHelper.createPersistentTestPhenotypeObject( "Abnormality of the head",
                "HP_0000234", "HPONTOLOGY", "1" ) );
        phenolist.add( persistentTestObjectHelper.createPersistentTestPhenotypeObject( "Abnormality of the face",
                "HP_0000271", "HPONTOLOGY", "1" ) );
    }

    @After
    public void tearDown() {
        for ( Phenotype pheno : phenolist ) {
            persistentTestObjectHelper.removePhenotype( pheno );
        }
    }

    @Test
    public void testConvertValueToHPOntologyStandardValue() throws InvalidDataException {
        String result = PhenotypeUtil.convertValueToHPOntologyStandardValue( "Y" );
        assertEquals( "1", result );
        result = PhenotypeUtil.convertValueToHPOntologyStandardValue( "1" );
        assertEquals( "1", result );
        result = PhenotypeUtil.convertValueToHPOntologyStandardValue( "N" );
        assertEquals( "0", result );
    }

    @Test
    public void testExpandOntologyTermsWithNullUri() throws InterruptedException {
        PhenotypeRestriction phenotype = new PhenotypeRestriction( "Abnormality of the head", "1" );
        PhenotypeFilterConfig config = new PhenotypeFilterConfig();
        config.setRestriction( phenotype );

        PhenotypeFilterConfig result = phenotypeUtil.expandOntologyTerms( config, null );
        RestrictionExpression restriction = result.getRestriction();
        boolean foundFace = false;

        Collection<PhenotypeRestriction> phenotypeRestrictions = new HashSet<PhenotypeRestriction>();
        expandRestrictions( restriction, phenotypeRestrictions );

        assertTrue( phenotypeRestrictions.size() > 1 );
        for ( PhenotypeRestriction pr : phenotypeRestrictions ) {
            if ( pr.getName().equals( "Abnormality of the face" ) ) {
                foundFace = true;
                break;
            }
        }

        assertTrue( foundFace );
    }

    @Test
    public void testIsUri() {
        boolean result = PhenotypeUtil.isUri( "HP_00000271" );
        assertEquals( true, result );
        result = PhenotypeUtil.isUri( "Abnormality" );
        assertEquals( false, result );
        result = PhenotypeUtil.isUri( "http://purl.obolibrary.org/obo/HP_0000271" );
        assertEquals( false, result );
    }

    private void expandRestrictions( RestrictionExpression re, Collection<PhenotypeRestriction> phenotypeRestrictions ) {
        if ( re instanceof PhenotypeRestriction ) {
            phenotypeRestrictions.add( ( PhenotypeRestriction ) re );
            return;
        } else if ( re instanceof Junction ) {
            for ( RestrictionExpression re2 : ( ( Junction ) re ).getRestrictions() ) {
                expandRestrictions( re2, phenotypeRestrictions );
            }
        } else {
            return;
        }
    }
}
