package ubc.pavlab.aspiredb.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.shared.GenomicRange;

public class GenomeCoordinateConverterTest extends BaseSpringContextTest {

    @Autowired
    GenomeCoordinateConverter converter;

    @Test
    public void testBandToBaseConversion() {
        // One band
        // Query : chr10 p11.22
        // Correct answer: 31300000 34400000
        GenomeCoordinateConverter.BaseInterval interval = converter.bandToBase( "10", "p11.22", "p11.22" );

        assertEquals( "Start mismatch", interval.getStart().intValue(), 31300000 );
        assertEquals( "End mismatch", interval.getEnd().intValue(), 34400000 );

        // Multi band
        // Query: chr12 p13.1 - q12
        // Correct answer: 12800000 - 46400000
        interval = converter.bandToBase( "12", "p13.1", "q12" );

        assertEquals( "Start mismatch", interval.getStart().intValue(), 12800000 );
        assertEquals( "End mismatch", interval.getEnd().intValue(), 46400000 );
    }

    @Test
    public void testBaseToBandConversion() {
        GenomeCoordinateConverter.BandInterval interval = converter.baseToBand( "10", 31300000, 34300000 );

        assertEquals( "Start mismatch", interval.getStartBand(), "p11.22" );
        assertEquals( "End mismatch", interval.getEndBand(), "p11.22" );

        interval = converter.baseToBand( "12", 12800000, 46399999 );

        assertEquals( "Start mismatch", interval.getStartBand(), "p13.1" );
        assertEquals( "End mismatch", interval.getEndBand(), "q12" );

        interval = converter.baseToBand( "12", 12800010, 46399995 );

        assertEquals( "Start mismatch", interval.getStartBand(), "p13.1" );
        assertEquals( "End mismatch", interval.getEndBand(), "q12" );
    }

    @Test
    public void testFillInCytobandCoordinates() {
        GenomicRange genomicRange = new GenomicRange( "12", 12800000, 46399999 );
        converter.fillInCytobandCoordinates( genomicRange );
        assertEquals( "Start mismatch", genomicRange.getBandStart(), "p13.1" );
        assertEquals( "End mismatch", genomicRange.getBandEnd(), "q12" );
    }

}
