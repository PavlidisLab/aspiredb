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

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.GeneValueObject;

/**
 * author: anton date: 22/05/13
 */
public class GeneServiceTest extends BaseSpringContextTest {

    private BioMartQueryService bioMartQueryServiceMock;
    private VariantDao variantDaoMock;
    private GeneService geneService = new GeneServiceImpl();

    @Before
    public void setup() throws Exception {
        Variant variant = new CNV();
        GenomicLocation location = new GenomicLocation( "Y", 1, 100 );

        variant.setLocation( location );

        variantDaoMock = EasyMock.createMock( VariantDao.class );
        EasyMock.expect( variantDaoMock.load( 2L ) ).andReturn( variant );
        EasyMock.replay( variantDaoMock );

        bioMartQueryServiceMock = EasyMock.createMock( BioMartQueryService.class );
        EasyMock.expect( bioMartQueryServiceMock.fetchGenesByLocation( "Y", 1L, 100L ) ).andReturn(
                Arrays.asList( new GeneValueObject( "1", "HAIRCH", "Hairy chest gene.", "", "human" ) ) );
        EasyMock.replay( bioMartQueryServiceMock );

        ReflectionTestUtils.setField( geneService, "variantDao", variantDaoMock, VariantDao.class );
        ReflectionTestUtils.setField( geneService, "bioMartQueryService", bioMartQueryServiceMock,
                BioMartQueryService.class );
    }

    @Test
    public void testFindGenesWithNeurocartaPhenotype() throws Exception {

    }

    @Test
    public void testGetGenesInsideVariants() throws Exception {
        List<GeneValueObject> genes = geneService.getGenesInsideVariants( Arrays.asList( 2L ) );

        assertEquals( genes.size(), 1 );
        assertEquals( genes.iterator().next().getSymbol(), "HAIRCH" );
    }
}
