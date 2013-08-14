package ubc.pavlab.aspiredb.client;

import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.handlers.SelectSubjectHandler;

public interface HasSubjectSelectionHandlers {
    public HandlerRegistration addSelectSubjectHandler( SelectSubjectHandler handler );
}
