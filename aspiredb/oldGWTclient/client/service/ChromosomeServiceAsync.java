package ubc.pavlab.aspiredb.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.shared.ChromosomeValueObject;

import java.util.Map;

public interface ChromosomeServiceAsync {
    void getChromosomes(AsyncCallback<Map<String, ChromosomeValueObject>> async);
}
