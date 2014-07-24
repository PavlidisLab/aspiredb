/*
 * The aspiredb project
 * 
 * Copyright (c) 2014 University of British Columbia
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

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author paul
 */
public class GenomeBinTest {

    private static Logger log = LoggerFactory.getLogger( GenomeBinTest.class );

    @Test
    public void test() {

        int a = GenomeBin.binFromRange( "1", 10, 10000 );
        assertEquals( 18441, a );

        a = GenomeBin.binFromRange( "1", 10000, 10000 );
        assertEquals( 18441, a );

        a = GenomeBin.binFromRange( "1", 100, 100000 );
        assertEquals( 18441, a );

        a = GenomeBin.binFromRange( "1", 200000000, 200000400 );
        assertEquals( 19966, a );

        a = GenomeBin.binFromRange( "1", 200000, 200000 );
        assertEquals( 18442, a );

        a = GenomeBin.binFromRange( "1", 100001, 100002 );
        assertEquals( 18441, a );

        a = GenomeBin.binFromRange( "1", 1000001, 1000002 );
        assertEquals( 18448, a );

        a = GenomeBin.binFromRange( "1", 1000001, 2000002 );
        assertEquals( 17865, a );

        a = GenomeBin.binFromRange( "1", 5000001, 9000002 );
        assertEquals( 17857, a );

        a = GenomeBin.binFromRange( "17", 37885247, 37885647 );
        assertEquals( 66005, a );

        a = GenomeBin.binFromRange( "4", 72247, 5545043 );
        assertEquals( 17958, a );

        a = GenomeBin.binFromRange( "4", 1, 2 );
        assertEquals( 18534, a );

        List<Integer> relevantBins = GenomeBin.relevantBins( "1", 10, 1000 );
        log.info( StringUtils.join( relevantBins, "," ) );
        assertEquals( 5, relevantBins.size() );
        assertEquals( new Integer( 18441 ), relevantBins.get( 0 ) );
        assertEquals( new Integer( 17929 ), relevantBins.get( 1 ) );
        assertEquals( new Integer( 17865 ), relevantBins.get( 2 ) );
        assertEquals( new Integer( 17857 ), relevantBins.get( 3 ) );
        assertEquals( new Integer( 17856 ), relevantBins.get( 4 ) );

        // assertEquals( new Integer( 1 ), relevantBins.get( 4 ) );
        // assertEquals( new Integer( 0 ), relevantBins.get( 5 ) );

        relevantBins = GenomeBin.relevantBins( "1", 100000000, 102000000 );
        log.info( StringUtils.join( relevantBins, "," ) );
        assertEquals( 24, relevantBins.size() );
        int[] expected = new int[] { 19203, 19204, 19205, 19206, 19207, 19208, 19209, 19210, 19211, 19212, 19213,
                19214, 19215, 19216, 19217, 19218, 19219, 18024, 18025, 18026, 17876, 17877, 17858, 17856 };
        for ( int i = 0; i < expected.length; i++ ) {
            assertEquals( new Integer( expected[i] ), relevantBins.get( i ) );
        }

    }

}
