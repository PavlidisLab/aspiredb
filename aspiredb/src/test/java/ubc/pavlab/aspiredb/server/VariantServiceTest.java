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
package ubc.pavlab.aspiredb.server;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.dao.ProjectDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.service.VariantService;
import ubc.pavlab.aspiredb.server.util.PersistentTestObjectHelper;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.CharacteristicProperty;
import ubc.pavlab.aspiredb.shared.query.NumericalDataType;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

public class VariantServiceTest extends BaseSpringContextTest {

    @Autowired
    private VariantService variantService;

    @Autowired
    private VariantDao variantDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private PersistentTestObjectHelper testObjectHelper;

    private Variant variant;
    private Subject subject;
    private Project project;

    @After
    public void cleanup() {
        new InlineTransaction() {
            @Override
            public void instructions() {
                variantDao.remove( variant );
                subjectDao.remove( subject );
                projectDao.remove( project );
            }
        }.execute();
    }

    @Before
    public void init() {
        new InlineTransaction() {
            @Override
            public void instructions() {
                Project p = new Project();
                p.setName( "VariantServiceTestProject" );
                project = testObjectHelper.createPersistentProject( p );
                subject = testObjectHelper.createPersistentTestIndividualObject( "testSubjectVariantServiceTest" );
                subject = testObjectHelper.addSubjectToProject( subject, project );
                variant = testObjectHelper.createPersistentTestCNVObject();
                variant.setSubject( subject );
            }
        }.execute();
    }

    @Test
    public void testGetVariant() {

        VariantValueObject variantValueObject = variantService.getVariant( variant.getId() );
        assertNotNull( variantValueObject );

    }

    @Test
    public void testSuggestProperties() throws NotLoggedInException {
        final String cnvLength = "cnvLength";
        new InlineTransaction() {
            @Override
            public void instructions() {
                variant.getCharacteristics().add( new Characteristic( cnvLength, "100" ) );
                variantDao.update( variant );
            }
        }.execute();
        Collection<Property> suggestions = variantService.suggestPropertiesForVariantType( VariantType.CNV );
        assertTrue( suggestions.size() > 2 );
        boolean found = false;
        for ( Property prop : suggestions ) {
            if ( prop.getName().equals( cnvLength ) && prop.getDataType() instanceof NumericalDataType ) {
                found = true;
            }
        }
        assertTrue( cnvLength + " not found", found );

        // with a projectId
        suggestions = variantService.suggestPropertiesForVariantTypeInProject( VariantType.CNV, project.getId() );
        assertTrue( suggestions.size() > 2 );
        found = false;
        for ( Property prop : suggestions ) {
            if ( prop.getName().equals( cnvLength ) && prop.getDataType() instanceof NumericalDataType ) {
                found = true;
            }
        }
        assertTrue( cnvLength + " not found", found );
    }

    @Test
    public void testSuggestValues() throws NotLoggedInException, BioMartServiceException, NeurocartaServiceException {
        SuggestionContext suggestionContext = new SuggestionContext();
        CharacteristicProperty property = new CharacteristicProperty( "BENIGN" );
        Collection<PropertyValue> suggestions = variantService.suggestValues( property, suggestionContext );
        Collection<String> stringValues = new ArrayList<String>();
        for ( PropertyValue suggestion : suggestions ) {
            stringValues.add( suggestion.getValue().toString() );
        }
        assertTrue( suggestions.size() > 0 );
        assertTrue( stringValues.contains( "YES" ) );
    }
}
