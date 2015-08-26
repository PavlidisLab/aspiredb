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
package ubc.pavlab.aspiredb.server.ws;

import java.util.Collection;

import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.Variant;

/**
 * Methods for converting variant positions to BED format.
 * 
 * @author Michelle Ly
 * @version $Id: CnvToBed.java,v 1.15 2013/07/23 23:19:33 ptan Exp $
 */
public class CnvToBed {

    /*
     * Example of BED
     * 
     * The first three required BED fields are: chrom - The name of the chromosome chromStart - The starting position of
     * the feature chromEnd - The ending position of the feature
     * 
     * browser position chr22:20100000-20100900 track name=coords description="Chromosome coordinates list" visibility=2
     * chr22 20100000 20100100 chr22 20100011 20100200 chr22 20100215 20100400 chr22 20100350 20100500
     */

    private static final String RED = "255,0,0";
    private static final String BLUE = "0,0,255";
    private static final String BLACK = "0,0,0";

    public static String create( Collection<Variant> variants, String chr, int start, int end, String appUrl ) {
        StringBuilder buffer = new StringBuilder();

        buffer.append( "browser position " );
        buffer.append( "chr" + chr + ":" + start + "-" + end + "\n" );

        String trackDesc = "ASPIREdb variants";
        String trackVisibility = "visibility=pack";

        // FIXME home.jsp does not currently support any parameters
        buffer.append( "track name=\"" + trackDesc + "\" type=bedDetail description=\"" + trackDesc + "\""
                + trackVisibility + " itemRgb=On " + "url=\"" + appUrl + "home.jsp?variantId=$$\"\n" );
        for ( Variant variant : variants ) {
            String varDesc = variant.getDescription() == null ? "" : variant.getDescription();
            buffer.append( "chr" + variant.getLocation().getChromosome() + "\t" + variant.getLocation().getStart()
                    + "\t" + variant.getLocation().getEnd() + "\t" + variant.getSubject().getPatientId() + "\t"
                    // filler, because we need to get to the 9th spot
                    + "0\t" + ".\t" + variant.getLocation().getStart() + "\t" + variant.getLocation().getEnd() + "\t"
                    + findColour( variant ) + "\t" + variant.getId() + "\t" + varDesc + "\n" );
        }

        return buffer.toString();
    }

    private static String findColour( Variant variant ) {
        if ( variant.getClass() == CNV.class ) {
            CNV cnv = ( CNV ) variant;
            if ( cnv.getType().equals( CnvType.LOSS ) )
                return RED;
            else
                return BLUE;
        }
        return BLACK;
    }

}
