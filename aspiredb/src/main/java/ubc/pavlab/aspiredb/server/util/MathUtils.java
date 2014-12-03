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

import java.util.Map;
import java.util.TreeMap;

import org.directwebremoting.annotations.RemoteMethod;

import ubic.basecode.dataStructure.matrix.RCDoubleMatrix1D;
import ubic.basecode.math.distribution.Histogram;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Commonly used math functions
 * 
 * @author ptan
 * @version $Id$
 */
public class MathUtils {

    @RemoteMethod
    public static Map<Double, Double> getBinHeights( double[] data ) {
        Map<Double, Double> result = new TreeMap<>();

        int nbins = Math.round( ( float ) Math.sqrt( 1.0 * data.length ) );
        DoubleMatrix1D data1D = new RCDoubleMatrix1D( data );

        Histogram h = new Histogram( "hist_" + data.length, nbins, data1D );
        Double[] bins = h.getBinEdges();
        h.stepSize();
        double[] heights = h.getArray();

        for ( int i = 0; i < heights.length; i++ ) {
            result.put( bins[i], heights[i] );
        }

        return result;
    }
}
