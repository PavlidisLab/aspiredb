package ubc.pavlab.aspiredb.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Captures information about a Phenotype such as the number of subjects for each phenotype value.
 * 
 * @author ptan
 * @version $Id$
 */
public class PhenotypeSummary implements Comparable<PhenotypeSummary> {

    // Numeric
    // Categorical
    private String name; // must be unique

    private String uri;
    private String valueType;

    private boolean isInferredBinaryType = false;

    private boolean isNeurocartaPhenotype;

    private Collection<PhenotypeSummary> descendantOntologyTermSummaries = new ArrayList<PhenotypeSummary>();

    private Map<String, Set<Long>> inferredValueToSubjectSet = new HashMap<String, Set<Long>>();

    private Map<String, Set<Long>> dbValueToSubjectSet = new HashMap<String, Set<Long>>();

    private PhenotypeValueObject selectedPhenotype;

    public PhenotypeSummary() {

    }

    public PhenotypeSummary( PhenotypeValueObject vo, Map<String, Set<Long>> valueToCountMap ) {
        this.name = vo.getName();
        this.uri = vo.getUri();
        this.valueType = vo.getValueType();
        this.dbValueToSubjectSet = valueToCountMap;

        this.descendantOntologyTermSummaries = new ArrayList<PhenotypeSummary>();
    }

    @Override
    public int compareTo( PhenotypeSummary summaryValueObject ) {
        return this.name.compareTo( summaryValueObject.getName() );
    }

    public Map<String, Set<Long>> getDbValueToSubjectSet() {
        return dbValueToSubjectSet;
    }

    public Collection<PhenotypeSummary> getDescendantOntologyTermSummaries() {
        return descendantOntologyTermSummaries;
    }

    public Map<String, Set<Long>> getInferredValueToSubjectSet() {
        return inferredValueToSubjectSet;
    }

    public String getName() {
        return name;
    }

    public double getPresentRatio() {
        double presentCount = 0;
        presentCount = dbValueToSubjectSet.get( "1" ) == null ? presentCount : presentCount
                + dbValueToSubjectSet.get( "1" ).size();
        presentCount = inferredValueToSubjectSet.get( "1" ) == null ? presentCount : presentCount
                + inferredValueToSubjectSet.get( "1" ).size();
        double absentCount = 0;
        absentCount = dbValueToSubjectSet.get( "0" ) == null ? absentCount : absentCount
                + dbValueToSubjectSet.get( "0" ).size();
        absentCount = inferredValueToSubjectSet.get( "0" ) == null ? absentCount : absentCount
                + inferredValueToSubjectSet.get( "0" ).size();

        if ( presentCount + absentCount == 0 ) {
            return 0;
        } else {
            return Math.round( 100 * presentCount / ( presentCount + absentCount ) );
        }
    }

    public PhenotypeValueObject getSelectedPhenotype() {
        return selectedPhenotype;
    }

    public PhenotypeSummary getSummary() {
        return this;
    }

    public String getUri() {
        return uri;
    }

    public String getValueType() {
        return valueType;
    }

    public void initializeInferredPhenotypes() {
        // Deep copy
        // inferredValueToSubjectSet = new HashMap<String, Set<Long>>();
        for ( String value : dbValueToSubjectSet.keySet() ) {
            inferredValueToSubjectSet.put( value, new HashSet<Long>( dbValueToSubjectSet.get( value ) ) );
        }
    }

    public boolean isInferredBinaryType() {
        return isInferredBinaryType;
    }

    public boolean isNeurocartaPhenotype() {
        return this.isNeurocartaPhenotype;
    }

    public void setDbValueToSubjectSet( Map<String, Set<Long>> dbValueToSubjectSet ) {
        this.dbValueToSubjectSet = dbValueToSubjectSet;
    }

    public void setDescendantOntologyTermSummaries( Collection<PhenotypeSummary> descendantOntologyTermSummaries ) {
        this.descendantOntologyTermSummaries = descendantOntologyTermSummaries;
    }

    public void setInferredBinaryType( boolean inferredBinaryType ) {
        isInferredBinaryType = inferredBinaryType;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setNeurocartaPhenotype( boolean isNeurocartaPhenotype ) {
        this.isNeurocartaPhenotype = isNeurocartaPhenotype;
    }

    public void setSelectedPhenotype( PhenotypeValueObject selectedPhenotype ) {
        this.selectedPhenotype = selectedPhenotype;
    }

    public void setSelectedSubjectId( String subjectId ) {
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

    public void setValueType( String valueType ) {
        this.valueType = valueType;
    }
}
