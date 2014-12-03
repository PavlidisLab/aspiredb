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

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO Document Me
 * 
 * @author ptan
 * @version $Id$
 */
public class MathUtilsTest {

    @Test
    public void getBinHeights() {
        double[] data = new double[] { 1, 400, 3000, 17445, 2465077, 3894858 };

        double[] bins = new double[] { 1.0, 1947429.5 };
        double[] heights = new double[] { 4.0, 2.0 };

        Map<Double, Double> out = MathUtils.getBinHeights( data );
        for ( int i = 0; i < bins.length; i++ ) {
            Assert.assertEquals( heights[i], out.get( bins[i] ).doubleValue(), 0.1 );
        }
    }
}
