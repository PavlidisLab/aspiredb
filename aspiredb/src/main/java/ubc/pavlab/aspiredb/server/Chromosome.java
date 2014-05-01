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
package ubc.pavlab.aspiredb.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import ubc.pavlab.aspiredb.shared.ChromosomeBand;

/**
 * @author: anton
 * @version $id$
 */
public class Chromosome implements Serializable {
    private static final long serialVersionUID = 471238175429963239L;

    public Map<String, ChromosomeBand> getBands() {
        return bands;
    }

    private String name;
    private Map<String, ChromosomeBand> bands;
    private NavigableMap<Integer, ChromosomeBand> baseToBand;
    private int centromereLocation;

    public Chromosome( String name ) {
        this.baseToBand = new TreeMap<Integer, ChromosomeBand>();
        this.bands = new HashMap<String, ChromosomeBand>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addBand( Integer start, Integer end, String bandName, String stainingAgent ) {
        ChromosomeBand band = new ChromosomeBand( start, end, bandName, stainingAgent );
        this.baseToBand.put( start, band );
        this.bands.put( bandName, band );
        if ( stainingAgent.equals( "acen" ) && bandName.startsWith( "p" ) ) {
            this.centromereLocation = end;
        }
    }

    public int getSize() {
        ChromosomeBand lastBand = this.baseToBand.lastEntry().getValue();
        return lastBand.getEnd();
    }

    public ChromosomeBand getBand( String bandName ) {
        return bands.get( bandName );
    }

    public ChromosomeBand getBand( int baseCoordinate ) {
        return baseToBand.floorEntry( baseCoordinate ).getValue();
    }

    public int getCentromereLocation() {
        return centromereLocation;
    }
}
