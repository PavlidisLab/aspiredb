package ubc.pavlab.aspiredb.shared;

import java.util.*;

public class PhenotypeSummary implements Comparable<PhenotypeSummary> {

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

    private Collection<PhenotypeSummary> descendantOntologyTermSummaries = new ArrayList<PhenotypeSummary>();

    private Map<String,Set<Long>> inferredValueToSubjectSet = new HashMap<String, Set<Long>>();
    private Map<String,Set<Long>> dbValueToSubjectSet = new HashMap<String, Set<Long>>();

    private PhenotypeValueObject selectedPhenotype;
    public Collection<PhenotypeSummary> getDescendantOntologyTermSummaries() {
		return descendantOntologyTermSummaries;
	}

	public void setDescendantOntologyTermSummaries(
            Collection<PhenotypeSummary> descendantOntologyTermSummaries) {
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

	public PhenotypeSummary() {
		
	}
	
	public PhenotypeSummary(
			PhenotypeValueObject vo,
			Map<String, Set<Long>> valueToCountMap) {
		this.name = vo.getName();
		this.uri = vo.getUri();
		this.valueType = vo.getValueType();
		this.dbValueToSubjectSet = valueToCountMap;

        this.descendantOntologyTermSummaries = new ArrayList<PhenotypeSummary>();
    }
	
	public PhenotypeSummary getSummary() {
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
    public int compareTo(PhenotypeSummary summaryValueObject) {
        return this.name.compareTo(summaryValueObject.getName());
    }
}
