package ubc.pavlab.aspiredb.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ubc.pavlab.aspiredb.shared.GenomicRange;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Convert from base coordinates to chromosome bands and vice versa. 1) Use db 2) Use some range datasctructure (easy:
 * from band, harder: to band)
 * 
 * @author anton
 */
@Component("genomeCoordinateConverter")
public class GenomeCoordinateConverter {

    @Autowired
    private CytobandFileLoader cytobandFileLoader;

    private Map<String, Chromosome> chromosomes = new HashMap<String, Chromosome>();

    @PostConstruct
    public void initializeFromFile() {
        chromosomes = cytobandFileLoader.load();
    }

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

    public Set<String> getChromosomeNames() {
        return chromosomes.keySet();
    }

    public Set<String> getBands( String chromosomeName ) {
        Chromosome chromosome = chromosomes.get( chromosomeName.toUpperCase() );
        if ( chromosome == null ) {
            return new HashSet<String>( 0 );
        }

        return chromosome.getBands().keySet();
    }

    // /**
    // * Used to fill in base coordinates for objects passed from front end where user
    // * only specified cytoband coordinates.
    // */
    // public GenomicRange constructFromGenomicRangeRestriction (GenomicRangeRestriction restriction) {
    // String chromosomeName = restriction.getChromosome().toUpperCase();
    //
    // // Default values for 'unbound' range searches.
    // int start = 0;
    // int end = Integer.MAX_VALUE;
    //
    // if ( restriction.getStart().isEmpty() || restriction.getStart().equals("start") ) {
    // // use default
    // } else {
    // //FIXME: ??? is there a better way?
    // try {
    // start = Integer.parseInt( restriction.getStart() );
    // }
    // catch (NumberFormatException e) {
    // // Not a number -> try cytoband coordinates
    // Chromosome chromosome = chromosomes.get( chromosomeName );
    // start = chromosome.getBand( restriction.getStart() ).getStart();
    // }
    // }
    //
    // if ( restriction.getEnd().isEmpty() || restriction.getEnd().equals("end") ) {
    // // use default
    // } else {
    // //FIXME: ??? is there a better way?
    // try {
    // end = Integer.parseInt( restriction.getEnd() );
    // }
    // catch (NumberFormatException e) {
    // // Not a number -> try cytoband coordinates
    // Chromosome chromosome = chromosomes.get( chromosomeName );
    // end = chromosome.getBand( restriction.getEnd() ).getEnd();
    // }
    // }
    // return new GenomicRange (chromosomeName, start, end);
    // }

    public void fillInCytobandCoordinates( GenomicRange range ) {
        BandInterval interval = baseToBand( range.getChromosome(), range.getBaseStart(), range.getBaseEnd() );
        range.setBandStart( interval.getStartBand() );
        range.setBandEnd( interval.getEndBand() );
        range.initBandCoordinates();
    }

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

        public void setChromosome( String chromosome ) {
            this.chromosome = chromosome;
        }

        public String getStartBand() {
            return startBand;
        }

        public void setStartBand( String startBand ) {
            this.startBand = startBand;
        }

        public String getEndBand() {
            return endBand;
        }

        public void setEndBand( String endBand ) {
            this.endBand = endBand;
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

        public Integer getStart() {
            return start;
        }

        public void setStart( Integer start ) {
            this.start = start;
        }

        public Integer getEnd() {
            return end;
        }

        public void setEnd( Integer end ) {
            this.end = end;
        }
    }

    public Map<String, Chromosome> getChromosomes() {
        return chromosomes;
    }

}
