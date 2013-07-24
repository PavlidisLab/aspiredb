package ubc.pavlab.aspiredb.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.shared.GenomicRange;

import java.util.Collection;

public interface UCSCConnectorAsync {
    void constructCustomTracksFile(GenomicRange range, Collection<Long> activeProjectIds, String appUrl, AsyncCallback<String> async);
}
