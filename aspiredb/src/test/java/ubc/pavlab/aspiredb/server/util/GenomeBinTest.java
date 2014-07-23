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

        int a = GenomeBin.binFromRange( 10, 10000 );
        assertEquals( 585, a );

        a = GenomeBin.binFromRange( 10000, 10000 );
        assertEquals( 585, a );

        a = GenomeBin.binFromRange( 100, 100000 );
        assertEquals( 585, a );

        a = GenomeBin.binFromRange( 200000000, 200000400 );
        assertEquals( 2110, a );

        a = GenomeBin.binFromRange( 200000, 200000 );
        assertEquals( 586, a );

        a = GenomeBin.binFromRange( 100001, 100002 );
        assertEquals( 585, a );

        a = GenomeBin.binFromRange( 1000001, 1000002 );
        assertEquals( 592, a );

        a = GenomeBin.binFromRange( 1000001, 2000002 );
        assertEquals( 9, a );

        a = GenomeBin.binFromRange( 5000001, 9000002 );
        assertEquals( 1, a );

        a = GenomeBin.binFromRange( 37885247, 37885647 );
        assertEquals( 874, a );

        List<Integer> relevantBins = GenomeBin.relevantBins( 10, 1000 );
        // log.info( StringUtils.join( relevantBins, "," ) );
        assertEquals( 4, relevantBins.size() );
        assertEquals( new Integer( 585 ), relevantBins.get( 0 ) );
        assertEquals( new Integer( 73 ), relevantBins.get( 1 ) );
        assertEquals( new Integer( 9 ), relevantBins.get( 2 ) );
        assertEquals( new Integer( 1 ), relevantBins.get( 3 ) );
        // assertEquals( new Integer( 1 ), relevantBins.get( 4 ) );
        // assertEquals( new Integer( 0 ), relevantBins.get( 5 ) );

        relevantBins = GenomeBin.relevantBins( 100000000, 102000000 );
        // log.info( StringUtils.join( relevantBins, "," ) );
        assertEquals( 23, relevantBins.size() );
        int[] expected = new int[] { 1347, 1348, 1349, 1350, 1351, 1352, 1353, 1354, 1355, 1356, 1357, 1358, 1359,
                1360, 1361, 1362, 1363, 168, 169, 170, 20, 21, 2 };
        for ( int i = 0; i < expected.length; i++ ) {
            assertEquals( new Integer( expected[i] ), relevantBins.get( i ) );
        }

    }

}
