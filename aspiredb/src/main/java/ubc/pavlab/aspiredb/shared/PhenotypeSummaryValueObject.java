package ubc.pavlab.aspiredb.shared;


import java.io.Serializable;
import java.util.*;

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
    
    private String displaySummary;
    
    private String selectedSubjectPhenotypes;
    
    private Map<String, Integer> phenoSummaryMap;

    public String getDisplaySummary() {
        return displaySummary;
    }

    public void setDisplaySummary( String displaySummary ) {
        this.displaySummary = displaySummary;
    }

    private boolean isNeurocartaPhenotype;    

    
    private PhenotypeValueObject selectedPhenotype;
    

  
    public PhenotypeValueObject getSelectedPhenotype() {
        return selectedPhenotype;
	}

	public void setSelectedPhenotype(PhenotypeValueObject selectedPhenotype) {
		this.selectedPhenotype = selectedPhenotype;
	}

	public PhenotypeSummaryValueObject() {
		
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

	
	public boolean isNeurocartaPhenotype() {
		return this.isNeurocartaPhenotype;
	}

	public void setNeurocartaPhenotype(boolean isNeurocartaPhenotype) {
		this.isNeurocartaPhenotype = isNeurocartaPhenotype;
	}

    @Override
    public int compareTo(PhenotypeSummaryValueObject summaryValueObject) {
        return this.name.compareTo(summaryValueObject.getName());
    }

    public Map<String, Integer> getPhenoSummaryMap() {
        return phenoSummaryMap;
    }

    public void setPhenoSummaryMap( Map<String, Integer> phenoSummaryMap ) {
        this.phenoSummaryMap = phenoSummaryMap;
    }

  

    public String getSelectedSubjectPhenotypes() {
        return selectedSubjectPhenotypes;
    }

    public void setSelectedSubjectPhenotypes( String selectedSubjectPhenotypes ) {
        this.selectedSubjectPhenotypes = selectedSubjectPhenotypes;
    }
}
