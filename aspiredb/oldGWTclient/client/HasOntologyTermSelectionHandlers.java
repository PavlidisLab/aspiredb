package ubc.pavlab.aspiredb.client;

import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.handlers.OntologyTermSelectionHandler;

public interface HasOntologyTermSelectionHandlers {
    public HandlerRegistration addOntologyTermSelectionHandler (OntologyTermSelectionHandler handler);
}
