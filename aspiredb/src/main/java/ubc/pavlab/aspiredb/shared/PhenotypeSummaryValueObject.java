package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.directwebremoting.annotations.DataTransferObject;

@DataTransferObject(javascript = "PhenotypeSummaryValueObject")
public class PhenotypeSummaryValueObject implements Serializable, Comparable<PhenotypeSummaryValueObject> {

    /**
     * 
     */
    private static final long serialVersionUID = -2189754457143562351L;

    // Numeric
    // Categorical
    private String name; // must be unique

    private String uri;
    private String valueType;

    private long sortValue;

    private String displaySummary;

    private Map<String, Integer> phenoSummaryMap;

    private Set<String> phenoSet;

    private Map<String, Set<Long>> subjects;

    private boolean isNeurocartaPhenotype;

    private PhenotypeValueObject selectedPhenotype;

    public PhenotypeSummaryValueObject() {

    }

    @Override
    public int compareTo( PhenotypeSummaryValueObject summaryValueObject ) {
        return this.name.compareTo( summaryValueObject.getName() );
    }

    public String getDisplaySummary() {
        return displaySummary;
    }

    public String getName() {
        return name;
    }

    public Set<String> getPhenoSet() {
        return phenoSet;
    }

    public Map<String, Integer> getPhenoSummaryMap() {
        return phenoSummaryMap;
    }

    public PhenotypeValueObject getSelectedPhenotype() {
        return selectedPhenotype;
    }

    public long getSortValue() {
        return sortValue;
    }

    public Map<String, Set<Long>> getSubjects() {
        return subjects;
    }

    public String getUri() {
        return uri;
    }

    public String getValueType() {
        return valueType;
    }

    public boolean isNeurocartaPhenotype() {
        return this.isNeurocartaPhenotype;
    }

    public void setDisplaySummary( String displaySummary ) {
        this.displaySummary = displaySummary;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setNeurocartaPhenotype( boolean isNeurocartaPhenotype ) {
        this.isNeurocartaPhenotype = isNeurocartaPhenotype;
    }

    public void setPhenoSet( Set<String> phenoset ) {
        this.phenoSet = phenoset;
        ;
    }

    public void setPhenoSummaryMap( Map<String, Integer> phenoSummaryMap ) {
        this.phenoSummaryMap = phenoSummaryMap;
    }

    public void setSelectedPhenotype( PhenotypeValueObject selectedPhenotype ) {
        this.selectedPhenotype = selectedPhenotype;
    }

    public void setSortValue( long sortValue ) {
        this.sortValue = sortValue;
    }

    public void setSubjects( Map<String, Set<Long>> subjects ) {
        this.subjects = subjects;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

    public void setValueType( String valueType ) {
        this.valueType = valueType;
    }

}
