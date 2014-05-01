package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;

@DataTransferObject
public class GenomicLocationValueObject implements Serializable {

    /**
     * Representation of a GenomicLocation
     */
    private static final long serialVersionUID = 2792229595744543800L;

    private int start;
    private int end;
    private String chromosome;

    public GenomicLocationValueObject() {
    }

    public void setStart( int start ) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setEnd( int end ) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setChromosome( String chromosome ) {
        this.chromosome = chromosome;
    }

    public String getChromosome() {
        return chromosome;
    }

}
