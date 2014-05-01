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
package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * @author anton
 */
@DataTransferObject(javascript = "GenomicRange")
public class GenomicRange implements Displayable, Serializable, Comparable<GenomicRange> {
    private static final long serialVersionUID = 6917870790522866428L;

    private String chromosome;
    private int baseStart;
    private int baseEnd;
    private String bandStart;
    private String bandEnd;

    private String bandString;

    public GenomicRange() {
    }

    public GenomicRange( String chromosome, String bandStart, String bandEnd ) {
        this.chromosome = chromosome;
        this.bandStart = bandStart;
        this.bandEnd = bandEnd;
    }

    public GenomicRange( String chromosome ) {
        this( chromosome, 0, Integer.MAX_VALUE );
    }

    public GenomicRange( String chromosome, int start, int end ) {
        this.chromosome = chromosome;
        this.baseStart = Math.min( start, end );
        this.baseEnd = Math.max( start, end );
    }

    public boolean isWithin( GenomicRange other ) {
        if ( chromosome.equals( other.getChromosome() ) ) {
            if ( baseStart >= other.getBaseStart() && baseEnd <= other.getBaseEnd() ) {
                return true;
            }
        }
        return false;
    }

    public String getBandString() {
        return this.bandString;
    }

    public void initBandCoordinates() {
        this.bandString = this.toCytobandString();
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome( String chromosome ) {
        this.chromosome = chromosome;
    }

    public int getBaseStart() {
        return baseStart;
    }

    public void setBaseStart( int baseStart ) {
        this.baseStart = baseStart;
    }

    public int getBaseEnd() {
        return baseEnd;
    }

    public void setBaseEnd( int baseEnd ) {
        this.baseEnd = baseEnd;
    }

    public String getBandStart() {
        return bandStart;
    }

    public void setBandStart( String bandStart ) {
        this.bandStart = bandStart;
    }

    public String getBandEnd() {
        return bandEnd;
    }

    public void setBandEnd( String bandEnd ) {
        this.bandEnd = bandEnd;
    }

    public String toBaseString() {
        String string = "";
        if ( chromosome != null ) {
            string += chromosome;
            if ( baseStart != 0 ) {
                string += ":" + baseStart;
                if ( baseEnd != 0 ) {
                    string += "-" + baseEnd;
                }
            }
        }
        return string;
    }

    public String toCytobandString() {
        String string = "";
        if ( chromosome != null ) {
            string += chromosome;
            if ( bandStart.equals( bandEnd ) ) {
                string += bandStart;
            } else {
                string += bandStart;
                if ( bandEnd != null ) {
                    string += "-" + bandEnd;
                }
            }
        }
        return string;
    }

    public String toString() {
        if ( this.bandStart != null ) {
            return toCytobandString();
        } else {
            return toBaseString();
        }
    }

    @Override
    public int compareTo( GenomicRange genomicRange ) {
        int myChromosomeIndex = getChromosomeIndex( this.chromosome );
        int otherChromosomeIndex = getChromosomeIndex( genomicRange.getChromosome() );
        if ( myChromosomeIndex == otherChromosomeIndex ) {
            return this.baseStart - genomicRange.getBaseStart();
        } else {
            return myChromosomeIndex - otherChromosomeIndex;
        }
    }

    private int getChromosomeIndex( String chr ) {
        if ( chr.equals( "X" ) ) {
            return 22;
        } else if ( chr.equals( "Y" ) ) {
            return 23;
        } else {
            return Integer.parseInt( chr );
        }
    }

    @Override
    public String getLabel() {
        return toString();
    }

    @Override
    public String getHtmlLabel() {
        return toString();
    }

    @Override
    public String getTooltip() {
        return toString();
    }
}