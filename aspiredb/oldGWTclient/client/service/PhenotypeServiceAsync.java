package ubc.pavlab.aspiredb.client.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ubc.pavlab.aspiredb.shared.PhenotypeEnrichmentValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.query.PhenotypeProperty;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PhenotypeServiceAsync {

    void getPhenotypes( Long subjectId, AsyncCallback<Map<String, PhenotypeValueObject>> async );

    void suggestPhenotypes( SuggestionContext suggestionContext, AsyncCallback<Collection<PhenotypeProperty>> async );

    void suggestPhenotypeValues( PhenotypeProperty property, SuggestionContext suggestionContext,
            AsyncCallback<Collection<PropertyValue>> async );

    public void getPhenotypeEnrichmentValueObjects( Collection<Long> activeProjects, Collection<Long> subjectIds,
            AsyncCallback<List<PhenotypeEnrichmentValueObject>> aspireAsyncCallback );
}
