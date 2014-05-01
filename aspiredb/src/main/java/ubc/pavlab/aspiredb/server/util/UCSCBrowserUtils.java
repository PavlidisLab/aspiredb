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
package ubc.pavlab.aspiredb.server.util;

import ubc.pavlab.aspiredb.shared.GenomicRange;

/**
 * 
 * 
 * 
 * @author mly
 *
 */
public class UCSCBrowserUtils {

    private static final String UCSC_URL = "http://genome.ucsc.edu/cgi-bin/hgTracks?";
    private static final String DB_VERSION = "hg19";

    @Deprecated
    public static String makeShowGenomicRangeURL( GenomicRange range ) {
        GenomicRange paddedRange = addPadding( range );

        return
                UCSC_URL + "org=human&db=" + DB_VERSION 
                + "&position=chr" + range.getChromosome() + ":"
                + paddedRange.getBaseStart() + "-" + paddedRange.getBaseEnd();
    }
    
    public static GenomicRange addPadding( GenomicRange range ) {
        int padding = 5000;
        int newStart = Math.max( 0, range.getBaseStart() - padding ); 
        int newEnd = range.getBaseEnd() + padding;
        
        return new GenomicRange( range.getChromosome(), newStart, newEnd );        
    }

}
