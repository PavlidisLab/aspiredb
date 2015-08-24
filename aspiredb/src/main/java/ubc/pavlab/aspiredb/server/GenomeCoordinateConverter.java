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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubc.pavlab.aspiredb.shared.GenomicRange;

/**
 * Convert from base coordinates to chromosome bands and vice versa. 1) Use db 2) Use some range datasctructure (easy:
 * from band, harder: to band)
 * 
 * @author anton
 */
@Component("genomeCoordinateConverter")
public class GenomeCoordinateConverter {

    class BandInterval {
        private String chromosome;
        private String startBand;
        private String endBand;

        public BandInterval( String chromosome, String startBand, String endBand ) {
            super();
            this.chromosome = chromosome;
            this.startBand = startBand;
            this.endBand = endBand;
        }

        public String getChromosome() {
            return chromosome;
        }

        public String getEndBand() {
            return endBand;
        }

        public String getStartBand() {
            return startBand;
        }

        public void setChromosome( String chromosome ) {
            this.chromosome = chromosome;
        }

        public void setEndBand( String endBand ) {
            this.endBand = endBand;
        }

        public void setStartBand( String startBand ) {
            this.startBand = startBand;
        }

        @Override
        public String toString() {
            if ( startBand.equals( endBand ) ) {
                return chromosome + ":" + startBand;
            } else {
                return chromosome + ":" + startBand + "-" + endBand;
            }
        }
    }

    class BaseInterval {
        private Integer start;
        private Integer end;

        public BaseInterval( Integer start, Integer end ) {
            super();
            this.start = start;
            this.end = end;
        }

        public Integer getEnd() {
            return end;
        }

        public Integer getStart() {
            return start;
        }

        public void setEnd( Integer end ) {
            this.end = end;
        }

        public void setStart( Integer start ) {
            this.start = start;
        }
    }

    @Autowired
    private CytobandFileLoader cytobandFileLoader;

    private Map<String, Chromosome> chromosomes = new HashMap<String, Chromosome>();

    public BaseInterval bandToBase( String chromosomeName, String bandStart, String bandEnd ) {
        Chromosome chromosome = chromosomes.get( chromosomeName );

        Integer start = chromosome.getBand( bandStart ).getStart();
        Integer end = chromosome.getBand( bandEnd ).getEnd();

        return new BaseInterval( start, end );
    }

    public BandInterval baseToBand( String chromosomeName, Integer baseStart, Integer baseEnd ) {
        Chromosome chromosome = chromosomes.get( chromosomeName );

        String start = chromosome.getBand( baseStart ).getName();
        String end = chromosome.getBand( baseEnd ).getName();

        return new BandInterval( chromosomeName, start, end );
    }

    public String baseToBandString( String chromosome, int start, int end ) {
        BandInterval interval = baseToBand( chromosome, start, end );
        return interval.toString();
    }

    public void fillInCytobandCoordinates( GenomicRange range ) {
        BandInterval interval = baseToBand( range.getChromosome(), range.getBaseStart(), range.getBaseEnd() );
        range.setBandStart( interval.getStartBand() );
        range.setBandEnd( interval.getEndBand() );
        range.initBandCoordinates();
    }

    public Set<String> getBands( String chromosomeName ) {
        Chromosome chromosome = chromosomes.get( chromosomeName.toUpperCase() );
        if ( chromosome == null ) {
            return new HashSet<String>( 0 );
        }

        return chromosome.getBands().keySet();
    }

    public Set<String> getChromosomeNames() {
        return chromosomes.keySet();
    }

    public Map<String, Chromosome> getChromosomes() {
        return chromosomes;
    }

    @PostConstruct
    public void initializeFromFile() {
        chromosomes = cytobandFileLoader.load();
    }

}
