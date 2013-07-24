package ubc.pavlab.aspiredb.client;

import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.handlers.ProjectSelectionHandler;

public interface HasProjectSelectionHandlers {
    public HandlerRegistration addProjectSelectionHandler (ProjectSelectionHandler handler);
}
