package ubc.pavlab.aspiredb.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.shared.GeneValueObject;

import java.util.Collection;
import java.util.List;

public interface GeneServiceAsync {

    void getGenesInsideVariants(Collection<Long> variantIds, AsyncCallback<List<GeneValueObject>> async) ;

    public void findGenesWithNeurocartaPhenotype(String phenotypeValueUri,
                                                 AsyncCallback<Collection<GeneValueObject>> callback);
}
