package ubc.pavlab.aspiredb.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

public interface LabelServiceAsync {
    void deleteSubjectLabel(LabelValueObject label, AsyncCallback<Void> async);
    void deleteVariantLabel(LabelValueObject label, AsyncCallback<Void> async);
}
