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
package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;
import java.util.Map;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * author: anton date: 19/02/13
 */
@DataTransferObject
public class ChromosomeValueObject implements Serializable {

    private String name;
    private Map<String, ChromosomeBand> bands;

    private int baseSize;
    private int centromereLocation;

    public ChromosomeValueObject( String name, Map<String, ChromosomeBand> bands, int baseSize, int centromereLocation ) {
        this.name = name;
        this.bands = bands;
        this.baseSize = baseSize;
        this.centromereLocation = centromereLocation;
    }

    public ChromosomeValueObject() {
    }

    public String getName() {
        return name;
    }

    public Map<String, ChromosomeBand> getBands() {
        return bands;
    }

    public int getSize() {
        return baseSize;
    }

    public int getCentromereLocation() {
        return centromereLocation;
    }

    // TODO: performance can be improved
    public String getBandName( int startBase ) {
        for ( ChromosomeBand band : bands.values() ) {
            if ( band.getStart() < startBase && band.getEnd() > startBase ) {
                return band.getName();
            }
        }
        return "";
    }
}
