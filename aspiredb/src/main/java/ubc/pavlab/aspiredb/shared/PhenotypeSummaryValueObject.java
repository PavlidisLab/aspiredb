package ubc.pavlab.aspiredb.shared;

import org.directwebremoting.annotations.DataTransferObject;

import java.io.Serializable;
import java.util.*;

@DataTransferObject(javascript = "PhenotypeSummaryValueObject")
public class PhenotypeSummaryValueObject implements Serializable, Comparable<PhenotypeSummaryValueObject> {

	private static final long serialVersionUID = 5581881760734417223L;
	
	// Numeric	
	// Categorical
    private String name; // must be unique

	private String uri;
    private String valueType;

    public boolean isInferredBinaryType() {
        return isInferredBinaryType;
    }

    public void setInferredBinaryType(boolean inferredBinaryType) {
        isInferredBinaryType = inferredBinaryType;
    }

    private boolean isInferredBinaryType = false;

    private boolean isNeurocartaPhenotype;    

    private Collection<PhenotypeSummaryValueObject> descendantOntologyTermSummaries = new ArrayList<PhenotypeSummaryValueObject>();

    private Map<String,Set<Long>> inferredValueToSubjectSet = new HashMap<String, Set<Long>>();
    private Map<String,Set<Long>> dbValueToSubjectSet = new HashMap<String, Set<Long>>();

    private PhenotypeValueObject selectedPhenotype;
    public Collection<PhenotypeSummaryValueObject> getDescendantOntologyTermSummaries() {
		return descendantOntologyTermSummaries;
	}

	public void setDescendantOntologyTermSummaries(
            Collection<PhenotypeSummaryValueObject> descendantOntologyTermSummaries) {
		this.descendantOntologyTermSummaries = descendantOntologyTermSummaries;
	}

    public Map<String, Set<Long>> getDbValueToSubjectSet() {
        return dbValueToSubjectSet;
    }

    public void setDbValueToSubjectSet(Map<String, Set<Long>> dbValueToSubjectSet) {
        this.dbValueToSubjectSet = dbValueToSubjectSet;
    }

    public void setSelectedSubjectId(String subjectId) {
    }

    public PhenotypeValueObject getSelectedPhenotype() {
        return selectedPhenotype;
	}

	public void setSelectedPhenotype(PhenotypeValueObject selectedPhenotype) {
		this.selectedPhenotype = selectedPhenotype;
	}

	public PhenotypeSummaryValueObject() {
		
	}
	
	public PhenotypeSummaryValueObject(
			PhenotypeValueObject vo,
			Map<String, Set<Long>> valueToCountMap) {
		this.name = vo.getName();
		this.uri = vo.getUri();
		this.valueType = vo.getValueType();
		this.inferredValueToSubjectSet = valueToCountMap;

        this.descendantOntologyTermSummaries = new ArrayList<PhenotypeSummaryValueObject>();
    }
	
	public PhenotypeSummaryValueObject getSummary() {
		return this;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Set<Long>> getInferredValueToSubjectSet() {
		return inferredValueToSubjectSet;
	}

	public boolean isNeurocartaPhenotype() {
		return this.isNeurocartaPhenotype;
	}

	public void setNeurocartaPhenotype(boolean isNeurocartaPhenotype) {
		this.isNeurocartaPhenotype = isNeurocartaPhenotype;
	}

    public void initializeInferredPhenotypes() {
        // Deep copy
        // inferredValueToSubjectSet = new HashMap<String, Set<Long>>();
        for ( String value : dbValueToSubjectSet.keySet() ) {
            inferredValueToSubjectSet.put( value, new HashSet<Long>( dbValueToSubjectSet.get( value ) ) );
        }
    }

    public double getPresentRatio() {
        double presentCount = 0;
        presentCount = dbValueToSubjectSet.get("1") == null ? presentCount : presentCount + dbValueToSubjectSet.get("1").size();
        presentCount = inferredValueToSubjectSet.get("1") == null ? presentCount : presentCount + inferredValueToSubjectSet.get("1").size();
        double absentCount = 0;
        absentCount = dbValueToSubjectSet.get("0") == null ? absentCount : absentCount + dbValueToSubjectSet.get("0").size();
        absentCount = inferredValueToSubjectSet.get("0") == null ? absentCount : absentCount + inferredValueToSubjectSet.get("0").size();

        if (presentCount + absentCount == 0) {
            return 0;
        } else {
            return Math.round(100*presentCount/(presentCount + absentCount));
        }
    }

    @Override
    public int compareTo(PhenotypeSummaryValueObject summaryValueObject) {
        return this.name.compareTo(summaryValueObject.getName());
    }
}
