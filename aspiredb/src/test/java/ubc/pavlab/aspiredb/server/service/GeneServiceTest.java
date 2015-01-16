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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.util.GenomeBin;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

/**
 * author: anton date: 22/05/13
 */
public class GeneServiceTest extends BaseSpringContextTest {

    private BioMartQueryService bioMartQueryServiceMock;
    private VariantDao variantDaoMock;
    private GeneService geneService = new GeneServiceImpl();
    private final String PATIENT_ID = "patient-01";
    private final String GENE_INSIDE_VARIANT = "GENE_INSIDE_VARIANT";
    private final String VARIANT_INSIDE_GENE = "VARIANT_INSIDE_GENE";
    private final String OVERLAPEND = "OVERLAPEND";
    private final String OVERLAPSTART = "OVERLAPSTART";
    private final String NOTOVERLAP_DIFFCHR = "NOTOVERLAP_DIFFCHR";
    private final String NOTOVERLAP_DOWNSTREAM = "NOTOVERLAP_DOWNSTREAM";

    private GeneValueObject createGene( String symbol, String chromosome, int start, int end ) {
        GeneValueObject gene = new GeneValueObject( "ENSG1", symbol, "Gene desc " + symbol, "", "human" );
        gene.setGenomicRange( new GenomicRange( chromosome, start, end ) );
        return gene;
    }

    @Before
    public void setup() throws Exception {
        Variant variant = new CNV();
        variant.setId( 2L );

        Subject subject = new Subject();
        subject.setPatientId( PATIENT_ID );
        variant.setSubject( subject );
        subject.setId( 1L );

        GenomicLocation location = new GenomicLocation( "Y", 1, 100 );
        variant.setLocation( location );

        Variant variant2 = new CNV();
        variant2.setId( 3L );
        variant2.setSubject( subject );
        variant2.setLocation( new GenomicLocation( "Y", 101, 105 ) );

        variantDaoMock = EasyMock.createMock( VariantDao.class );
        // EasyMock.expect( variantDaoMock.load( 2L ) ).andReturn( variant );
        EasyMock.expect( variantDaoMock.load( Arrays.asList( 2L ) ) ).andReturn( Arrays.asList( variant ) ).times( 2 );
        EasyMock.expect( variantDaoMock.load( Arrays.asList( 2L, 3L ) ) )
                .andReturn( Arrays.asList( variant, variant2 ) ).times( 2 );
        EasyMock.replay( variantDaoMock );

        GenomicRange geneRange = new GenomicRange( "Y", 1, 100 );
        bioMartQueryServiceMock = EasyMock.createMock( BioMartQueryService.class );
        for ( int bin : GenomeBin.relevantBins( geneRange.getChromosome(), geneRange.getBaseStart(),
                geneRange.getBaseEnd() ) ) {
            if ( bin == GenomeBin.binFromRange( geneRange.getChromosome(), geneRange.getBaseStart(),
                    geneRange.getBaseEnd() ) ) {

                EasyMock.expect( bioMartQueryServiceMock.fetchGenesByBin( bin ) )
                        .andReturn(
                                Arrays.asList(
                                        createGene( "HAIRCH", geneRange.getChromosome(), geneRange.getBaseStart(),
                                                geneRange.getBaseEnd() ),
                                        createGene( this.GENE_INSIDE_VARIANT, geneRange.getChromosome(),
                                                geneRange.getBaseStart() + 10, geneRange.getBaseEnd() - 10 ),
                                        createGene( this.VARIANT_INSIDE_GENE, geneRange.getChromosome(),
                                                geneRange.getBaseStart() - 1, geneRange.getBaseEnd() + 10 ),
                                        createGene( this.OVERLAPEND, geneRange.getChromosome(),
                                                geneRange.getBaseStart() + 50, geneRange.getBaseEnd() + 50 ),
                                        createGene( this.OVERLAPSTART, geneRange.getChromosome(),
                                                geneRange.getBaseStart() - 1, geneRange.getBaseEnd() - 50 ),
                                        createGene( this.NOTOVERLAP_DIFFCHR, "22", geneRange.getBaseStart(),
                                                geneRange.getBaseEnd() ),
                                        createGene( this.NOTOVERLAP_DOWNSTREAM, geneRange.getChromosome(),
                                                geneRange.getBaseStart() + 1000, geneRange.getBaseEnd() + 1000 ) ) )
                        .times( 2 );
            } else {
                EasyMock.expect( bioMartQueryServiceMock.fetchGenesByBin( bin ) )
                        .andReturn( new ArrayList<GeneValueObject>() ).times( 2 );
            }
        }

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
        Collection<GeneValueObject> genes = geneService.getGenesInsideVariants( Arrays.asList( 2L ) );

        assertEquals( genes.size(), 5 );
        int found = 0;
        for ( GeneValueObject gene : genes ) {
            String symbol = gene.getSymbol();
            if ( symbol.equals( "HAIRCH" ) || symbol.equals( this.VARIANT_INSIDE_GENE )
                    || symbol.equals( this.GENE_INSIDE_VARIANT ) || symbol.equals( this.OVERLAPEND )
                    || symbol.equals( this.OVERLAPSTART ) ) {
                found++;
            }
        }
        assertEquals( genes.size(), found );
    }

    @Test
    public void testGetCompoundHeterozygotes() throws Exception {
        Map<String, Map<GeneValueObject, Collection<VariantValueObject>>> output = geneService
                .getCompoundHeterozygotes( Arrays.asList( 2L ) );
        assertEquals( output.keySet().size(), 0 );

        // now let's try adding a variant that hits the same gene
        List<Long> varIds = Arrays.asList( 2L, 3L );
        output = geneService.getCompoundHeterozygotes( varIds );
        assertEquals( output.keySet().size(), 1 );
        Map<GeneValueObject, Collection<VariantValueObject>> actuals = output.get( this.PATIENT_ID );
        assertEquals( actuals.keySet().size(), 2 );
        int found = 0;
        for ( GeneValueObject gene : actuals.keySet() ) {
            if ( gene.getSymbol().equals( this.VARIANT_INSIDE_GENE ) || gene.getSymbol().equals( this.OVERLAPEND ) ) {
                assertEquals( 2, actuals.get( gene ).size() ); // 2 variants
                for ( VariantValueObject variant : actuals.get( gene ) ) {
                    if ( variant.getId() == 2L || variant.getId() == 3L ) {
                        found++;
                    }
                }

            }
        }
        assertEquals( 4, found );
    }
}
