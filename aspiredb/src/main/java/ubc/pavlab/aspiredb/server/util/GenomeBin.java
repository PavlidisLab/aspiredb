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

import java.util.ArrayList;
import java.util.List;

/**
 * Map genomic locations to 'bins' to speed up range queries (based on Jim Kent's UCSC goldenpath code). See
 * http://genome.cshlp.org/content/12/6/996/F7.expansion.html and
 * http://genomewiki.ucsc.edu/index.php/Bin_indexing_system.
 * 
 * @author paul
 */
public class GenomeBin {

    private static int _binFirstShift = 17; /* How much to shift to get to finest bin. */

    private static int _binNextShift = 3; /* How much to shift to get to next larger bin. */

    // private static int _binOffsetOldToExtended = 4681;

    private static int binOffsets[] = { 512 + 64 + 8 + 1, 64 + 8 + 1, 8 + 1, 1, 0 };

    // 4681,585,73,9,1,1
    // private static int binOffsetsExtended[] = { 4096 + 512 + 64 + 8 + 1, 512 + 64 + 8 + 1, 64 + 8 + 1, 8 + 1, 1, 0 };

    public static int BINRANGE_MAXEND_512M = ( 512 * 1024 * 1024 );

    /**
     * return bin that this start-end segment is in
     */
    public static int binFromRange( int start, int end ) {
        assert end >= start;
        // if ( end <= BINRANGE_MAXEND_512M )
        return binFromRangeStandard( start, end );
        // return binFromRangeExtended( start, end );
    }

    /**
     * Return all bins that this range overlaps
     * 
     * @param start
     * @param end
     * @return
     */
    public static List<Integer> relevantBins( int start, int end ) {
        assert end >= start;

        List<Integer> bins = new ArrayList<>();

        int startBin = start, endBin = end - 1;
        startBin >>= _binFirstShift;
        endBin >>= _binFirstShift;

        for ( int i = 0; i < binOffsets.length; i++ ) {

            // add all the bins at this level
            for ( int j = startBin; j <= endBin; j++ ) {
                bins.add( binOffsets[i] + j );
            }

            startBin >>= _binNextShift;
            endBin >>= _binNextShift;

        }

        return bins;
    }

    // /**
    // * Given start,end in chromosome coordinates assign it a bin. There's a bin for each 128k segment, for each 1M
    // * segment, for each 8M segment, for each 64M segment, for each 512M segment, and one top level bin for 4Gb. Note,
    // * since start and end are int's, the practical limit is up to 2Gb-1, and thus, only four result bins on the
    // second
    // * level. A range goes into the smallest bin it will fit in.
    // */
    // private static int binFromRangeExtended( int start, int end ) {
    // int startBin = start, endBin = end - 1;
    // startBin >>= _binFirstShift;
    // endBin >>= _binFirstShift;
    // for ( int i = 0; i < binOffsetsExtended.length; ++i ) {
    // if ( startBin == endBin ) {
    // return _binOffsetOldToExtended + binOffsetsExtended[i] + startBin;
    // }
    // startBin >>= _binNextShift;
    // endBin >>= _binNextShift;
    // }
    //
    // throw new IllegalArgumentException( "start " + start + ", end " + end + " out of range" );
    // }

    /**
     * Given start,end in chromosome coordinates assign it a bin. There's a bin for each 128k segment, for each 1M
     * segment, for each 8M segment, for each 64M segment, and for each chromosome (which is assumed to be less than
     * 512M.) A range goes into the smallest bin it will fit in.
     */
    private static int binFromRangeStandard( int start, int end ) {
        int startBin = start, endBin = end - 1, i;
        startBin >>= _binFirstShift;
        endBin >>= _binFirstShift;
        for ( i = 0; i < binOffsets.length; ++i ) {
            if ( startBin == endBin ) return binOffsets[i] + startBin;
            startBin >>= _binNextShift;
            endBin >>= _binNextShift;
        }
        throw new IllegalArgumentException( "start " + start + ", end " + end + " out of range " );
    }

}
