package ubc.pavlab.aspiredb.client;

import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.handlers.VariantSelectionHandler;

public interface HasVariantSelectionHandlers {
    public HandlerRegistration addVariantSelectionHandler (VariantSelectionHandler handler);
}
