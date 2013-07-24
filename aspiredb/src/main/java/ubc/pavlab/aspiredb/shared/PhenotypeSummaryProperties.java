package ubc.pavlab.aspiredb.shared;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface PhenotypeSummaryProperties extends PropertyAccess<PhenotypeSummaryValueObject> {
	
		@Path("name")
	    public ModelKeyProvider<PhenotypeSummaryValueObject> id();

	    public ValueProvider<PhenotypeSummaryValueObject, String> uri();

	    public ValueProvider<PhenotypeSummaryValueObject, String> valueType();

	    public ValueProvider<PhenotypeSummaryValueObject, String> name();
	    	    
	    public ValueProvider<PhenotypeSummaryValueObject, PhenotypeSummaryValueObject> summary();
	    
	    public ValueProvider<PhenotypeSummaryValueObject, PhenotypeValueObject> selectedPhenotype();

        public ValueProvider<PhenotypeSummaryValueObject, Double> presentRatio();
}
