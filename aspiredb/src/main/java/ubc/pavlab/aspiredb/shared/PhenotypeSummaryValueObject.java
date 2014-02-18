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
    
    private Map<String, Integer> phenoSummaryMap;

    public String getDisplaySummary() {
        return displaySummary;
    }

    public void setDisplaySummary( String displaySummary ) {
        this.displaySummary = displaySummary;
    }

    private boolean isNeurocartaPhenotype;    

    
    private PhenotypeValueObject selectedPhenotype;
    
    private PhenotypeValueObject selectedPhenotypeMulti;
    

  
    public PhenotypeValueObject getSelectedPhenotype() {
        return selectedPhenotype;
	}

	public void setSelectedPhenotype(PhenotypeValueObject selectedPhenotype) {
		this.selectedPhenotype = selectedPhenotype;
	}

	public PhenotypeValueObject getSelectedPhenotypeMulti() {
        return selectedPhenotypeMulti;
    }

    public void setSelectedPhenotypeMulti( PhenotypeValueObject selectedPhenotypeMulti ) {
        this.selectedPhenotypeMulti = selectedPhenotypeMulti;
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
  

   
}
