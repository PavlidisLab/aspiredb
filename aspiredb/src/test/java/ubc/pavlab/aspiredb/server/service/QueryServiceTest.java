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
package ubc.pavlab.aspiredb.server.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;

/**
 * author: anton
 * date: 22/05/13
 */
public class QueryServiceTest extends BaseSpringContextTest {

    @Autowired
    QueryService queryService;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetPhenotypeSuggestionLoadResult() throws Exception {

    }

    @Test
    public void testGetOntologyTermSuggestions() throws Exception {

    }

    @Test
    public void testQuerySubjects() throws Exception {

    }

    @Test
    public void testQueryVariants() throws Exception {

    }

    @Test
    public void testGetValuesForOntologyTerm() throws Exception {

    }

    @Test
    public void testGetGeneSuggestionLoadResult() throws Exception {

    }

    @Test
    public void testGetGeneSuggestions() throws Exception {

    }

    @Test
    public void testGetNeurocartaPhenotypeSuggestionLoadResult() throws Exception {

    }

    @Test
    public void testGetNeurocartaPhenotypeSuggestions() throws Exception {

    }

    @Test
    public void testGetSubjectSuggestionLoadResult() throws Exception {

    }

    @Test
    public void testGetGenes() throws Exception {

    }

    @Test
    public void testGetNeurocartaPhenotypes() throws Exception {

    }

    @Test
    public void testGetPhenotypeSuggestions() throws Exception {

    }

    @Test
    public void testGetSubjects() throws Exception {

    }

    @Test
    public void testSaveLoadQuery() throws Exception {
        /*
        Set<RestrictionFilterConfig> filters = new HashSet<RestrictionFilterConfig>();
        filters.add( new VariantFilterConfig(
                        QueryTestUtils.makeTestVariantRestrictionExpression()) );
        
        Long id = queryService.saveQuery("testSavedQuery", filters);

        QueryValueObject loadedQueryVO = queryService.loadQuery(savedQueryVO.getId());

        AspireDbFilterConfig filterConfig = loadedQueryVO.getQuery().iterator().next();
        RestrictionExpression expression = ((VariantFilterConfig) filterConfig).getRestriction();

        assertTrue(expression instanceof Conjunction);
        assertTrue(((Conjunction) expression).getRestrictions().size() == 3);
        */
    }

}
